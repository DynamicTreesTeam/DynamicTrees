package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.systems.GrowSignal;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockBranchCactus extends BlockBranch {

	public BlockBranchCactus(String name) {
		super(Material.CACTUS, name);
		setSoundType(SoundType.CLOTH);
		setHarvestLevel("axe", 0);
		setDefaultState(this.blockState.getBaseState().withProperty(RADIUS, 1));
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        entityIn.attackEntityFrom(DamageSource.cactus, 1.0F);
    }
    
    @Override
    public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal, int fromRadius) {
    	int radius = (int) signal.getSpecies().getPrimaryThickness();
    	setRadius(world, pos, radius);
		signal.radius = radius;//For the benefit of the parent branch
    	return signal;
    }
}
