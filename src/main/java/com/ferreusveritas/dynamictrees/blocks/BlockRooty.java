package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.cells.ICell;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.MimicProperty.IMimic;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree.DestroyType;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
public abstract class BlockRooty extends Block implements ITreePart, ITileEntityProvider, IMimic {
	
	public static final PropertyInteger LIFE = PropertyInteger.create("life", 0, 15);
	
	public BlockRooty(String name, Material material, boolean isTileEntity) {
		super(material);
		this.hasTileEntity = isTileEntity;
		setSoundType(SoundType.GROUND);
		setDefaultState(this.blockState.getBaseState().withProperty(LIFE, 15));
		setTickRandomly(true);
		setUnlocalizedName(name);
		setRegistryName(name);
	}
	
	///////////////////////////////////////////
	// TILE ENTITY
	///////////////////////////////////////////
	
	/** Called serverside after this block is replaced with another in Chunk, but before the Tile Entity is updated */
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		if(hasTileEntity(state)) {
			worldIn.removeTileEntity(pos);
		}
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return hasTileEntity;
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return hasTileEntity ? new TileEntitySpecies() : null;
	}
	
	/**
	 * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
	 * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
	 * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
	 */
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[]{LIFE}, new IUnlistedProperty[] {MimicProperty.MIMIC});
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
	
	
	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////
	@Override
	public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
		updateTree(state, world, pos, random, true);
	}
	
	public EnumFacing getTrunkDirection(IBlockAccess access, BlockPos rootPos) {
		return EnumFacing.UP; 
	}
	
	/**
	 * 
	 * @param world
	 * @param rootPos
	 * @param random
	 * @param natural
	 * @return false if tree was not found
	 */
	public boolean updateTree(IBlockState rootyState, World world, BlockPos rootPos, Random random, boolean natural) {
		
		if(CoordUtils.isSurroundedByLoadedChunks(world, rootPos)) {
			
			boolean viable = false;
			
			Species species = getSpecies(rootyState, world, rootPos);
			
			if(species != Species.NULLSPECIES) {
				BlockPos treePos = rootPos.offset(getTrunkDirection(world, rootPos));
				ITreePart treeBase = TreeHelper.getTreePart(world.getBlockState(treePos));
				
				if(treeBase != TreeHelper.nullTreePart) {
					viable = species.update(world, this, rootPos, getSoilLife(rootyState, world, rootPos), treeBase, treePos, random, natural);
				}
			}
			
			if(!viable) {
				world.setBlockState(rootPos, getDecayBlockState(world, rootPos), 3);
			}
			
		}
		
		return true;
	}
	
	/**
	 * This is the state the rooty dirt returns to once it no longer supports a tree structure.
	 * 
	 * @param access
	 * @param pos The position of the {@link BlockRooty}
	 * @return
	 */
	public IBlockState getDecayBlockState(IBlockAccess access, BlockPos pos) {
		return Blocks.DIRT.getDefaultState();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(Blocks.DIRT);
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		IBlockState mimicState = getMimic(world, pos);
		return new ItemStack(mimicState.getBlock(), 1, mimicState.getBlock().damageDropped(mimicState));
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
		return getSoilLife(blockState, world, pos);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		return getFamily(state, world, pos).onTreeActivated(world, pos, state, player, hand, heldItem, facing, hitX, hitY, hitZ);
	}
	
	public void destroyTree(World world, BlockPos rootPos) {
		BlockBranch branch = TreeHelper.getBranch(world.getBlockState(rootPos.up()));
		if(branch != null) {
			BranchDestructionData destroyData = branch.destroyBranchFromNode(world, rootPos.up(), EnumFacing.DOWN, true, true);
			BlockBranch.dropTree(world, destroyData, new ArrayList<ItemStack>(0), DestroyType.ROOT);
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
	
	public int getSoilLife(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos) {
		return blockState.getValue(LIFE);
	}
	
	public void setSoilLife(World world, BlockPos rootPos, int life) {
		Species species = getSpecies(world.getBlockState(rootPos), world, rootPos);
		world.setBlockState(rootPos, getDefaultState().withProperty(LIFE, MathHelper.clamp(life, 0, 15)), 3);
		world.notifyNeighborsOfStateChange(rootPos, this, false);//Notify all neighbors of NSEWUD neighbors(for comparator)
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
	public ICell getHydrationCell(IBlockAccess blockAccess, BlockPos pos, IBlockState blockState, EnumFacing dir, ILeavesProperties leavesTree) {
		return CellNull.NULLCELL;
	}
	
	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}
	
	@Override
	public int getRadius(IBlockState blockState) {
		return 8;
	}
	
	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		return 8;
	}
	
	@Override
	public int probabilityForBlock(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from) {
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
		EnumFacing dir = getTrunkDirection(world, rootPos);
		BlockPos treePos = rootPos.offset(dir);
		IBlockState treeState = world.getBlockState(treePos);
		
		TreeHelper.getTreePart(treeState).analyse(treeState, world, treePos, null, signal);
		
		return signal;
	}
	
	@Override
	public boolean shouldAnalyse() {
		return true;
	}
	
	@Override
	public MapSignal analyse(IBlockState blockState, World world, BlockPos pos, EnumFacing fromDir, MapSignal signal) {
		signal.run(blockState, world, pos, fromDir);//Run inspector of choice
		
		signal.root = pos;
		signal.found = true;
		
		return signal;
	}
	
	@Override
	public int branchSupport(IBlockState blockState, IBlockAccess blockAccess, BlockBranch branch, BlockPos pos, EnumFacing dir, int radius) {
		return dir == EnumFacing.DOWN ? BlockBranch.setSupport(1, 1) : 0;
	}
	
	@Override
	public TreeFamily getFamily(IBlockState rootyState, IBlockAccess blockAccess, BlockPos rootPos) {
		BlockPos treePos = rootPos.offset(getTrunkDirection(blockAccess, rootPos));
		IBlockState treeState = blockAccess.getBlockState(treePos);
		return TreeHelper.isBranch(treeState) ? TreeHelper.getBranch(treeState).getFamily(treeState, blockAccess, treePos) : TreeFamily.NULLFAMILY;
	}
	
	private TileEntitySpecies getTileEntitySpecies(World world, BlockPos pos) {
		return (TileEntitySpecies) world.getTileEntity(pos);
	}
	
	/**
	 * Rooty Dirt can report whatever {@link TreeFamily} species it wants to be.  
	 * We'll use a stored value to determine the species for the {@link TileEntity} version.
	 * Otherwise we'll just make it report whatever {@link DynamicTree} the above 
	 * {@link BlockBranch} says it is.
	 */
	public Species getSpecies(IBlockState blockState, World world, BlockPos rootPos) {
		
		TreeFamily tree = getFamily(blockState, world, rootPos);
		
		if(hasTileEntity) {
			TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, rootPos);
			
			if(rootyDirtTE instanceof TileEntitySpecies) {
				Species species = rootyDirtTE.getSpecies();
				if(species.getFamily() == tree) {//As a sanity check we should see if the tree and the stored species are a match
					return rootyDirtTE.getSpecies();
				}
			}		
		} 
		
		return tree.getSpeciesForLocation(world, rootPos.offset(getTrunkDirection(world, rootPos)));
	}
	
	public void setSpecies(World world, BlockPos rootPos, Species species) {
		if(hasTileEntity) {
			TileEntitySpecies rootyDirtTE = getTileEntitySpecies(world, rootPos);
			if(rootyDirtTE instanceof TileEntitySpecies) {
				rootyDirtTE.setSpecies(species);
			}		
		} 
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}
	
	public final TreePartType getTreePartType() {
		return TreePartType.ROOT;
	}
	
	@Override
	public final boolean isRootNode() {
		return true;
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess access, BlockPos pos) {
		return state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).withProperty(MimicProperty.MIMIC, getMimic(access, pos)) : state;
	}
	
	@Override
	public IBlockState getMimic(IBlockAccess access, BlockPos pos) {
		IBlockState mimic = Blocks.DIRT.getDefaultState(); //Default to dirt
		return mimic;
	}
	
	/**
	 * We have to reinvent this wheel because Minecraft colors the particles with tintindex 0.. which is used for the grass texture.
	 * So dirt bits end up green if we don't.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		IBlockState state = world.getBlockState(pos);
		IBlockState mimicState = ((IExtendedBlockState) getExtendedState(state, world, pos)).getValue(MimicProperty.MIMIC);
		
		manager.addBlockDestroyEffects(pos, mimicState);
		
		return true;
	}
	
	/**
	 * We have to reinvent this wheel because Minecraft colors the particles with tintindex 0.. which is used for the grass texture.
	 * So dirt bits end up green if we don't.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		BlockPos pos = target.getBlockPos();
		IBlockState mimicState = ((IExtendedBlockState) getExtendedState(state, world, pos)).getValue(MimicProperty.MIMIC);
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
		ParticleDigging particle = (ParticleDigging) manager.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), d0, d1, d2, 0, 0, 0, new int[]{Block.getStateId(mimicState)});
		if(particle != null) {
			particle.setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
		}
		
		return true;
	}
	
}
