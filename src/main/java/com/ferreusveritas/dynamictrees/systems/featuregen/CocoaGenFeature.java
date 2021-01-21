package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class CocoaGenFeature implements IPostGenFeature, IPostGrowFeature {

	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
		if(soilLife == 0 && world.rand.nextInt() % 16 == 0) {
			addCocoa(world, rootPos, false);
		}
		return false;
	}

	@Override
	public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
		if(world.getRandom().nextInt() % 8 == 0) {
			addCocoa(world, rootPos, true);
			return true;
		}
		return false;
	}

	private void addCocoa(IWorld world, BlockPos rootPos, boolean worldGen) {
//		TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new NodeFruitCocoa().setWorldGen(worldGen)));
	}

}
