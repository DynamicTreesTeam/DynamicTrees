package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.api.treepacks.JsonApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.resources.JsonRegistryEntryReloadListener;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.ferreusveritas.dynamictrees.util.json.JsonPropertyApplierList;
import com.google.gson.JsonObject;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;

import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class LeavesPropertiesManager extends JsonRegistryEntryReloadListener<LeavesProperties> {

    private JsonPropertyApplierList<AbstractBlock.Properties> blockPropertyAppliers;

    public LeavesPropertiesManager() {
        super(LeavesProperties.REGISTRY, JsonApplierRegistryEvent.LEAVES_PROPERTIES);
    }

    @Override
    public void registerAppliers(final String applierListIdentifier) {
        this.blockPropertyAppliers = new JsonPropertyApplierList<>(AbstractBlock.Properties.class);
        this.blockPropertyAppliers.registerIfTrueApplier("does_not_block_movement", AbstractBlock.Properties::noCollission)
                .registerIfTrueApplier("not_solid", AbstractBlock.Properties::noOcclusion)
                .register("harvest_level", Integer.class, AbstractBlock.Properties::harvestLevel)
                .register("harvest_tool", ToolType.class, AbstractBlock.Properties::harvestTool)
                .register("slipperiness", Float.class, AbstractBlock.Properties::friction)
                .register("speed_factor", Float.class, AbstractBlock.Properties::speedFactor)
                .register("jump_factor", Float.class, AbstractBlock.Properties::jumpFactor)
                .register("sound", SoundType.class, AbstractBlock.Properties::sound)
                .register("hardness", Float.class, (properties, hardness) -> properties.strength(hardness, properties.explosionResistance))
                .register("resistance", Float.class, (properties, resistance) -> properties.strength(properties.destroyTime, resistance))
                .registerIfTrueApplier("zero_hardness_and_resistance", AbstractBlock.Properties::instabreak)
                .register("hardness_and_resistance", Float.class, AbstractBlock.Properties::strength)
                .registerIfTrueApplier("tick_randomly", AbstractBlock.Properties::randomTicks)
                .registerIfTrueApplier("variable_opacity", AbstractBlock.Properties::dynamicShape)
                .registerIfTrueApplier("no_drops", AbstractBlock.Properties::noDrops)
                .registerIfTrueApplier("air", AbstractBlock.Properties::air)
                .registerIfTrueApplier("requires_tool", AbstractBlock.Properties::requiresCorrectToolForDrops);

        this.reloadAppliers.register("requires_shears", Boolean.class, LeavesProperties::setRequiresShears)
                .register("primitive_leaves", Block.class, LeavesProperties::setPrimitiveLeaves)
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
    protected void preLoad(JsonObject jsonObject, LeavesProperties leavesProperties, Consumer<String> errorConsumer, Consumer<String> warningConsumer) {
        // Generate block by default, but allow it to be turned off.
        if (JsonHelper.getOrDefault(jsonObject, "generate_block", Boolean.class, true)) {
            final Material material = JsonHelper.getOrDefault(jsonObject, "material", Material.class, leavesProperties.getDefaultMaterial());
            final AbstractBlock.Properties blockProperties = leavesProperties.getDefaultBlockProperties(material, JsonHelper.getOrDefault(jsonObject, "material_color", MaterialColor.class, material.getColor()));

            this.blockPropertyAppliers.applyAll(jsonObject, blockProperties).forEachErrorWarning(errorConsumer, warningConsumer);
            leavesProperties.generateDynamicLeaves(blockProperties);
        }
    }

}
