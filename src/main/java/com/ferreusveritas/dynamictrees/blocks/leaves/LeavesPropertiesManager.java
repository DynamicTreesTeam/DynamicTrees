package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.resources.JsonReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;
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
        this.blockPropertyAppliers.registerIfTrueApplier("does_not_block_movement", AbstractBlock.Properties::doesNotBlockMovement)
                .registerIfTrueApplier("not_solid", AbstractBlock.Properties::notSolid)
                .register("harvest_level", Integer.class, AbstractBlock.Properties::harvestLevel)
                .register("harvest_tool", ToolType.class, AbstractBlock.Properties::harvestTool)
                .register("slipperiness", Float.class, AbstractBlock.Properties::slipperiness)
                .register("speed_factor", Float.class, AbstractBlock.Properties::speedFactor)
                .register("jump_factor", Float.class, AbstractBlock.Properties::jumpFactor)
                .register("sound", SoundType.class, AbstractBlock.Properties::sound)
                .register("hardness", Float.class, (properties, hardness) -> properties.hardnessAndResistance(hardness, properties.resistance))
                .register("resistance", Float.class, (properties, resistance) -> properties.hardnessAndResistance(properties.hardness, resistance))
                .registerIfTrueApplier("zero_hardness_and_resistance", AbstractBlock.Properties::zeroHardnessAndResistance)
                .register("hardness_and_resistance", Float.class, AbstractBlock.Properties::hardnessAndResistance)
                .registerIfTrueApplier("tick_randomly", AbstractBlock.Properties::tickRandomly)
                .registerIfTrueApplier("variable_opacity", AbstractBlock.Properties::variableOpacity)
                .registerIfTrueApplier("no_drops", AbstractBlock.Properties::noDrops)
                .registerIfTrueApplier("air", AbstractBlock.Properties::setAir)
                .registerIfTrueApplier("requires_tool", AbstractBlock.Properties::setRequiresTool);

        this.reloadAppliers.register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
                .register("cell_kit", CellKit.class, LeavesProperties::setCellKit)
                .register("smother", Integer.class, LeavesProperties::setSmotherLeavesMax)
                .register("light_requirement", Integer.class, LeavesProperties::setLightRequirement)
                .register("fire_spread", Integer.class, LeavesProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, LeavesProperties::setFlammability)
                .register("connect_any_radius", Boolean.class, LeavesProperties::setConnectAnyRadius);

        this.postApplierEvent(this.blockPropertyAppliers, "leaves_block_property");
        super.registerAppliers(applierListIdentifier);
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> preparedObject, final IResourceManager resourceManager, final boolean firstLoad) {
        LeavesProperties.REGISTRY.unlock();

        preparedObject.forEach((registryName, jsonElement) -> {
            if (!jsonElement.isJsonObject()) {
                LOGGER.warn("Skipping loading leaves properties '{}' as its root element is not a Json object.", registryName);
                return;
            }

            final JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Skip the current entry if it shouldn't load.
            if (!this.shouldLoad(jsonObject, "Error loading data for leaves properties '" + registryName + "': "))
                return;

            final boolean newEntry = !LeavesProperties.REGISTRY.has(registryName);
            final LeavesProperties leavesProperties;

            final Consumer<String> errorConsumer = errorMessage -> LOGGER.error("Error whilst loading leaves properties '{}': {}", registryName, errorMessage);
            final Consumer<String> warningConsumer = warningMessage -> LOGGER.warn("Warning whilst loading leaves properties '{}': {}", registryName, warningMessage);

            if (newEntry) {
                LeavesProperties.Type leavesPropertiesType = JsonHelper.getFromObjectOrWarn(jsonObject, TYPE, LeavesProperties.Type.class,
                        "Error loading leaves properties type for leaves properties '" + registryName + "' (defaulting to base leaves properties) :", false);

                if (leavesPropertiesType == null)
                    leavesPropertiesType = LeavesProperties.REGISTRY.getDefaultType();

                leavesProperties = leavesPropertiesType.construct(registryName);

                if (firstLoad) {
                    this.loadAppliers.applyAll(jsonObject, leavesProperties).forEachErrorWarning(errorConsumer, warningConsumer);

                    // Generate block by default, but allow it to be turned off.
                    if (JsonHelper.getOrDefault(jsonObject, "generate_block", Boolean.class, true)) {
                        final Material material = JsonHelper.getOrDefault(jsonObject, "material", Material.class, leavesProperties.getDefaultMaterial());
                        final AbstractBlock.Properties blockProperties = leavesProperties.getDefaultBlockProperties(material, JsonHelper.getOrDefault(jsonObject, "material_color", MaterialColor.class, material.getColor()));

                        this.blockPropertyAppliers.applyAll(jsonObject, blockProperties).forEachErrorWarning(errorConsumer, warningConsumer);
                        leavesProperties.generateDynamicLeaves(blockProperties);
                    }
                }
            } else {
                leavesProperties = LeavesProperties.REGISTRY.get(registryName).reset().setPreReloadDefaults();
            }

            if (!firstLoad)
                this.reloadAppliers.applyAll(jsonObject, leavesProperties).forEachErrorWarning(errorConsumer, warningConsumer);

            this.appliers.applyAll(jsonObject, leavesProperties).forEachError(errorConsumer).forEachErrorWarning(errorConsumer, warningConsumer);

            if (!firstLoad)
                leavesProperties.setPostReloadDefaults();

            if (newEntry) {
                LeavesProperties.REGISTRY.register(leavesProperties);
                LOGGER.debug("Loaded and registered leaves properties: {}.", leavesProperties.getDisplayString());
            } else {
                LOGGER.debug("Loaded leaves properties data: {}.", leavesProperties.getDisplayString());
            }
        });

        if (!firstLoad)
            LeavesProperties.REGISTRY.lock();
    }

}
