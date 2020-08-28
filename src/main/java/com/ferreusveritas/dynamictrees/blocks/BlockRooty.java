package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty.IMimic;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;


/**
 * A version of Rooty Dirt block that holds on to a species with a TileEntity.
 *
 * When to use this:
 *  You can't determine a species of a tree family by location alone (e.g. Swamp Oak by biome)
 * 	The species is rare and you don't want to commit all the resources necessary to make a whole tree family(e.g. Apple Oak)
 * 
 * This is a great method for creating numerous fruit species(Pam's Harvestcraft) under one {@link TreeFamily} family.
 * 
 * @author ferreusveritas
 *
 */
public class BlockRooty extends Block implements ITreePart {

	public static final IntegerProperty FERTILITY = IntegerProperty.create("fertility", 0, 15);
	private final Block primitiveDirt;

	public BlockRooty(Block primitiveDirt) {
		super(Properties.from(primitiveDirt).tickRandomly());
		setRegistryName("rooty_"+ primitiveDirt.getRegistryName().getPath()); //ModLoadingContext.get().getActiveNamespace();

		this.primitiveDirt = primitiveDirt;

//		FERTILITY = IntegerProperty.create("fertility", 0, maxFertility);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	public Block getPrimitiveDirt (){
		return primitiveDirt;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FERTILITY);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public void tick(BlockState state, World world, BlockPos pos, Random random) {
		if(random.nextInt(DTConfigs.treeGrowthFolding.get()) == 0) {
			updateTree(state, world, pos, random, true);
		}
	}

	public Direction getTrunkDirection(IBlockReader access, BlockPos rootPos) {
		return Direction.UP;
	}

	/**
//	 *
//	 * @param world
//	 * @param rootPos
//	 * @param random
//	 * @param natural
//	 */
	public void updateTree(BlockState rootyState, World world, BlockPos rootPos, Random random, boolean natural) {

		if(CoordUtils.isSurroundedByLoadedChunks(world, rootPos)) {

			boolean viable = false;

			Species species = getSpecies(rootyState, world, rootPos);

			if(species.isValid()) {
				BlockPos treePos = rootPos.offset(getTrunkDirection(world, rootPos));
				ITreePart treeBase = TreeHelper.getTreePart(world.getBlockState(treePos));

				if(treeBase != TreeHelper.nullTreePart) {
					viable = species.update(world, this, rootPos, getSoilLife(rootyState, world, rootPos), treeBase, treePos, random, natural);
				}
			}

			if(!viable) {
				//TODO: Attempt to destroy what's left of the tree before setting rooty to dirt
				world.setBlockState(rootPos, getDecayBlockState(world, rootPos), 3);
			}

		}

	}

	/**
	 * This is the state the rooty dirt returns to once it no longer supports a tree structure.
	 *
	 * @param access
	 * @param pos The position of the {@link BlockRooty}
	 * @return
	 */
	public BlockState getDecayBlockState(IBlockReader access, BlockPos pos) {
		return Blocks.DIRT.getDefaultState();
	}

//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return Item.getItemFromBlock(Blocks.DIRT);
//	}
//
//	@Override
//	public ItemStack getPickBlock(BlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
//		BlockState mimicState = getMimic(world, pos);
//		return new ItemStack(mimicState.getBlock(), 1, mimicState.getBlock().damageDropped(mimicState));
//	}
//
//	@Override
//	public float getBlockHardness(BlockState blockState, World worldIn, BlockPos pos) {
//		return 20.0f;//Encourage proper tool usage and discourage bypassing tree felling by digging the root from under the tree
//	};
//
//	@Override
//	protected boolean canSilkHarvest() {
//		return false;
//	}
//
//	@Override
//	public boolean hasComparatorInputOverride(BlockState state) {
//		return true;
//	}
//
//	@Override
//	public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
//		return getSoilLife(blockState, world, pos);
//	}
//
//	@Override
//	public boolean onBlockActivated(World world, BlockPos pos, BlockState state, EntityPlayer player, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
//		ItemStack heldItem = player.getHeldItem(hand);
//		return getFamily(state, world, pos).onTreeActivated(world, pos, state, player, hand, heldItem, facing, hitX, hitY, hitZ);
//	}
//
//	public void destroyTree(World world, BlockPos rootPos) {
//		Optional<BlockBranch> branch = TreeHelper.getBranchOpt(world.getBlockState(rootPos.up()));
//
//		if(branch.isPresent()) {
//			BranchDestructionData destroyData = branch.get().destroyBranchFromNode(world, rootPos.up(), Direction.DOWN, true);
//			EntityFallingTree.dropTree(world, destroyData, new ArrayList<ItemStack>(0), DestroyType.ROOT);
//		}
//	}
//
//	@Override
//	public void onBlockHarvested(World world, BlockPos pos, BlockState state, EntityPlayer player) {
//		destroyTree(world, pos);
//	}
//
//	@Override
//	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
//		destroyTree(world, pos);
//	}

	public int getSoilLife(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
		return blockState.get(FERTILITY);
	}

	public void setSoilLife(World world, BlockPos rootPos, int life) {
		Species species = getSpecies(world.getBlockState(rootPos), world, rootPos);
		world.setBlockState(rootPos, getDefaultState().with(FERTILITY, MathHelper.clamp(life, 0, 15)), 3);
		world.notifyNeighborsOfStateChange(rootPos, this);//Notify all neighbors of NSEWUD neighbors(for comparator)
		setSpecies(world, rootPos, species);

	}

	public boolean fertilize(World world, BlockPos pos, int amount) {
		int soilLife = getSoilLife(world.getBlockState(pos), world, pos);
		if((soilLife == 0 && amount < 0) || (soilLife == 15 && amount > 0)) {
			return false;//Already maxed out
		}
		setSoilLife(world, pos, soilLife + amount);
		return true;
	}

	@Override
	public ICell getHydrationCell(IBlockReader blockAccess, BlockPos pos, BlockState blockState, Direction dir, ILeavesProperties leavesTree) {
		return CellNull.NULLCELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadius(BlockState blockState) {
		return 8;
	}

	@Override
	public int getRadiusForConnection(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from, Direction side, int fromRadius) {
		return 8;
	}

	@Override
	public int probabilityForBlock(BlockState blockState, IBlockReader blockAccess, BlockPos pos, BlockBranch from) {
		return 0;
	}

	/**
	 * Analysis typically begins with the root node.  This function allows
	 * the rootyBlock to direct the analysis in the direction of the tree since
	 * trees are not always "up" from the rootyBlock
	 *
	 * @param world
	 * @param rootPos
	 * @param signal
	 * @return
	 */
	public MapSignal startAnalysis(World world, BlockPos rootPos, MapSignal signal) {
		Direction dir = getTrunkDirection(world, rootPos);
		BlockPos treePos = rootPos.offset(dir);
		BlockState treeState = world.getBlockState(treePos);

		TreeHelper.getTreePart(treeState).analyse(treeState, world, treePos, null, signal);

		return signal;
	}

	@Override
	public boolean shouldAnalyse() {
		return true;
	}

	@Override
	public MapSignal analyse(BlockState blockState, World world, BlockPos pos, Direction fromDir, MapSignal signal) {
		signal.run(blockState, world, pos, fromDir);//Run inspector of choice

		signal.root = pos;
		signal.found = true;

		return signal;
	}

	@Override
	public int branchSupport(BlockState blockState, IBlockReader blockAccess, BlockBranch branch, BlockPos pos, Direction dir, int radius) {
		return dir == Direction.DOWN ? BlockBranch.setSupport(1, 1) : 0;
	}

	@Override
	public TreeFamily getFamily(BlockState rootyState, IBlockReader blockAccess, BlockPos rootPos) {
		BlockPos treePos = rootPos.offset(getTrunkDirection(blockAccess, rootPos));
		BlockState treeState = blockAccess.getBlockState(treePos);
		return TreeHelper.isBranch(treeState) ? TreeHelper.getBranch(treeState).getFamily(treeState, blockAccess, treePos) : TreeFamily.NULLFAMILY;
	}

	private TileEntitySpecies getTileEntitySpecies(World world, BlockPos pos) {
		return (TileEntitySpecies) world.getTileEntity(pos);
	}

	/**
	 * Rooty Dirt can report whatever {@link TreeFamily} species it wants to be.
	 * We'll use a stored value to determine the species for the {@link TileEntity} version.
	 * Otherwise we'll just make it report whatever {@link DynamicTrees} the above
	 * {@link BlockBranch} says it is.
	 */
	public Species getSpecies(BlockState state, World world, BlockPos rootPos) {

		TreeFamily tree = getFamily(state, world, rootPos);

		if(hasTileEntity(state)) {
			TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, rootPos);

			if(rootyDirtTE != null) {
				Species species = rootyDirtTE.getSpecies();
				if(species.getFamily() == tree) {//As a sanity check we should see if the tree and the stored species are a match
					return rootyDirtTE.getSpecies();
				}
			}
		}

		return tree.getSpeciesForLocation(world, rootPos.offset(getTrunkDirection(world, rootPos)));
	}

	public void setSpecies(World world, BlockPos rootPos, Species species) {
		if(hasTileEntity(world.getBlockState(rootPos))) {
			TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, rootPos);
			if(rootyDirtTE != null) {
				rootyDirtTE.setSpecies(species);
			}
		}
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	public final TreePartType getTreePartType() {
		return TreePartType.ROOT;
	}

	@Override
	public final boolean isRootNode() {
		return true;
	}


//
//	/**
//	 * We have to reinvent this wheel because Minecraft colors the particles with tintindex 0.. which is used for the grass texture.
//	 * So dirt bits end up green if we don't.
//	 */
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
//		BlockState mimicState = getMimic(world, pos);
//		manager.addBlockDestroyEffects(pos, mimicState);
//
//		return true;
//	}
//
//	/**
//	 * We have to reinvent this wheel because Minecraft colors the particles with tintindex 0.. which is used for the grass texture.
//	 * So dirt bits end up green if we don't.
//	 */
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public boolean addHitEffects(BlockState state, World world, RayTraceResult target, ParticleManager manager) {
//		BlockPos pos = target.getBlockPos();
//		BlockState mimicState = ((BlockState) getExtendedState(state, world, pos)).getValue(MimicProperty.MIMIC);
//		Random rand = world.rand;
//
//		int x = pos.getX();
//		int y = pos.getY();
//		int z = pos.getZ();
//		AxisAlignedBB axisalignedbb = state.getBoundingBox(world, pos);
//		double d0 = x + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2D) + 0.1D + axisalignedbb.minX;
//		double d1 = y + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2D) + 0.1D + axisalignedbb.minY;
//		double d2 = z + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2D) + 0.1D + axisalignedbb.minZ;
//
//		switch(target.sideHit) {
//			case DOWN:  d1 = y + axisalignedbb.minY - 0.1D; break;
//			case UP:    d1 = y + axisalignedbb.maxY + 0.1D; break;
//			case NORTH: d2 = z + axisalignedbb.minZ - 0.1D; break;
//			case SOUTH: d2 = z + axisalignedbb.maxZ + 0.1D; break;
//			case WEST:  d0 = x + axisalignedbb.minX - 0.1D; break;
//			case EAST:  d0 = x + axisalignedbb.maxX + 0.1D; break;
//		}
//
//		//Safe to spawn particles here since this is a client side only member function
//		ParticleDigging particle = (ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, 0, 0, 0, new int[]{Block.getStateId(mimicState)});
//		if(particle != null) {
//			particle.setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
//		}
//
//		return true;
//	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////


	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@OnlyIn(Dist.CLIENT)
	public int rootColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
		return getFamily(state, blockAccess, pos).getRootColor(state, blockAccess, pos);
	}
	
}
