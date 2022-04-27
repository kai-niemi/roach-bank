package io.roach.bank.api.support;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.roach.bank.api.CurrencyMismatchException;

/**
 * Immutable monetary type that couples an amount with a currency.
 * The amount value is represented by {@code java.math.BigDecimal} and the currency
 * by a ISO-4701 {@code java.aspect.Currency}.
 */
@Embeddable  // Support for JPA only (cant use final modifier)
public class Money implements Serializable, Comparable<Money> {
    public static final Currency SEK = Currency.getInstance("SEK");

    public static final Currency EUR = Currency.getInstance("EUR");

    public static final Currency USD = Currency.getInstance("USD");

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private static final BigDecimal ONE = BigDecimal.ONE;

    private static final RoundingMode roundingMode = RoundingMode.HALF_EVEN;

    private BigDecimal amount;

    private Currency currency;

    protected Money() {
    }

    /**
     * Creates a new Money instance.
     *
     * @param amount the decimal amount (required)
     * @param currency the currency (required)
     */
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new NullPointerException("value is null");
        }
        if (currency == null) {
            throw new NullPointerException("currency is null");
        }
        if (amount.scale() != currency.getDefaultFractionDigits()) {
            if (currency.getDefaultFractionDigits() == 0) {
                amount = amount.setScale(0, RoundingMode.DOWN);
            } else {
                throw new IllegalArgumentException("Wrong number of fraction digits for currency: "
                        + currency.getCurrencyCode() + "(" + currency.getDisplayName() + "): "
                        + amount.scale()
                        + " != " + currency.getDefaultFractionDigits() + " for " + amount);
            }
        }
        this.amount = amount;
        this.currency = currency;
    }

    public static Money parse(String unit) {
        String[] parts = unit.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expected tuple: 'value currency' got " + unit);
        }
        return of(parts[0], parts[1]);
    }

    public static Money of(String amount, String currencyCode) {
        return of(amount, Currency.getInstance(currencyCode));
    }

    public static Money of(String amount, Currency currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(String currency) {
        return zero(Currency.getInstance(currency));
    }

    public static Money zero(Currency currency) {
        BigDecimal amount = ZERO.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
        return new Money(amount, currency);
    }

    public Money plus(Money... addends) {
        BigDecimal copy = BigDecimal.ZERO.add(amount);
        for (Money add : addends) {
            assertSameCurrency(add);
            copy = copy.add(add.amount);
        }
        return new Money(copy, currency);
    }

    public Money minus(Money... subtrahends) {
        BigDecimal copy = BigDecimal.ZERO.add(amount);
        for (Money subtrahend : subtrahends) {
            assertSameCurrency(subtrahend);
            copy = copy.subtract(subtrahend.amount);
        }
        return new Money(copy, currency);
    }

    public Money multiply(int multiplier) {
        BigDecimal newAmount = amount.multiply(new BigDecimal(Integer.toString(multiplier)));
        newAmount = setScale(newAmount);
        return new Money(newAmount, currency);
    }

    public Money multiply(double multiplier) {
        BigDecimal newAmount = amount.multiply(new BigDecimal(Double.toString(multiplier)));
        newAmount = setScale(newAmount);
        return new Money(newAmount, currency);
    }

    public Money multiply(BigDecimal multiplier) {
        assertNotNull(multiplier);
        BigDecimal newAmount = amount.multiply(multiplier);
        newAmount = setScale(newAmount);
        return new Money(newAmount, currency);
    }

    public Money divideAndRound(double divisor) {
        BigDecimal newAmount = amount.divide(new BigDecimal(Double.toString(divisor)), 16, roundingMode);
        newAmount = setScale(newAmount);
        return new Money(newAmount, currency);
    }

    public Money divide(BigDecimal divisor) {
        assertNotNull(divisor);
        BigDecimal newAmount = amount.divide(divisor, RoundingMode.HALF_EVEN);
        newAmount = setScale(newAmount);
        return new Money(newAmount, currency);
    }

    public Money divide(double divisor) {
        BigDecimal newAmount = amount
                .divide(new BigDecimal(Double.toString(divisor)), RoundingMode.HALF_EVEN);
        newAmount = setScale(newAmount);
        return new Money(newAmount, currency);
    }

    public Money remainder(int divisor) {
        return new Money(amount.remainder(
                new BigDecimal(Integer.toString(divisor))), currency);
    }

    public boolean isGreaterThan(Money right) {
        assertSameCurrency(right);
        return compareTo(right) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Money right) {
        assertSameCurrency(right);
        return compareTo(right) >= 0;
    }

    public boolean isLessThan(Money right) {
        assertSameCurrency(right);
        return compareTo(right) < 0;
    }

    public boolean isLessThanOrEqualTo(Money right) {
        assertSameCurrency(right);
        return compareTo(right) <= 0;
    }

    @JsonIgnore
    public boolean isNegative() {
        return amount.compareTo(ZERO) < 0;
    }

    public boolean isSameCurrency(Money right) {
        return currency.equals(right.getCurrency());
    }

    private BigDecimal setScale(BigDecimal bigDecimal) {
        return bigDecimal.setScale(currency.getDefaultFractionDigits(), roundingMode);
    }

    private void assertSameCurrency(Money right) {
        assertNotNull(right);
        if (!isSameCurrency(right)) {
            throw new CurrencyMismatchException(
                    currency + " doesn't match " + right.currency);
        }
    }

    private void assertNotNull(Object money) {
        if (money == null) {
            throw new NullPointerException("money is null");
        }
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public Currency getCurrency() {
        return currency;
    }

    /**
     * Return the underlying monetary amount.
     *
     * @return the monetary amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Compares this money object with another instance. The money objects are
     * compared by their underlying long value.
     * <p/>
     * {@inheritDoc}
     */
    public int compareTo(Money o) {
        return amount.compareTo(o.amount);
    }

    /**
     * Compares two money objects for equality. The money objects are
     * compared by their underlying bigdecimal value and currency ISO code.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Money money = (Money) o;

        if (!amount.equals(money.amount)) {
            return false;
        }
        if (!currency.equals(money.currency)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = amount.hashCode();
        result = 31 * result + currency.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return amount + " " + currency;
    }
}
