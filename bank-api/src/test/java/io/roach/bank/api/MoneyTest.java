package io.roach.bank.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.roach.bank.api.support.Money;

import static io.roach.bank.api.support.Money.SEK;
import static io.roach.bank.api.support.Money.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoneyTest {
    public double interpolate(double x1, double y1, double x2, double y2, double x) {
        if (x2 <= x1) {
            return y1;
        }
        if (x > x2) {
            return y2;
        }
        if (x < x1) {
            return y1;
        }
        return y1 + ((x - x1) * (y2 - y1)) / (x2 - x1);
    }

    @Test
    public void interpolate() {
        System.out.println(interpolate(2400, 50, 9500, 250, 9600));
        System.out.println(interpolate(22095, 50, 27136, 250, 23000));
        System.out.println(interpolate(22095, 50, 22099, 250, 22300));
        System.out.println(interpolate(4460, 50, 5329.5, 250, 3739.08));
//        Box size for city riga (min balance: 4460 max balance: 5329.5 current balance: 3739.08) is -115.82403680276022px
    }

    @Test
    public void whenBinaryArithmetics_thenSucceed() {
        assertEquals(
                of("100.00", SEK),
                of("80.00", SEK).plus(of("20.00", SEK)));

        assertEquals(
                of("19.50", SEK),
                of("10.05", SEK).plus(of("9.95", SEK), of("-0.50", SEK)));

        assertEquals(
                of("80.00", SEK),
                of("100.00", SEK).minus(of("20.00", SEK)));

        assertEquals(
                of("100.00", SEK),
                of("10.00", SEK).multiply(10));

        assertEquals(
                of("20.00", SEK),
                of("100.00", SEK).divide(5));

        assertEquals(
                of("16.67", SEK),
                of("100.00", SEK).divideAndRound(6));

        assertEquals(
                of("0.00", SEK),
                of("100.00", SEK).remainder(100));
    }

    @Test
    public void whenComparisonOperators_thenSucceed() {
        assertTrue(of("110.00", SEK).isGreaterThan(of("100.00", SEK)));
        assertTrue(of("100.00", SEK).isGreaterThanOrEqualTo(of("100.00", SEK)));
        assertTrue(of("99.00", SEK).isLessThan(of("100.00", SEK)));
        assertTrue(of("100.00", SEK).isLessThanOrEqualTo(of("100.00", SEK)));
        assertTrue(of("100.00", SEK).isSameCurrency(of("100.00", SEK)));
    }

    @Test
    public void whenUnaryOperators_thenExpectImmutability() {
        Money m = Money.of("15.00", "SEK");
        Assertions.assertNotSame(m, m.negate().negate());
        Assertions.assertEquals(m, m.negate().negate());
    }

    @Test
    public void whenMixingCurrencies_thenFail() {
        Assertions.assertThrows(CurrencyMismatchException.class, () -> {
            Money.of("15.00", "SEK").minus(Money.of("0.00", "USD"));
            Assertions.fail("Must not succeed");
        });
        Assertions.assertThrows(CurrencyMismatchException.class, () -> {
            Money.of("15.00", "SEK").plus(Money.of("0.00", "USD"));
            Assertions.fail("Must not succeed");
        });
        Assertions.assertThrows(CurrencyMismatchException.class, () -> {
            Money.of("15.00", "SEK").isGreaterThan(Money.of("0.00", "USD"));
            Assertions.fail("Must not succeed");
        });
        Assertions.assertThrows(CurrencyMismatchException.class, () -> {
            Money.of("15.00", "SEK").isGreaterThanOrEqualTo(Money.of("0.00", "USD"));
            Assertions.fail("Must not succeed");
        });
        Assertions.assertThrows(CurrencyMismatchException.class, () -> {
            Money.of("15.00", "SEK").isLessThan(Money.of("0.00", "USD"));
            Assertions.fail("Must not succeed");
        });
        Assertions.assertThrows(CurrencyMismatchException.class, () -> {
            Money.of("15.00", "SEK").isLessThanOrEqualTo(Money.of("0.00", "USD"));
            Assertions.fail("Must not succeed");
        });
    }
}
