package com.ferreusveritas.dynamictrees.loot.function;

import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DTLootFunctions {

    public static LootFunctionType MULTIPLY_LOGS_COUNT = register("dynamictrees:multiply_logs_count", new MultiplyLogsCount.Serializer());
    public static LootFunctionType MULTIPLY_STICKS_COUNT = register("dynamictrees:multiply_sticks_count", new MultiplySticksCount.Serializer());

    private static LootFunctionType register(String name, ILootSerializer<? extends ILootFunction> serializer) {
        return Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(name), new LootFunctionType(serializer));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
