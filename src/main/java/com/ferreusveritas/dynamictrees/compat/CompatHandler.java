package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.compat.seasons.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Tuple;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.ModList;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public final class CompatHandler {

    public static void init () {
        if (ModList.get().isLoaded(DynamicTrees.SERENE_SEASONS)) {
            handleSereneSeasons();
        }
    }

    public static void handleSereneSeasons() {
        SeasonManager seasonManager = new SeasonManager(
                world -> SeasonsConfig.isDimensionWhitelisted(world.getDimensionKey()) ?
                        new Tuple<>(new SeasonProviderSereneSeasons(), new SeasonGrowthCalculatorActive()) :
                        new Tuple<>(new SeasonProviderNull(), new SeasonGrowthCalculatorNull())
        );
        seasonManager.setTropicalPredicate((world, pos) -> BiomeConfig.usesTropicalSeasons(RegistryKey.getOrCreateKey(Registry.BIOME_KEY,
                Objects.requireNonNull(world.getBiome(pos).getRegistryName()))));
        SeasonHelper.setSeasonManager(seasonManager);
    }

}
