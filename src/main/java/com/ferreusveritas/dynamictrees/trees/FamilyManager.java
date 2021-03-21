package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.JsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages loading tree families from the <tt>trees</tt> folder.
 *
 * @author Harley O'Connor
 */
public final class FamilyManager extends JsonReloadListener<Family> {

    private static final Logger LOGGER = LogManager.getLogger();

    public FamilyManager() {
        super("families", Family.class, JsonApplierRegistryEvent.FAMILY);
    }

    @Override
    public void registerAppliers(final String applierListIdentifier) {
        this.appliers.register("common_leaves", LeavesProperties.class, Family::setCommonLeaves)
                .register("max_branch_radius", Integer.class, Family::setMaxBranchRadius);

        this.loadAppliers.register("common_species", ResourceLocation.class, (family, registryName) -> {
            registryName = TreeRegistry.processResLoc(registryName);
            Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName, family::setupCommonSpecies, setCommonWarn(family, registryName)));
        }).register("generate_surface_root", Boolean.class, Family::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, Family::setHasStrippedBranch);

        this.reloadAppliers.register("common_species", ResourceLocation.class, (family, registryName) -> {
            registryName = TreeRegistry.processResLoc(registryName);
            Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(registryName, family::setCommonSpecies, setCommonWarn(family, registryName)));
        }).register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .register("stick", Item.class, Family::setStick)
                .register("conifer_variants", Boolean.class, Family::setHasConiferVariants)
                .register("can_support_cocoa", Boolean.class, Family::setCanSupportCocoa);

        super.registerAppliers(applierListIdentifier);
    }

    /**
     * Generates a runnable for if there was not a registered {@link Species} under the
     * {@link ResourceLocation} given to set as common for the {@link Family} given.
     *
     * @param family The {@link Family} object.
     * @param registryName The registry name {@link ResourceLocation}.
     * @return A {@link Runnable} that warns the user of the error.
     */
    private static Runnable setCommonWarn(final Family family, final ResourceLocation registryName) {
        return () -> LOGGER.warn("Could not set common species for '" + family + "' as Species '" + registryName + "' was not found.");
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> preparedObject, final IResourceManager resourceManager, final boolean firstLoad) {
        Family.REGISTRY.unlock(); // Ensure registry is unlocked.

        preparedObject.forEach((registryName, jsonElement) -> {
            final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(jsonElement);

            if (!jsonObjectFetchResult.wasSuccessful()) {
                LOGGER.warn("Skipping loading data for family '{}' due to error: {}", registryName, jsonObjectFetchResult.getErrorMessage());
                return;
            }

            final JsonObject jsonObject = jsonObjectFetchResult.getValue();

            // Skip the current entry if it shouldn't load.
            if (!this.shouldLoad(jsonObject, "Error loading data for family '" + registryName + "': "))
                return;

            final Family family;
            final boolean newRegistry = !Family.REGISTRY.has(registryName);

            final Consumer<PropertyApplierResult> failureConsumer = failureResult -> LOGGER.warn("Error whilst loading tree family data for '{}': {}", registryName, failureResult.getErrorMessage());

            if (newRegistry) {
                Family.Type familyType = JsonHelper.getFromObjectOrWarn(jsonObject, TYPE, Family.Type.class,
                        "Error loading family type for family '" + registryName + "' (defaulting to tree family) :", false);

                // Default to tree family if it wasn't set or couldn't be found.
                if (familyType == null)
                    familyType = Family.REGISTRY.getDefaultType();

                // Construct the tree family class from initial setup properties.
                family = familyType.construct(registryName);

                if (firstLoad)
                    this.loadAppliers.applyAll(jsonObject, family).forEach(failureConsumer);
                else family.setPreReloadDefaults();
            } else {
                family = Family.REGISTRY.get(registryName).reset().setPreReloadDefaults();
            }

            if (!firstLoad)
                this.reloadAppliers.applyAll(jsonObject, family).forEach(failureConsumer);

            this.appliers.applyAll(jsonObject.getAsJsonObject(), family).forEach(failureConsumer);

            if (!firstLoad)
                family.setPostReloadDefaults();

            if (newRegistry) {
                if (firstLoad) {
                    family.setupBlocks();
                }

                Family.REGISTRY.register(family);
                LOGGER.debug("Loaded and registered tree family data: {}.", family.getDisplayString());
            } else {
                LOGGER.debug("Loaded data for tree family: {}.", family.getDisplayString());
            }
        });

        // Lock registry (don't lock on first load as registry events are fired after).
        if (!firstLoad)
            Family.REGISTRY.lock();
    }

}
