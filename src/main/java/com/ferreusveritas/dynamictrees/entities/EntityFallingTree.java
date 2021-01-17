package com.ferreusveritas.dynamictrees.entities;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.RootyBlock;
import com.ferreusveritas.dynamictrees.entities.animation.DataAnimationHandler;
import com.ferreusveritas.dynamictrees.entities.animation.AnimationHandlers;
import com.ferreusveritas.dynamictrees.entities.animation.IAnimationHandler;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.models.IModelTracker;
import com.ferreusveritas.dynamictrees.models.ModelTrackerCacheEntityFallingTree;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

///**
// *
// * @author ferreusveritas
// *
// */
public class EntityFallingTree extends Entity implements IModelTracker {
	
	
	
	public static final DataParameter<CompoundNBT> voxelDataParameter = EntityDataManager.createKey(EntityFallingTree.class, DataSerializers.COMPOUND_NBT);
	
	//Not needed in client
	protected List<ItemStack> payload = new ArrayList<>(0);
	
	//Needed in client and server
	protected BranchDestructionData destroyData = new BranchDestructionData();
	protected Vector3d geomCenter = Vector3d.ZERO;
	protected Vector3d massCenter = Vector3d.ZERO;
	protected AxisAlignedBB normAABB = new AxisAlignedBB(BlockPos.ZERO);
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
	public DataAnimationHandler dataAnimationHandler = null;
	
	public enum DestroyType {
		VOID,
		HARVEST,
		BLAST,
		FIRE,
		ROOT
	}
	
	public EntityFallingTree(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
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
			onKillCommand();
			return this;
		}
		BlockPos cutPos = destroyData.cutPos;
		this.payload = payload;
		this.destroyType = destroyType;
		this.onFire = destroyType == DestroyType.FIRE;

		this.setRawPosition(cutPos.getX() + 0.5, cutPos.getY(), cutPos.getZ() + 0.5);

		int numBlocks = destroyData.getNumBranches();
		geomCenter = new Vector3d(0, 0, 0);
		double totalMass = 0;
		
		//Calculate center of geometry, center of mass and bounding box, remap to relative coordinates
		for(int index = 0; index < destroyData.getNumBranches(); index++) {
			BlockPos relPos = destroyData.getBranchRelPos(index);
			
			int radius = destroyData.getBranchRadius(index);
			float mass = (radius * radius * 64) / 4096f;//Assume full height cuboids for simplicity
			totalMass += mass;
			
			Vector3d relVec = new Vector3d(relPos.getX(), relPos.getY(), relPos.getZ());
			geomCenter = geomCenter.add(relVec);
			massCenter = massCenter.add(relVec.scale(mass));
		}
		
		geomCenter = geomCenter.scale(1.0 / numBlocks);
		massCenter = massCenter.scale(1.0 / totalMass);
		
		setVoxelData(buildVoxelData(destroyData));
		
		return this;
	}
	
	public CompoundNBT buildVoxelData(BranchDestructionData destroyData) {
		CompoundNBT tag = new CompoundNBT();
		destroyData.writeToNBT(tag);
		
		tag.putDouble("geomx", geomCenter.x);
		tag.putDouble("geomy", geomCenter.y);
		tag.putDouble("geomz", geomCenter.z);
		tag.putDouble("massx", massCenter.x);
		tag.putDouble("massy", massCenter.y);
		tag.putDouble("massz", massCenter.z);
		tag.putInt("destroytype", destroyType.ordinal());
		tag.putBoolean("onfire", onFire);
		
		return tag;
	}
	
	public void setupFromNBT(CompoundNBT tag) {
		destroyData = new BranchDestructionData(tag);
		if(destroyData.getNumBranches() == 0) {
			onKillCommand();
		}
		destroyType = DestroyType.values()[tag.getInt("destroytype")];
		geomCenter = new Vector3d(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
		massCenter = new Vector3d(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));
		buildAABBFromDestroyData(destroyData);
		setBoundingBox(normAABB.offset(this.getPosX(), this.getPosY(), this.getPosZ()));
		onFire = tag.getBoolean("onfire");
	}
	
	public void buildClient() {
		
		CompoundNBT tag = getVoxelData();
		
		if(tag.contains("species")) {
			setupFromNBT(tag);
			clientBuilt = true;
		} else {
			System.out.println("Error: No species tag has been set");
		}
		
		BlockBounds renderBounds = new BlockBounds(destroyData.cutPos);
		
		for(BlockPos absPos: Iterables.concat(destroyData.getPositions(BranchDestructionData.PosType.BRANCHES), destroyData.getPositions(BranchDestructionData.PosType.LEAVES))) {
			BlockState state = world.getBlockState(absPos);
			if(TreeHelper.isTreePart(state)) {
				world.setBlockState(absPos, DTRegistries.blockStates.air, 0);////The client needs to set it's blocks to air
				renderBounds.union(absPos);//Expand the re-render volume to include this block
			}
		}
		
		cleanupShellBlocks(destroyData);
		
		Minecraft.getInstance().worldRenderer.markBlockRangeForRenderUpdate(renderBounds.getMin().getX(), renderBounds.getMin().getY(), renderBounds.getMin().getZ(), renderBounds.getMax().getX(), renderBounds.getMax().getY(), renderBounds.getMax().getZ());//This forces the client to rerender the chunks
	}
	
	protected void cleanupShellBlocks(BranchDestructionData destroyData) {
		BlockPos cutPos = destroyData.cutPos;
		for(int i = 0; i < destroyData.getNumBranches(); i++) {
			if(destroyData.getBranchRadius(i) > 8) {
				BlockPos pos = destroyData.getBranchRelPos(i).add(cutPos);
				for(Surround dir: Surround.values()) {
					BlockPos dPos = pos.add(dir.getOffset());
					if(world.getBlockState(dPos).getBlock() == DTRegistries.trunkShellBlock) {
						world.removeBlock(dPos, false);
					}
				}
			}
		}
	}
	
	public AxisAlignedBB buildAABBFromDestroyData(BranchDestructionData destroyData) {
		
		normAABB = new AxisAlignedBB(BlockPos.ZERO);
		
		for(BlockPos relPos: destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, false)) {
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
	
	public Vector3d getGeomCenter() {
		return geomCenter;
	}
	
	public Vector3d getMassCenter() {
		return massCenter;
	}
	
	@Override
	public void setPosition(double x, double y, double z) {
		//This comes to the client as a packet from the server. But it doesn't set up the bounding box correctly
		this.setRawPosition(x, y, z);
		//This function is called by the Entity constructor during which normAABB hasn't yet been assigned.
		this.setBoundingBox(normAABB != null ? normAABB.offset(this.getPosX(), getPosY(), this.getPosZ()) : new AxisAlignedBB(BlockPos.ZERO));
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if(world.isRemote && !clientBuilt) {
			buildClient();
			if(!isAlive()) {
				return;
			}
		}
		
		if(!world.isRemote && firstUpdate) {
			updateNeighbors();
		}
		
		handleMotion();
		
		setBoundingBox(normAABB.offset(this.getPosX(), this.getPosY(), this.getPosZ()));
		
		if(shouldDie()) {
			dropPayLoad();
			onKillCommand();
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
		Iterables.concat(destroyData.getPositions(BranchDestructionData.PosType.BRANCHES), destroyData.getPositions(BranchDestructionData.PosType.LEAVES)).forEach(pos -> destroyed.add(pos));
		
		//Gather a list of all of the non-destroyed blocks surrounding each destroyed block
		for(BlockPos d: destroyed) {
			for(Direction dir: Direction.values()) {
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
		return DTConfigs.enableFallingTrees.get() ? destroyData.species.selectAnimationHandler(this) : AnimationHandlers.voidAnimationHandler;
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
		
		if(getDestroyData().cutDir == Direction.DOWN) {
			if(getMassCenter().y >= 1.0) {
				return AnimHandlerFall;
			} else {
				return AnimHandlerFling;
			}
		}
		
		return AnimHandlerDrop;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
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
	
	@OnlyIn(Dist.CLIENT)
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
		if (!worldIn.isRemote && !stack.isEmpty() && worldIn.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			ItemEntity entityitem = new ItemEntity(worldIn, (double)pos.getX() + 0.5F, (double)pos.getY() + 0.5F, (double)pos.getZ() + 0.5F, stack);
			entityitem.setVelocity(0,0,0);
			entityitem.setDefaultPickupDelay();
			worldIn.addEntity(entityitem);
		}
	}
	
	@Override
	protected void registerData() {
		getDataManager().register(voxelDataParameter, new CompoundNBT());
	}
	
	public void cleanupRootyDirt() {
		//Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
		if(!world.isRemote) {
			BlockPos rootPos = getDestroyData().cutPos.down();
			BlockState belowState = world.getBlockState(rootPos);
			
			if(TreeHelper.isRooty(belowState)) {
				RootyBlock rootyBlock = (RootyBlock) belowState.getBlock();
				rootyBlock.doDecay(world, rootPos, belowState, getDestroyData().species);
			}
		}
	}
	
	
	//This is shipped off to the clients
	public void setVoxelData(CompoundNBT tag) {
		setBoundingBox(buildAABBFromDestroyData(destroyData).offset(this.getPosX(), this.getPosY(), this.getPosZ()));
		getDataManager().set(voxelDataParameter, tag);
	}
	
	public CompoundNBT getVoxelData() {
		return getDataManager().get(voxelDataParameter);
	}
	
	@Override
	protected void readAdditional(CompoundNBT compound) {
		CompoundNBT vox = (CompoundNBT) compound.get("vox");
		setupFromNBT(vox);
		setVoxelData(vox);
		
		if(compound.contains("payload")) {
			ListNBT list = (ListNBT) compound.get("payload");
			
			Iterator<INBT> iter = list.iterator();
			while(iter.hasNext()) {
				INBT tag = iter.next();
				if(tag instanceof CompoundNBT) {
					CompoundNBT compTag = (CompoundNBT) tag;
					payload.add(ItemStack.read(compTag));
				}
			}
		}
		
	}
	
	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.put("vox", getVoxelData());
		
		if(!payload.isEmpty()) {
			ListNBT list = new ListNBT();
			
			for(ItemStack stack : payload) {
				list.add(stack.serializeNBT());
			}
			
			compound.put("payload", list);
		}
	}
	
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	public static EntityFallingTree dropTree(World world, BranchDestructionData destroyData, List<ItemStack> woodDropList, DestroyType destroyType) {
		//Spawn the appropriate item entities into the world
		if(!world.isRemote) {// Only spawn entities server side
			// Falling tree currently has severe rendering issues.
//			EntityFallingTree entity = new EntityFallingTree(DTRegistries.fallingTree, world).setData(destroyData, woodDropList, destroyType);
//			if(entity.isAlive()) {
//				world.addEntity(entity);
//			}
//			return entity;
		}
		
		return null;
	}
	
}
