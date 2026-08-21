package com.dtteam.dynamictrees.api.registry;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class RegistryHandler extends RegistryEntry<RegistryHandler> {

    public static final ConcurrentRegistry<RegistryHandler> REGISTRY = new ConcurrentRegistry<>(RegistryHandler.class, Services.REGISTRY.newRegistryHandler(), true);

    public RegistryHandler(final Identifier modId) {
        super(modId);
    }

    /**
     * Constructor only to be used by the {@link RegistryHandler#REGISTRY} initialization as a default value,
     * for all other purposes use the constructor with modId.
     */
    public RegistryHandler() {
        this(DynamicTrees.NULL);
    }

    /**
     * Gets the {@link RegistryHandler} for the given mod ID, or the null registry handler if it doesn't exist.
     *
     * @param modId The mod ID of the mod to get the {@link RegistryHandler} for.
     * @return The {@link RegistryHandler} object.
     */
    public static RegistryHandler get(final String modId) {
        return REGISTRY.get(Identifier.fromNamespaceAndPath(modId, modId));
    }

    /**
     * Gets the {@link RegistryHandler} for the given mod ID, or defaults to the Dynamic Trees one if it doesn't exist.
     *
     * @param modId The mod ID of the mod to get the {@link RegistryHandler} for.
     * @return The {@link RegistryHandler} object.
     */
    public static RegistryHandler getOrCorrected(final String modId) {
        final RegistryHandler handler = get(modId);
        return handler.isValid() ? handler : get(DynamicTrees.MOD_ID);
    }


    /**
     * Ensures the given registry name is 'correct'. This will change the namespace to
     * <code>dynamictrees</code> if the namespace for the given {@link Identifier}
     * doesn't have a {@link RegistryHandler} registered, so that we don't register blocks or items to mod without a
     * {@link RegistryHandler} (non-add-on mods).
     *
     * @param registryName The {@link Identifier} registry name.
     * @return The correct {@link Identifier} registry name.
     */
    public static Identifier correctRegistryName(Identifier registryName) {
        if (!get(registryName.getNamespace()).isValid()) {
            registryName = ResourceLocationUtils.namespace(registryName, DynamicTrees.MOD_ID);
        }
        return registryName;
    }

    /**
     * Adds a {@link Block} to be registered with the given registry name, for the namespace of that registry name.
     *
     * @param registryName The {@link Identifier} registry name to set for the block.
     * @param blockSup The supplier of the {@link Block} object to register.
     * @param <T> The {@link Class} of the {@link Block}.
     * @return The supplier of the {@link Block}, allowing for this to be called in-line.
     */
    public static <T extends Block> Supplier<T> addBlock(Identifier registryName, Supplier<T> blockSup) {
        registryName = correctRegistryName(registryName);
        return get(registryName.getNamespace()).putBlock(registryName, blockSup);
    }

    /**
     * Adds an {@link Item} to be registered with the given registry name, for the namespace of that registry name.
     *
     * @param registryName The {@link Identifier} registry name to set for the block.
     * @param itemSup The supplier of the {@link Item} object to register.
     * @param <T> The {@link Class} of the {@link Item}.
     * @return The supplier of the {@link Item}, allowing for this to be called in-line.
     */
    public static <T extends Item> Supplier<T> addItem(Identifier registryName, Supplier<T> itemSup) {
        registryName = correctRegistryName(registryName);
        return get(registryName.getNamespace()).putItem(registryName, itemSup);
    }

    @Nullable
    public abstract Supplier<Block> getBlock(final Identifier registryName);

    @Nullable
    public abstract Supplier<Item> getItem(final Identifier registryName);

    public abstract  <T extends Block> Supplier<T> putBlock(final Identifier registryName, final Supplier<T> blockSup);

    public abstract  <T extends Item> Supplier<T> putItem(final Identifier registryName, final Supplier<T> itemSup);

}
