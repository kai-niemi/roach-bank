package io.roach.bank.client.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.roach.bank.api.support.WeightedItem;

import static io.roach.bank.api.support.RandomData.selectRandom;
import static io.roach.bank.api.support.RandomData.selectRandomUnique;
import static io.roach.bank.api.support.RandomData.selectRandomWeighted;

public class RandomDistributionTest {
    static class Account implements WeightedItem {
        static Account of(String name, double balance) {
            return new Account(name,balance);
        }

        String name;
        double balance;

        Account(String name, double balance) {
            this.name = name;
            this.balance = balance;
        }

        @Override
        public double getWeight() {
            return balance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Account account = (Account) o;
            return name.equals(account.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name + "(" + balance + ")";
        }
    }

    @Test
    public void testWeightedDistribution1() {
        List<Account> data = Arrays.asList(
                Account.of("a",1800.00),
                Account.of("b",100.00),
                Account.of("c",100.00),
                Account.of("d",100.00),
                Account.of("e",100.00),
                Account.of("f",100.00),
                Account.of("g",100.00),
                Account.of("h",110.00)
        );

        Map<Account, AtomicInteger> hits = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            hits.computeIfAbsent(selectRandomWeighted(data), k -> new AtomicInteger()).incrementAndGet();
        }

        System.out.println(hits);
    }

    @Test
    public void testWeightedDistribution2() {
        List<Integer> data = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        List<Double> weights = Arrays.asList(0.9, 0.12, 0.13, 0.14, 0.15, 0.16, 0.17, 0.18, 0.19, 1.0);

        Map<Integer, AtomicInteger> hits = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            hits.computeIfAbsent(selectRandomWeighted(data, weights), k -> new AtomicInteger()).incrementAndGet();
        }

        System.out.println(hits);
    }

    @Test
    public void testDistribution1() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Map<Integer, AtomicInteger> hits = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            hits.computeIfAbsent(selectRandom(data), k -> new AtomicInteger()).incrementAndGet();
        }

        System.out.println(hits);
    }

    @Test
    public void testDistribution2() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Map<Integer, AtomicInteger> hits = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            selectRandomUnique(data, 10).forEach((k) -> {
                hits.computeIfAbsent(k, v -> new AtomicInteger()).incrementAndGet();
            });
        }

        System.out.println(hits);
    }
}
