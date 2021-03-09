package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.datapacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.resources.MultiJsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.JsonElement;
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
public final class TreeFamilyManager extends MultiJsonReloadListener<TreeFamily> {

    private static final Logger LOGGER = LogManager.getLogger();

    public TreeFamilyManager() {
        super("families", TreeFamily.class, "tree_family");
    }

    @Override
    public void registerAppliers(final String applierRegistryName) {
        this.appliers.register("common_leaves", LeavesProperties.class, TreeFamily::setCommonLeaves)
                .register("max_branch_radius", Integer.class, TreeFamily::setMaxBranchRadius)
                .register("conifer_variants", Boolean.class, TreeFamily::setHasConiferVariants)
                .register("generate_surface_root", Boolean.class, TreeFamily::setHasSurfaceRoot)
                .register("generate_stripped_branch", Boolean.class, TreeFamily::setHasStrippedBranch);

        this.reloadAppliers.register("primitive_log", Block.class, TreeFamily::setPrimitiveLog)
                .register("primitive_stripped_log", Block.class, TreeFamily::setPrimitiveStrippedLog)
                .register("stick", Item.class, TreeFamily::setStick);

        super.registerAppliers(applierRegistryName);
    }

    @Override
    protected void apply(final Map<ResourceLocation, List<Pair<String, JsonElement>>> preparedObject, final IResourceManager resourceManager, final boolean firstLoad) {
        for (final Map.Entry<ResourceLocation, List<Pair<String, JsonElement>>> entry : preparedObject.entrySet()) {
            final ResourceLocation registryName = entry.getKey();

            final TreeFamily family;

            if (firstLoad) {
                family = new TreeFamily(registryName);
            } else {
                family = TreeFamily.REGISTRY.getValue(registryName);

                if (family == null) {
                    LOGGER.warn("Skipping loading data for tree family '{}' due to it not being registered.", family);
                    return;
                }
            }

            for (Pair<String, JsonElement> elementPair : entry.getValue()) {
                final String fileName = elementPair.getKey();
                final JsonElement jsonElement = elementPair.getValue();

                if (!jsonElement.isJsonObject()) {
                    LOGGER.warn("Skipping loading tree family data for {} from {} as its root element is not a Json object.", registryName, fileName);
                    return;
                }

                final Consumer<PropertyApplierResult> failureConsumer = failureResult -> LOGGER.warn("Error whilst loading tree family data for {} from {}: {}", registryName, fileName, failureResult.getErrorMessage());

                if (!firstLoad) {
                    this.reloadAppliers.applyAll(jsonElement.getAsJsonObject(), family).forEach(failureConsumer);
                }

                this.appliers.applyAll(jsonElement.getAsJsonObject(), family).forEach(failureConsumer);
            }

            if (firstLoad) {
                family.setupBlocks();
                TreeFamily.REGISTRY.register(family);
                LOGGER.debug("Loaded and registered tree family data: {}.", family.getDisplayString());
            } else {
                LOGGER.debug("Loaded data for tree family: {}.", family.getDisplayString());
            }
        }
    }

}
