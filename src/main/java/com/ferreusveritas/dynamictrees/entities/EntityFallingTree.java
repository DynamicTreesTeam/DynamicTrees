package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandlerData;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandlers;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.models.IModelTracker;
import com.ferreusveritas.dynamictrees.models.ModelTrackerCacheEntityFallingTree;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData.PosType;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.google.common.collect.Iterables;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author ferreusveritas
 *
 */
public class EntityFallingTree extends Entity implements IModelTracker {
	
	public static final DataParameter<NBTTagCompound> voxelDataParameter = EntityDataManager.createKey(EntityFallingTree.class, DataSerializers.COMPOUND_TAG);
	
	//Not needed in client
	protected List<ItemStack> payload = new ArrayList<>(0);
	
	//Needed in client and server
	protected BranchDestructionData destroyData = new BranchDestructionData();
	protected Vec3d geomCenter = Vec3d.ZERO;
	protected Vec3d massCenter = Vec3d.ZERO;
	protected AxisAlignedBB normAABB = new AxisAlignedBB(BlockPos.ORIGIN);
	protected boolean clientBuilt = false;
	protected boolean firstUpdate = true;
	public boolean landed = false;
	public DestroyType destroyType = DestroyType.HARVEST;
	public boolean onFire = false;
	
	public static IAnimationHandler AnimHandlerFall = AnimationHandlers.falloverAnimationHandler;
	public static IAnimationHandler AnimHandlerDrop = AnimationHandlers.defaultAnimationHandler;
	public static IAnimationHandler AnimHandlerBurn = AnimationHandlers.defaultAnimationHandler;
	public static IAnimationHandler AnimHandlerFling = AnimationHandlers.defaultAnimationHandler;
	public static IAnimationHandler AnimHandlerBlast = AnimationHandlers.blastAnimationHandler;
	
	public IAnimationHandler currentAnimationHandler = AnimationHandlers.voidAnimationHandler;
	public AnimationHandlerData animationHandlerData = null;
	
	public enum DestroyType {
		VOID,
		HARVEST,
		BLAST,
		FIRE,
		ROOT
	}
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
		setSize(1.0f, 1.0f);
	}
	
	public boolean isClientBuilt() {
		return clientBuilt;
	}
	
	/**
	 * This is only run by the server to set up the object data
	 * 
	 * @param destroyData
	 * @param payload
	 */
	public EntityFallingTree setData(BranchDestructionData destroyData, List<ItemStack> payload, DestroyType destroyType) {
		this.destroyData = destroyData;
		if(destroyData.getNumBranches() == 0) { //If the entity contains no branches there's no reason to create it at all
			System.err.println("Warning: Tried to create a EntityFallingTree with no branch blocks. This shouldn't be possible.");
			new Exception().printStackTrace();
			setDead();
			return this;
		}
		BlockPos cutPos = destroyData.cutPos;
		this.payload = payload;
		this.destroyType = destroyType;
		this.onFire = destroyType == DestroyType.FIRE;
		
		this.posX = cutPos.getX() + 0.5;
		this.posY = cutPos.getY();
		this.posZ = cutPos.getZ() + 0.5;
		
		int numBlocks = destroyData.getNumBranches();
		geomCenter = new Vec3d(0, 0, 0);
		double totalMass = 0;
		
		//Calculate center of geometry, center of mass and bounding box, remap to relative coordinates
		for(int index = 0; index < destroyData.getNumBranches(); index++) {
			BlockPos relPos = destroyData.getBranchRelPos(index);
			
			int radius = destroyData.getBranchRadius(index);
			float mass = (radius * radius * 64) / 4096f;//Assume full height cuboids for simplicity
			totalMass += mass;
			
			Vec3d relVec = new Vec3d(relPos.getX(), relPos.getY(), relPos.getZ());
			geomCenter = geomCenter.add(relVec);
			massCenter = massCenter.add(relVec.scale(mass));
		}
		
		geomCenter = geomCenter.scale(1.0 / numBlocks);
		massCenter = massCenter.scale(1.0 / totalMass);
		
		setEntityBoundingBox(buildAABBFromDestroyData(destroyData).offset(posX, posY, posZ));
		
		setVoxelData(buildVoxelData(destroyData));
		
		return this;
	}
	
	public NBTTagCompound buildVoxelData(BranchDestructionData destroyData) {
		NBTTagCompound tag = new NBTTagCompound();
		destroyData.writeToNBT(tag);
		
		tag.setDouble("geomx", geomCenter.x);
		tag.setDouble("geomy", geomCenter.y);
		tag.setDouble("geomz", geomCenter.z);
		tag.setDouble("massx", massCenter.x);
		tag.setDouble("massy", massCenter.y);
		tag.setDouble("massz", massCenter.z);
		tag.setInteger("destroytype", destroyType.ordinal());
		tag.setBoolean("onfire", onFire);
		
		return tag;
	}
	
	public void buildClient() {
		
		NBTTagCompound tag = getVoxelData();
		
		if(tag.hasKey("species")) {			
			destroyData = new BranchDestructionData(tag);
			if(destroyData.getNumBranches() == 0) {
				setDead();
			}
			destroyType = DestroyType.values()[tag.getInteger("destroytype")];
			geomCenter = new Vec3d(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
			massCenter = new Vec3d(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));
			buildAABBFromDestroyData(destroyData);
			setEntityBoundingBox(normAABB.offset(posX, posY, posZ));
			onFire = tag.getBoolean("onfire");
			clientBuilt = true;
		} else {
			System.out.println("Error: No species tag has been set");
		}
				
		BlockBounds renderBounds = new BlockBounds(destroyData.cutPos);
		
		for(BlockPos absPos: Iterables.concat(destroyData.getPositions(PosType.BRANCHES), destroyData.getPositions(PosType.LEAVES))) {
			world.setBlockState(absPos, ModBlocks.blockStates.air, 0);////The client needs to set it's blocks to air
			renderBounds.union(absPos);//Expand the re-render volume to include this block
		}
		
		cleanupShellBlocks(destroyData);
		
		world.markBlockRangeForRenderUpdate(renderBounds.getMin(), renderBounds.getMax());//This forces the client to rerender the chunks
	}
	
	protected void cleanupShellBlocks(BranchDestructionData destroyData) {
		BlockPos cutPos = destroyData.cutPos;
		for(int i = 0; i < destroyData.getNumBranches(); i++) {
			if(destroyData.getBranchRadius(i) > 8) {
				BlockPos pos = destroyData.getBranchRelPos(i).add(cutPos);
				for(Surround dir: Surround.values()) {
					BlockPos dPos = pos.add(dir.getOffset());
					if(world.getBlockState(dPos).getBlock() == ModBlocks.blockTrunkShell) {
						world.setBlockToAir(dPos);
					}
				}
			}
		}
	}
	
	public AxisAlignedBB buildAABBFromDestroyData(BranchDestructionData destroyData) {
		normAABB = new AxisAlignedBB(BlockPos.ORIGIN);
		
		for(BlockPos relPos: destroyData.getPositions(PosType.BRANCHES, false)) {
			normAABB = normAABB.union(new AxisAlignedBB(relPos));
		}
		
		//Adjust the bounding box to account for the tree falling over
		double height = normAABB.maxY - normAABB.minY;
		double width = MathHelper.absMax(normAABB.maxX - normAABB.minX, normAABB.maxZ - normAABB.minZ);
		double grow = Math.max(0, height - (width / 2) ) + 2;
		normAABB = normAABB.grow(grow + 4, 4, grow + 4);
		
		return normAABB;
	}
	
	public BranchDestructionData getDestroyData() {
		return destroyData;
	}
	
	public List<ItemStack> getPayload() {
		return payload;
	}
	
	public Vec3d getGeomCenter() {
		return geomCenter;
	}
	
	public Vec3d getMassCenter() {
		return massCenter;
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		//This comes to the client as a packet from the server. But it doesn't set up the bounding box correctly
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		//This function is called by the Entity constructor during which normAABB hasn't yet been assigned.
		this.setEntityBoundingBox(normAABB != null ? normAABB.offset(posX, posY, posZ) : new AxisAlignedBB(BlockPos.ORIGIN));
	}
		
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
				
		if(world.isRemote && !clientBuilt) {
			buildClient();
			if(isDead) {
				return;
			}
		}
		
		if(!world.isRemote && firstUpdate) {
			updateNeighbors();
		}
		
		handleMotion();
		
		setEntityBoundingBox(normAABB.offset(posX, posY, posZ));
		
		if(shouldDie()) {
			dropPayLoad();
			setDead();
			modelCleanup();
		}

		firstUpdate = false;
	}
	
	/**
	 * This is run server side to update all of the neighbors
	 */
	protected void updateNeighbors() {
		HashSet<BlockPos> destroyed = new HashSet<>();
		HashSet<BlockPos> toUpdate = new HashSet<>();
		
		//Gather a set of all of the block positions that were recently destroyed
		Iterables.concat(destroyData.getPositions(PosType.BRANCHES), destroyData.getPositions(PosType.LEAVES)).forEach(pos -> destroyed.add(pos));
	
		//Gather a list of all of the non-destroyed blocks surrounding each destroyed block
		for(BlockPos d: destroyed) {
			for(EnumFacing dir: EnumFacing.values()) {
				BlockPos dPos = d.offset(dir);
				if(!destroyed.contains(dPos)) {
					toUpdate.add(dPos);
				}
			}
		}
		
		//Update each of the blocks that need to be updated
		toUpdate.forEach(pos -> world.neighborChanged(pos, Blocks.AIR, pos));
	}
	
	protected IAnimationHandler selectAnimationHandler() {
		return ModConfigs.enableFallingTrees ? destroyData.species.selectAnimationHandler(this) : AnimationHandlers.voidAnimationHandler;
	}
	
	public IAnimationHandler defaultAnimationHandler() {
		if(destroyType == DestroyType.VOID || destroyType == DestroyType.ROOT) {
			return AnimationHandlers.voidAnimationHandler;
		}
		
		if(destroyType == DestroyType.BLAST) {
			return AnimHandlerBlast;
		}
		
		if(destroyType == DestroyType.FIRE) {
			return AnimHandlerBurn;
		}
		
		if(getDestroyData().cutDir == EnumFacing.DOWN) {
			if(getMassCenter().y >= 1.0) {
				return AnimHandlerFall;
			} else {
				return AnimHandlerFling;
			}
		}
		
		return AnimHandlerDrop;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void modelCleanup() {
		ModelTrackerCacheEntityFallingTree.cleanupModels(world, this);
	}
	
	public void handleMotion() {
		if(firstUpdate) {
			currentAnimationHandler = selectAnimationHandler();
			currentAnimationHandler.initMotion(this);
		} else {
			currentAnimationHandler.handleMotion(this);
		}
	}
	
	public void dropPayLoad() {		
		if(!world.isRemote) {
			currentAnimationHandler.dropPayload(this);
		}
	}
	
	public boolean shouldDie() {
		return ticksExisted > 20 && currentAnimationHandler.shouldDie(this); //Give the entity 20 ticks to receive it's data from the server.
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldRender() {
		return currentAnimationHandler.shouldRender(this);
	}
	
	/**
	 * Same style payload droppers that have always existed in Dynamic Trees.
	 * 
	 * Drops wood materials at the cut position
	 * Leaves drops fall from their original location
	 * 
	 * @param entity
	 */
	public static void standardDropLogsPayload(EntityFallingTree entity) {
		World world = entity.world;
		if(!world.isRemote) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getPayload().forEach(i -> spawnItemAsEntity(world, cutPos, i));
		}
	}
	
	public static void standardDropLeavesPayLoad(EntityFallingTree entity) {
		World world = entity.world;
		if(!world.isRemote) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getDestroyData().leavesDrops.forEach(bis -> Block.spawnAsEntity(world, cutPos.add(bis.pos), bis.stack));
		}
	}
	
	/**
	 * Same as Block.spawnAsEntity only this arrests the entityItem's random motion. Useful for CC turtles to pick up the loot. 
	 */
	public static void spawnItemAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
		if (!worldIn.isRemote && !stack.isEmpty() && worldIn.getGameRules().getBoolean("doTileDrops") && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			EntityItem entityitem = new EntityItem(worldIn, (double)pos.getX() + 0.5F, (double)pos.getY() + 0.5F, (double)pos.getZ() + 0.5F, stack);
			entityitem.motionX = 0;
			entityitem.motionY = 0;
			entityitem.motionZ = 0;
			entityitem.setDefaultPickupDelay();
			worldIn.spawnEntity(entityitem);
		}
	}
	
	@Override
	protected void entityInit() {
		getDataManager().register(voxelDataParameter, new NBTTagCompound());
	}
	
	public void cleanupRootyDirt() {
		//Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
		if(!world.isRemote) {
			BlockPos rootPos = getDestroyData().cutPos.down();
			IBlockState belowState = world.getBlockState(rootPos);

			if(TreeHelper.isRooty(belowState)) {
				@SuppressWarnings("serial")
				Random rand = new Random() { public int nextInt(int bound) { return 0; } };//Special random generator that always returns 0.
				belowState.getBlock().randomTick(world, getDestroyData().cutPos.down(), belowState, rand);//This will turn the rooty dirt back to it's default soil block. Usually dirt or sand
				
				//First compare the biome's top block and the default soil block for matching materials 
				belowState = world.getBlockState(rootPos);
				IBlockState biomeState = world.getBiome(rootPos).topBlock;
				
				Material belowMaterial = belowState.getMaterial();
				Material biomeMaterial = biomeState.getMaterial();
				
				//GRASS is basically GROUND so will treat it as such
				if(biomeMaterial == Material.GRASS) {
					biomeMaterial = Material.GROUND;
				}
				if(belowMaterial == Material.GRASS) {
					belowMaterial = Material.GROUND;
				}
				
				//If the materials match and the species can be planted in the default soil for that biome then we'll look around
				//the block for matching samples of the biome soil.  If we find one then we'll use it to replace the dirt block
				if(biomeMaterial == belowMaterial && getDestroyData().species.getAcceptableSoils().contains(biomeState.getBlock())) {
					for(EnumFacing dir : EnumFacing.HORIZONTALS) {
						BlockPos dPos = rootPos.offset(dir);
						IBlockState findState = world.getBlockState(dPos);
						if(findState == biomeState) {
							world.setBlockState(rootPos, world.getBiome(rootPos).topBlock);
							return;
						}
					}

				}
			}
		}
	}
	
	//This is shipped off to the clients
	public void setVoxelData(NBTTagCompound tag) {
		getDataManager().set(voxelDataParameter, tag);
	}
	
	public NBTTagCompound getVoxelData() {
		return getDataManager().get(voxelDataParameter);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		setDead();//Falling Trees are never saved to disk.  In the event one is read from disk just kill it.
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
	public static EntityFallingTree dropTree(World world, BranchDestructionData destroyData, List<ItemStack> woodDropList, DestroyType destroyType) {
		//Spawn the appropriate item entities into the world
		if(!world.isRemote) {// Only spawn entities server side
			EntityFallingTree entity = new EntityFallingTree(world).setData(destroyData, woodDropList, destroyType);
			if(entity.isEntityAlive()) {
				world.spawnEntity(entity);
			}
			return entity;
		}
		
		return null;
	}
	
}
