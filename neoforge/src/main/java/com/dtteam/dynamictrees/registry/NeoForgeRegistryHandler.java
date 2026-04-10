package com.dtteam.dynamictrees.registry;

import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.SimpleRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Handles registries for the given mod ID in the constructor. Add-ons should instantiate one of these in their
 * constructor by calling {@link #setup(String, IEventBus)} with their mod ID.
 *
 * <p>The main purpose of this is to prevent Forge from complaining about blocks and items
 * for a different mod ID having their registry names set when the active mod container is <code>dynamictrees</code>, but it
 * also provides an easy way to register items and blocks.</p>
 *
 * @author Harley O'Connor
 */
public class NeoForgeRegistryHandler extends RegistryHandler {

    private static final Method ADD_ENTRIES_METHOD;

    static {
        try {
            ADD_ENTRIES_METHOD = DeferredRegister.class.getDeclaredMethod("addEntries", RegisterEvent.class);
            ADD_ENTRIES_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected final DeferredRegister<Block> blocksDeferredRegister = DeferredRegister.create(BuiltInRegistries.BLOCK, this.getRegistryName().getNamespace());
    protected final DeferredRegister<Item> itemsDeferredRegister = DeferredRegister.create(BuiltInRegistries.ITEM, this.getRegistryName().getNamespace());

    /**
     * Constructor only to be used by the {@link RegistryHandler#REGISTRY} initialization as a default value,
     * for all other purposes use the constructor with modId.
     */
    public NeoForgeRegistryHandler() {
        super();
    }
    /**
     * Instantiates a new {@link RegistryHandler} object for the given mod ID. This should be registered using {@link
     * SimpleRegistry#register(RegistryEntry)} on {@link #REGISTRY}. It will also need to be registered to the mod event bus,
     * which can be grabbed from {@link NeoForge#EVENT_BUS}, so the registry events are fired.
     *
     * @param modId The mod ID for the relevant mod.
     */
    public NeoForgeRegistryHandler(final String modId, IEventBus modEventBus) {
        super(Identifier.fromNamespaceAndPath(modId, modId));
        RegistryHandler.REGISTRY.register(this);

        modEventBus.register(new RegisterEventHandler<>(blocksDeferredRegister));
        modEventBus.register(new RegisterEventHandler<>(itemsDeferredRegister));
    }

    /**
     * Sets up a {@link RegistryHandler} for the given {@code modId}. This includes instantiating, registering, and
     * subscribing it to the {@code mod event bus}. This should be {@code only} be called from the relevant mod
     * constructor!
     *
     * @param modId The {@code mod ID} to setup for.
     */
    public static RegistryHandler setup(final String modId, IEventBus modEventBus) {
        return new NeoForgeRegistryHandler(modId, modEventBus);
    }

    @Nullable
    public Supplier<Block> getBlock(final Identifier registryName) {
        return DeferredHolder.create(BuiltInRegistries.BLOCK.key(), registryName);
    }

    @Nullable
    public Supplier<Item> getItem(final Identifier registryName) {
        return DeferredHolder.create(BuiltInRegistries.ITEM.key(), registryName);
    }

    @SuppressWarnings("unchecked")
    public <T extends Block> Supplier<T> putBlock(final Identifier registryName, final Supplier<T> blockSup) {
        if (this.warnIfInvalid("Block", registryName)) {
            return (Supplier<T>) getBlock(registryName);
        }

        return this.blocksDeferredRegister.register(registryName.getPath(), blockSup);
    }

    @SuppressWarnings("unchecked")
    public <T extends Item> Supplier<T> putItem(final Identifier registryName, final Supplier<T> itemSup) {
        if (this.warnIfInvalid("Item", registryName)) {
            return (Supplier<T>) getItem(registryName);
        }

        return this.itemsDeferredRegister.register(registryName.getPath(), itemSup);
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