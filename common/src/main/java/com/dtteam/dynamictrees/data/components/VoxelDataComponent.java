package com.dtteam.dynamictrees.data.components;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record VoxelDataComponent(
        BranchDestructionData destroyData,
        Vec3 geomCenter,
        Vec3 massCenter,
        FallingTreeEntity.DestroyType destroyType,
        boolean onFire,
        float volume,
        boolean hasLeaves
) {
    private static final Codec<Vec3> VEC3_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vec3::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vec3::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vec3::z)
    ).apply(i, Vec3::new));

    public static final Codec<VoxelDataComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
            BranchDestructionDataComponent.CODEC.fieldOf("destroy_data").forGetter(VoxelDataComponent::destroyData),
            VEC3_CODEC.fieldOf("geom_center").forGetter(VoxelDataComponent::geomCenter),
            VEC3_CODEC.fieldOf("mass_center").forGetter(VoxelDataComponent::massCenter),
            Codec.STRING.fieldOf("destroy_type").forGetter(c -> c.destroyType().toString()),
            Codec.BOOL.fieldOf("on_fire").forGetter(VoxelDataComponent::onFire),
            Codec.FLOAT.fieldOf("volume").forGetter(VoxelDataComponent::volume),
            Codec.BOOL.fieldOf("has_leaves").forGetter(VoxelDataComponent::hasLeaves)
    ).apply(i, (destroyData, geom, mass, destroyTypeName, onFire, volume, hasLeaves) ->
            new VoxelDataComponent(destroyData, geom, mass, FallingTreeEntity.DestroyType.valueOf(destroyTypeName), onFire, volume, hasLeaves)
    ));

    public static final StreamCodec<ByteBuf, VoxelDataComponent> STREAM_CODEC = null;
}