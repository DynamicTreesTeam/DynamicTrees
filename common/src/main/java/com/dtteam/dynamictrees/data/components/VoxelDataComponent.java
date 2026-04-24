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

    public VoxelDataComponent(){
        this(new BranchDestructionData(), Vec3.ZERO, Vec3.ZERO, FallingTreeEntity.DestroyType.VOID, false, 0, false);
    }

    public static final Codec<VoxelDataComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
            BranchDestructionDataComponent.CODEC.fieldOf("destroy_data").forGetter(VoxelDataComponent::destroyData),
            Vec3.CODEC.fieldOf("geom_center").forGetter(VoxelDataComponent::geomCenter),
            Vec3.CODEC.fieldOf("mass_center").forGetter(VoxelDataComponent::massCenter),
            Codec.STRING.fieldOf("destroy_type").forGetter(c -> c.destroyType().toString()),
            Codec.BOOL.fieldOf("on_fire").forGetter(VoxelDataComponent::onFire),
            Codec.FLOAT.fieldOf("volume").forGetter(VoxelDataComponent::volume),
            Codec.BOOL.fieldOf("has_leaves").forGetter(VoxelDataComponent::hasLeaves)
    ).apply(i, (destroyData, geom, mass, destroyTypeName, onFire, volume, hasLeaves) ->
            new VoxelDataComponent(destroyData, geom, mass, FallingTreeEntity.DestroyType.valueOf(destroyTypeName), onFire, volume, hasLeaves)
    ));

    public static final StreamCodec<ByteBuf, VoxelDataComponent> STREAM_CODEC = new StreamCodec<>() {

        @Override
        public VoxelDataComponent decode(ByteBuf buf) {
            BranchDestructionData destroyData = BranchDestructionDataComponent.STREAM_CODEC.decode(buf);
            Vec3 geomCenter = Vec3.STREAM_CODEC.decode(buf);
            Vec3 massCenter = Vec3.STREAM_CODEC.decode(buf);
            FallingTreeEntity.DestroyType destroyType = FallingTreeEntity.DestroyType.values()[buf.readByte()];
            boolean onFire = buf.readBoolean();
            float volume = buf.readFloat();
            boolean hasLeaves = buf.readBoolean();
            return new VoxelDataComponent(destroyData, geomCenter, massCenter, destroyType, onFire, volume, hasLeaves);
        }

        @Override
        public void encode(ByteBuf buf, VoxelDataComponent c) {
            BranchDestructionDataComponent.STREAM_CODEC.encode(buf, c.destroyData());
            Vec3.STREAM_CODEC.encode(buf, c.geomCenter());
            Vec3.STREAM_CODEC.encode(buf, c.massCenter());
            buf.writeByte(c.destroyType().ordinal());
            buf.writeBoolean(c.onFire());
            buf.writeFloat(c.volume());
            buf.writeBoolean(c.hasLeaves());
        }
    };

}