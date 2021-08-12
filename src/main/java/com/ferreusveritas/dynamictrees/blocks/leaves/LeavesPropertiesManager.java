package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.treepacks.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.ResourceLocationDeserialiser;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.ToolTypes;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class LeavesPropertiesManager extends JsonRegistryEntryReloadListener<LeavesProperties> {

    private static final Logger LOGGER = LogManager.getLogger();

    public LeavesPropertiesManager() {
        super(LeavesProperties.REGISTRY, ApplierRegistryEvent.LEAVES_PROPERTIES);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers.register("color", String.class, LeavesProperties::setColorString)
                .register("color", Integer.class, LeavesProperties::setColorNumber);

        this.reloadAppliers.register("requires_shears", Boolean.class, LeavesProperties::setRequiresShears)
                .register("cell_kit", CellKit.class, LeavesProperties::setCellKit)
                .register("smother", Integer.class, LeavesProperties::setSmotherLeavesMax)
                .register("light_requirement", Integer.class, LeavesProperties::setLightRequirement)
                .register("fire_spread", Integer.class, LeavesProperties::setFireSpreadSpeed)
                .register("flammability", Integer.class, LeavesProperties::setFlammability)
                .register("connect_any_radius", Boolean.class, LeavesProperties::setConnectAnyRadius)
                .register("does_age", String.class, LeavesProperties::setDoesAge)
                .register("can_grow_on_ground", Boolean.class, LeavesProperties::setCanGrowOnGround);

        // Primitive leaves are needed both client and server (so cannot be done on load).
        this.setupAppliers.register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
                .register("family", ResourceLocation.class, (leavesProperties, registryName) -> {
                    final ResourceLocation processedRegName = TreeRegistry.processResLoc(registryName);
                    Family.REGISTRY.runOnNextLock(Family.REGISTRY.generateIfValidRunnable(processedRegName, leavesProperties::setFamily, () -> LOGGER.warn("Could not set family for '" + leavesProperties + "' as Family '" + processedRegName + "' was not found.")));
                });

        super.registerAppliers();
    }

    @Override
    protected void preLoad(JsonObject jsonObject, LeavesProperties leavesProperties, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        // If a custom block registry name was set, set and use it.
        JsonHelper.JsonObjectReader.of(jsonObject).ifContains("block_registry_name", jsonElement ->
                ResourceLocationDeserialiser.create(leavesProperties.getRegistryName().getNamespace())
                        .deserialise(jsonElement).ifSuccess(leavesProperties::setBlockRegistryName)
        );

        // Generate block by default, but allow it to be turned off.
        if (JsonHelper.getOrDefault(jsonObject, "generate_block", Boolean.class, true)) {
            final AbstractBlock.Properties blockProperties = JsonHelper.getBlockProperties(jsonObject,
                    leavesProperties.getDefaultMaterial(), leavesProperties.getDefaultMaterial().getColor(),
                    leavesProperties::getDefaultBlockProperties, errorConsumer, warningConsumer);

            if (blockProperties.getHarvestTool() == ToolTypes.SHEARS) {
                leavesProperties.setRequiresShears(true);
            }

            leavesProperties.generateDynamicLeaves(blockProperties);
        }
    }

}
