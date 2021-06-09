package com.ferreusveritas.dynamictrees.util;

import java.util.LinkedHashSet;
import java.util.stream.Collector;

/**
 * @author Harley O'Connor
 */
public final class CommonCollectors {

    public static <T> Collector<T, ?, LinkedHashSet<T>> toLinkedSet() {
        return Collector.of(LinkedHashSet::new, LinkedHashSet::add,
                (left, right) -> { left.addAll(right); return left; },
                Collector.Characteristics.IDENTITY_FINISH);
    }

    public static <T> Collector<T, ?, AlternateLinkedHashSet<T>> toAlternateLinkedSet() {
        return Collector.of(AlternateLinkedHashSet::new, LinkedHashSet::add,
                (left, right) -> { left.addAll(right); return left; },
                Collector.Characteristics.IDENTITY_FINISH);
    }

}
