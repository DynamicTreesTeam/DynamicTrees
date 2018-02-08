package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;

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
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRootyDirt extends BlockRooty {

	public static final PropertyEnum MIMIC = PropertyEnum.create("mimic", EnumMimicDirtType.class);
	
	static String name = "rootydirt";
	
	public BlockRootyDirt() {
		this(name);
	}
	
	public BlockRootyDirt(String name) {
		super(name, Material.GROUND);
		setDefaultState(this.blockState.getBaseState().withProperty(LIFE, 15).withProperty(MIMIC, EnumMimicDirtType.DIRT));
		
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{LIFE, MIMIC});
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(MIMIC, getMimicType(worldIn, pos));
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	public EnumMimicDirtType getMimicType(IBlockAccess blockAccess, BlockPos pos) {
		final int dMap[] = {0, -1, 1};
		
		for(int depth: dMap) {
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				IBlockState mimic = blockAccess.getBlockState(pos.offset(dir).down(depth));
				
				for(EnumMimicDirtType muse: EnumMimicDirtType.values()) {
					if(muse != EnumMimicDirtType.DIRT) {
						if(mimic == muse.getBlockState()) {
							return muse;
						}
					}
				}
			}
		}
		
		return EnumMimicDirtType.DIRT;//Default to plain old dirt
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
	
	public static enum EnumMimicDirtType implements IStringSerializable {
		
		DIRT(ModBlocks.blockStates.dirt, "dirt"),
		GRASS(Blocks.GRASS.getDefaultState(), "grass"),
		PODZOL(ModBlocks.blockStates.podzol, "podzol"),
		MYCELIUM(Blocks.MYCELIUM.getDefaultState(), "mycelium"),
		COARSEDIRT(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT) , "coarsedirt"),
		SNOWY(Blocks.GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, true), "snowy");
		
		private final IBlockState muse;
		private final String name;
		
		private EnumMimicDirtType(IBlockState muse, String name) {
			this.muse = muse;
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		public IBlockState getBlockState() {
			return muse;
		}
		
	}
}
