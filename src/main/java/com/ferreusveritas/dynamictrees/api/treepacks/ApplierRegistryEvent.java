package com.ferreusveritas.dynamictrees.api.treepacks;

import com.ferreusveritas.dynamictrees.deserialisation.JsonPropertyApplierList;
import net.minecraft.command.Commands;
import net.minecraft.resources.DataPackRegistries;
import net.minecraftforge.eventbus.api.GenericEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * An event fired when a {@link JsonPropertyApplierList} is registered. This can be used for registering custom property
 * appliers; for example, DT+ uses this to register appliers specific to the cactus species sub-class.
 * <p>
 * This is an {@link IModBusEvent}, meaning it is always fired on the mod bus.
 *
 * @param <O> the type of object the appliers being registered handle applying to
 * @author Harley O'Connor
 * @see Load
 * @see Setup
 * @see Reload
 * @see Common
 */
public class ApplierRegistryEvent<O> extends GenericEvent<O> implements IModBusEvent {

    public static final String SPECIES = "species";
    public static final String FAMILY = "family";
    public static final String LEAVES_PROPERTIES = "leaves_properties";
    public static final String GLOBAL_DROP_CREATORS = "global_drop_creators";
    public static final String SOIL_PROPERTIES = "soil_properties";

    public final JsonPropertyApplierList<O> applierList;
    private final String identifier;

    public ApplierRegistryEvent(JsonPropertyApplierList<O> applierList, String identifier) {
        super(applierList.getObjectType());

        this.applierList = applierList;
        this.identifier = identifier;
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are only invoked on initial load.
     * This therefore involves any persistently immutable properties, particularly relating to generated blocks and
     * items since these can only be registered once on initial load.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Setup
     * @see Reload
     * @see Common
     */
    public static class Load<O> extends ApplierRegistryEvent<O> {
        public Load(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are only invoked on initial setup.
     * Initial setup refers to when {@link FMLCommonSetupEvent} is fired.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see Reload
     * @see Common
     */
    public static class Setup<O> extends ApplierRegistryEvent<O> {
        public Setup(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are invoked on every reload. This
     * refers to any time {@link DataPackRegistries#loadResources(List, Commands.EnvironmentType, int, Executor,
     * Executor)} is invoked, including when launching a world and when executing the {@code /reload} command.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see Setup
     * @see Common
     */
    public static class Reload<O> extends ApplierRegistryEvent<O> {
        public Reload(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    /**
     * An {@link ApplierRegistryEvent} that is fired when registering appliers that are invoked both on initial load and
     * on every reload.
     *
     * @param <O> the type of object the appliers being registered handle applying to
     * @see ApplierRegistryEvent
     * @see Load
     * @see Setup
     * @see Reload
     */
    public static class Common<O> extends ApplierRegistryEvent<O> {
        public Common(JsonPropertyApplierList<O> applierList, String applierListIdentifier) {
            super(applierList, applierListIdentifier);
        }
    }

    public JsonPropertyApplierList<O> getApplierList() {
        return applierList;
    }

    public String getIdentifier() {
        return identifier;
    }

}
