package com.dtteam.dynamictrees.worldgen;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.network.NodeInspector;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.systems.nodemapper.CoderNode;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.SimpleVoxmap;
import com.dtteam.dynamictrees.utility.helper.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class RootsJoCode extends JoCode {

    public RootsJoCode(String code) {
        super(code);
    }

    public RootsJoCode(Level level, BlockPos rootPos, Direction facing) {
        super(level, rootPos, facing);
    }

    @Override
    protected void getCodeFromWorld(Level level, BlockPos rootPos, Direction facing) {
        Optional<BranchBlock> branch = TreeHelper.getBranchOpt(level.getBlockState(rootPos.below()));

        if (branch.isPresent()) {
            CoderNode coder = new CoderNode();
            //Warning!  This sends a RootyBlock BlockState into a branch for the kickstart of the analysis.
            branch.get().analyse(level.getBlockState(rootPos), level, rootPos, Direction.UP, new MapSignal(coder));
            instructions = coder.compile(this);
            rotate(facing);
        }
    }

    @Override
    public void generate(DynamicTreeGenerationContext context) {
        LevelAccessor level = context.level();
        Species species = context.species();
        int radius = context.radius();
        boolean worldGen = context.isWorldGen();

        this.setFacing(context.facing());

        BlockPos rootPos = context.rootPos();

        if (rootPos == BlockPos.ZERO) {
            return;
        }

        int rootRadius;
        BlockState rootState = level.getBlockState(context.rootPos());
        if (rootState.getBlock() instanceof SoilBlock rooty){
            rootRadius = rooty.updateRadius(level, rootState, context.rootPos(), 2, false);
        } else rootRadius = 8;

        // Make the root branch structure.
        this.generateFork(level, species, 0, rootPos, false);

        // Establish a position for the root crown.
        final BlockPos rootsPos = rootPos.below();

        // Fix branch thicknesses and map out leaf locations.
        final BlockState rootsState = level.getBlockState(rootsPos);
        final BranchBlock firstBranch = TreeHelper.getBranch(rootsState);

        // If a branch doesn't exist the growth failed... don't do anything else
        if (firstBranch == null) {
            return;
        }

        // If a branch exists then the growth was successful.

        final SimpleVoxmap rootsMap = new SimpleVoxmap(radius * 2 + 1, species.getWorldGenLeafMapHeight(), radius * 2 + 1).setMapAndCenter(rootsPos, new BlockPos(radius, species.getWorldGenLeafMapHeight(), radius));
        final NodeInspector inflator = species.getNodeInflator(rootsMap, rootRadius); // This is responsible for thickening the branches.
        final FindEndsNode endFinder = new FindEndsNode(); // This is responsible for gathering a list of branch end points.
        final MapSignal signal = new MapSignal(inflator, endFinder); // The inflator signal will "paint" a temporary voxmap of all of the leaves and branches.
        signal.destroyLoopedNodes = this.careful;

        firstBranch.analyse(rootsState, level, rootsPos, Direction.UP, signal);

        if (signal.foundRoot || signal.overflow) { // Something went terribly wrong.
            this.tryGenerateAgain(context, worldGen, rootsPos, rootsState, endFinder);
            return;
        }

        final List<BlockPos> endPoints = endFinder.getEnds();

        // Rot the unsupported branches.
        species.handleRot(level, endPoints, rootPos, rootsPos, 0, context.isWorldGen());
    }

    @Override
    protected boolean setBlockForGeneration(LevelAccessor level, Species species, BlockPos pos, Direction dir, boolean careful, @SuppressWarnings("unused") boolean isLast) {
        if (isFreeToSetBlock(level, pos, species) && (!careful || this.isClearOfNearbyBranches(level, pos, dir.getOpposite()))) {
            species.getFamily().getBranchForRootsPlacement(level, species, pos).ifPresent(branch ->
                    branch.setRadius(level, pos, species.getFamily().getPrimaryRootThickness(), null, careful ? 3 : 2)
            );
            return false;
        }
        return true;
    }

    protected boolean isFreeToSetBlock(LevelAccessor level, BlockPos pos, Species species) {
        return species.getFamily().isAcceptableSoilForRootSystem(level.getBlockState(pos)) || super.isFreeToSetBlock(level, pos, species);
    }

}
