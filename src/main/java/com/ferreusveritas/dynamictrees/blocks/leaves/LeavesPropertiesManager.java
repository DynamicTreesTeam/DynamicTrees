package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.resources.JsonReloadListener;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class LeavesPropertiesManager extends JsonReloadListener<LeavesProperties> {

    private static final Logger LOGGER = LogManager.getLogger();

    private JsonPropertyApplierList<AbstractBlock.Properties> blockPropertyAppliers;

    public LeavesPropertiesManager() {
        super("leaves_properties", LeavesProperties.class, "leaves_properties");
    }

    @Override
    public void registerAppliers(final String applierListIdentifier) {
        this.blockPropertyAppliers = new JsonPropertyApplierList<>(AbstractBlock.Properties.class);
        this.blockPropertyAppliers.register("hardness", Float.class, AbstractBlock.Properties::hardnessAndResistance);

        this.reloadAppliers.register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
                .register("cell_kit", CellKit.class, LeavesProperties::setCellKit)
                .register("smother", Integer.class, LeavesProperties::setSmotherLeavesMax)
                .register("light_requirement", Integer.class, LeavesProperties::setLightRequirement)
                .register("fire_spread", Integer.class, LeavesProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, LeavesProperties::setFlammability)
                .register("connect_any_radius", Boolean.class, LeavesProperties::setConnectAnyRadius);

        super.registerAppliers(applierListIdentifier);
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> preparedObject, final IResourceManager resourceManager, final boolean firstLoad) {
        LeavesProperties.REGISTRY.unlock();

        for (final Map.Entry<ResourceLocation, JsonElement> entry : preparedObject.entrySet()) {
            final ResourceLocation registryName = entry.getKey();
            final JsonElement jsonElement = entry.getValue();

            if (!jsonElement.isJsonObject()) {
                LOGGER.warn("Skipping loading leaves properties '{}' as its root element is not a Json object.", registryName);
                return;
            }

            final JsonObject jsonObject = jsonElement.getAsJsonObject();
            final boolean newEntry = !LeavesProperties.REGISTRY.has(registryName);
            final LeavesProperties leavesProperties;
            final Consumer<PropertyApplierResult> failureConsumer = failureResult -> LOGGER.warn("Error whilst loading leaves properties '{}': {}", registryName, failureResult.getErrorMessage());

            if (newEntry) {
                LeavesProperties.Type leavesPropertiesType = JsonHelper.getFromObjectOrWarn(jsonObject, TYPE, LeavesProperties.Type.class,
                        "Error loading leaves properties type for leaves properties '" + registryName + "' (defaulting to base leaves properties) :", false);

                if (leavesPropertiesType == null)
                    leavesPropertiesType = LeavesProperties.REGISTRY.getDefaultType();

                leavesProperties = leavesPropertiesType.construct(registryName);

                if (firstLoad) {
                    this.loadAppliers.applyAll(jsonObject, leavesProperties).forEach(failureConsumer);

                    final AbstractBlock.Properties blockProperties = leavesProperties.getDefaultBlockProperties();
                    this.blockPropertyAppliers.applyAll(jsonObject, blockProperties).forEach(failureConsumer);
                    leavesProperties.generateDynamicLeaves(blockProperties);
                }
            } else {
                leavesProperties = LeavesProperties.REGISTRY.get(registryName);
            }

            if (!firstLoad)
                this.reloadAppliers.applyAll(jsonObject, leavesProperties).forEach(failureConsumer);

            this.appliers.applyAll(jsonObject, leavesProperties).forEach(failureConsumer);

            if (newEntry) {
                LeavesProperties.REGISTRY.register(leavesProperties);
                LOGGER.debug("Loaded and registered leaves properties: {}.", leavesProperties.getDisplayString());
            } else {
                LOGGER.debug("Loaded leaves properties data: {}.", leavesProperties.getDisplayString());
            }
        }

        if (!firstLoad)
            LeavesProperties.REGISTRY.lock();
    }

}
