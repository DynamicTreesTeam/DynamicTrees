package com.ferreusveritas.dynamictrees.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

/**
 * @author Harley O'Connor
 */
public final class CommonCollectors {

    public static <T> Collector<T, ?, LinkedHashSet<T>> toLinkedSet() {
        return Collector.of(LinkedHashSet::new, LinkedHashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                IDENTITY_FINISH);
    }

    public static <T> Collector<T, ?, AlternateLinkedHashSet<T>> toAlternateLinkedSet() {
        return Collector.of(AlternateLinkedHashSet::new, LinkedHashSet::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                IDENTITY_FINISH);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return Collector.of(HashSet::new, Set::add,
                (left, right) -> {
                    if (left.size() < right.size()) {
                        right.addAll(left);
                        return right;
                    } else {
                        left.addAll(right);
                        return left;
                    }
                },
                set -> (Set<T>) Collections.unmodifiableSet(set),
                UNORDERED, IDENTITY_FINISH);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, Set<T>> toUnmodifiableLinkedSet() {
        return Collector.of(LinkedHashSet::new, Set::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                set -> (Set<T>) Collections.unmodifiableSet(set),
                IDENTITY_FINISH);
    }

    public static <T> Collector<T, ?, LinkedList<T>> toLinkedList() {
        return Collector.of(LinkedList::new, LinkedList::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }, IDENTITY_FINISH);
    }

}
