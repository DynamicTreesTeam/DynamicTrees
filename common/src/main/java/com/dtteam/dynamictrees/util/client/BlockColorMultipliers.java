package com.dtteam.dynamictrees.util.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

//@OnlyIn(Dist.CLIENT)
public class BlockColorMultipliers {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Map<String, BlockColor> colorBase = new HashMap<>();

    public static void register(ResourceLocation label, BlockColor colorMultiplier) {
        register(label.toString(), colorMultiplier);
    }
    public static void register(String label, BlockColor colorMultiplier) {
        if (colorBase == null)
            LOGGER.error("Error registering Color Multiplier \""+label+"\". Called too late, block color multipliers have already been registered.");
        else
            colorBase.put(label, colorMultiplier);
    }

    @Nullable
    public static BlockColor find(ResourceLocation label) {
        return find(label.toString());
    }
    @Nullable
    public static BlockColor find(String label) {
        return colorBase.get(label);
    }

    public static void cleanUp() {
        colorBase = null;//Once all the color multipliers have been resolved we no longer need this data structure
    }

}
