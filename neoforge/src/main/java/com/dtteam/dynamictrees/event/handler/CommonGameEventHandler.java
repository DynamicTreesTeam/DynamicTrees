package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.recipe.DendroPotionRecipeHandler;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public class CommonGameEventHandler {

    ///////////////////////////////////////////
    // LEVEL
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onPreLevelTick(LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide()) {
            FutureBreak.process(event.getLevel());
        }
        SeasonHelper.updateTick(event.getLevel(), event.getLevel().getOverworldClockTime());
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ClientModEventHandler.discoverWoodColors();
        } else {
            BiomeDatabases.populateBlacklistFromConfig();
        }
    }

    /**
     * We'll use this instead because at least new chunks aren't created after the world is unloaded. I hope. >:(
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        final LevelAccessor level = event.getLevel();
        if (!level.isClientSide()) {
            DynamicTreeFeature.DISC_PROVIDER.unloadWorld((ServerLevel) level);//clears the circles
        }
    }

    ///////////////////////////////////////////
    // SERVER
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onServerStart(final ServerStartingEvent event) {
        SeasonCompatibilityHandler.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        new DTCommand().registerDTCommand(event.getDispatcher());
    }

    ///////////////////////////////////////////
    // RESOURCES
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void addReloadListeners(final AddServerReloadListenersEvent event) {
        event.addListener(DynamicTrees.location("trees"), new Resources.ReloadListener(null));
    }

    ///////////////////////////////////////////
    // REGISTRY
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void registerBrewingRecipes(final RegisterBrewingRecipesEvent event) {
        DendroPotionRecipeHandler.getAllDendroRecipes().forEach(
                recipe -> event.getBuilder().addRecipe(recipe));
    }

}