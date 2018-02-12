package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRootyDirt extends BlockRooty {
	
	static String name = "rootydirt";
		
	public BlockRootyDirt(boolean isTileEntity) {
		this(name + (isTileEntity ? "species" : ""), isTileEntity);
	}
	
	public BlockRootyDirt(String name, boolean isTileEntity) {
		super(name, Material.GROUND, isTileEntity);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState extState = (IExtendedBlockState) state;
			
			final int dMap[] = {0, -1, 1};
			
			IBlockState mimic = Blocks.DIRT.getDefaultState(); // Default to dirt in case no dirt or grass is found
			
			for (int depth : dMap) {
				for (EnumFacing dir : EnumFacing.HORIZONTALS) {
					IBlockState ground = world.getBlockState(pos.offset(dir).down(depth));
					
					if (ground.getBlock() instanceof BlockGrass) {
						return extState.withProperty(MIMIC, ground); // Prioritize Grass by returning as soon as grass is found
					}
					if (ground.getBlock() instanceof BlockDirt) {
						mimic = ground; // Store the dirt in case grass isn't found
					}
				}
			}
			return extState.withProperty(MIMIC, mimic);
		}
		return state;
	}
	
    
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	/**
	 * We have to reinvent this wheel because Minecraft colors the particles with tintindex 0.. which is used for the grass texture.
	 * So dirt bits end up green if we don't.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		
		BlockPos pos = target.getBlockPos();
		Random rand = world.rand;
		
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
		double d0 = x + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2D) + 0.1D + axisalignedbb.minX;
		double d1 = y + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2D) + 0.1D + axisalignedbb.minY;
		double d2 = z + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2D) + 0.1D + axisalignedbb.minZ;
		
		switch(target.sideHit) {
			case DOWN:  d1 = y + axisalignedbb.minY - 0.1D; break;
			case UP:    d1 = y + axisalignedbb.maxY + 0.1D; break;
			case NORTH: d2 = z + axisalignedbb.minZ - 0.1D; break;
			case SOUTH: d2 = z + axisalignedbb.maxZ + 0.1D; break;
			case WEST:  d0 = x + axisalignedbb.minX - 0.1D; break;
			case EAST:  d0 = x + axisalignedbb.maxX + 0.1D; break;
		}
		
		//Safe to spawn particles here since this is a client side only member function
		ParticleDigging particle = (ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), d0, d1, d2, 0, 0, 0, new int[]{Block.getStateId(state)});
		particle.setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F).setRBGColorF(0.6f, 0.6f, 0.6f);
		
		return true;
	}
	
}
