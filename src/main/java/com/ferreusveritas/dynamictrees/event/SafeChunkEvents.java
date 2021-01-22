package com.ferreusveritas.dynamictrees.event;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

/**
 * Something like this may become useful for solving some of the world gen crashes, but it's not currently in use.
 *
 * @author Harley O'Connor
 */
public final class SafeChunkEvents {

    private static final Map<IWorld, List<ChunkPos>> loadedChunks = new HashMap<>();

    @SubscribeEvent
    public void onChunkLoad (ChunkEvent.Load event) {
        final IWorld world = event.getWorld();
        final ChunkPos chunkPos = event.getChunk().getPos();

        if (!loadedChunks.containsKey(world)) {
            loadedChunks.put(world, new ArrayList<>(Arrays.asList(chunkPos)));
        } else loadedChunks.get(world).add(chunkPos);
    }

    @SubscribeEvent
    public void onChunkUnload (ChunkEvent.Unload event) {
        final IWorld world = event.getWorld();
        final ChunkPos chunkPos = event.getChunk().getPos();

        if (!loadedChunks.containsKey(world)) return;

        loadedChunks.get(world).remove(chunkPos);
    }

    public static boolean isChunkLoaded (IWorld world, ChunkPos chunkPos) {
        return loadedChunks.containsKey(world) && loadedChunks.get(world).contains(chunkPos);
    }

}
