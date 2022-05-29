package io.roach.bank.util;

import java.util.Objects;

public class Pair<A, B> {
    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    private final A left;

    private final B right;

    private Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public A getLeft() {
        return left;
    }

    public B getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(left, pair.left) &&
                Objects.equals(right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
