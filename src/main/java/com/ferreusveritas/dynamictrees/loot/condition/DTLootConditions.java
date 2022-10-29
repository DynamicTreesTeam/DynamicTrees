package com.ferreusveritas.dynamictrees.loot.condition;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * @author Harley O'Connor
 */
public final class DTLootConditions {

    public static final LootItemConditionType SPECIES_MATCHES = register("dynamictrees:species_matches", new SpeciesMatches.Serializer());
    public static final LootItemConditionType SEASONAL_SEED_DROP_CHANCE = register("dynamictrees:seasonal_seed_drop_chance", new SeasonalSeedDropChance.Serializer());
    public static final LootItemConditionType VOLUNTARY_SEED_DROP_CHANCE = register("dynamictrees:voluntary_seed_drop_chance", new VoluntarySeedDropChance.Serializer());

    private static LootItemConditionType register(String name, Serializer<? extends LootItemCondition> serializer) {
        return Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(name), new LootItemConditionType(serializer));
    }

    /**
     * Invoked to initialise static fields.
     */
    public static void load() {
    }

}
