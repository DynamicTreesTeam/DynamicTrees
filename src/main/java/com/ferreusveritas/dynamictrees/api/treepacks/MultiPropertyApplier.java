package com.ferreusveritas.dynamictrees.api.treepacks;

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

    private final List<PropertyApplier<T, ?, I>> appliers = Lists.newLinkedList();

    @SafeVarargs
    public MultiPropertyApplier(final String key, final Class<T> objectClass, final PropertyApplier<T, ?, I>... appliers) {
        super(key, objectClass, Object.class, (object, value) -> {
        });
        this.appliers.addAll(Arrays.asList(appliers));
    }

    public void addApplier(final PropertyApplier<T, ?, I> applier) {
        this.appliers.add(applier);
    }

    @Nullable
    @Override
    public PropertyApplierResult applyIfShould(String keyIn, Object object, I input) {
        if (!this.key.equalsIgnoreCase(keyIn) || !this.objectClass.isInstance(object)) {
            return null;
        }

        final Iterator<PropertyApplier<T, ?, I>> iterator = appliers.iterator();
        PropertyApplierResult applierResult;

        do {
            applierResult = this.applyIfShould(object, input, iterator.next());
        } while (applierResult == null || !applierResult.wasSuccessful());

        return applierResult;
    }

    /**
     * @deprecated not used
     */
    @Deprecated
    @Nullable
    @Override
    protected <S, R> PropertyApplierResult applyIfShould(Object object, I input, Class<R> valueClass, Applier<S, R> applier) {
        return PropertyApplierResult.success();
    }

    @Nullable
    private <S, R> PropertyApplierResult applyIfShould(final Object object, final I input, final PropertyApplier<S, R, I> applier) {
        return this.applyIfShould(object, input, applier.getValueClass(), applier.applier);
    }

}
