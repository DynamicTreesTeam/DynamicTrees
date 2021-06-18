package com.ferreusveritas.dynamictrees.util.json;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraftforge.common.ToolType;

/**
 * Holds common {@link JsonPropertyApplierList} objects.
 *
 * @author Harley O'Connor
 */
public final class JsonPropertyApplierLists {

    public static final JsonPropertyApplierList<AbstractBlock.Properties>  PROPERTIES = new JsonPropertyApplierList<>(AbstractBlock.Properties.class)
            .registerIfTrueApplier("does_not_block_movement", AbstractBlock.Properties::noCollission)
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
            .register("light", Integer.class, (properties, light) -> properties.lightLevel(state -> light))
            .registerIfTrueApplier("tick_randomly", AbstractBlock.Properties::randomTicks)
            .registerIfTrueApplier("variable_opacity", AbstractBlock.Properties::dynamicShape)
            .registerIfTrueApplier("no_drops", AbstractBlock.Properties::noDrops)
            .registerIfTrueApplier("air", AbstractBlock.Properties::air)
            .registerIfTrueApplier("requires_tool", AbstractBlock.Properties::requiresCorrectToolForDrops);

}
