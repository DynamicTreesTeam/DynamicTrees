package com.dtteam.dynamictrees.data.components;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public record BranchDestructionDataComponent(
        Species species,
        int[] destroyedBranchesRadiusPosition,
        int[] destroyedBranchesConnections,
        int[] destroyedBranchesBlockIndex,
        int[] destroyedLeaves,
        int[] destroyedLeavesBlockIndex,
        int[] endPoints,
        int[] woodVolume,
        BlockPos cutPos,
        BlockPos basePos,
        Direction cutDir,
        Direction toolDir,
        int trunkHeight,
        @Nullable BlockState soilState
) {

    private static final Codec<int[]> INT_ARRAY_CODEC = Codec.INT.listOf().xmap(
            list -> list.stream().mapToInt(Integer::intValue).toArray(),
            arr -> Arrays.stream(arr).boxed().toList()
    );

    public static final Codec<BranchDestructionData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Species.CODEC.fieldOf("species")
                    .forGetter(d -> d.species),
            INT_ARRAY_CODEC.fieldOf("branchpos")
                    .forGetter(d -> d.destroyedBranchesRadiusPosition),
            INT_ARRAY_CODEC.fieldOf("branchcon")
                    .forGetter(d -> d.destroyedBranchesConnections),
            INT_ARRAY_CODEC.fieldOf("branchblock")
                    .forGetter(d -> d.destroyedBranchesBlockIndex),
            INT_ARRAY_CODEC.fieldOf("leavespos")
                    .forGetter(d -> d.destroyedLeaves),
            INT_ARRAY_CODEC.fieldOf("leavesblock")
                    .forGetter(d -> d.destroyedLeavesBlockIndex),
            INT_ARRAY_CODEC.fieldOf("ends")
                    .forGetter(d -> d.endPoints),
            INT_ARRAY_CODEC.fieldOf("volume")
                    .forGetter(d -> d.woodVolume.getRawVolumesArray()),
            BlockPos.CODEC.fieldOf("cutpos")
                    .forGetter(d -> d.cutPos),
            BlockPos.CODEC.fieldOf("basepos")
                    .forGetter(d -> d.basePos),
            Direction.CODEC.fieldOf("cutdir")
                    .forGetter(d -> d.cutDir),
            Direction.CODEC.fieldOf("tooldir")
                    .forGetter(d -> d.toolDir),
            Codec.INT.fieldOf("trunkheight")
                    .forGetter(d -> d.trunkHeight),
            BlockState.CODEC.optionalFieldOf("soil")
                    .forGetter(d -> Optional.ofNullable(d.soilState))
    ).apply(i, (species, branchpos, branchcon, branchblock, leavespos, leavesblock, ends, volume, cutpos, basepos, cutdir, tooldir, trunkheight, soil) ->
            new BranchDestructionData(species, branchpos, branchcon, branchblock, leavespos, leavesblock, ends, volume, cutpos, basepos, cutdir, tooldir, trunkheight, soil.orElse(null))
    ));

    public static final StreamCodec<ByteBuf, BranchDestructionData> STREAM_CODEC = new StreamCodec<>() {

        private static void writeIntArray(ByteBuf buf, int[] arr) {
            buf.writeInt(arr.length);
            for (int v : arr) buf.writeInt(v);
        }

        private static int[] readIntArray(ByteBuf buf) {
            int len = buf.readInt();
            int[] arr = new int[len];
            for (int i = 0; i < len; i++) arr[i] = buf.readInt();
            return arr;
        }

        public static final StreamCodec<ByteBuf, BlockState> BLOCK_STATE_STREAM_CODEC = ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY);

        @Override
        public BranchDestructionData decode(ByteBuf buf) {
            Species species = Species.STREAM_CODEC.decode(buf);
            int[] branchRadPos = readIntArray(buf);
            int[] branchCon = readIntArray(buf);
            int[] branchBlockIdx = readIntArray(buf);
            int[] leavesPos = readIntArray(buf);
            int[] leavesBlockIdx = readIntArray(buf);
            int[] ends = readIntArray(buf);
            int[] volume = readIntArray(buf);
            BlockPos cutPos  = BlockPos.STREAM_CODEC.decode(buf);
            BlockPos basePos = BlockPos.STREAM_CODEC.decode(buf);
            Direction cutDir  = Direction.from3DDataValue(buf.readByte());
            Direction toolDir = Direction.from3DDataValue(buf.readByte());
            int trunkHeight = buf.readInt();
            boolean hasSoil = buf.readBoolean();
            BlockState soilState = hasSoil ? BLOCK_STATE_STREAM_CODEC.decode(buf) : null;
            return new BranchDestructionData(
                    species, branchRadPos, branchCon, branchBlockIdx,
                    leavesPos, leavesBlockIdx, ends, volume,
                    cutPos, basePos, cutDir, toolDir, trunkHeight, soilState
            );
        }

        @Override
        public void encode(ByteBuf buf, BranchDestructionData c) {
            Species.STREAM_CODEC.encode(buf, c.species);
            writeIntArray(buf, c.destroyedBranchesRadiusPosition);
            writeIntArray(buf, c.destroyedBranchesConnections);
            writeIntArray(buf, c.destroyedBranchesBlockIndex);
            writeIntArray(buf, c.destroyedLeaves);
            writeIntArray(buf, c.destroyedLeavesBlockIndex);
            writeIntArray(buf, c.endPoints);
            writeIntArray(buf, c.woodVolume.getRawVolumesArray());
            BlockPos.STREAM_CODEC.encode(buf, c.cutPos);
            BlockPos.STREAM_CODEC.encode(buf, c.basePos);
            buf.writeByte(c.cutDir.get3DDataValue());
            buf.writeByte(c.toolDir.get3DDataValue());
            buf.writeInt(c.trunkHeight);
            buf.writeBoolean(c.soilState != null);
            if (c.soilState != null) {
                BLOCK_STATE_STREAM_CODEC.encode(buf, c.soilState);
            }
        }
    };
}