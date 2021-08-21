package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.resources.DTResourceRegistries;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * An extension of {@link SimpleRegistry} that allows for custom {@link EntryType}.
 *
 * @param <V> The {@link RegistryEntry} type that will be registered.
 * @author Harley O'Connor
 */
// TODO: Update Javadoc
public class TypedRegistry<V extends RegistryEntry<V>> extends SimpleRegistry<V> {

    /**
     * A {@link Map} of {@link EntryType} objects and their registry names. These handle construction of the {@link
     * RegistryEntry}. This is useful for other mods to register sub-classes of the registry entry that can then be
     * referenced from a Json file via a simple resource location.
     */
    private final Map<ResourceLocation, EntryType<V>> typeRegistry = new HashMap<>();

    /**
     * The default {@link EntryType<V>}, the base {@link TypedRegistry.EntryType} for this registry.
     */
    private final EntryType<V> defaultType;

    /**
     * Constructs a new {@link TypedRegistry} with the name being set to {@link Class#getSimpleName()} of the given
     * {@link RegistryEntry}.
     *
     * @param type        The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue   A null entry. See {@link #nullValue} for more details.
     * @param defaultType The default {@link EntryType<V>}.
     */
    public TypedRegistry(Class<V> type, V nullValue, EntryType<V> defaultType) {
        super(type, nullValue);
        this.defaultType = defaultType.setRegistry(this);
    }

    /**
     * Constructs a new {@link TypedRegistry}.
     *
     * @param name        The {@link #name} for this {@link SimpleRegistry}.
     * @param type        The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue   A null entry. See {@link #nullValue} for more details.
     * @param defaultType The default {@link EntryType<V>}.
     */
    public TypedRegistry(String name, Class<V> type, V nullValue, EntryType<V> defaultType) {
        super(name, type, nullValue);
        this.defaultType = defaultType.setRegistry(this);
    }

    /**
     * Registers a custom {@link EntryType}, allowing custom sub-classes of the registry entry to be created and then
     * referenced from Json via the registry name {@link ResourceLocation}.
     *
     * @param registryName The registry name {@link ResourceLocation}.
     * @param type         The {@link EntryType<V>} to register.
     */
    public final void registerType(final ResourceLocation registryName, final EntryType<V> type) {
        this.typeRegistry.put(registryName, type.setRegistry(this));
    }

    public final boolean hasType(final ResourceLocation registryName) {
        return this.typeRegistry.containsKey(registryName);
    }

    @Nullable
    public final EntryType<V> getType(final ResourceLocation registryName) {
        return this.typeRegistry.get(registryName);
    }

    public final EntryType<V> getType(final JsonObject jsonObject, final ResourceLocation registryName) {
        final AtomicReference<EntryType<V>> type = new AtomicReference<>(this.defaultType);
        final JsonElement typeElement = jsonObject.get("type");

        if (typeElement != null) {
            JsonDeserialisers.RESOURCE_LOCATION.deserialise(typeElement)
                    .map(resourceLocation -> this.getType(TreeRegistry.processResLoc(resourceLocation)), "Could not find type for '{}' (will use default).")
                    .ifSuccessOrElse(
                            type::set,
                            error -> LogManager.getLogger().error("Error constructing " + this.name + " '" + registryName + "': " + error),
                            warning -> LogManager.getLogger().warn("Warning whilst constructing " + this.name + " '" + registryName + "': " + warning)
                    );
        }

        return type.get();
    }

    public final EntryType<V> getDefaultType() {
        return defaultType;
    }

    /**
     * Posts a {@link TypeRegistryEvent} to the mod event bus and then calls {@link super#postRegistryEvent()} to
     * register all entries.
     */
    @Override
    public void postRegistryEvent() {
        ModLoader.get().postEvent(new TypeRegistryEvent<>(this));
        super.postRegistryEvent();
    }

    /**
     * Handles creation of the registry entry. Custom types can be registered via {@link #registerType(ResourceLocation,
     * EntryType)}.
     *
     * @param <V> The {@link RegistryEntry} sub-class.
     */
    public static class EntryType<V extends RegistryEntry<V>> {

        private TypedRegistry<V> registry;
        private final Codec<V> codec;

        public EntryType(Codec<V> codec) {
            this.codec = codec;
        }

        public Codec<V> getCodec() {
            return codec;
        }

        public EntryType<V> setRegistry(TypedRegistry<V> registry) {
            this.registry = registry;
            return this;
        }

        @Nullable
        public V decode(final JsonObject jsonObject) {
            final DataResult<Pair<V, JsonElement>> dataResult = this.codec.decode(JsonOps.INSTANCE, jsonObject);

            if (!dataResult.result().isPresent()) {
                if (dataResult.error().isPresent()) {
                    LogManager.getLogger().error("Error constructing " + this.registry.getName() + ": " + dataResult.error().get().message());
                }
                return null;
            }

            return dataResult.result().get().getFirst();
        }

    }

    public static JsonObject putJsonRegistryName(final JsonObject jsonObject, final ResourceLocation registryName) {
        jsonObject.add(DTResourceRegistries.RESOURCE_LOCATION.toString(), new JsonPrimitive(registryName.toString()));
        return jsonObject;
    }

    public static <V extends RegistryEntry<V>> Codec<V> createDefaultCodec(final Function<ResourceLocation, V> constructor) {
        return RecordCodecBuilder.create(instance -> instance
                .group(ResourceLocation.CODEC.fieldOf(DTResourceRegistries.RESOURCE_LOCATION.toString()).forGetter(RegistryEntry::getRegistryName))
                .apply(instance, constructor));
    }

    public static <V extends RegistryEntry<V>> EntryType<V> newType(final Codec<V> codec) {
        return new EntryType<>(codec);
    }

    public static <V extends RegistryEntry<V>> EntryType<V> newType(final Function<ResourceLocation, V> constructor) {
        return newType(createDefaultCodec(constructor));
    }

}
