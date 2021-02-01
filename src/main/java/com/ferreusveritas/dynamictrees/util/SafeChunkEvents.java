package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * Something like this may become useful for solving some of the world gen crashes, but it's not currently in use.
 *
 * @author Harley O'Connor
 */
public final class SafeChunkEvents {

    private static final Map<ResourceLocation, List<ChunkPos>> loadedChunks = new HashMap<>();

    @SubscribeEvent
    public void onWorldUnload (WorldEvent.Unload event) {
        if (!(event.getWorld() instanceof World))
            return;

        loadedChunks.remove(((World) event.getWorld()).getDimensionKey().getLocation());
    }

    @SubscribeEvent
    public void onChunkLoad (ChunkEvent.Load event) {
        if (!(event.getWorld() instanceof World))
            return;

        loadedChunks.computeIfAbsent(((World) event.getWorld()).getDimensionKey().getLocation(), k ->
                new ArrayList<>(Collections.singletonList(event.getChunk().getPos())));
    }

    @SubscribeEvent
    public void onChunkUnload (ChunkEvent.Unload event) {
        if (!(event.getWorld() instanceof World))
            return;

        loadedChunks.remove(((World) event.getWorld()).getDimensionKey().getLocation()).remove(event.getChunk().getPos());
    }

    public static boolean isChunkLoaded (World world, ChunkPos chunkPos) {
        return loadedChunks.containsKey(world.getDimensionKey().getLocation()) && loadedChunks.get(world.getDimensionKey().getLocation()).contains(chunkPos);
    }

}
