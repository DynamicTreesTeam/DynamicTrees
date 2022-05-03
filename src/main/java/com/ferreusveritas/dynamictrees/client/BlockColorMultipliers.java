package com.ferreusveritas.dynamictrees.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BlockColorMultipliers {

    private static Map<String, BlockColor> colorBase = new HashMap<>();

    public static void register(String label, BlockColor colorMultiplier) {
        colorBase.put(label, colorMultiplier);
    }

    public static void register(ResourceLocation label, BlockColor colorMultiplier) {
        colorBase.put(label.toString(), colorMultiplier);
    }

    public static BlockColor find(String label) {
        return colorBase.get(label);
    }

    public static BlockColor find(ResourceLocation label) {
        return colorBase.get(label.toString());
    }

    public static void cleanUp() {
        colorBase = null;//Once all of the color multipliers have been resolved we no longer need this data structure
    }

}
