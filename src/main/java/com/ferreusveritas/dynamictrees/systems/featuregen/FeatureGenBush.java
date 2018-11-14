package com.ferreusveritas.dynamictrees.systems.featuregen;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class FeatureGenBush implements IFullGenFeature, IPostGenFeature {
	
	private Species species;
	private IBlockState logState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
	private IBlockState leavesState = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, false);
	private IBlockState secondaryLeavesState = null;
	private Predicate<Biome> biomePredicate = i -> true;
	
	public FeatureGenBush(Species species) {
		this.species = species;
	}
	
	public FeatureGenBush setLogState(IBlockState logState) {
		this.logState = logState;
		return this;
	}
	
	public FeatureGenBush setLeavesState(IBlockState leavesState) {
		this.leavesState = leavesState;
		return this;
	}
	
	public FeatureGenBush setSecondaryLeavesState(IBlockState secondaryLeavesState) {
		this.secondaryLeavesState = secondaryLeavesState;
		return this;
	}
	
	public FeatureGenBush setBiomePredicate(Predicate<Biome> biomePredicate) {
		this.biomePredicate = biomePredicate;
		return this;
	}
	
	@Override
	public boolean generate(World world, BlockPos rootPos, Biome biome, Random random, int radius, SafeChunkBounds safeBounds) {
		commonGen(world, rootPos, random, radius, safeBounds);
		return true;
	}
	
	@Override
	public boolean postGeneration(World world, BlockPos rootPos, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, IBlockState initialDirtState) {
		if(safeBounds != SafeChunkBounds.ANY && biomePredicate.test(biome)) {
			commonGen(world, rootPos, world.rand, radius, safeBounds);
			return true;
		}
		return false;
	}
	
	protected void commonGen(World world, BlockPos rootPos, Random random, int radius, SafeChunkBounds safeBounds) {
		if (radius <= 2) return;
		
		Vec3d vTree = new Vec3d(rootPos).addVector(0.5, 0.5, 0.5);
		
		for (int i = 0; i < 2; i++) {
			int rad = MathHelper.clamp(world.rand.nextInt(radius - 2) + 2, 2, radius - 1);
			Vec3d v = vTree.add(new Vec3d(1, 0, 0).scale(rad).rotateYaw((float) (random.nextFloat() * Math.PI * 2)));
			BlockPos vPos = new BlockPos(v);
			if (!safeBounds.inBounds(vPos, true)) continue;
			
			BlockPos pos = CoordUtils.findGround(world, vPos);
			IBlockState soilBlockState = world.getBlockState(pos);
			
			pos = pos.up();
			if (!world.getBlockState(pos).getMaterial().isLiquid() && species.isAcceptableSoil(world, pos, soilBlockState)) {
				world.setBlockState(pos, logState);
				
				SimpleVoxmap leafMap = LeafClusters.bush;
				MutableBlockPos leafPos = new MutableBlockPos();
				for (MutableBlockPos dPos : leafMap.getAllNonZero()) {
					leafPos.setPos( pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ() );
					if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos)) {
						world.setBlockState(leafPos, (secondaryLeavesState == null || random.nextInt(4) != 0) ? leavesState : secondaryLeavesState);
					}
				}
			}
		}
	}
	
	public static int coordHashCode(BlockPos pos) {
		int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
		return hash & 0xFFFF;
	}
	
}
