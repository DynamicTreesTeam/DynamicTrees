package com.dtteam.dynamictrees.mixin;

import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.systems.poissondisc.UniversalPoissonDiscProvider;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public class MixinChunkSerializer {

    @Unique
    private static final ThreadLocal<ServerLevel> dynamictrees$writingLevel = new ThreadLocal<>();

    @Inject(at = @At("HEAD"), method = "parse")
    private static void dynamictrees$readPoisson(LevelHeightAccessor heightAccessor, PalettedContainerFactory factory, CompoundTag tag, CallbackInfoReturnable<SerializableChunkData> cir) {
        if (!DTConfigs.SERVER.worldGen.get()) return;
        if (!(heightAccessor instanceof ServerLevel level)) return;

        final byte[] circleData = tag.getByteArray(UniversalPoissonDiscProvider.CIRCLE_DATA_ID).orElseGet(() -> new byte[0]);
        final ChunkPos pos = new ChunkPos(tag.getIntOr("xPos", 0), tag.getIntOr("zPos", 0));
        DynamicTreeFeature.DISC_PROVIDER.setChunkPoissonData(LevelContext.create(level), pos, circleData);
    }

    @Inject(at = @At("HEAD"), method = "copyOf")
    private static void dynamictrees$captureWriteLevel(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<SerializableChunkData> cir) {
        dynamictrees$writingLevel.set(level);
    }

    @Inject(at = @At("RETURN"), method = "write")
    private void dynamictrees$writePoisson(CallbackInfoReturnable<CompoundTag> cir) {
        if (!DTConfigs.SERVER.worldGen.get()) return;
        ServerLevel level = dynamictrees$writingLevel.get();
        dynamictrees$writingLevel.remove();
        if (level == null) return;

        SerializableChunkData self = (SerializableChunkData) (Object) this;
        byte[] circleData = DynamicTreeFeature.DISC_PROVIDER.getChunkPoissonData(LevelContext.create(level), self.chunkPos());
        CompoundTag tag = cir.getReturnValue();
        if (tag != null) {
            tag.putByteArray(UniversalPoissonDiscProvider.CIRCLE_DATA_ID, circleData);
        }
    }
}
