package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.treepacks.Applier;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplier;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.util.function.TriFunction;
import com.google.gson.JsonElement;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

public class TagKeyJsonPropertyApplier<K, O, V> extends PropertyApplier<O, V, JsonElement> {
    protected final TriFunction<TagKey<K>, O, V, PropertyApplierResult> tagKeyFunction;
    protected final ResourceKey<? extends Registry<K>> registryKey;

    public TagKeyJsonPropertyApplier(ResourceKey<? extends Registry<K>> registryKey, Class<O> objectClass, Class<V> valueClass, TriConsumer<TagKey<K>, O, V> tagKeyConsumer) {
        this(registryKey, objectClass, valueClass, (tagKey, o, v) -> {
            tagKeyConsumer.accept(tagKey, o, v);
            return PropertyApplierResult.success();
        });
    }

    public TagKeyJsonPropertyApplier(ResourceKey<? extends Registry<K>> registryKey, Class<O> objectClass, Class<V> valueClass, TriFunction<TagKey<K>, O, V, PropertyApplierResult> tagKeyFunction) {
        super("none", objectClass, valueClass, (o, v) -> {});
        this.tagKeyFunction = tagKeyFunction;
        this.registryKey = registryKey;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public PropertyApplierResult applyIfShould(String key, Object object, JsonElement input) {
        if (!this.objectClass.isInstance(object))
            return null;

        try {
            TagKey<K> tagKey = TagKey.create(this.registryKey, new ResourceLocation(key.charAt(0) == '#' ? key.substring(1) : key));
            return JsonDeserialisers.getOrThrow(this.valueClass).deserialise(input).map(value -> this.tagKeyFunction.apply(tagKey, (O) object, value))
                    .orElseApply(
                            PropertyApplierResult::failure,
                            PropertyApplierResult::addWarnings,
                            null
                    );
        } catch (ResourceLocationException e) {
            return PropertyApplierResult.failure(e.getMessage());
        }
    }

    @Nullable
    @Override
    protected <S, R> PropertyApplierResult applyIfShould(Object object, JsonElement input, Class<R> valueClass, Applier<S, R> applier) {
        return null;
    }
}
