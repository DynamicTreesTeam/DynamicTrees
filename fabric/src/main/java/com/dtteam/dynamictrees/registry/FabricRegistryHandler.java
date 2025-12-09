package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.api.registry.*;
import net.minecraft.core.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
        super(ResourceLocation.fromNamespaceAndPath(modId, modId));
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
     * @param registryName The {@link ResourceLocation} registry name.
     * @return True if it was invalid.
     */
    private boolean warnIfInvalid(final String type, final ResourceLocation registryName) {
        if (!this.isValid()) {
            LogManager.getLogger().warn("{} '{}' was added to null registry handler.", type, registryName);
        }
        return !this.isValid();
    }

    @Override
    public @Nullable Supplier<Block> getBlock(ResourceLocation registryName) {
        return null;
    }

    @Override
    public @Nullable Supplier<Item> getItem(ResourceLocation registryName) {
        return null;
    }

    @Override
    public <T extends Block> Supplier<T> putBlock(ResourceLocation registryName, Supplier<T> blockSup) {
        return ()->(T)Registry.register(BuiltInRegistries.BLOCK, registryName, (Block)blockSup.get());
    }

    @Override
    public <T extends Item> Supplier<T> putItem(ResourceLocation registryName, Supplier<T> itemSup) {
        return ()->(T)Registry.register(BuiltInRegistries.ITEM, registryName, (Item)itemSup.get());
    }

    public static class RegisterEventHandler<T> {

    }

}