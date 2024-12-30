package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.platform.Services;
import com.mojang.datafixers.kinds.Const;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class DynamicTreesCommon {

    public static final String MOD_ID = "dynamictrees";
    public static final String MOD_NAME = "Dynamic Trees";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static final String MINECRAFT = "minecraft";
    public static final String SERENE_SEASONS = "sereneseasons";

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

    public static final ResourceLocation NULL = DynamicTreesCommon.location("null");

    public static final ResourceLocation OAK = DynamicTreesCommon.location("oak");
    public static final ResourceLocation BIRCH = DynamicTreesCommon.location("birch");
    public static final ResourceLocation SPRUCE = DynamicTreesCommon.location("spruce");
    public static final ResourceLocation JUNGLE = DynamicTreesCommon.location("jungle");
    public static final ResourceLocation DARK_OAK = DynamicTreesCommon.location("dark_oak");
    public static final ResourceLocation ACACIA = DynamicTreesCommon.location("acacia");
    public static final ResourceLocation AZALEA = DynamicTreesCommon.location("azalea");
    public static final ResourceLocation CRIMSON = DynamicTreesCommon.location("crimson");
    public static final ResourceLocation WARPED = DynamicTreesCommon.location("warped");


    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {

        RegistryHandler.setup(MOD_ID);

    }

    public static ResourceLocation location (String name){
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}