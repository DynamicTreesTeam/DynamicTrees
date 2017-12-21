package com.ferreusveritas.dynamictrees.genfeatures;

import java.util.List;

import com.ferreusveritas.dynamictrees.VanillaTreeData;
import com.ferreusveritas.dynamictrees.api.IGenFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.BlockState;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.PropertyInteger;
import com.ferreusveritas.dynamictrees.api.backport.Vec3d;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;

import net.minecraft.init.Blocks;

public class GenFeatureUndergrowth implements IGenFeature {

	public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 3, PropertyInteger.Bits.B00XX);
	public static final PropertyInteger NO_DECAY = PropertyInteger.create("nodecay", 0, 1, PropertyInteger.Bits.B0X00);
	public static final PropertyInteger CHECK_DECAY = PropertyInteger.create("checkdecay", 0, 1, PropertyInteger.Bits.BX000); 
	
	private Species species;
	private int radius = 2;
	
	public GenFeatureUndergrowth(Species species) {
		this.species = species;
	}
	
	public GenFeatureUndergrowth setRadius(int radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public void gen(World world, BlockPos treePos, List<BlockPos> endPoints) {

		Vec3d vTree = new Vec3d(treePos).addVector(0.5, 0.5, 0.5);

		for(int i = 0; i < 2; i++) {

			int rad = MathHelper.clamp(radius, 2, world.rand.nextInt(radius - 1) + 2);
			Vec3d v = vTree.add(new Vec3d(1, 0, 0).scale(rad).rotateYaw((float) (world.rand.nextFloat() * Math.PI * 2)));

			BlockPos pos = TreeHelper.findGround(world, new BlockPos(v));
			IBlockState soilBlockState = world.getBlockState(pos);

			if(species.isAcceptableSoil(world, pos, soilBlockState)) {
					int variant = world.rand.nextInt(2) == 0 ? VanillaTreeData.EnumType.OAK.getMetadata() : VanillaTreeData.EnumType.JUNGLE.getMetadata();
					world.setBlockState(pos, new BlockState(Blocks.log).withProperty(VARIANT, variant));
					pos = pos.up(world.rand.nextInt(3));
					
					IBlockState leavesState = new BlockState(Blocks.leaves).withProperty(VARIANT, variant).withProperty(CHECK_DECAY, 0);
					
					SimpleVoxmap leafMap = species.getTree().getLeafCluster();
					for(BlockPos dPos : leafMap.getAllNonZero()) {
						BlockPos leafPos = pos.add(dPos);
						if((coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getBlock().isReplaceable(world, leafPos.getX(), leafPos.getY(), leafPos.getZ())) {
							world.setBlockState(leafPos, leavesState);
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
