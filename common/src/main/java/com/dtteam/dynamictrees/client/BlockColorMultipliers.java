package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

//
public class BlockColorMultipliers {

    private static Map<Identifier, BlockColor> colorBase = new HashMap<>();

    public static void register(String label, BlockColor colorMultiplier) {
        register(IdentifierUtils.parse(label, DynamicTrees.MOD_ID), colorMultiplier);
    }
    public static void register(Identifier label, BlockColor colorMultiplier) {
        if (colorBase == null)
            DynamicTrees.LOG.error("Error registering Color Multiplier \"{}\". Called too late, block color multipliers have already been registered.", label);
        else
            colorBase.put(label, colorMultiplier);
    }

    @Nullable
    public static BlockColor find(String label) {
        return find(Identifier.parse(label));
    }
    @Nullable
    public static BlockColor find(Identifier label) {
        return colorBase.get(label);
    }

    public static void cleanUp() {
        colorBase = null;//Once all the color multipliers have been resolved we no longer need this data structure
    }

}
