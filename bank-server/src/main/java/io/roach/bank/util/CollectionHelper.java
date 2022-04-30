package io.roach.bank.util;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class CollectionHelper {
    private CollectionHelper() {
    }

    public static <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        }
        int size = source.size();
        if (size <= 0) {
            return Stream.empty();
        }
        int chunks = (size - 1) / length;
        return IntStream.range(0, chunks + 1)
                .mapToObj(n -> source.subList(n * length, n == chunks ? size : (n + 1) * length));
    }


}
