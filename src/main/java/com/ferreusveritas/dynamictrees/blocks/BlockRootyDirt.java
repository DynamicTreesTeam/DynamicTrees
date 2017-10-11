package com.ferreusveritas.dynamictrees.blocks;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ConfigHandler;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.GrowSignal;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.inspectors.NodeDisease;
import com.ferreusveritas.dynamictrees.inspectors.NodeFruit;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRootyDirt extends Block implements ITreePart {

	static String name = "rootydirt";
	
	public static final PropertyInteger LIFE = PropertyInteger.create("life", 0, 15);
	public static final PropertyEnum MIMIC = PropertyEnum.create("mimic", EnumMimicType.class);
	
	public BlockRootyDirt() {
		super(Material.GROUND);
		setSoundType(SoundType.GROUND);
		setDefaultState(this.blockState.getBaseState().withProperty(LIFE, 15).withProperty(MIMIC, EnumMimicType.DIRT));
		setTickRandomly(true);
		setUnlocalizedName(name);
		setRegistryName(name);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	public static enum EnumMimicType implements IStringSerializable {

		DIRT(Blocks.DIRT.getDefaultState(), "dirt"),
		GRASS(Blocks.GRASS.getDefaultState(), "grass"),
		PODZOL( Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL) , "podzol"),
		MYCELIUM(Blocks.MYCELIUM.getDefaultState(), "mycelium"),
		COARSEDIRT( Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT) , "coarsedirt"),
		SNOWY(Blocks.GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, true), "snowy");

		private final IBlockState muse;
		private final String name;

		private EnumMimicType(IBlockState muse, String name) {
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

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{LIFE, MIMIC});
	}
		
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(LIFE, meta);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(LIFE).intValue();
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.withProperty(MIMIC, getMimicType(worldIn, pos));
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
		grow(world, pos, random);
	}

	public boolean grow(World world, BlockPos pos, Random random) {

		BlockBranch branch = TreeHelper.getBranch(world, pos.up());

		if(branch != null) {
			DynamicTree tree = branch.getTree();
			float growthRate = tree.getGrowthRate(world, pos.up()) * ConfigHandler.treeGrowthRateMultiplier;
			do {
				if(random.nextFloat() < growthRate) {
					int life = getSoilLife(world, pos);
					if(life > 0 && TreeHelper.isSurroundedByExistingChunks(world, pos)){
						boolean success = false;

						float energy = tree.getEnergy(world, pos.up());
						for(int i = 0; !success && i < 1 + tree.getRetries(); i++) {//Some species have multiple growth retry attempts
							success = branch.growSignal(world, pos.up(), new GrowSignal(branch, pos, energy)).success;
						}

						int soilLongevity = tree.getSoilLongevity(world, pos.up()) * (success ? 1 : 16);//Don't deplete the soil as much if the grow operation failed

						if(random.nextInt(soilLongevity) == 0) {//1 in X(soilLongevity) chance to draw nutrients from soil
							setSoilLife(world, pos, life - 1);//decrement soil life
						}
					} else {
						if(random.nextFloat() < ConfigHandler.diseaseChance && TreeHelper.isSurroundedByExistingChunks(world, pos)) {
							branch.analyse(world, pos.up(), EnumFacing.DOWN, new MapSignal(new NodeDisease(tree)));
						} else {
							NodeFruit nodeFruit = tree.getNodeFruit(world, pos.up());
							if(nodeFruit != null && TreeHelper.isSurroundedByExistingChunks(world, pos)) {
								branch.analyse(world, pos.up(), EnumFacing.DOWN, new MapSignal(nodeFruit));
							}
						}
					}
				}
			} while(--growthRate > 0.0f);
		} else {
			world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
			return false;
		}

		return true;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.DIRT);
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return 20.0f;//Encourage proper tool usage and discourage bypassing tree felling by digging the root from under the tree
	};

	@Override
	protected boolean canSilkHarvest() {
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		return getSoilLife(world, pos);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);

		if(heldItem != null) {//Something in the hand
			return applyItemSubstance(world, pos, player, hand, heldItem);
		}

		return false;
	}

	@Override
	public boolean applyItemSubstance(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack itemStack) {
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());

		if(branch != null && branch.getTree().applySubstance(world, pos, this, itemStack)) {
			if (itemStack.getItem() instanceof IEmptiable) {//A substance deployed from a refillable container
				if(!player.capabilities.isCreativeMode) {
					IEmptiable emptiable = (IEmptiable) itemStack.getItem();
					player.setHeldItem(hand, emptiable.getEmptyContainer());
				}
			}
			else if(itemStack.getItem() == Items.POTIONITEM) {//An actual potion
				if(!player.capabilities.isCreativeMode) {
					player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
				}
			} else {
				itemStack.shrink(1); //Just a regular item like bonemeal
			}
			return true;
		}
		return false;
	}

	public void destroyTree(World world, BlockPos pos) {
		BlockBranch branch = TreeHelper.getBranch(world, pos.up());
		if(branch != null) {
			branch.destroyEntireTree(world, pos.up());
		}
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		destroyTree(world, pos);
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		destroyTree(world, pos);
	}

	public int getSoilLife(IBlockAccess blockAccess, BlockPos pos) {
		return blockAccess.getBlockState(pos).getValue(LIFE);
	}

	public void setSoilLife(World world, BlockPos pos, int life) {
		world.setBlockState(pos, getDefaultState().withProperty(LIFE, MathHelper.clamp(life, 0, 15)), 3);
		world.notifyNeighborsOfStateChange(pos, this, false);//Notify all neighbors of NSEWUD neighbors(for comparator)
	}

	public boolean fertilize(World world, BlockPos pos, int amount) {
		int soilLife = getSoilLife(world, pos);
		if((soilLife == 0 && amount < 0) || (soilLife == 15 && amount > 0)) {
			return false;//Already maxed out
		}
		setSoilLife(world, pos, soilLife + amount);
		return true;
	}

	@Override
	public int getHydrationLevel(IBlockAccess blockAccess, BlockPos pos, EnumFacing dir, DynamicTree leavesTree) {
		return 0;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadiusForConnection(IBlockAccess blockAccess, BlockPos pos, BlockBranch from, int fromRadius) {
		return 8;
	}

	@Override
	public int probabilityForBlock(IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
		return 0;
	}

	@Override
	public int getRadius(IBlockAccess blockAccess, BlockPos pos) {
		return 0;
	}

	@Override
	public boolean isRootNode() {
		return true;
	}

	@Override
	public MapSignal analyse(World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		signal.run(world, this, pos, fromDir);//Run inspector of choice

		signal.root = pos;
		signal.found = true;

		return signal;
	}

	@Override
	public int branchSupport(IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return dir == EnumFacing.DOWN ? 0x11 : 0;
	}

	@Override
	public DynamicTree getTree(IBlockAccess blockAccess, BlockPos pos) {
		return TreeHelper.isBranch(blockAccess, pos.up()) ? TreeHelper.getSafeTreePart(blockAccess, pos.up()).getTree(blockAccess, pos.up()) : null;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	public EnumMimicType getMimicType(IBlockAccess blockAccess, BlockPos pos) {
		final int dMap[] = {0, -1, 1};

		for(int depth: dMap) {
			for(EnumFacing dir: EnumFacing.HORIZONTALS) {
				IBlockState mimic = blockAccess.getBlockState(pos.offset(dir).down(depth));

				for(EnumMimicType muse: EnumMimicType.values()) {
					if(muse != EnumMimicType.DIRT) {
						if(mimic == muse.getBlockState()) {
							return muse;
						}
					}
				}
			}
		}

		return EnumMimicType.DIRT;//Default to plain old dirt
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
