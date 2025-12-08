package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

/**
 * Since root colors are affected by climate, we cache them to avoid calculating them every frame
 */
public class RootColorCache {
    // chunkKey -> (posKey -> color)
    private static final Long2ObjectOpenHashMap<Long2IntOpenHashMap> CACHE = new Long2ObjectOpenHashMap<>();

    final static int white = 0xFFFFFFFF;

    public static int getOrComputeColor(TriFunction<BlockState, Level, BlockPos, Species> speciesGetter, BlockState state, @Nullable BlockPos pos, int baseColor){
        if (pos == null) return baseColor;
        ChunkPos chunk = new ChunkPos(pos);
        long chunkKey = chunk.toLong();
        long posKey = pos.asLong();

        Long2IntOpenHashMap chunkMap = CACHE.get(chunkKey);
        if (chunkMap == null) {
            chunkMap = new Long2IntOpenHashMap();
            chunkMap.defaultReturnValue(Integer.MIN_VALUE);
            CACHE.put(chunkKey, chunkMap);
        }
        int cached = chunkMap.get(posKey);
        if (cached != Integer.MIN_VALUE) {
            return cached;
        }
        int result = computeColor(speciesGetter, state, pos, baseColor);
        chunkMap.put(posKey, result);
        return result;
    }

    public static int computeColor(TriFunction<BlockState, Level, BlockPos, Species> speciesGetter, BlockState state, BlockPos pos, int baseColor){
        if (Minecraft.getInstance().level instanceof Level level){
            double multiplier = speciesGetter.apply(state, level, pos).getClimateSuitabilityMultiplier(level, pos);
            return lerpColor(white, baseColor, (float)multiplier);
        }
        return baseColor;
    }

    public static int lerpColor(int startHex, int endHex, float t) {
        t = Math.max(0f, Math.min(1f, t));

        int r1 = (startHex >> 16) & 0xFF;
        int g1 = (startHex >> 8) & 0xFF;
        int b1 = startHex & 0xFF;

        int r2 = (endHex >> 16) & 0xFF;
        int g2 = (endHex >> 8) & 0xFF;
        int b2 = endHex & 0xFF;

        int r = (int)(r1 + (r2 - r1) * t);
        int g = (int)(g1 + (g2 - g1) * t);
        int b = (int)(b1 + (b2 - b1) * t);

        return (r << 16) | (g << 8) | b;
    }

    public static void invalidate(BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        Long2IntOpenHashMap chunkMap = CACHE.get(chunk.toLong());
        if (chunkMap != null) chunkMap.remove(pos.asLong());
    }

    public static void invalidateChunk(ChunkPos chunkPos) {
        CACHE.remove(chunkPos.toLong());
    }

    public static void clear() {
        CACHE.clear();
    }
}
