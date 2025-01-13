package com.dtteam.dynamictrees.loot.condition;

import com.dtteam.dynamictrees.loot.DTLootContextParams;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * @author Harley O'Connor
 */
public final class SpeciesMatches implements LootItemCondition {

    public static final MapCodec<SpeciesMatches> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(Codec.STRING.fieldOf("name").forGetter((c)->c.regex))
                    .apply(instance, SpeciesMatches::new));

    private final String regex;

    public SpeciesMatches(String regex) {
        this.regex = regex;
    }

    @Override
    public LootItemConditionType getType() {
        return DTRegistries.SPECIES_MATCHES.get();
    }

    @Override
    public boolean test(LootContext context) {
        final Species species = context.getParamOrNull(DTLootContextParams.SPECIES);
        assert species != null;
        return String.valueOf(species.getRegistryName()).matches(regex);
    }

}
