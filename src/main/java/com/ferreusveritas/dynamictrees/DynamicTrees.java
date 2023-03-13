package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.GatherDataHelper;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.command.DTArgumentTypes;
import com.ferreusveritas.dynamictrees.compat.CompatHandler;
import com.ferreusveritas.dynamictrees.event.handler.EventHandlers;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.loot.DTLoot;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CommonSetup;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import com.ferreusveritas.dynamictrees.worldgen.structure.VillageTreeReplacement;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod(DynamicTrees.MOD_ID)
public final class DynamicTrees {

    static {
        DTArgumentTypes.register();
    }

    public static final String MOD_ID = "dynamictrees";
    public static final String NAME = "Dynamic Trees";

    public static final String MINECRAFT = "minecraft";
    public static final String FORGE = "forge";
    public static final String SERENE_SEASONS = "sereneseasons";
    public static final String BETTER_WEATHER = "betterweather";
    public static final String FAST_LEAF_DECAY = "fastleafdecay";
    public static final String PASSABLE_FOLIAGE = "passablefoliage";

    public enum AxeDamage {
        VANILLA,
        THICKNESS,
        VOLUME
    }

    public enum DestroyMode {
        IGNORE,
        SLOPPY,
        SET_RADIUS,
        HARVEST,
        ROT,
        OVERFLOW
    }

    public enum SwampOakWaterState {
        ROOTED,
        SUNK,
        DISABLED
    }

    public DynamicTrees() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        final ModLoadingContext loadingContext = ModLoadingContext.get();

        loadingContext.registerConfig(ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
        loadingContext.registerConfig(ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);
        loadingContext.registerConfig(ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);

//        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DTClient::clientStart);

        RegistryHandler.setup(MOD_ID);

        DTRegistries.setup();

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::gatherData);

        modEventBus.addListener(CommonSetup::onCommonSetup);

        EventHandlers.registerCommon();
        CompatHandler.registerBuiltInSeasonManagers();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTClient.setup();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        DTLoot.load();
        DynamicTreeFeature.setup();

        // Clears and locks registry handlers to free them from memory.
        RegistryHandler.REGISTRY.clear();

        DTRegistries.DENDRO_POTION.get().registerRecipes();

        Resources.MANAGER.setup();

        if (DTConfigs.REPLACE_AZALEA_TREES.get()) {
            DTTrees.replaceAzaleaTrees();
        }
        if (DTConfigs.REPLACE_NYLIUM_FUNGI.get()) {
            DTTrees.replaceNyliumFungiFeatures();
        }
        if (DTConfigs.CANCEL_VANILLA_VILLAGE_TREES.get()) {
            VillageTreeReplacement.replaceTreesFromVanillaVillages();
        }
    }

    private void gatherData(final GatherDataEvent event) {
        Resources.MANAGER.gatherData();
        GatherDataHelper.gatherAllData(
                MOD_ID,
                event,
                SoilProperties.REGISTRY,
                Family.REGISTRY,
                Species.REGISTRY,
                LeavesProperties.REGISTRY
        );
    }

    public static ResourceLocation location(final String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
