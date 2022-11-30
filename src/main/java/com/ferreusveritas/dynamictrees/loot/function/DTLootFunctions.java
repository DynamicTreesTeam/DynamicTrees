package com.ferreusveritas.dynamictrees.loot.function;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

/**
 * @author Harley O'Connor
 */
public final class DTLootFunctions {

    public static LootItemFunctionType MULTIPLY_COUNT = register("dynamictrees:multiply_count", new MultiplyCount.Serializer());
    public static LootItemFunctionType MULTIPLY_LOGS_COUNT = register("dynamictrees:multiply_logs_count", new MultiplyLogsCount.Serializer());
    public static LootItemFunctionType MULTIPLY_STICKS_COUNT = register("dynamictrees:multiply_sticks_count", new MultiplySticksCount.Serializer());

    private static LootItemFunctionType register(String name, LootItemConditionalFunction.Serializer<? extends LootItemFunction> serializer) {
        return Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(name), new LootItemFunctionType(serializer));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
