package com.dtteam.dynamictrees.data.components;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.tree.species.Species;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
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
}