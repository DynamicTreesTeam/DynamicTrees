package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.api.registry.*;
import net.minecraft.core.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Handles registries for the given mod ID in the constructor. Add-ons should instantiate one of these in their
 * constructor by calling {@link #setup(String)} with their mod ID.
 *
 * <p>The main purpose of this is to prevent Forge from complaining about blocks and items
 * for a different mod ID having their registry names set when the active mod container is <code>dynamictrees</code>, but it
 * also provides an easy way to register items and blocks.</p>
 *
 * @author Harley O'Connor
 */
public class FabricRegistryHandler extends RegistryHandler {

    /**
     * Constructor only to be used by the {@link RegistryHandler#REGISTRY} initialization as a default value,
     * for all other purposes use the constructor with modId.
     */
    public FabricRegistryHandler() {
        super();
    }

    public FabricRegistryHandler(String modId) {
        super(Identifier.fromNamespaceAndPath(modId, modId));
        RegistryHandler.REGISTRY.register(this);
    }

    /**
     * Sets up a {@link RegistryHandler} for the given {@code modId}. This includes instantiating, registering, and
     * subscribing it to the {@code mod event bus}. This should be {@code only} be called from the relevant mod
     * constructor!
     *
     * @param modId The {@code mod ID} to setup for.
     */
    public static RegistryHandler setup(final String modId) {
        return new FabricRegistryHandler(modId);
    }

    /**
     * Checks if this {@link RegistryHandler} is valid, and if not prints a warning to the console.
     *
     * @param type The type of registry being added.
     * @param registryName The {@link Identifier} registry name.
     * @return True if it was invalid.
     */
    private boolean warnIfInvalid(final String type, final Identifier registryName) {
        if (!this.isValid()) {
            LogManager.getLogger().warn("{} '{}' was added to null registry handler.", type, registryName);
        }
        return !this.isValid();
    }

    @Override
    public @Nullable Supplier<Block> getBlock(Identifier registryName) {
        Block block = BuiltInRegistries.BLOCK.get(registryName);
        return () -> block;
    }

    @Override
    public @Nullable Supplier<Item> getItem(Identifier registryName) {
        Item item = BuiltInRegistries.ITEM.get(registryName);
        return () -> item;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Block> Supplier<T> putBlock(Identifier registryName, Supplier<T> blockSup) {
        if (this.warnIfInvalid("Block", registryName)) {
            return (Supplier<T>) getBlock(registryName);
        }
        T block = Registry.register(BuiltInRegistries.BLOCK, registryName, blockSup.get());
        return () -> block;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Item> Supplier<T> putItem(Identifier registryName, Supplier<T> itemSup) {
        if (this.warnIfInvalid("Item", registryName)) {
            return (Supplier<T>) getItem(registryName);
        }
        T item = Registry.register(BuiltInRegistries.ITEM, registryName, itemSup.get());
        return () -> item;
    }

    public static class RegisterEventHandler<T> {

    }

}