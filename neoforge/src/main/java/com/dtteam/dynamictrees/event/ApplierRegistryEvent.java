package com.dtteam.dynamictrees.event;

import com.dtteam.dynamictrees.deserialization.JsonPropertyAppliers;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;
import com.dtteam.dynamictrees.deserialization.applier.JsonPropertyApplier;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * An event fired when a {@link JsonPropertyApplier} is registered. This can be used for registering custom property
 * appliers; for example, DT+ uses this to register appliers specific to the cactus species sub-class.
 * <p>
 * This is an {@link IModBusEvent}, and as such is always fired on the mod bus.
 *
 * @param <O> the type of object the appliers being registered handle applying to
 * @author Harley O'Connor
 * @see Load
 * @see Setup
 * @see Reload
 * @see Common
 */
public class ApplierRegistryEvent<O, I> extends Event implements IModBusEvent {

    public final PropertyAppliers<O, I> appliers;
    private final String identifier;

    public ApplierRegistryEvent(PropertyAppliers<O, I> appliers, String identifier) {
        super();

        this.appliers = appliers;
        this.identifier = identifier;
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are only invoked on initial load.
     * This therefore involves any persistently immutable properties, particularly relating to generated blocks and
     * items since these can only be registered once on initial load.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see GatherData
     * @see Setup
     * @see Reload
     * @see Common
     */
    public static class Load<O, I> extends ApplierRegistryEvent<O, I> {
        public Load(PropertyAppliers<O, I> appliers, String applierListIdentifier) {
            super(appliers, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are invoked on gather data. This
     * refers to when {@link net.minecraftforge.forge.event.lifecycle.GatherDataEvent} is fired.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see Setup
     * @see Reload
     * @see Common
     */
    public static class GatherData<O, I> extends ApplierRegistryEvent<O, I> {
        public GatherData(PropertyAppliers<O, I> appliers, String applierListIdentifier) {
            super(appliers, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are only invoked on initial setup.
     * Initial setup refers to when {@link FMLCommonSetupEvent} is fired.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see GatherData
     * @see Reload
     * @see Common
     */
    public static class Setup<O, I> extends ApplierRegistryEvent<O, I> {
        public Setup(PropertyAppliers<O, I> appliers, String applierListIdentifier) {
            super(appliers, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are invoked on every reload. This
     * Executor)} is invoked, including when launching a world and when executing the {@code /reload} command.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see GatherData
     * @see Setup
     * @see Common
     */
    public static class Reload<O, I> extends ApplierRegistryEvent<O, I> {
        public Reload(PropertyAppliers<O, I> appliers, String applierListIdentifier) {
            super(appliers, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are invoked both on initial load and
     * on every reload.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see GatherData
     * @see Setup
     * @see Reload
     */
    public static class Common<O, I> extends ApplierRegistryEvent<O, I> {
        public Common(PropertyAppliers<O, I> appliers, String applierListIdentifier) {
            super(appliers, applierListIdentifier);
        }
    }

    public PropertyAppliers<O, I> getAppliers() {
        return appliers;
    }

    public String getIdentifier() {
        return identifier;
    }

}
