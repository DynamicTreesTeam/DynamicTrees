package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;

import java.util.Map;

/**
 * Handles registries for the given mod ID in the constructor. Add-ons should instantiate
 * one of these in their constructor, registering it to {@link #REGISTRY} and the mod event
 * bus such as is done below:<br><br><br>
 *
 * <pre>
 * final RegistryHandler registryHandler = new RegistryHandler(MOD_ID);
 * RegistryHandler.REGISTRY.register(registryHandler);
 * FMLJavaModLoadingContext.get().getModEventBus().register(registryHandler);
 * </pre>
 *
 * <p>The main purpose of this is to prevent Forge from complaining about blocks and items
 * for a different mod ID having their registry names set when the active mod container
 * is <tt>dynamictrees</tt>, but it also provides an easy way to register items and blocks.</p>
 *
 * @author Harley O'Connor
 */
public class RegistryHandler extends RegistryEntry<RegistryHandler> {

    public static final Registry<RegistryHandler> REGISTRY = new Registry<>(RegistryHandler.class, new RegistryHandler("null"), true);

    /**
     * Gets the {@link RegistryHandler} for the given mod ID, or the null registry handler
     * if it doesn't exist.
     *
     * @param modId The mod ID of the mod to get the {@link RegistryHandler} for.
     * @return The {@link RegistryHandler} object.
     */
    public static RegistryHandler get(final String modId) {
        return REGISTRY.get(new ResourceLocation(modId, modId));
    }

    /**
     * Gets the {@link RegistryHandler} for the given mod ID, or defaults to the Dynamic Trees
     * one if it doesn't exist.
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
     * <tt>dynamictrees</tt> if the namespace for the given {@link ResourceLocation}
     * doesn't have a {@link RegistryHandler} registered, so that we don't register
     * blocks or items to mod without a {@link RegistryHandler} (non-add-on mods).
     *
     * @param registryName The {@link ResourceLocation} registry name.
     * @return The correct {@link ResourceLocation} registry name.
     */
    public static ResourceLocation correctRegistryName (ResourceLocation registryName) {
        if (!get(registryName.getNamespace()).isValid())
            registryName = ResourceLocationUtils.namespace(registryName, DynamicTrees.MOD_ID);
        return registryName;
    }

    /**
     * Adds a {@link Block} to be registered with the given registry name, for the namespace
     * of that registry name. {@link Block#setRegistryName(ResourceLocation)} will be called
     * by us on the correct registry event to prevent Forge from complaining - so it
     * shouldn't have been called on the block already!
     *
     * @param registryName The {@link ResourceLocation} registry name to set for the block.
     * @param block The {@link Block} object to register.
     * @param <T> The {@link Class} of the {@link Block}.
     * @return The {@link Block}, allowing for this to be called in-line.
     */
    public static <T extends Block> T addBlock(ResourceLocation registryName, final T block) {
        registryName = correctRegistryName(registryName);
        get(registryName.getNamespace()).putBlock(registryName, block);
        return block;
    }

    /**
     * Adds an {@link Item} to be registered with the given registry name, for the namespace
     * of that registry name. {@link Item#setRegistryName(ResourceLocation)} will be called
     * by us on the correct registry event to prevent Forge from complaining - so it
     * shouldn't have been called on the block already!
     *
     * @param registryName The {@link ResourceLocation} registry name to set for the block.
     * @param item The {@link Item} object to register.
     * @param <T> The {@link Class} of the {@link Item}.
     * @return The {@link Item}, allowing for this to be called in-line.
     */
    public static <T extends Item> T addItem(ResourceLocation registryName, final T item) {
        registryName = correctRegistryName(registryName);
        get(registryName.getNamespace()).putItem(registryName, item);
        return item;
    }

    protected final Map<ResourceLocation, Block> blocks = Maps.newHashMap();
    protected final Map<ResourceLocation, Item> items = Maps.newHashMap();

    /**
     * Instantiates a new {@link RegistryHandler} object for the given mod ID. This should
     * be registered using {@link Registry#register(RegistryEntry)} on {@link #REGISTRY}.
     * It will also need to be registered to the mod event bus, which can be grabbed from
     * {@link FMLJavaModLoadingContext#getModEventBus()}, so the registry events are fired.
     *
     * @param modId The mod ID for the relevant mod.
     */
    public RegistryHandler(final String modId) {
        super(new ResourceLocation(modId, modId));
    }

    public void putBlock(final ResourceLocation registryName, final Block block) {
        if (this.warnIfInvalid("Block", registryName)) {
            return;
        }

        this.blocks.put(registryName, block);
    }

    public void putItem(final ResourceLocation registryName, final Item item) {
        if (this.warnIfInvalid("Item", registryName)) {
            return;
        }

        this.items.put(registryName, item);
    }

    /**
     * Checks if this {@link RegistryHandler} is valid, and if not prints a warning to the
     * console.
     *
     * @param type The type of registry being added.
     * @param registryName The {@link ResourceLocation} registry name.
     * @return True if it was invalid.
     */
    private boolean warnIfInvalid(final String type, final ResourceLocation registryName) {
        if (!this.isValid())
            LogManager.getLogger().warn("{} '{}' was added to null registry handler.", type, registryName);
        return !this.isValid();
    }

    @SubscribeEvent
    public void onBlockRegistry (final RegistryEvent.Register<Block> event) {
        this.blocks.forEach((resourceLocation, block) -> event.getRegistry().register(block.setRegistryName(resourceLocation)));
    }

    @SubscribeEvent
    public void onItemRegistry (final RegistryEvent.Register<Item> event) {
        this.items.forEach(((resourceLocation, item) -> event.getRegistry().register(item.setRegistryName(resourceLocation))));
    }

}
