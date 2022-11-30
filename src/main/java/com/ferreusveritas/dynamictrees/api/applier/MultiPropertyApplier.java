package com.ferreusveritas.dynamictrees.api.applier;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * An applier for applying multiple {@link PropertyApplier} with different values for the same key.
 *
 * @author Harley O'Connor
 */
public class MultiPropertyApplier<T, I> extends PropertyApplier<T, Object, I> {

    private final List<PropertyApplier<T, Object, I>> appliers = Lists.newLinkedList();

    @SafeVarargs
    public MultiPropertyApplier(final String key, final Class<T> objectClass,
                                final PropertyApplier<T, Object, I>... appliers) {
        super(key, objectClass, (object, value) -> {
        });
        this.appliers.addAll(Arrays.asList(appliers));
    }

    public void addApplier(final PropertyApplier<T, Object, I> applier) {
        this.appliers.add(applier);
    }

    /**
     * @deprecated not used
     */
    @Deprecated
    @Nullable
    @Override
    protected PropertyApplierResult applyIfShould(T object, I input, Applier<T, Object> applier) {
        final Iterator<PropertyApplier<T, Object, I>> iterator = appliers.iterator();
        PropertyApplierResult applierResult;

        do {
            applierResult = applyNext(object, input, applier, iterator);
        } while (applierResult == null || !applierResult.wasSuccessful());

        return applierResult;
    }

    @Nullable
    private PropertyApplierResult applyNext(T object, I input, Applier<T, Object> applier,
                                                   Iterator<PropertyApplier<T, Object, I>> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        return iterator.next().applyIfShould(object, input, applier);
    }

}
