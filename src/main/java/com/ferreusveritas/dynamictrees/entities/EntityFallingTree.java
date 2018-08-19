package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandlerData;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandlers;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.models.IModelTracker;
import com.ferreusveritas.dynamictrees.models.ModelCacheFallingTree;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
		HARVEST,
		BLAST,
		FIRE,
		ROOT
	}
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
		setSize(1.0f, 1.0f);
		
		ModConfigs.enableFallingTrees = true;
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
			destroyType = DestroyType.values()[tag.getInteger("destroytype")];
			geomCenter = new Vec3d(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
			massCenter = new Vec3d(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));
			buildAABBFromDestroyData(destroyData);
			setEntityBoundingBox(normAABB.offset(posX, posY, posZ));
			onFire = tag.getBoolean("onfire");
			clientBuilt = true;
		}
		
		for(int i = 0; i < destroyData.getNumBranches(); i++) {
			BlockPos relPos = destroyData.getBranchRelPos(i);
			BlockPos absPos = destroyData.cutPos.add(relPos);
			world.setBlockState(absPos, Blocks.STONE.getDefaultState(), 0);//This forces the client to rerender the chunks
			world.setBlockState(absPos, Blocks.AIR.getDefaultState(), 0);
		}
		
		for(int i = 0; i < destroyData.getNumLeaves(); i++) {
			BlockPos relPos = destroyData.getLeavesRelPos(i);
			BlockPos absPos = destroyData.cutPos.add(relPos);
			world.destroyBlock(absPos, false);
		}
		
	}
	
	public AxisAlignedBB buildAABBFromDestroyData(BranchDestructionData destroyData) {
		normAABB = new AxisAlignedBB(BlockPos.ORIGIN);
		
		for(int i = 0; i < destroyData.getNumBranches(); i++) {
			normAABB = normAABB.union(new AxisAlignedBB(destroyData.getBranchRelPos(i)));
		}
		
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
		final int numBranches = destroyData.getNumBranches();
		for(int i = 0; i < numBranches; i++) {
			destroyed.add(destroyData.cutPos.add(destroyData.getBranchRelPos(i)));
		}

		//Continue gathering a set of all of the block positions that were recently destroyed
		final int numLeaves = destroyData.getNumLeaves();
		for(int i = 0; i < numLeaves; i++) {
			destroyed.add(destroyData.cutPos.add(destroyData.getLeavesRelPos(i)));
		}
		
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
		ModelCacheFallingTree.cleanupModels(world, this);
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
		return currentAnimationHandler.shouldDie(this);
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
			entity.getPayload().forEach(i -> Block.spawnAsEntity(world, cutPos, i));
		}
	}
	
	public static void standardDropLeavesPayLoad(EntityFallingTree entity) {
		World world = entity.world;
		if(!world.isRemote) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getDestroyData().leavesDrops.forEach(bis -> Block.spawnAsEntity(world, cutPos.add(bis.pos), bis.stack));
		}
	}
	
	@Override
	protected void entityInit() {
		getDataManager().register(voxelDataParameter, new NBTTagCompound());
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
			world.spawnEntity(entity);
			return entity;
		}
		
		return null;
	}
}
