package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Handles registries for the given mod ID in the constructor. Add-ons should instantiate one of these in their
 * constructor by calling {@link #setup(String)} with their mod ID.
 *
 * <p>The main purpose of this is to prevent Forge from complaining about blocks and items
 * for a different mod ID having their registry names set when the active mod container is <tt>dynamictrees</tt>, but it
 * also provides an easy way to register items and blocks.</p>
 *
 * @author Harley O'Connor
 */
public class RegistryHandler extends RegistryEntry<RegistryHandler> {

    /**
     * The central registry for {@link RegistryHandler}s. Stored in a {@link ConcurrentRegistry} as these are created
     * from the mod constructor which are called from a Stream.
     */
    public static final ConcurrentRegistry<RegistryHandler> REGISTRY = new ConcurrentRegistry<>(RegistryHandler.class, new RegistryHandler("null"), true);
    private static final Method ADD_ENTRIES_METHOD;

    static {
        try {
            ADD_ENTRIES_METHOD = DeferredRegister.class.getDeclaredMethod("addEntries", RegisterEvent.class);
            ADD_ENTRIES_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up a {@link RegistryHandler} for the given {@code modId}. This includes instantiating, registering, and
     * subscribing it to the {@code mod event bus}. This should be {@code only} be called from the relevant mod
     * constructor!
     *
     * @param modId The {@code mod ID} to setup for.
     */
    public static void setup(final String modId) {
        final RegistryHandler registryHandler = new RegistryHandler(modId);
        RegistryHandler.REGISTRY.register(registryHandler);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(new RegisterEventHandler<>(registryHandler.blocksDeferredRegister));
        modEventBus.register(new RegisterEventHandler<>(registryHandler.itemsDeferredRegister));
    }

    /**
     * Gets the {@link RegistryHandler} for the given mod ID, or the null registry handler if it doesn't exist.
     *
     * @param modId The mod ID of the mod to get the {@link RegistryHandler} for.
     * @return The {@link RegistryHandler} object.
     */
    public static RegistryHandler get(final String modId) {
        return REGISTRY.get(new ResourceLocation(modId, modId));
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
     * <tt>dynamictrees</tt> if the namespace for the given {@link ResourceLocation}
     * doesn't have a {@link RegistryHandler} registered, so that we don't register blocks or items to mod without a
     * {@link RegistryHandler} (non-add-on mods).
     *
     * @param registryName The {@link ResourceLocation} registry name.
     * @return The correct {@link ResourceLocation} registry name.
     */
    public static ResourceLocation correctRegistryName(ResourceLocation registryName) {
        if (!get(registryName.getNamespace()).isValid()) {
            registryName = ResourceLocationUtils.namespace(registryName, DynamicTrees.MOD_ID);
        }
        return registryName;
    }

    /**
     * Adds a {@link Block} to be registered with the given registry name, for the namespace of that registry name.
     * { Block#setRegistryName(ResourceLocation)} will be called by us on the correct registry event to prevent
     * Forge from complaining - so it shouldn't have been called on the block already!
     *
     * @param registryName The {@link ResourceLocation} registry name to set for the block.
     * @param blockSup The supplier of the {@link Block} object to register.
     * @param <T> The {@link Class} of the {@link Block}.
     * @return The supplier of the {@link Block}, allowing for this to be called in-line.
     */
    public static <T extends Block> RegistryObject<T> addBlock(ResourceLocation registryName, Supplier<T> blockSup) {
        registryName = correctRegistryName(registryName);
        return get(registryName.getNamespace()).putBlock(registryName, blockSup);
    }

    /**
     * Adds an {@link Item} to be registered with the given registry name, for the namespace of that registry name.
     * { Item#setRegistryName(ResourceLocation)} will be called by us on the correct registry event to prevent
     * Forge from complaining - so it shouldn't have been called on the block already!
     *
     * @param registryName The {@link ResourceLocation} registry name to set for the block.
     * @param itemSup The supplier of the {@link Item} object to register.
     * @param <T> The {@link Class} of the {@link Item}.
     * @return The supplier of the {@link Item}, allowing for this to be called in-line.
     */
    public static <T extends Item> RegistryObject<T> addItem(ResourceLocation registryName, Supplier<T> itemSup) {
        registryName = correctRegistryName(registryName);
        return get(registryName.getNamespace()).putItem(registryName, itemSup);
    }

    protected final DeferredRegister<Block> blocksDeferredRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, this.getRegistryName().getNamespace());
    protected final DeferredRegister<Item> itemsDeferredRegister = DeferredRegister.create(ForgeRegistries.ITEMS, this.getRegistryName().getNamespace());

    /**
     * Instantiates a new {@link RegistryHandler} object for the given mod ID. This should be registered using {@link
     * SimpleRegistry#register(RegistryEntry)} on {@link #REGISTRY}. It will also need to be registered to the mod event bus,
     * which can be grabbed from {@link FMLJavaModLoadingContext#getModEventBus()}, so the registry events are fired.
     *
     * @param modId The mod ID for the relevant mod.
     */
    public RegistryHandler(final String modId) {
        super(new ResourceLocation(modId, modId));
    }

    @Nullable
    public RegistryObject<Block> getBlock(final ResourceLocation registryName) {
        return RegistryObject.create(registryName, ForgeRegistries.BLOCKS);
    }

    @Nullable
    public RegistryObject<Item> getItem(final ResourceLocation registryName) {
        return RegistryObject.create(registryName, ForgeRegistries.ITEMS);
    }

    @SuppressWarnings("unchecked")
    public <T extends Block> RegistryObject<T> putBlock(final ResourceLocation registryName, final Supplier<T> blockSup) {
        if (this.warnIfInvalid("Block", registryName)) {
            return (RegistryObject<T>) getBlock(registryName);
        }

        return this.blocksDeferredRegister.register(registryName.getPath(), blockSup);
    }

    @SuppressWarnings("unchecked")
    public <T extends Item> RegistryObject<T> putItem(final ResourceLocation registryName, final Supplier<T> itemSup) {
        if (this.warnIfInvalid("Item", registryName)) {
            return (RegistryObject<T>) getItem(registryName);
        }

        return this.itemsDeferredRegister.register(registryName.getPath(), itemSup);
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

    public static class RegisterEventHandler<T> {
        private final DeferredRegister<T> deferredRegister;

        public RegisterEventHandler(DeferredRegister<T> deferredRegister) {
            this.deferredRegister = deferredRegister;
        }

        // LOWEST allows DT to accumulate blocks & items from inside other listeners to this register event if necessary
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onRegister(RegisterEvent event) {
            if (event.getRegistryKey() == this.deferredRegister.getRegistryKey()) {
                try {
                    ADD_ENTRIES_METHOD.invoke(this.deferredRegister, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
