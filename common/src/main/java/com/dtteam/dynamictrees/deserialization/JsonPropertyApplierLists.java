package com.dtteam.dynamictrees.deserialization;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Holds common {@link JsonPropertyAppliers} objects.
 *
 * @author Harley O'Connor
 */
public final class JsonPropertyApplierLists {

    public static final JsonPropertyAppliers<BlockBehaviour.Properties> PROPERTIES = new JsonPropertyAppliers<>(BlockBehaviour.Properties.class)
            .registerIfTrueApplier("no_collision", BlockBehaviour.Properties::noCollission)
            .registerIfTrueApplier("not_occlusion", BlockBehaviour.Properties::noOcclusion)
            .register("friction", Float.class, BlockBehaviour.Properties::friction)
            .register("speed_factor", Float.class, BlockBehaviour.Properties::speedFactor)
            .register("jump_factor", Float.class, BlockBehaviour.Properties::jumpFactor)
            .register("sound", SoundType.class, BlockBehaviour.Properties::sound)
            .register("strength", Float.class, BlockBehaviour.Properties::strength)
            .register("explosion_resistance", Float.class, BlockBehaviour.Properties::explosionResistance)
            .registerIfTrueApplier("instabreak", BlockBehaviour.Properties::instabreak)
            .register("light", Integer.class, (properties, light) -> properties.lightLevel(state -> light))
            .registerIfTrueApplier("random_ticks", BlockBehaviour.Properties::randomTicks)
            .registerIfTrueApplier("dynamic_shape", BlockBehaviour.Properties::dynamicShape)
            .registerIfTrueApplier("no_loot_table", BlockBehaviour.Properties::noLootTable)
            .registerIfTrueApplier("air", BlockBehaviour.Properties::air)
            .registerIfTrueApplier("requires_correct_tool_for_drops", BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .register("map_color", MapColor.class, BlockBehaviour.Properties::mapColor)
            ;

}
