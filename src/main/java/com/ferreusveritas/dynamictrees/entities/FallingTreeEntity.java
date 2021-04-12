package com.ferreusveritas.dynamictrees.entities;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
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
import net.minecraft.client.renderer.model.BakedQuad;
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

import javax.annotation.Nonnull;
import java.util.*;

/**
 *
 * @author ferreusveritas
 *
 */
public class FallingTreeEntity extends Entity implements IModelTracker {

	public static final DataParameter<CompoundNBT> voxelDataParameter = EntityDataManager.defineId(FallingTreeEntity.class, DataSerializers.COMPOUND_TAG);
	
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

	//Stores color for tinted quads that aren't the leaves
	protected Map<BakedQuad, Integer> quadTints = new HashMap<>();

	public enum DestroyType {
		VOID,
		HARVEST,
		BLAST,
		FIRE,
		ROOT
	}
	
	public FallingTreeEntity(World world) {
		super(DTRegistries.FALLING_TREE, world);
	}

	public FallingTreeEntity(EntityType<? extends FallingTreeEntity> type, World world) {
		super(type, world);
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
	public FallingTreeEntity setData(BranchDestructionData destroyData, List<ItemStack> payload, DestroyType destroyType) {
		this.destroyData = destroyData;
		if(destroyData.getNumBranches() == 0) { //If the entity contains no branches there's no reason to create it at all
			System.err.println("Warning: Tried to create a EntityFallingTree with no branch blocks. This shouldn't be possible.");
			new Exception().printStackTrace();
			kill();
			return this;
		}
		BlockPos cutPos = destroyData.cutPos;
		this.payload = payload;
		this.destroyType = destroyType;
		this.onFire = destroyType == DestroyType.FIRE;

		this.setPosRaw(cutPos.getX() + 0.5, cutPos.getY(), cutPos.getZ() + 0.5);

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
			kill();
		}
		destroyType = DestroyType.values()[tag.getInt("destroytype")];
		geomCenter = new Vector3d(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
		massCenter = new Vector3d(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));
		buildAABBFromDestroyData(destroyData);
		setBoundingBox(normAABB.move(this.getX(), this.getY(), this.getZ()));
		onFire = tag.getBoolean("onfire");
	}

	public Map<BakedQuad, Integer> getQuadTints (){
		return quadTints;
	}
	public void addTintedQuad (int tint, BakedQuad quad){
		quadTints.put(quad, tint);
	}
	public void addTintedQuads (int tint, BakedQuad... quads){
		for (BakedQuad quad : quads)
			addTintedQuad(tint, quad);
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
			BlockState state = level.getBlockState(absPos);
			if(TreeHelper.isTreePart(state)) {
				level.setBlock(absPos, DTRegistries.BLOCK_STATES.AIR, 0);////The client needs to set it's blocks to air
				renderBounds.union(absPos);//Expand the re-render volume to include this block
			}
		}
		
		cleanupShellBlocks(destroyData);
		
		Minecraft.getInstance().levelRenderer.setBlocksDirty(renderBounds.getMin().getX(), renderBounds.getMin().getY(), renderBounds.getMin().getZ(), renderBounds.getMax().getX(), renderBounds.getMax().getY(), renderBounds.getMax().getZ());//This forces the client to rerender the chunks
	}
	
	protected void cleanupShellBlocks(BranchDestructionData destroyData) {
		BlockPos cutPos = destroyData.cutPos;
		for(int i = 0; i < destroyData.getNumBranches(); i++) {
			if(destroyData.getBranchRadius(i) > 8) {
				BlockPos pos = destroyData.getBranchRelPos(i).offset(cutPos);
				for(Surround dir: Surround.values()) {
					BlockPos dPos = pos.offset(dir.getOffset());
					if(level.getBlockState(dPos).getBlock() == DTRegistries.TRUNK_SHELL) {
						level.removeBlock(dPos, false);
					}
				}
			}
		}
	}
	
	public AxisAlignedBB buildAABBFromDestroyData(BranchDestructionData destroyData) {
		
		normAABB = new AxisAlignedBB(BlockPos.ZERO);
		
		for(BlockPos relPos: destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, false)) {
			normAABB = normAABB.minmax(new AxisAlignedBB(relPos));
		}
		
		//Adjust the bounding box to account for the tree falling over
		double height = normAABB.maxY - normAABB.minY;
		double width = MathHelper.absMax(normAABB.maxX - normAABB.minX, normAABB.maxZ - normAABB.minZ);
		double grow = Math.max(0, height - (width / 2) ) + 2;
		normAABB = normAABB.inflate(grow + 4, 4, grow + 4);
		
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
	public void setPos(double x, double y, double z) {
		//This comes to the client as a packet from the server. But it doesn't set up the bounding box correctly
		this.setPosRaw(x, y, z);
		//This function is called by the Entity constructor during which normAABB hasn't yet been assigned.
		this.setBoundingBox(normAABB != null ? normAABB.move(x, y, z) : new AxisAlignedBB(BlockPos.ZERO));
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.level.isClientSide && !this.clientBuilt) {
			this.buildClient();
			if (!isAlive()) {
				return;
			}
		}
		
		if (!this.level.isClientSide && this.firstUpdate) {
			this.updateNeighbors();
		}
		
		this.handleMotion();
		
		this.setBoundingBox(normAABB.move(this.getX(), this.getY(), this.getZ()));
		
		if (this.shouldDie()) {
			this.dropPayLoad();
			this.kill();
			this.modelCleanup();
		}
		
		this.firstUpdate = false;
	}
	
	/**
	 * This is run server side to update all of the neighbors
	 */
	protected void updateNeighbors() {
		HashSet<BlockPos> destroyed = new HashSet<>();
		HashSet<BlockPos> toUpdate = new HashSet<>();
		
		//Gather a set of all of the block positions that were recently destroyed
		Iterables.concat(destroyData.getPositions(BranchDestructionData.PosType.BRANCHES), destroyData.getPositions(BranchDestructionData.PosType.LEAVES)).forEach(destroyed::add);
		
		//Gather a list of all of the non-destroyed blocks surrounding each destroyed block
		for(BlockPos d: destroyed) {
			for(Direction dir: Direction.values()) {
				BlockPos dPos = d.relative(dir);
				if(!destroyed.contains(dPos)) {
					toUpdate.add(dPos);
				}
			}
		}
		
		//Update each of the blocks that need to be updated
		toUpdate.forEach(pos -> level.neighborChanged(pos, Blocks.AIR, pos));
	}
	
	protected IAnimationHandler selectAnimationHandler() {
		return DTConfigs.ENABLE_FALLING_TREES.get() ? destroyData.species.selectAnimationHandler(this) : AnimationHandlers.voidAnimationHandler;
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
		ModelTrackerCacheEntityFallingTree.cleanupModels(level, this);
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
		if(!level.isClientSide) {
			currentAnimationHandler.dropPayload(this);
		}
	}
	
	public boolean shouldDie() {
		return tickCount > 20 && currentAnimationHandler.shouldDie(this); //Give the entity 20 ticks to receive it's data from the server.
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
	 * @param entity The {@link FallingTreeEntity} object.
	 */
	public static void standardDropLogsPayload(FallingTreeEntity entity) {
		World world = entity.level;
		if(!world.isClientSide) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getPayload().forEach(i -> spawnItemAsEntity(world, cutPos, i));
		}
	}
	
	public static void standardDropLeavesPayLoad(FallingTreeEntity entity) {
		World world = entity.level;
		if(!world.isClientSide) {
			BlockPos cutPos = entity.getDestroyData().cutPos;
			entity.getDestroyData().leavesDrops.forEach(bis -> Block.popResource(world, cutPos.offset(bis.pos), bis.stack));
		}
	}
	
	/**
	 * Same as Block.spawnAsEntity only this arrests the entityItem's random motion. Useful for CC turtles to pick up the loot.
	 */
	public static void spawnItemAsEntity(World worldIn, BlockPos pos, ItemStack stack) {
		if (!worldIn.isClientSide && !stack.isEmpty() && worldIn.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !worldIn.restoringBlockSnapshots) { // do not drop items while restoring blockstates, prevents item dupe
			ItemEntity entityitem = new ItemEntity(worldIn, (double)pos.getX() + 0.5F, (double)pos.getY() + 0.5F, (double)pos.getZ() + 0.5F, stack);
			entityitem.lerpMotion(0,0,0);
			entityitem.setDefaultPickUpDelay();
			worldIn.addFreshEntity(entityitem);
		}
	}
	
	@Override
	protected void defineSynchedData() {
		getEntityData().define(voxelDataParameter, new CompoundNBT());
	}
	
	public void cleanupRootyDirt() {
		// Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
		if (!this.level.isClientSide) {
			final BlockPos rootPos = getDestroyData().cutPos.below();
			final BlockState belowState = this.level.getBlockState(rootPos);
			
			if (TreeHelper.isRooty(belowState)) {
				final RootyBlock rootyBlock = (RootyBlock) belowState.getBlock();
				rootyBlock.doDecay(this.level, rootPos, belowState, getDestroyData().species);
			}
		}
	}
	
	
	//This is shipped off to the clients
	public void setVoxelData(CompoundNBT tag) {
		setBoundingBox(buildAABBFromDestroyData(destroyData).move(this.getX(), this.getY(), this.getZ()));
		getEntityData().set(voxelDataParameter, tag);
	}
	
	public CompoundNBT getVoxelData() {
		return getEntityData().get(voxelDataParameter);
	}
	
	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {
		CompoundNBT vox = (CompoundNBT) compound.get("vox");
		setupFromNBT(vox);
		setVoxelData(vox);
		
		if (compound.contains("payload")) {
			final ListNBT nbtList = (ListNBT) compound.get("payload");

			for (INBT tag : Objects.requireNonNull(nbtList)) {
				if(tag instanceof CompoundNBT) {
					CompoundNBT compTag = (CompoundNBT) tag;
					this.payload.add(ItemStack.of(compTag));
				}
			}
		}
		
	}
	
	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		compound.put("vox", getVoxelData());
		
		if(!payload.isEmpty()) {
			ListNBT list = new ListNBT();
			
			for(ItemStack stack : payload) {
				list.add(stack.serializeNBT());
			}
			
			compound.put("payload", list);
		}
	}

	@Nonnull
	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	public static FallingTreeEntity dropTree(World world, BranchDestructionData destroyData, List<ItemStack> woodDropList, DestroyType destroyType) {
		//Spawn the appropriate item entities into the world
		if(!world.isClientSide) {// Only spawn entities server side
			// Falling tree currently has severe rendering issues.
			FallingTreeEntity entity = new FallingTreeEntity(world).setData(destroyData, woodDropList, destroyType);
			if(entity.isAlive()) {
				world.addFreshEntity(entity);
			}
			return entity;
		}
		
		return null;
	}
	
}
