package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.compat.seasons.*;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.ModList;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;

/**
 * @author Harley O'Connor
 */
public final class CompatHandler {

    public static void init () {
        if (ModList.get().isLoaded(DynamicTrees.SERENE_SEASONS)) {
            initSereneSeasons();
        }
    }

    public static void initSereneSeasons() {
        SeasonManager seasonManager = new SeasonManager(
                world -> SeasonsConfig.isDimensionWhitelisted(world.dimension()) ?
                        new Tuple<>(new SeasonProviderSereneSeasons(), new SeasonGrowthCalculatorActive()) :
                        new Tuple<>(new SeasonProviderNull(), new SeasonGrowthCalculatorNull())
        );
        seasonManager.setTropicalPredicate((world, pos) -> {
            final ResourceLocation biomeResLoc = world.getBiome(pos).getRegistryName();
            return biomeResLoc != null && BiomeConfig.usesTropicalSeasons(RegistryKey.create(Registry.BIOME_REGISTRY, biomeResLoc));
        });
        SeasonHelper.setSeasonManager(seasonManager);
    }

}
