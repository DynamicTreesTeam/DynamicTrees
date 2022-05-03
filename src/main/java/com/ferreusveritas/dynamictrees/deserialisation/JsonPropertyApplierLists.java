package com.ferreusveritas.dynamictrees.deserialisation;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.ToolType;

/**
 * Holds common {@link JsonPropertyAppliers} objects.
 *
 * @author Harley O'Connor
 */
public final class JsonPropertyApplierLists {

    public static final JsonPropertyAppliers<BlockBehaviour.Properties> PROPERTIES = new JsonPropertyAppliers<>(BlockBehaviour.Properties.class)
            .registerIfTrueApplier("does_not_block_movement", BlockBehaviour.Properties::noCollission)
            .registerIfTrueApplier("not_solid", BlockBehaviour.Properties::noOcclusion)
            .register("harvest_level", Integer.class, BlockBehaviour.Properties::harvestLevel)
            .register("harvest_tool", ToolType.class, BlockBehaviour.Properties::harvestTool)
            .register("slipperiness", Float.class, BlockBehaviour.Properties::friction)
            .register("speed_factor", Float.class, BlockBehaviour.Properties::speedFactor)
            .register("jump_factor", Float.class, BlockBehaviour.Properties::jumpFactor)
            .register("sound", SoundType.class, BlockBehaviour.Properties::sound)
            .register("hardness", Float.class, (properties, hardness) -> properties.strength(hardness, properties.explosionResistance))
            .register("resistance", Float.class, (properties, resistance) -> properties.strength(properties.destroyTime, resistance))
            .registerIfTrueApplier("zero_hardness_and_resistance", BlockBehaviour.Properties::instabreak)
            .register("hardness_and_resistance", Float.class, BlockBehaviour.Properties::strength)
            .register("light", Integer.class, (properties, light) -> properties.lightLevel(state -> light))
            .registerIfTrueApplier("tick_randomly", BlockBehaviour.Properties::randomTicks)
            .registerIfTrueApplier("variable_opacity", BlockBehaviour.Properties::dynamicShape)
            .registerIfTrueApplier("no_drops", BlockBehaviour.Properties::noDrops)
            .registerIfTrueApplier("air", BlockBehaviour.Properties::air)
            .registerIfTrueApplier("requires_tool", BlockBehaviour.Properties::requiresCorrectToolForDrops);

}
