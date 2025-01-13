package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.loot.DTLoot;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class DynamicTrees {

    public static final String MOD_ID = "dynamictrees";
    public static final String MOD_NAME = "Dynamic Trees";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static final String MINECRAFT = "minecraft";
    public static final String SERENE_SEASONS = "sereneseasons";
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

    public static final ResourceLocation NULL = DynamicTrees.location("null");

    public static final ResourceLocation OAK = DynamicTrees.location("oak");
    public static final ResourceLocation BIRCH = DynamicTrees.location("birch");
    public static final ResourceLocation SPRUCE = DynamicTrees.location("spruce");
    public static final ResourceLocation JUNGLE = DynamicTrees.location("jungle");
    public static final ResourceLocation DARK_OAK = DynamicTrees.location("dark_oak");
    public static final ResourceLocation ACACIA = DynamicTrees.location("acacia");
    public static final ResourceLocation AZALEA = DynamicTrees.location("azalea");
    public static final ResourceLocation CRIMSON = DynamicTrees.location("crimson");
    public static final ResourceLocation WARPED = DynamicTrees.location("warped");


    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {
        DTLoot.load();

        RegistryHandler.setup(MOD_ID);

        SeasonCompatibilityHandler.registerBuiltInSeasonManagers();
    }

    public static ResourceLocation location (String name){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}