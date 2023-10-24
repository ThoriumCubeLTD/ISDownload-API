package com.infernalsuite.isdownloadapi.util;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;

public final class BringChaosToOrder {
    private BringChaosToOrder() {
    }

    public static <T extends NameSource & TimeSource> Comparator<T> timeOrNameComparator() {
        return (o1, o2) -> {
            final Instant t1 = o1.time();
            final Instant t2 = o2.time();
            // Both objects are not guaranteed to have a time present, but are guaranteed
            // to have a name present - we prefer to compare them by time, but in cases where
            // the time is not available on both objects we will compare them using their name
            if (t1 != null && t2 != null) {
                return t1.compareTo(t2);
            }
            final String n1 = Objects.requireNonNull(o1.name(), () -> "name of " + o1);
            final String n2 = Objects.requireNonNull(o2.name(), () -> "name of " + o2);
            return n1.compareTo(n2);
        };
    }
}
