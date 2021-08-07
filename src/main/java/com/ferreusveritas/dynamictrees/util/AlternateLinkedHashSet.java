package com.ferreusveritas.dynamictrees.util;

import java.util.LinkedHashSet;

/**
 * An extension of {@link java.util.LinkedHashSet} which changes the order on insertion. This means that if an element
 * is a added, instead of keeping the original insertion order it will be moved to the back of the set.
 *
 * @author Harley O'Connor
 */
public final class AlternateLinkedHashSet<E> extends LinkedHashSet<E> {

    @Override
    public boolean add(final E e) {
        final boolean previouslyPresent = this.remove(e);
        super.add(e);
        return !previouslyPresent;
    }

}
