package com.ferreusveritas.dynamictrees.trees.species;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.leaves.PalmLeavesProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PalmSpecies extends Species {
    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(PalmSpecies::new);

    public PalmSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        setGrowthLogicKit(GrowthLogicKits.PALM); //palm growth logic kit by default
    }

    @Override
    public boolean postGrow(Level world, BlockPos rootPos, BlockPos treePos, int fertility, boolean natural) {
        BlockState trunkBlockState = world.getBlockState(treePos);
        BranchBlock branch = TreeHelper.getBranch(trunkBlockState);
        if (branch == null) {
            return false;
        }
        FindEndsNode endFinder = new FindEndsNode();
        MapSignal signal = new MapSignal(endFinder);
        branch.analyse(trunkBlockState, world, treePos, Direction.DOWN, signal);
        List<BlockPos> endPoints = endFinder.getEnds();

        for (BlockPos endPoint : endPoints) {
            TreeHelper.ageVolume(world, endPoint, 2, 3, 3, SafeChunkBounds.ANY);
        }

        // Make sure the bottom block is always just a little thicker that the block above it.
        int radius = branch.getRadius(world.getBlockState(treePos.above()));
        if (radius != 0) {
            branch.setRadius(world, treePos, radius + 1, null);
        }

        return super.postGrow(world, rootPos, treePos, fertility, natural);
    }

    public boolean transitionToTree(Level world, BlockPos pos) {
        //Ensure planting conditions are right
        Family family = getFamily();
        if (world.isEmptyBlock(pos.above()) && isAcceptableSoil(world, pos.below(), world.getBlockState(pos.below()))) {
            family.getBranch().ifPresent(branch ->
                    // Set to a single branch with 1 radius.
                    branch.setRadius(world, pos, family.getPrimaryThickness(), null)
            );
            world.setBlockAndUpdate(pos.above(), getLeavesProperties().getDynamicLeavesState().setValue(DynamicLeavesBlock.DISTANCE, 4));//Place 2 leaf blocks on top
            world.setBlockAndUpdate(pos.above(2), getLeavesProperties().getDynamicLeavesState().setValue(DynamicLeavesBlock.DISTANCE, 3));
            placeRootyDirtBlock(world, pos.below(), 15);//Set to fully fertilized rooty dirt underneath
            return true;
        }
        return false;
    }

    @Override
    public void postGeneration(PostGenerationContext context) {
        final LevelAccessor world = context.world();

        if (!context.endPoints().isEmpty()) {
            BlockPos tip = context.endPoints().get(0).above(2);
            if (context.bounds().inBounds(tip, true)) {
                if (world.getBlockState(tip).getBlock() instanceof DynamicLeavesBlock) {
                    for (CoordUtils.Surround surr : CoordUtils.Surround.values()) {
                        BlockPos leafPos = tip.offset(surr.getOffset());
                        BlockState leafState = world.getBlockState(leafPos);
                        if (leafState.getBlock() instanceof DynamicLeavesBlock) {
                            DynamicLeavesBlock block = (DynamicLeavesBlock) leafState.getBlock();
                            world.setBlock(leafPos, block.getLeavesBlockStateForPlacement(world, leafPos, leafState, leafState.getValue(LeavesBlock.DISTANCE), true), 2);
                        }
                    }
                }
            }
        }
        super.postGeneration(context);
    }

    @Nullable
    @Override
    public HashMap<BlockPos, BlockState> getFellingLeavesClusters(BranchDestructionData destructionData) {

        int endPointsNum = destructionData.getNumEndpoints();

        if (endPointsNum < 1) {
            return null;
        }

        HashMap<BlockPos, BlockState> leaves = new HashMap<>();

        for (int i = 0; i < endPointsNum; i++) {
            BlockPos relPos = destructionData.getEndPointRelPos(i).above(2);//A palm tree is only supposed to have one endpoint at it's top.
            relPos = relPos.below();
            LeavesProperties leavesProperties = destructionData.species.getLeavesProperties();

            Set<BlockPos> existingLeaves = new HashSet<>();
            for (int j = 0; j < destructionData.getNumLeaves(); j++) {
                existingLeaves.add(destructionData.getLeavesRelPos(j));
            }

            if (existingLeaves.contains(relPos)) {
                    leaves.put(relPos, leavesProperties.getDynamicLeavesState(4));//The barky overlapping part of the palm frond cluster
                }
            if (existingLeaves.contains(relPos.above())) {
                leaves.put(relPos.above(), leavesProperties.getDynamicLeavesState(3));//The leafy top of the palm frond cluster
            }

            //The 4 corners and 4 sides of the palm frond cluster
            for (int hydro = 1; hydro <= 2; hydro++) {
                BlockState state = leavesProperties.getDynamicLeavesState(hydro);
                for (CoordUtils.Surround surr : PalmLeavesProperties.DynamicPalmLeavesBlock.hydroSurroundMap[hydro]) {
                    BlockPos leafPos = relPos.above().offset(surr.getOpposite().getOffset());
                    if (existingLeaves.contains(leafPos)) {
                        leaves.put(leafPos, PalmLeavesProperties.DynamicPalmLeavesBlock.getDirectionState(state, surr));
                    }
                }
            }
        }

        return leaves;
    }

}
