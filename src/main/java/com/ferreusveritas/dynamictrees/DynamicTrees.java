package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.api.GatherDataHelper;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.command.DTArgumentTypes;
import com.ferreusveritas.dynamictrees.compat.CompatHandler;
import com.ferreusveritas.dynamictrees.event.handlers.EventHandlers;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.resources.Resources;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommonSetup;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicTrees.MOD_ID)
public final class DynamicTrees {

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

        TreeGenerator.setup();

        RegistryHandler.setup(MOD_ID);

        DTRegistries.setup();

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(CommonSetup::onCommonSetup);

        EventHandlers.registerCommon();
        CompatHandler.registerBuiltInSeasonManagers();
        DTArgumentTypes.ARGUMENT_TYPES.register(modEventBus);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        DTClient.setup();
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        // Clears and locks registry handlers to free them from memory.
        RegistryHandler.REGISTRY.clear();

        DTRegistries.DENDRO_POTION.get().registerRecipes();

        Resources.MANAGER.setup();

        if (DTConfigs.REPLACE_NYLIUM_FUNGI.get()) {
            DTTrees.replaceNyliumFungiFeatures();
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

    public static ResourceLocation resLoc(final String path) {
        return new ResourceLocation(MOD_ID, path);
    }

}
