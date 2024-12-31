package com.dtteam.dynamictrees.systems.season;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.season.SeasonManager;
import com.dtteam.dynamictrees.compat.SereneSeasonsSeasonProvider;
import com.dtteam.dynamictrees.platform.Services;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class SeasonCompatibilityHandler {

    private static final LinkedHashMap<String, Supplier<SeasonManager>> SEASON_MANAGERS = Maps.newLinkedHashMap();

    /**
     * Registers the specified {@link NormalSeasonManager} supplier for the specified {@code modId}. Given as a supplier for
     * lazy initialisation.
     *
     * <p>The season manager to use is then selected by {@link DTConfigs#PREFERRED_SEASON_MOD}
     * on config reload.</p>
     *
     * @param modId    The mod ID the season manager handles.
     * @param supplier The {@link NormalSeasonManager} supplier.
     */
    public static void registerSeasonManager(final String modId, Supplier<SeasonManager> supplier) {
        SEASON_MANAGERS.put(modId, supplier);
    }

    public static void registerBuiltInSeasonManagers() {
        if (Services.PLATFORM.isModLoaded(DynamicTrees.SERENE_SEASONS)){
            SereneSeasonsSeasonProvider.registerSereneSeasonsProvider();
        }
    }

    public static final String DISABLED = "!";
    public static final String ANY = "*";

    public static void reloadSeasonManager() {
        final String modId = Services.CONFIG.getStringConfig("preferredSeasonMod");

        // If disabled, use null manager.
        if (Objects.equals(modId, DISABLED)) {
            SeasonHelper.setSeasonManager(NormalSeasonManager.NULL.get());
            return;
        }

        // If any, select first manager registered.
        if (Objects.equals(modId, ANY)) {
            SeasonHelper.setSeasonManager(
                    SEASON_MANAGERS.entrySet().stream()
                            .filter(entry -> Services.PLATFORM.isModLoaded(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(NormalSeasonManager.NULL)
                            .get()
            );
            return;
        }

        if (!Services.PLATFORM.isModLoaded(modId)) {
            LogManager.getLogger().warn("Preferred season mod \"{}\" not installed.", modId);
            return;
        }

        if (!SEASON_MANAGERS.containsKey(modId)) {
            LogManager.getLogger().warn("Season manager not found for preferred season mod \"{}\".", modId);
            return;
        }

        SeasonHelper.setSeasonManager(SEASON_MANAGERS.get(modId).get());
    }

}
