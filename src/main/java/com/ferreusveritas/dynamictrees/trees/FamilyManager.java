package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.datapacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.JsonReloadListener;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonObjectGetters;
import com.ferreusveritas.dynamictrees.util.json.ObjectFetchResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages loading tree families from the <tt>trees</tt> folder.
 *
 * @author Harley O'Connor
 */
public final class FamilyManager extends JsonReloadListener<Family> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TYPE = "type";

    public FamilyManager() {
        super("families", Family.class, JsonApplierRegistryEvent.FAMILY);
    }

    @Override
    public void registerAppliers(final String applierListIdentifier) {
        this.appliers.register("common_leaves", LeavesProperties.class, Family::setCommonLeaves)
                .register("max_branch_radius", Integer.class, Family::setMaxBranchRadius);

        this.loadAppliers.register("generate_surface_root", Boolean.class, Family::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, Family::setHasStrippedBranch);

        this.reloadAppliers.register("primitive_log", Block.class, Family::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, Family::setPrimitiveStrippedLog)
                .register("stick", Item.class, Family::setStick)
                .register("conifer_variants", Boolean.class, Family::setHasConiferVariants);

        super.registerAppliers(applierListIdentifier);
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> preparedObject, final IResourceManager resourceManager, final boolean firstLoad) {
        for (final Map.Entry<ResourceLocation, JsonElement> entry : preparedObject.entrySet()) {
            final ResourceLocation registryName = entry.getKey();

            final ObjectFetchResult<JsonObject> jsonObjectFetchResult = JsonObjectGetters.JSON_OBJECT_GETTER.get(entry.getValue());

            if (!jsonObjectFetchResult.wasSuccessful()) {
                LOGGER.warn("Skipping loading data for species '{}' due to error: {}", registryName, jsonObjectFetchResult.getErrorMessage());
                return;
            }

            final JsonObject jsonObject = jsonObjectFetchResult.getValue();
            final Family family;

            final Consumer<PropertyApplierResult> failureConsumer = failureResult -> LOGGER.warn("Error whilst loading tree family data for '{}': {}", registryName, failureResult.getErrorMessage());

            if (firstLoad) {
                if (Family.REGISTRY.containsKey(registryName)) {
                    LOGGER.warn("Skipping loading tree family '{}' due to it already being registered.", registryName);
                    return;
                }

                FamilyType<Family> familyType = JsonHelper.getFromObjectOrWarn(jsonObject, TYPE, TreeFamily.CLASS,
                        "Error loading species type for species '" + registryName + "' (defaulting to tree species) :", false);

                // Default to tree family if it wasn't set or couldn't be found.
                if (familyType == null)
                    familyType = TreeFamily.TREE_FAMILY;

                // Construct the tree family class from initial setup properties.
                family = familyType.construct(registryName);

                this.loadAppliers.applyAll(jsonObject, family).forEach(failureConsumer);
            } else {
                family = Family.REGISTRY.getValue(registryName);

                if (family == null) {
                    LOGGER.warn("Skipping loading data for tree family '{}' due to it not being registered.", family);
                    return;
                }

                this.reloadAppliers.applyAll(jsonObject, family).forEach(failureConsumer);
            }

            this.appliers.applyAll(jsonObject.getAsJsonObject(), family).forEach(failureConsumer);

            if (firstLoad) {
                family.setupBlocks();
                Family.REGISTRY.register(family);
                LOGGER.debug("Loaded and registered tree family data: {}.", family.getDisplayString());
            } else {
                LOGGER.debug("Loaded data for tree family: {}.", family.getDisplayString());
            }
        }
    }

}
