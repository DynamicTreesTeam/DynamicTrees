package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

//@OnlyIn(Dist.CLIENT)
public class BlockColorMultipliers {

    private static Map<ResourceLocation, BlockColor> colorBase = new HashMap<>();

    public static void register(String label, BlockColor colorMultiplier) {
        register(ResourceLocationUtils.parse(label, DynamicTrees.MOD_ID), colorMultiplier);
    }
    public static void register(ResourceLocation label, BlockColor colorMultiplier) {
        if (colorBase == null)
            DynamicTrees.LOG.error("Error registering Color Multiplier \"{}\". Called too late, block color multipliers have already been registered.", label);
        else
            colorBase.put(label, colorMultiplier);
    }

    @Nullable
    public static BlockColor find(String label) {
        return find(ResourceLocation.parse(label));
    }
    @Nullable
    public static BlockColor find(ResourceLocation label) {
        return colorBase.get(label);
    }

    public static void cleanUp() {
        colorBase = null;//Once all the color multipliers have been resolved we no longer need this data structure
    }

}
