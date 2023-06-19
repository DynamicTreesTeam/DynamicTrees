package com.ferreusveritas.dynamictrees.loot.function;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class DTLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION_TYPES = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, DynamicTrees.MOD_ID);
    public static final RegistryObject<LootItemFunctionType> MULTIPLY_COUNT = register("multiply_count", MultiplyCount.Serializer::new);
    public static final RegistryObject<LootItemFunctionType> MULTIPLY_LOGS_COUNT = register("multiply_logs_count", MultiplyLogsCount.Serializer::new);
    public static final RegistryObject<LootItemFunctionType> MULTIPLY_STICKS_COUNT = register("multiply_sticks_count", MultiplySticksCount.Serializer::new);

    private static RegistryObject<LootItemFunctionType> register(String name, Supplier<LootItemConditionalFunction.Serializer<? extends LootItemFunction>> serializerFactory) {
        return LOOT_FUNCTION_TYPES.register(name, () -> new LootItemFunctionType(serializerFactory.get()));
    }
}
