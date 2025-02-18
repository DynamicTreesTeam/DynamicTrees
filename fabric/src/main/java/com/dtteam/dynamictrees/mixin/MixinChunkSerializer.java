package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Inject(at = @At("HEAD"), method = "read (Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/ai/village/poi/PoiManager;Lnet/minecraft/world/level/chunk/storage/RegionStorageInfo;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/chunk/ProtoChunk;")
    private static void read(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionStorageInfo, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
        if (!Services.CONFIG.getBoolConfig(IConfigHelper.WORLD_GEN)) return;

        final byte[] circleData = tag.getByteArray(UniversalPoissonDiscProvider.CIRCLE_DATA_ID);
        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;

        discProvider.setChunkPoissonData(LevelContext.create(level), pos, circleData);
    }

    @Inject(at = @At("RETURN"), method = "write (Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/nbt/CompoundTag;")
    private static void write(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        if (!Services.CONFIG.getBoolConfig(IConfigHelper.WORLD_GEN)) return;

        final LevelContext levelContext = LevelContext.create(level);
        final UniversalPoissonDiscProvider discProvider = DynamicTreeFeature.DISC_PROVIDER;
        final ChunkPos chunkPos = chunk.getPos();

        final byte[] circleData = discProvider.getChunkPoissonData(levelContext, chunkPos);

        //Fetch the tag that was just returned and append our data to it
        cir.getReturnValue().putByteArray(UniversalPoissonDiscProvider.CIRCLE_DATA_ID, circleData); // Set circle data.

        if (chunk instanceof LevelChunk && !((LevelChunk) chunk).loaded) {
            discProvider.unloadChunkPoissonData(levelContext, chunkPos);
        }
    }

}
