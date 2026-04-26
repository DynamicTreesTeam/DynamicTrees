package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BlockColorMultipliers {

    public static final BlockTintSource BLANK_LAYER = BlockTintSources.constant(-1);

    private static Map<Identifier, BlockTintSource> colorBase = new HashMap<>();
    static {
        colorBase.put(DynamicTrees.NULL, BLANK_LAYER);
    }

    public static void register(String label, BlockTintSource colorMultiplier) {
        register(IdentifierUtils.parse(label, DynamicTrees.MOD_ID), colorMultiplier);
    }
    public static void register(Identifier label, BlockTintSource colorMultiplier) {
        if (colorBase == null)
            DynamicTrees.LOG.error("Error registering Color Multiplier \"{}\". Called too late, block color multipliers have already been registered.", label);
        else
            colorBase.put(label, colorMultiplier);
    }

    @Nullable
    public static BlockTintSource find(String label) {
        return find(Identifier.parse(label));
    }
    @Nullable
    public static BlockTintSource find(Identifier label) {
        if (colorBase.containsKey(label))
            return colorBase.get(label);
        return BLANK_LAYER;
    }

    public static void cleanUp() {
        colorBase = null;//Once all the color multipliers have been resolved we no longer need this data structure
    }

    public static List<BlockTintSource> gatherBlockSources (List<Identifier> sources){
        List<BlockTintSource> list = new LinkedList<>();
        for (Identifier id : sources){
            if (id == null) list.add(null);
            else list.add(find(id));
        }
        return list;
    }

}
