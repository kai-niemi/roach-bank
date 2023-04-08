package io.roach.bank.client.command.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class ByteFormat {
    public static final long ONE_KB = 1000;

    public static final long ONE_MB = ONE_KB * ONE_KB;

    public static final long ONE_GB = ONE_MB * ONE_KB;

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static String byteCountToDisplaySize(final long byteCount) {
        Unit closestUnit = Unit.BYTE;
        BigDecimal tmp = BigDecimal.ZERO;

        for (Unit unit : Unit.values()) {
            tmp = new BigDecimal(byteCount).divide(
                    new BigDecimal(unit.byteCount), 5, ROUNDING_MODE);
            if (tmp.compareTo(BigDecimal.ONE) >= 0) {
                closestUnit = unit;
                break;
            }
        }

        String rounded = tmp.setScale(closestUnit.scale, ROUNDING_MODE).toString();
        if (rounded.endsWith(".00")) {
            rounded = rounded.substring(0, rounded.length() - 3);
        } else if (rounded.endsWith(".0")) {
            rounded = rounded.substring(0, rounded.length() - 2);
        }

        return String.format("%s %s", rounded, closestUnit.suffix);
    }

    private enum Unit {
        GIGABYTE("GB", ONE_GB, 1),
        MEGABYTE("MB", ONE_MB, 1),
        KILOBYTE("KB", ONE_KB, 0),
        BYTE("bytes", 1, 0);

        final String suffix;

        final long byteCount;

        final int scale;

        Unit(String suffix, long byteCount, int scale) {
            this.suffix = suffix;
            this.byteCount = byteCount;
            this.scale = scale;
        }
    }
}
