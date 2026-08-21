package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BlockColorMultipliers {

    @FunctionalInterface
    public interface ColorMultiplier {
        int getColor(BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int tintIndex);
    }

    private static Map<Identifier, ColorMultiplier> colorBase = new HashMap<>();

    public static void register(String label, ColorMultiplier colorMultiplier) {
        register(ResourceLocationUtils.parse(label, DynamicTrees.MOD_ID), colorMultiplier);
    }

    public static void register(Identifier label, ColorMultiplier colorMultiplier) {
        if (colorBase == null)
            DynamicTrees.LOG.error("Error registering Color Multiplier \"{}\". Called too late, block color multipliers have already been registered.", label);
        else
            colorBase.put(label, colorMultiplier);
    }

    @Nullable
    public static ColorMultiplier find(String label) {
        return find(Identifier.parse(label));
    }

    @Nullable
    public static ColorMultiplier find(Identifier label) {
        return colorBase.get(label);
    }

    public static void cleanUp() {
        colorBase = null;//Once all the color multipliers have been resolved we no longer need this data structure
    }

    public static int primitiveLeavesColor(BlockState primitiveLeaves, @Nullable BlockGetter level, @Nullable BlockPos pos) {
        BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(primitiveLeaves, 0);
        if (source == null) {
            return 0x48B518;
        }
        if (level instanceof BlockAndTintGetter tintLevel && pos != null) {
            return source.colorInWorld(primitiveLeaves, tintLevel, pos);
        }
        return source.color(primitiveLeaves);
    }

}
