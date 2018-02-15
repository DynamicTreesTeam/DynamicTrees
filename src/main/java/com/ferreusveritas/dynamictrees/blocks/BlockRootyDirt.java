package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
		final int dMap[] = {0, -1, 1};//Y-Axis depth map
		
		IBlockState mimic = Blocks.DIRT.getDefaultState();//Default to dirt in case no dirt or grass is found
		IBlockState cache[] = new IBlockState[12];//A cache so we don't need to pull the blocks from the world twice
		int i = 0;
		
		//Prioritize Grass by searching for grass first
		for (int depth : dMap) {
			for (EnumFacing dir : EnumFacing.HORIZONTALS) {
				IBlockState ground = cache[i++] = access.getBlockState(pos.offset(dir).down(depth));
				if (ground.getMaterial() == Material.GRASS) {
					return ground; 
				}
			}
		}

		//Settle for other kinds of dirt
		for (i = 0; i < 12; i++) {
			IBlockState ground = cache[i];
			if(ground != mimic && ground.getMaterial() == Material.GROUND){
				return ground;
			}
		}
		
		//If all else fails then just return plain ol' dirt
		return mimic;
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
