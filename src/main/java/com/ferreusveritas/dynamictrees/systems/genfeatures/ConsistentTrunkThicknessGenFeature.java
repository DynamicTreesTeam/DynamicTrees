package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class ConsistentTrunkThicknessGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature{

	public ConsistentTrunkThicknessGenFeature(ResourceLocation registryName) {
		super(registryName, MAX_HEIGHT);
	}

	@Override
	public ConfiguredGenFeature<?> createDefaultConfiguration() {
		return super.createDefaultConfiguration().with(MAX_HEIGHT, 32);
	}

	@Override
	public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		if(soilLife > 0) {
			this.setTrunkThickness(configuredGenFeature, world, rootPos, species);
			return true;
		}
		return false;
	}

	@Override
	public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
		this.setTrunkThickness(configuredGenFeature, world, rootPos, species);
		return true;
	}

	private int getTreeHeight (IWorld world, BlockPos rootPos, int maxHeight){
		for (int i = 1; i < maxHeight; i++) {
			if (!TreeHelper.isBranch(world.getBlockState(rootPos.up(i)))){
				return i - 1;
			}
		}
		return maxHeight;
	}

	/**
	 * Put a cute little flare on the bottom of the dark oaks
	 * 
	 * @param world The world
	 * @param rootPos The position of the rooty dirt block of the tree
	 */
	public void setTrunkThickness(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species) {
		int currentRadius = TreeHelper.getRadius(world, rootPos.up());
		if (currentRadius > 0){
			int currentTreeHeight = getTreeHeight(world, rootPos, configuredGenFeature.get(MAX_HEIGHT));
			int tallestTreeHeight = species.getLowestBranchHeight();
			if (currentTreeHeight <= tallestTreeHeight){
				int radiusToSet = (int)(species.getMaxBranchRadius() * 0.8 * (currentTreeHeight / (float)tallestTreeHeight));
				if (radiusToSet > currentRadius){
					for (int i = 1; i < currentTreeHeight; i++){
						BranchBlock branch =TreeHelper.getBranch(world.getBlockState(rootPos.up(i)));
						if (branch == null) return;
						branch.setRadius(world, rootPos.up(i), radiusToSet, Direction.DOWN);
					}
				}
			}
		}
	}
	
}
