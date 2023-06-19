package com.ferreusveritas.dynamictrees.loot.condition;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class DTLootConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, DynamicTrees.MOD_ID);
    public static final RegistryObject<LootItemConditionType> SPECIES_MATCHES = register("species_matches", SpeciesMatches.Serializer::new);
    public static final RegistryObject<LootItemConditionType> SEASONAL_SEED_DROP_CHANCE = register("seasonal_seed_drop_chance", SeasonalSeedDropChance.Serializer::new);
    public static final RegistryObject<LootItemConditionType> VOLUNTARY_SEED_DROP_CHANCE = register("voluntary_seed_drop_chance", VoluntarySeedDropChance.Serializer::new);

    private static RegistryObject<LootItemConditionType> register(String name, Supplier<Serializer<? extends LootItemCondition>> serializerFactory) {
        return LOOT_CONDITION_TYPES.register(name, () -> new LootItemConditionType(serializerFactory.get()));
    }
}
