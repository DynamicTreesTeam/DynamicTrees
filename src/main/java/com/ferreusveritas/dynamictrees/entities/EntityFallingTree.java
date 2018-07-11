package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.List;

import com.ferreusveritas.dynamictrees.render.AnimationHandler;
import com.ferreusveritas.dynamictrees.render.AnimationHandlers;
import com.ferreusveritas.dynamictrees.render.FallingTreeModelCache;
import com.ferreusveritas.dynamictrees.render.ModelTracker;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;

import net.minecraft.entity.Entity;
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

public class EntityFallingTree extends Entity implements ModelTracker {
	
	public static final DataParameter<NBTTagCompound> voxelDataParameter = EntityDataManager.createKey(EntityFallingTree.class, DataSerializers.COMPOUND_TAG);
	
	//Not needed in client
	protected List<ItemStack> payload = new ArrayList<>();
	
	//Needed in client and server
	protected BranchDestructionData destroyData = new BranchDestructionData();
	protected Vec3d geomCenter = Vec3d.ZERO;
	protected Vec3d massCenter = Vec3d.ZERO;
	protected boolean clientBuilt = false;
	protected boolean firstUpdate = true;
	
	
	public static AnimationHandler AnimHandlerFall = AnimationHandlers.falloverAnimationHandler;
	public static AnimationHandler AnimHandlerDrop = AnimationHandlers.defaultAnimationHandler;
	public static AnimationHandler AnimHandlerFling = AnimationHandlers.defaultAnimationHandler;
	public static AnimationHandler AnimHandlerBlast = AnimationHandlers.defaultAnimationHandler;

	public AnimationHandler currentAnimationHandler = AnimationHandlers.voidAnimationHandler;
	
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
	public void setData(BranchDestructionData destroyData, List<ItemStack> payload) {
		this.destroyData = destroyData;
		BlockPos cutPos = destroyData.cutPos;
		this.payload = payload;
		
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
		
		setEntityBoundingBox(buildAABBFromDestroyData(destroyData));
		
		setVoxelData(buildVoxelData(destroyData));
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
		
		return tag;
	}
	
	public void buildClient() {
		
		NBTTagCompound tag = getVoxelData();
		
		if(tag.hasKey("species")) {
			destroyData = new BranchDestructionData(tag);
			geomCenter = new Vec3d(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
			massCenter = new Vec3d(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));
			setEntityBoundingBox(buildAABBFromDestroyData(destroyData));
			clientBuilt = true;
		}
	}
	
	public AxisAlignedBB buildAABBFromDestroyData(BranchDestructionData destroyData) {
		AxisAlignedBB aabb = new AxisAlignedBB(destroyData.cutPos);
		
		for(int i = 0; i < destroyData.getNumBranches(); i++) {
			aabb = aabb.union(new AxisAlignedBB(destroyData.cutPos.add(destroyData.getBranchRelPos(i))));
		}
		
		return aabb;
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
		double dx = x - this.posX;
		double dy = y - this.posY;
		double dz = z - this.posZ;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.setEntityBoundingBox(getEntityBoundingBox().offset(dx, dy, dz));
	}
	
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
		if(world.isRemote && !clientBuilt) {
			buildClient();
		}
		
		currentAnimationHandler = selectAnimationHandler();
		
		handleMotion();
		
		setEntityBoundingBox(getEntityBoundingBox().offset(motionX, motionY, motionZ));
		
		if(shouldDie()) {
			dropPayLoad();
			setDead();
			modelCleanup();
		}
	}
	
	protected AnimationHandler selectAnimationHandler() {
		if(currentAnimationHandler == AnimationHandlers.voidAnimationHandler) {
			
			if(getDestroyData().cutDir == EnumFacing.DOWN) {
				if(getMassCenter().y >= 1.0) {
					return AnimHandlerFall;
				} else {
					return AnimHandlerFling;
				}
			}

			return AnimHandlerDrop;
		}
		
		return currentAnimationHandler;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void modelCleanup() {
		FallingTreeModelCache.cleanupModels(world, this);
	}
	
	public void handleMotion() {
		if(firstUpdate) {
			currentAnimationHandler.initMotion(this);
			firstUpdate = false;
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
	
	@Override
	protected void entityInit() {
		getDataManager().register(voxelDataParameter, new NBTTagCompound());
	}
	
	public void setVoxelData(NBTTagCompound tag) {
		getDataManager().set(voxelDataParameter, tag);
	}
	
	public NBTTagCompound getVoxelData() {
		return getDataManager().get(voxelDataParameter);
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) { }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
}
