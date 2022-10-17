package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.treepacks.*;
import com.google.gson.JsonElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public interface PropertyAppliers<O, I> {
    ResultList applyAll(Map<String, JsonElement> inputs, O object);

    PropertyApplierResult apply(O object, String key, I input);

    <E extends O> PropertyAppliers<O, I> register(PropertyApplier<E, ?, I> applier);

    <V> PropertyAppliers<O, I> register(String key, Class<V> valueClass, Applier<O, V> applier);

    <V> PropertyAppliers<O, I> register(String key, Class<V> valueClass, VoidApplier<O, V> applier);

    <V> PropertyAppliers<O, I> registerArrayApplier(String key, Class<V> valueClass, Applier<O, V> applier);

    <V> PropertyAppliers<O, I> registerArrayApplier(String key, Class<V> valueClass, VoidApplier<O, V> applier);

    <V> PropertyAppliers<O, I> registerListApplier(String key, Class<V> valueClass, Applier<O, List<V>> applier);

    <V> PropertyAppliers<O, I> registerListApplier(String key, Class<V> valueClass, VoidApplier<O, List<V>> applier);

    PropertyAppliers<O, I> registerIfTrueApplier(String key, IfTrueApplier<O> applier);

    <E extends O, V> PropertyAppliers<O, I> register(String key, Class<E> subClass, Class<V> valueClass,
                                                         Applier<E, V> applier);

    <E extends O, V> PropertyAppliers<O, I> register(String key, Class<E> subClass, Class<V> valueClass,
                                                         VoidApplier<E, V> applier);

    <E extends O> PropertyAppliers<O, I> registerIfTrueApplier(String key, Class<E> subClass,
                                                                   IfTrueApplier<E> applier);

    <E extends O, V> PropertyAppliers<O, I> registerArrayApplier(String key, Class<E> subClass, Class<V> valueClass,
                                                                     Applier<E, V> applier);

    <E extends O, V> PropertyAppliers<O, I> registerArrayApplier(String key, Class<E> subClass, Class<V> valueClass,
                                                                     VoidApplier<E, V> applier);

    <E extends O, V> PropertyAppliers<O, I> registerListApplier(String key, Class<E> subClass, Class<V> valueClass,
                                                                Applier<E, List<V>> applier);

    <E extends O, V> PropertyAppliers<O, I> registerListApplier(String key, Class<E> subClass, Class<V> valueClass,
                                                                VoidApplier<E, List<V>> applier);

    Class<O> getObjectType();

    final class ResultList extends LinkedList<PropertyApplierResult> {

        public ResultList forEachError(final Consumer<String> errorConsumer) {
            this.forEach(result -> result.getError().ifPresent(errorConsumer));
            return this;
        }

        public ResultList forEachWarning(final Consumer<String> warningConsumer) {
            this.forEach(result -> result.getWarnings().forEach(warningConsumer));
            return this;
        }

        public void forEachErrorWarning(final Consumer<String> errorConsumer, final Consumer<String> warningConsumer) {
            this.forEach(result -> {
                result.getError().ifPresent(errorConsumer);
                result.getWarnings().forEach(warningConsumer);
            });
        }

    }
}
