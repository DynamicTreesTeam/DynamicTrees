package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public void randomTick(BlockState state, World world, BlockPos pos, Random random) {
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
	public BlockState getDecayBlockState(World access, BlockPos pos) {
		return primitiveDirt.getDefaultState();
	}
	
	//Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
	public void doDecay(World world, BlockPos rootPos, BlockState rootyState, Species species) {
		if(!world.isRemote) {
			if(TreeHelper.isRooty(rootyState)) {
				updateTree(rootyState, world, rootPos, world.rand, true);//This will turn the rooty dirt back to it's default soil block. Usually dirt or sand
				BlockState newState = world.getBlockState(rootPos);
				
				if(!TreeHelper.isRooty(newState)) { //Make sure we're not still a rooty block
					/*if(customRootDecay != null && customRootDecay.doDecay(world, rootPos, rootyState, species)) {
						return;
					}*/
					
					BlockState topBlock = world.getBiome(rootPos).getSurfaceBuilderConfig().getTop();
					BlockState fillerBlock = world.getBiome(rootPos).getSurfaceBuilderConfig().getUnder();
					
					
					Biome biome = world.getBiome(rootPos);
					BlockState mimic = primitiveDirt.getDefaultState();//getMimic(world, rootPos);
					
					if(mimic == topBlock || mimic == fillerBlock) {
						world.setBlockState(rootPos, mimic);
					}
					else if(topBlock.getMaterial() == newState.getMaterial()) {
						world.setBlockState(rootPos, topBlock);
					}
					else if(fillerBlock.getMaterial() == newState.getMaterial()) {
						world.setBlockState(rootPos, fillerBlock);
					}
				}
			}
		}
	}
	
	
	
	/**
	 * This is the state the rooty dirt returns to once it no longer supports a tree structure.
	 *
	 * @param access world
	 * @param pos The position of the {@link BlockRooty}
	 * @return blockstate that the rooty dirt turns into when decayed
	 */
	public BlockState getDecayBlockState(IBlockReader access, BlockPos pos) {
		return primitiveDirt.getDefaultState();
	}
	
	@Nonnull
	@Override
	public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
		return primitiveDirt.getDefaultState().getDrops(builder);
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return primitiveDirt.getPickBlock(primitiveDirt.getDefaultState(), target, world, pos, player);
	}
	
	@Override
	public float getBlockHardness(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
		return (float) (primitiveDirt.getDefaultState().getBlockHardness(worldIn, pos) * DTConfigs.rootyBlockHardnessMultiplier.get());
	}
	
	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
		return getSoilLife(blockState, world, pos);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return getFamily(state, worldIn, pos).onTreeActivated(worldIn, pos, state, player, handIn, player.getHeldItem(handIn), hit);
	}
	
	
	public void destroyTree(World world, BlockPos rootPos) {
		Optional<BlockBranch> branch = TreeHelper.getBranchOpt(world.getBlockState(rootPos.up()));
		
		if(branch.isPresent()) {
			BranchDestructionData destroyData = branch.get().destroyBranchFromNode(world, rootPos.up(), Direction.DOWN, true);
			EntityFallingTree.dropTree(world, destroyData, new ArrayList<>(0), EntityFallingTree.DestroyType.ROOT);
		}
	}
	
	@Override
	public void onBlockHarvested(World world, @Nonnull BlockPos pos, BlockState state, @Nonnull PlayerEntity player) {
		destroyTree(world, pos);
		super.onBlockHarvested(world, pos, state, player);
	}
	
	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		destroyTree(world, pos);
		super.onBlockExploded(state, world, pos, explosion);
	}
	
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
		
		TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, rootPos);
		
		if(rootyDirtTE != null) {
			Species species = rootyDirtTE.getSpecies();
			if(species.getFamily() == tree) {//As a sanity check we should see if the tree and the stored species are a match
				return rootyDirtTE.getSpecies();
			}
		}
		
		return tree.getSpeciesForLocation(world, rootPos.offset(getTrunkDirection(world, rootPos)));
	}
	
	public void setSpecies(World world, BlockPos rootPos, Species species) {
		TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, rootPos);
		if(rootyDirtTE != null) {
			rootyDirtTE.setSpecies(species);
		}
	}
	
	@Nonnull
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
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////
	
	@Nonnull
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
	
	@Override
	public boolean isSolid(BlockState state) {
		return true;
	}
	
	@OnlyIn(Dist.CLIENT)
	public int rootColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
		return getFamily(state, blockAccess, pos).getRootColor(state, blockAccess, pos);
	}
	
}
