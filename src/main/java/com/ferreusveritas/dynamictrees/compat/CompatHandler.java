package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.compat.seasons.*;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.google.common.collect.Maps;
import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class CompatHandler {

    private static final Map<String, Supplier<SeasonManager>> SEASON_MANAGERS = Maps.newHashMap();

    /**
     * Registers the specified {@link SeasonManager} supplier for the specified {@code modId}.
     * Given as a supplier for lazy initialisation.
     *
     * <p>The season manager to use is then selected by {@link DTConfigs#PREFERRED_SEASON_MOD}
     * on config reload.</p>
     *
     * @param modId The mod ID the season manager handles.
     * @param supplier The {@link SeasonManager} supplier.
     */
    public static void registerSeasonManager(final String modId, Supplier<SeasonManager> supplier) {
        SEASON_MANAGERS.put(modId, supplier);
    }

    public static void registerBuiltInSeasonManagers() {
        registerSeasonManager(DynamicTrees.SERENE_SEASONS, () -> {
            SeasonManager seasonManager = new SeasonManager(
                    world -> SeasonsConfig.isDimensionWhitelisted(world.dimension()) ?
                            new Tuple<>(new SereneSeasonsSeasonProvider(), new ActiveSeasonGrowthCalculator()) :
                            new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator())
            );
            seasonManager.setTropicalPredicate((world, pos) -> {
                final ResourceLocation registryName = world.getBiome(pos).getRegistryName();
                return registryName != null && BiomeConfig.usesTropicalSeasons(RegistryKey.create(Registry.BIOME_REGISTRY, registryName));
            });
            return seasonManager;
        });

        registerSeasonManager(DynamicTrees.BETTER_WEATHER, () -> new SeasonManager(
                world -> Season.getSeason(world) == null ?
                        new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator()) :
                        new Tuple<>(new BetterWeatherSeasonProvider(), new ActiveSeasonGrowthCalculator())
        ));
    }

    public static void reloadSeasonManager() {
        final String modId = DTConfigs.PREFERRED_SEASON_MOD.get();

        // If the selected mod is not loaded take that as disabling integration, set a null manager.
        if (!ModList.get().isLoaded(modId)) {
            SeasonHelper.setSeasonManager(new SeasonManager());
            return;
        }

        if (!SEASON_MANAGERS.containsKey(modId)) {
            LogManager.getLogger().warn("Season manager not found for preferred season mod \"{}\".", modId);
            return;
        }

        SeasonHelper.setSeasonManager(SEASON_MANAGERS.get(modId).get());
    }

}
