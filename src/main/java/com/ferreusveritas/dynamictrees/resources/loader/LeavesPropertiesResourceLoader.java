package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.api.treepacks.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.treepacks.PropertyApplierResult;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.ResourceLocationDeserialiser;
import com.ferreusveritas.dynamictrees.deserialisation.result.JsonResult;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.ToolTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.logging.log4j.LogManager;

/**
 * @author Harley O'Connor
 */
public final class LeavesPropertiesResourceLoader extends JsonRegistryResourceLoader<LeavesProperties> {

    public LeavesPropertiesResourceLoader() {
        super(LeavesProperties.REGISTRY, ApplierRegistryEvent.LEAVES_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers.register("color", String.class, LeavesProperties::setColorString)
                .register("color", Integer.class, LeavesProperties::setColorNumber);

        // Primitive leaves are needed before gathering data.
        this.gatherDataAppliers.register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves);

        // Primitive leaves are needed both client and server (so cannot be done on load).
        this.setupAppliers.register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
                .register("family", ResourceLocation.class, (leavesProperties, registryName) -> {
                    final ResourceLocation processedRegName = TreeRegistry.processResLoc(registryName);
                    Family.REGISTRY.runOnNextLock(Family.REGISTRY.generateIfValidRunnable(
                            processedRegName,
                            leavesProperties::setFamily,
                            () -> this.logWarning(leavesProperties.getRegistryName(),
                                    "Could not set family for leaves properties with name \"" + leavesProperties
                                            + "\" as family \"" + processedRegName + "\" was not found.")
                    ));
                });

        this.reloadAppliers.register("requires_shears", Boolean.class, LeavesProperties::setRequiresShears)
                .register("cell_kit", CellKit.class, LeavesProperties::setCellKit)
                .register("smother", Integer.class, LeavesProperties::setSmotherLeavesMax)
                .register("light_requirement", Integer.class, LeavesProperties::setLightRequirement)
                .register("fire_spread", Integer.class, LeavesProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, LeavesProperties::setFlammability)
                .register("connect_any_radius", Boolean.class, LeavesProperties::setConnectAnyRadius)
                .register("does_age", String.class, (leavesProperties, configurationName) -> {
                    return readDoesAge(leavesProperties, configurationName);
                })
                .register("ageing_configuration", LeavesProperties.AgeingConfiguration.class, LeavesProperties::setAgeingConfiguration)
                .register("can_grow_on_ground", Boolean.class, LeavesProperties::setCanGrowOnGround);

        super.registerAppliers();
    }

    private PropertyApplierResult readDoesAge(LeavesProperties leavesProperties, String configurationName) {
        LogManager.getLogger().warn("Deprecated use of leaves properties `does_age` property by \"" +
                leavesProperties.getRegistryName() + "\". This has been renamed to `ageing_configuration`.");
        // Account for refactors: YES -> ALWAYS, NO -> NEVER
        if (configurationName.equalsIgnoreCase("yes")) {
            leavesProperties.setAgeingConfiguration(LeavesProperties.AgeingConfiguration.ALWAYS);
        } else if (configurationName.equalsIgnoreCase("no")) {
            leavesProperties.setAgeingConfiguration(LeavesProperties.AgeingConfiguration.NEVER);
        } else {
            try {
                leavesProperties.setDoesAge(configurationName);
            } catch (IllegalArgumentException e) {
                return PropertyApplierResult.failure("Unsupported ageing configuration: \"" + configurationName + "\".");
            }
        }
        return PropertyApplierResult.success();
    }

    @Override
    protected void applyLoadAppliers(JsonRegistryResourceLoader<LeavesProperties>.LoadData loadData, JsonObject json) {
        final LeavesProperties leavesProperties = loadData.getResource();
        this.readCustomBlockRegistryName(leavesProperties, json);

        if (this.shouldGenerateBlocks(json)) {
            this.generateBlocks(leavesProperties, json);
        }

        super.applyLoadAppliers(loadData, json);
    }

    private void readCustomBlockRegistryName(LeavesProperties leavesProperties, JsonObject json) {
        JsonResult.forInput(json)
                .mapIfContains("block_registry_name", JsonElement.class, input ->
                        ResourceLocationDeserialiser.create(leavesProperties.getRegistryName().getNamespace())
                                .deserialise(input).orElseThrow(), leavesProperties.getBlockRegistryName()
                ).ifSuccessOrElse(
                        leavesProperties::setBlockRegistryName,
                        error -> this.logError(leavesProperties.getRegistryName(), error),
                        warning -> this.logWarning(leavesProperties.getRegistryName(), warning)
                );
    }

    private Boolean shouldGenerateBlocks(JsonObject json) {
        return JsonHelper.getOrDefault(json, "generate_block", Boolean.class, true);
    }

    private void generateBlocks(LeavesProperties leavesProperties, JsonObject json) {
        final BlockBehaviour.Properties blockProperties = JsonHelper.getBlockProperties(
                json,
                leavesProperties.getDefaultMaterial(),
                leavesProperties.getDefaultMaterial().getColor(),
                leavesProperties::getDefaultBlockProperties,
                error -> this.logError(leavesProperties.getRegistryName(), error),
                warning -> this.logWarning(leavesProperties.getRegistryName(), warning)
        );

        if (blockProperties.getHarvestTool() == ToolTypes.SHEARS) {
            leavesProperties.setRequiresShears(true);
        }

        leavesProperties.generateDynamicLeaves(blockProperties);
    }

}
