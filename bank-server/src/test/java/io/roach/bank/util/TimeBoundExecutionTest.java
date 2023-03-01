package io.roach.bank.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionTimedOutException;

import static io.roach.bank.util.TimeUtils.executionTime;

@Disabled("for now")
public class TimeBoundExecutionTest {
    private static final Logger logger = LoggerFactory.getLogger(TimeBoundExecutionTest.class);

    public static Integer doMassiveCompute_AndBlock(int value) {
        logger.debug("Doing massive compute ({}) - will block for eternity", value);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        throw new IllegalStateException();
    }

    public static Integer doMassiveCompute_AndSucceed(int value, long delayMillis) {
        logger.debug("Doing massive compute ({}) - will succeed after {}", value, delayMillis);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.debug("Done: result of compute({}) is {}", value, value * value);
        return value * value;
    }

    public static Integer doMassiveCompute_AndFail(int value, long delayMillis) {
        logger.debug("Doing massive compute ({}) - will fail after {}", value, delayMillis);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new TransactionTimedOutException("Fail");
    }

    @Test
    public void whenRunningSingleTasks_withinTime_thenSucceed() {
        try {
            Integer v1 = ConcurrencyUtils
                    .run(() -> doMassiveCompute_AndSucceed(1, 1000), 10, TimeUnit.SECONDS);
            Assertions.assertEquals(v1, 1);
            Integer v2 = ConcurrencyUtils
                    .run(() -> doMassiveCompute_AndSucceed(2, 1000), 10, TimeUnit.SECONDS);
            Assertions.assertEquals(v2, 4);
        } catch (TimeoutException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void whenRunningSingleTasks_thatTimeout_thenFail() {
        Assertions.assertThrows(TimeoutException.class, () -> {
            ConcurrencyUtils.run(() -> doMassiveCompute_AndSucceed(1, 10000), 5, TimeUnit.SECONDS);
            Assertions.fail("Must not succeed");
            ConcurrencyUtils.run(() -> doMassiveCompute_AndSucceed(2, 10000), 5, TimeUnit.SECONDS);
            Assertions.fail("Must not succeed");
        });
    }

    @Test
    public void whenSchedulingManyTasks_withinTime_thenSucceed() {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int k = i + 1;
            tasks.add(() -> doMassiveCompute_AndSucceed(k, 1000));
        }

        long time = executionTime(() -> {
            ConcurrencyUtils.runConcurrentlyAndWait(tasks, 10, TimeUnit.SECONDS);
            return null;
        });
        Assertions.assertTrue(time <= 15_000, "" + time);
    }

    @Test
    public void whenSchedulingManyTasks_thatTimeout_thenFail() {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int k = i + 1;
            tasks.add(() -> doMassiveCompute_AndSucceed(k, 15000));
        }

        long time = executionTime(() -> {
            ConcurrencyUtils.runConcurrentlyAndWait(tasks, 10, TimeUnit.SECONDS, result -> {
                logger.debug("Result ({})", result);
            });
            return null;
        });
        Assertions.assertTrue(time <= 15_000, "" + time);
    }

    @Test
    public void whenSchedulingManyRandomTasks_withinTime_thenSucceed() {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int k = i + 1;
            if (i % 2 == 0) {
                tasks.add(() -> doMassiveCompute_AndFail(k, 5000));
            } else {
                tasks.add(() -> doMassiveCompute_AndSucceed(k, (long) (Math.random() * 5000 + 5000)));
            }
        }

        long time = executionTime(() -> {
            ConcurrencyUtils.runConcurrentlyAndWait(tasks, 10, TimeUnit.SECONDS);
            return null;
        });
        Assertions.assertTrue(time <= 15_000, "" + time);
    }

    @Test
    public void whenSchedulingManyRandomTasks_thatTimeout_thenFail() {
        List<Callable<Integer>> tasks = new ArrayList<>();
        tasks.add(() -> doMassiveCompute_AndFail(1, 5000));
        tasks.add(() -> doMassiveCompute_AndSucceed(2, (long) (Math.random() * 5000 + 5000)));
        tasks.add(() -> doMassiveCompute_AndBlock(2));
        tasks.add(() -> doMassiveCompute_AndFail(1, 5000));
        tasks.add(() -> doMassiveCompute_AndSucceed(2, (long) (Math.random() * 5000 + 5000)));
        tasks.add(() -> doMassiveCompute_AndBlock(2));
        tasks.add(() -> doMassiveCompute_AndFail(1, 5000));
        tasks.add(() -> doMassiveCompute_AndSucceed(2, (long) (Math.random() * 5000 + 5000)));
        tasks.add(() -> doMassiveCompute_AndBlock(2));

        long time = executionTime(() -> {
            ConcurrencyUtils.runConcurrentlyAndWait(tasks, 10, TimeUnit.SECONDS);
            return null;
        });
        Assertions.assertTrue(time <= 15_000, "" + time);
    }
}
