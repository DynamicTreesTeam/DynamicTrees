package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.api.applier.Applier;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplier;
import com.ferreusveritas.dynamictrees.api.applier.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.util.function.TriFunction;
import com.google.gson.JsonElement;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

public class TagKeyJsonPropertyApplier<K, O, V> extends PropertyApplier<O, Float, JsonElement> {
    protected final TriFunction<TagKey<K>, O, Float, PropertyApplierResult> tagKeyFunction;
    protected final ResourceKey<? extends Registry<K>> registryKey;

    public TagKeyJsonPropertyApplier(ResourceKey<? extends Registry<K>> registryKey, Class<O> objectClass, TriConsumer<TagKey<K>, O, Float> tagKeyConsumer) {
        this(registryKey, objectClass, (tagKey, o, v) -> {
            tagKeyConsumer.accept(tagKey, o, v);
            return PropertyApplierResult.success();
        });
    }

    public TagKeyJsonPropertyApplier(ResourceKey<? extends Registry<K>> registryKey, Class<O> objectClass, TriFunction<TagKey<K>, O, Float, PropertyApplierResult> tagKeyFunction) {
        super("none", objectClass, (o, v) -> {});
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
            return JsonDeserialisers.getOrThrow(Float.class).deserialise(input).map(value -> this.tagKeyFunction.apply(tagKey, (O) object, value))
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
    protected PropertyApplierResult applyIfShould(O object, JsonElement input, Applier<O, Float> applier) {
        return null;
    }

}