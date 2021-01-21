package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.VineBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class VineGenFeature implements IPostGenFeature {

	protected final BooleanProperty vineMap[] = new BooleanProperty[] {null, null, VineBlock.NORTH, VineBlock.SOUTH, VineBlock.WEST, VineBlock.EAST};
	protected int qty = 4;
	protected int maxLength = 8;
	protected float verSpread = 60;
	protected float rayDistance = 5;
	protected Block vineBlock = Blocks.VINE;

	public VineGenFeature setQuantity(int qty) {
		this.qty = qty;
		return this;
	}

	public VineGenFeature setMaxLength(int length) {
		this.maxLength = length;
		return this;
	}

	public VineGenFeature setVerSpread(float verSpread) {
		this.verSpread = verSpread;
		return this;
	}

	public VineGenFeature setRayDistance(float rayDistance) {
		this.rayDistance = rayDistance;
		return this;
	}

	public VineGenFeature setVineBlock(Block vineBlock) {
		this.vineBlock = vineBlock;
		return this;
	}

	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
		if(safeBounds != SafeChunkBounds.ANY) {//worldgen
			if(!endPoints.isEmpty()) {
				for(int i = 0; i < qty; i++) {
					BlockPos endPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
					addVine(world, species, rootPos, endPoint, safeBounds);
				}
				return true;
			}
		}

		return false;
	}

	protected void addVine(IWorld world, Species species, BlockPos rootPos, BlockPos branchPos, SafeChunkBounds safeBounds) {

	    BlockRayTraceResult result;
        {
            RayTraceResult ray = CoordUtils.branchRayTrace(world, species, rootPos, branchPos, 90, verSpread, rayDistance, safeBounds);
            if (ray instanceof BlockRayTraceResult) {
                result = (BlockRayTraceResult)ray;
            } else {
                return;
            }
        }

        BlockPos vinePos = result.getPos().offset(result.getFace());
        if(vinePos != BlockPos.ZERO && safeBounds.inBounds(vinePos, true)) {
            BooleanProperty vineSide = vineMap[result.getFace().getOpposite().getIndex()];
            if(vineSide != null) {
                BlockState vineState = vineBlock.getDefaultState().with(vineSide, true);
                int len = MathHelper.clamp(world.getRandom().nextInt(maxLength) + 3, 3, maxLength);
                BlockPos.Mutable mPos = new BlockPos.Mutable(vinePos.getX(), vinePos.getY(), vinePos.getZ());
                for(int i = 0; i < len; i++) {
                    if(world.isAirBlock(mPos)) {
                        world.setBlockState(mPos, vineState, 3);
                        mPos.setY(mPos.getY() - 1);
                    } else {
                        break;
                    }
                }
            }
        }
    }

}
