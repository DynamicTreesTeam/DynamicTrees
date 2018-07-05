package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class EntityFallingTree extends Entity {
	
	protected Species species;
	protected BlockPos cutPos = BlockPos.ORIGIN;
	protected List<ItemStack> payload = new ArrayList<>();
	protected Map<BlockPos, IExtendedBlockState> stateMap = new HashMap<>();
	protected Vec3d geomCenter;
	protected Vec3d massCenter;
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
		setSize(1.0f, 1.0f);
	}
	
	public void setData(Species species, BlockPos cutPos, List<ItemStack> payload, Map<BlockPos, IExtendedBlockState> stateMap) {
		this.species = species;
		this.cutPos = cutPos;
		this.payload = payload;
		this.stateMap = stateMap;
		
		this.motionY = 0.5;
		
		this.posX = cutPos.getX() + 0.5;
		this.posY = cutPos.getY();
		this.posZ = cutPos.getZ() + 0.5;
		
		geomCenter = new Vec3d(0, 0, 0);
		AxisAlignedBB aabb = new AxisAlignedBB(cutPos);
		int numBlocks = stateMap.size();

		//Calculate center of geometry and bounding box
		for(Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
			BlockPos absPos = entry.getKey();
			BlockPos relPos = absPos.subtract(getCutPos()); //Get the relative position of the block
			aabb = aabb.union(new AxisAlignedBB(absPos));
			geomCenter.addVector(relPos.getX(), relPos.getY(), relPos.getZ());
		}
		
		geomCenter = new Vec3d(geomCenter.x / numBlocks, geomCenter.y / numBlocks, geomCenter.z / numBlocks);
		massCenter = geomCenter;
		this.setEntityBoundingBox(aabb);
		
		//Calculate center of mass
		for(Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
			BlockPos relPos = entry.getKey().subtract(getCutPos()); //Get the relative position of the block
			IExtendedBlockState exState = entry.getValue();
			int radius = 1;
			if(exState.getBlock() instanceof BlockBranchBasic) {
				BlockBranchBasic bbb = (BlockBranchBasic) exState.getBlock();
				radius = bbb.getRawRadius(exState); //This needs to be better
			}
			float weight = (radius * radius * radius) / 512f;
			massCenter.addVector(relPos.getX() * weight, relPos.getY() * weight, relPos.getZ() * weight);
		}
		
	}
	
	public BlockPos getCutPos() {
		return cutPos;
	}

	public Vec3d getGeomCenter() {
		return geomCenter;
	}
	
	public Vec3d getMassCenter() {
		return massCenter;
	}
	
	public Map<BlockPos, IExtendedBlockState> getStateMap() {
		return stateMap;
	}
	
	@Override
	public void onEntityUpdate() {
		//super.onEntityUpdate();
		
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
		
        //motionY = 0.025;
        //motionY = 0.00;
        
		motionY -= 0.03;//Gravity
        posX += motionX;
		posY += motionY;
		posZ += motionZ;
		rotationYaw += motionY * 10;
		
        //this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		setEntityBoundingBox(getEntityBoundingBox().offset(motionX, motionY, motionZ));
		
		if(ticksExisted > 30) {
			BlockPos pos = new BlockPos(posX, posY, posZ);
			payload.forEach(i -> Block.spawnAsEntity(world, pos, i));
			setDead();
		}
		
		//setPosition(posX, posY, posZ);
	}
	
    /*public void setPosition(double x, double y, double z) {
    	double dx = this.posX - x;
    	double dy = this.posY - y;
    	double dz = this.posZ - z;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.setEntityBoundingBox( getEntityBoundingBox().offset(dx + dx, dy, dz) );
    }*/
	
	@Override
	protected void entityInit() { }
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) { }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
}
