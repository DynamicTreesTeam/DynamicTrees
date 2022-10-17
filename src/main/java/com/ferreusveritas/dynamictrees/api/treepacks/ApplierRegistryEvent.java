package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyAppliers;
import com.ferreusveritas.dynamictrees.deserialisation.PropertyAppliers;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * An event fired when a {@link JsonPropertyAppliers} is registered. This can be used for registering custom property
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
public class ApplierRegistryEvent<O, I> extends GenericEvent<O> implements IModBusEvent {

    public static final String FRUITS = "fruits";
    public static final String PODS = "pods";
    public static final String SPECIES = "species";
    public static final String FAMILY = "family";
    public static final String LEAVES_PROPERTIES = "leaves_properties";
    public static final String GLOBAL_DROP_CREATORS = "global_drop_creators";
    public static final String SOIL_PROPERTIES = "soil_properties";

    public final PropertyAppliers<O, I> appliers;
    private final String identifier;

    public ApplierRegistryEvent(PropertyAppliers<O, I> appliers, String identifier) {
        super(appliers.getObjectType());

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
