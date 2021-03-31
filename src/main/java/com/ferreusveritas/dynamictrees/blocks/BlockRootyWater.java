package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty;
import com.ferreusveritas.dynamictrees.blocks.properties.UnlistedPropertyBool;
import com.ferreusveritas.dynamictrees.blocks.properties.UnlistedPropertyFloat;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRootyWater extends BlockRooty {
	
	protected final boolean isSmoothWaterInstalled;
	
	protected static final AxisAlignedBB WATER_ROOTS_AABB = new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
	
	public static final UnlistedPropertyBool[] RENDER_SIDES = new UnlistedPropertyBool[] {
			new UnlistedPropertyBool("render_d"),
			new UnlistedPropertyBool("render_u"),
			new UnlistedPropertyBool("render_n"),
			new UnlistedPropertyBool("render_s"),
			new UnlistedPropertyBool("render_w"),
			new UnlistedPropertyBool("render_e"),
	};
	public static final UnlistedPropertyFloat[] CORNER_HEIGHTS = new UnlistedPropertyFloat[] {
			new UnlistedPropertyFloat("level_nw"),
			new UnlistedPropertyFloat("level_sw"),
			new UnlistedPropertyFloat("level_se"),
			new UnlistedPropertyFloat("level_ne"),
	};

	public BlockRootyWater(boolean isTileEntity) {
		this(isTileEntity,"rootywater");
	}

	public BlockRootyWater(boolean isTileEntity, String name) {
		super(name, Material.WATER, isTileEntity);
		setSoundType(SoundType.PLANT);
		setDefaultState(super.getDefaultState());
		isSmoothWaterInstalled = Loader.isModLoaded("smoothwater");
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockStateWater(this, new IProperty[] {LIFE}, new IUnlistedProperty[] {
				MimicProperty.MIMIC,
				RENDER_SIDES[0],
				RENDER_SIDES[1],
				RENDER_SIDES[2],
				RENDER_SIDES[3],
				RENDER_SIDES[4],
				RENDER_SIDES[5],
				CORNER_HEIGHTS[0],
				CORNER_HEIGHTS[1],
				CORNER_HEIGHTS[2],
				CORNER_HEIGHTS[3]
		});
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
		int avgLvl = 0;
		int total = 0;
		for (EnumFacing dir : EnumFacing.HORIZONTALS) {
			IBlockState st = access.getBlockState(pos.offset(dir));
			if (st.getProperties().containsKey(BlockLiquid.LEVEL)) {
				avgLvl += st.getValue(BlockLiquid.LEVEL);
				total++;
			}
		}
		
		avgLvl = total == 0 ? 7 : avgLvl / total;
		
		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState extState = (IExtendedBlockState) state;
			for (EnumFacing dir : EnumFacing.VALUES) {
				//extState = extState.withProperty(RENDER_SIDES[dir.ordinal()], Blocks.WATER.shouldSideBeRendered(state, access, pos, dir));
				extState = extState.withProperty(RENDER_SIDES[dir.ordinal()], shouldSideBeRendered(state, access, pos, dir));
			}
			
			float defaultHeight = 1 - BlockLiquid.getLiquidHeightPercent(avgLvl);
			
			float c0 = getFluidHeight(access, pos, Material.WATER);
			float c1 = getFluidHeight(access, pos.south(), Material.WATER);
			float c2 = getFluidHeight(access, pos.east().south(), Material.WATER);
			float c3 = getFluidHeight(access, pos.east(), Material.WATER);
			
			if(isSmoothWaterInstalled) {
				if(c0 > 0.88f && c0 < 0.89f) {
					c0 = 0.875f;
				}
				if(c1 > 0.88f && c1 < 0.89f) {
					c1 = 0.875f;
				}
				if(c2 > 0.88f && c2 < 0.89f) {
					c2 = 0.875f;
				}
				if(c3 > 0.88f && c3 < 0.89f) {
					c3 = 0.875f;
				}
			}
			
			float avg = 0;
			int i = 0;
			if (c0 > 0) {
				avg += c0;
				i++;
			}
			if (c1 > 0) {
				avg += c1;
				i++;
			}
			if (c2 > 0) {
				avg += c2;
				i++;
			}
			if (c3 > 0) {
				avg += c3;
				i++;
			}
			if (i > 0) avg /= i;
			
			defaultHeight = avg;
			
			extState = extState.withProperty(CORNER_HEIGHTS[0], c0 < 0 ? defaultHeight : c0);
			extState = extState.withProperty(CORNER_HEIGHTS[1], c1 < 0 ? defaultHeight : c1);
			extState = extState.withProperty(CORNER_HEIGHTS[2], c2 < 0 ? defaultHeight : c2);
			extState = extState.withProperty(CORNER_HEIGHTS[3], c3 < 0 ? defaultHeight : c3);
			
			return extState;
		}
		
		return state;
	}
	
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		
		int y = fromPos.getY() - pos.getY();
		
		
		if(y < 1) {
			IBlockState newState = worldIn.getBlockState(fromPos);
			if(canFlowInto(worldIn, fromPos, newState)) {
				newState.getBlock().dropBlockAsItem(worldIn, pos, newState, 0);
				worldIn.setBlockState(fromPos, Blocks.FLOWING_WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, 7), 2);
			}
		}
	}
	
	private boolean canFlowInto(World worldIn, BlockPos pos, IBlockState state) {
		Material material = state.getMaterial();
		return material != this.blockMaterial && material != Material.LAVA && !this.isBlocked(worldIn, pos, state);
	}
	
	private boolean isBlocked(World worldIn, BlockPos pos, IBlockState state) {
		Block block = state.getBlock(); //Forge: state must be valid for position
		Material mat = state.getMaterial();
		
		if (!(block instanceof BlockDoor) && block != Blocks.STANDING_SIGN && block != Blocks.LADDER && block != Blocks.REEDS) {
			return mat != Material.PORTAL && mat != Material.STRUCTURE_VOID ? mat.blocksMovement() : true;
		}
		else {
			return true;
		}
	}
	
	private float getFluidHeight(IBlockAccess blockAccess, BlockPos blockPosIn, Material blockMaterial) {
		int i = 0;
		int w = 0;
		float f = 0.0F;
		
		for (int j = 0; j < 4; ++j) {
			BlockPos blockpos = blockPosIn.add(-(j & 1), 0, -(j >> 1 & 1));
			
			if (blockAccess.getBlockState(blockpos.up()).getMaterial() == blockMaterial) {
				return 1.0F;
			}
			
			IBlockState iblockstate = blockAccess.getBlockState(blockpos);
			Material material = iblockstate.getMaterial();
			
			if (material != blockMaterial) {
				if (!material.isSolid()) {
					++f;
					++i;
				}
			} else {
				int k = iblockstate.getValue(BlockLiquid.LEVEL);
				
				if (k >= 8 || k == 0) {
					f += BlockLiquid.getLiquidHeightPercent(k) * 10.0F;
					i += 10;
				}
				
				f += BlockLiquid.getLiquidHeightPercent(k);
				++i;
			}
			if (iblockstate.isFullCube() || iblockstate.isOpaqueCube() || iblockstate.isFullBlock()) {
				w++;
			}
		}
		if (w == 0 && i == 0) return 0;
		if (i == 0) return -1;
		return 1.0F - f / (float) i;
	}
	
	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		IBlockState upState = world.getBlockState(pos.up());
		if (TreeHelper.isBranch(upState))
			return new ItemStack(TreeHelper.getBranch(upState).getFamily().getDynamicBranch());
		return ItemStack.EMPTY;
	}

	@Override
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
		return  Blocks.WATER.getDefaultState();
	}
	
	@Override
	public IBlockState getDecayBlockState(IBlockAccess access, BlockPos pos) {
		return Blocks.WATER.getDefaultState();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		
		if (blockAccess.getBlockState(pos.offset(side)).getMaterial() == Material.WATER) {
			return false;
		}
		
		if(side == EnumFacing.UP) {
			return true;
		}
		
		return !blockAccess.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccess, pos.offset(side), side.getOpposite());
	}
	
	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return world.getBlockState(pos.offset(face)).getMaterial() == Material.WATER;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return hasTileEntity ? new TileEntitySpecies() : null;
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
	
	private static final Vec3d acceleration_modifier = new Vec3d(0, 0, 0);
	@Override
	public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
		return motion.add(acceleration_modifier);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return WATER_ROOTS_AABB;
	}
	
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}
	
	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		return 8;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
		int i = source.getCombinedLight(pos, 0);
		int j = source.getCombinedLight(pos.up(), 0);
		int k = i & 255;
		int l = j & 255;
		int i1 = i >> 16 & 255;
			int j1 = j >> 16 & 255;
			return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
	}
	
	@Override
	@SideOnly (Side.CLIENT)
	public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d originalColor, float partialTicks) {
		return super.getFogColor(world, pos, Blocks.WATER.getDefaultState(), entity, originalColor, partialTicks);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		return true;
	}
	
}
