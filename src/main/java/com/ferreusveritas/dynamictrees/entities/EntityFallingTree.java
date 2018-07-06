package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
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
	protected Vec3d geomCenter = Vec3d.ZERO;
	protected Vec3d massCenter = Vec3d.ZERO;
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
		setSize(1.0f, 1.0f);
	}
	
	public void setData(Species species, BlockPos cutPos, List<ItemStack> payload, Map<BlockPos, IExtendedBlockState> inStateMap) {
		this.species = species;
		this.cutPos = cutPos;
		this.payload = payload;
		this.stateMap = inStateMap;
		
		this.motionY = 0.5;
		
		this.posX = cutPos.getX() + 0.5;
		this.posY = cutPos.getY();
		this.posZ = cutPos.getZ() + 0.5;
		
		geomCenter = new Vec3d(0, 0, 0);
		AxisAlignedBB aabb = new AxisAlignedBB(cutPos);

		Map<BlockPos, IExtendedBlockState> relStateMap = new HashMap<>();
		
		//Calculate center of geometry and bounding box, remap to relative coordinates
		for(Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
			BlockPos absPos = entry.getKey();
			BlockPos relPos = absPos.subtract(cutPos); //Get the relative position of the block
			relStateMap.put(relPos, entry.getValue());
			
			aabb = aabb.union(new AxisAlignedBB(absPos));
			geomCenter = geomCenter.addVector(relPos.getX(), relPos.getY(), relPos.getZ());
		}
		
		stateMap = relStateMap;//The state map is now in relative coordinates
		int numBlocks = stateMap.size();
		geomCenter = geomCenter.scale(1.0 / numBlocks);
		this.setEntityBoundingBox(aabb);
		
		double totalMass = 0;
		Vec3d totalMassLen = new Vec3d(0, 0, 0);
		
		//Calculate center of mass
		for(Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
			BlockPos pos = entry.getKey(); //Get the relative position of the block
			IExtendedBlockState exState = entry.getValue();
			int radius = 1;
			if(exState.getBlock() instanceof BlockBranch) {
				BlockBranch bbb = (BlockBranch) exState.getBlock();
				radius = bbb.getRadius(exState);
			}
			float mass = (radius * radius * 64) / 4096f;//Assume full height cuboids for simplicity
			
			totalMass += mass;
			totalMassLen = totalMassLen.addVector(pos.getX() * mass, pos.getY() * mass, pos.getZ() * mass);
		}
		
		massCenter = totalMassLen.scale(1 / totalMass);
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
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		
		//motionY = 0.0;
		//motionY = 0.03;
		motionY -= 0.03;//Gravity
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		rotationYaw += 10;
		
		setEntityBoundingBox(getEntityBoundingBox().offset(motionX, motionY, motionZ));
		
		if(ticksExisted > 30) {
			BlockPos pos = new BlockPos(posX, posY, posZ);
			payload.forEach(i -> Block.spawnAsEntity(world, pos, i));
			setDead();
		}
	}
	
	@Override
	protected void entityInit() { }
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) { }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
}
