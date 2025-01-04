package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import com.dtteam.dynamictrees.tree.species.PalmSpecies;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.api.cell.Cell;
import com.dtteam.dynamictrees.api.cell.CellNull;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.systems.GrowSignal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;

public class PalmFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(PalmFamily::new);

    public PalmFamily(ResourceLocation name) {
        super(name);
    }

    @Override
    public void setCommonSpecies(Species species) {
        super.setCommonSpecies(species);
        if (!(species instanceof PalmSpecies)) {
            LogManager.getLogger().warn("Common species {} for palm family {} is not of type {}", species.getRegistryName(), getRegistryName(), PalmSpecies.class);
        }
    }

    @Override
    public void setCommonLeaves(LeavesProperties properties) {
        super.setCommonLeaves(properties);
        if (!(properties instanceof PalmLeavesProperties)) {
            LogManager.getLogger().warn("Common leaves properties " + properties.getRegistryName() + " for palm family " + getRegistryName() + "is not of type "+ PalmLeavesProperties.class);
        }
    }

    @Override
    protected BranchBlock createBranchBlock(ResourceLocation name) {
        final BasicBranchBlock branch = new BasicBranchBlock(name, this.getProperties()){
            @Override
            public Cell getHydrationCell(BlockGetter level, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
                if (getRadius(state) != getFamily().getPrimaryThickness()) return CellNull.NULL_CELL;
                return super.getHydrationCell(level, pos, state, dir, leavesProperties);
            }

            @Override
            public GrowSignal growIntoAir(Level world, BlockPos pos, GrowSignal signal, int fromRadius) {
                final Species species = signal.getSpecies();

                final DynamicLeavesBlock leaves = species.getLeavesBlock().orElse(null);
                if (leaves != null) {
                    if (fromRadius == getFamily().getPrimaryThickness()) {// If we came from a twig (and we're not a stripped branch) then just make some leaves
                        if (isNextToBranch(world, pos, signal.dir.getOpposite())){
                            signal.success = false;
                            return signal;
                        }
                        signal.success = 0 != leaves.growLeavesIfLocationIsSuitable(world, species.getLeavesProperties(), pos.above(), 0);
                        if (signal.success)
                            return leaves.branchOut(world, pos, signal);
                    } else {// Otherwise make a proper branch
                        return leaves.branchOut(world, pos, signal);
                    }
                }
                return super.growIntoAir(world, pos, signal, fromRadius);
            }
        };
        if (this.isFireProof())
            branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
    }
}