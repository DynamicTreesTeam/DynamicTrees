package com.ferreusveritas.dynamictrees.loot.condition;

import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DTLootConditions {

    public static final LootConditionType IS_SPECIES = register("dynamictrees:is_species", new IsSpecies.Serializer());
    public static final LootConditionType SEASONAL_SEED_DROP_CHANCE = register("dynamictrees:seasonal_seed_drop_chance", new SeasonalSeedDropChance.Serializer());
    public static final LootConditionType VOLUNTARY_SEED_DROP_CHANCE = register("dynamictrees:voluntary_seed_drop_chance", new VoluntarySeedDropChance.Serializer());

    private static LootConditionType register(String name, ILootSerializer<? extends ILootCondition> serializer) {
        return Registry.register(Registry.LOOT_CONDITION_TYPE, new ResourceLocation(name), new LootConditionType(serializer));
    }

    /** Invoked to initialise static fields. */
    public static void load() {}

}
