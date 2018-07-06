package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch.BlockItemStack;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch.BranchDestructionData;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class EntityFallingTree extends Entity {
	
	protected BranchDestructionData destroyData;
	protected List<ItemStack> payload = new ArrayList<>();
	protected Vec3d geomCenter = Vec3d.ZERO;
	protected Vec3d massCenter = Vec3d.ZERO;
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
		setSize(1.0f, 1.0f);
	}
	
	public void setData(BranchDestructionData destroyData, List<ItemStack> payload) {
		this.destroyData = destroyData;
		BlockPos cutPos = destroyData.cutPos;
		this.payload = payload;
		Map<BlockPos, IExtendedBlockState> stateMap = destroyData.destroyedBranches;
	
		this.posX = cutPos.getX() + 0.5;
		this.posY = cutPos.getY();
		this.posZ = cutPos.getZ() + 0.5;

		int numBlocks = stateMap.size();
		geomCenter = new Vec3d(0, 0, 0);
		AxisAlignedBB aabb = new AxisAlignedBB(cutPos);
		double totalMass = 0;
		
		//Calculate center of geometry, center of mass and bounding box, remap to relative coordinates
		for(Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
			BlockPos relPos = entry.getKey();
			BlockPos absPos = cutPos.add(relPos);
			
			aabb = aabb.union(new AxisAlignedBB(absPos));

			IExtendedBlockState exState = entry.getValue();
			int radius = 1;
			if(exState.getBlock() instanceof BlockBranch) {
				BlockBranch bbb = (BlockBranch) exState.getBlock();
				radius = bbb.getRadius(exState);
			}
			float mass = (radius * radius * 64) / 4096f;//Assume full height cuboids for simplicity
			totalMass += mass;
			
			Vec3d relVec = new Vec3d(relPos.getX(), relPos.getY(), relPos.getZ());
			geomCenter = geomCenter.add(relVec);
			massCenter = massCenter.add(relVec.scale(mass));
		}

		this.setEntityBoundingBox(aabb);
		geomCenter = geomCenter.scale(1.0 / numBlocks);
		massCenter = massCenter.scale(1.0 / totalMass);
		
		initMotion();
	}
	
	public BlockPos getCutPos() {
		return destroyData.cutPos;
	}
	
	public EnumFacing getCutDir() {
		return destroyData.cutDir;
	}
	
	public Vec3d getGeomCenter() {
		return geomCenter;
	}
	
	public Vec3d getMassCenter() {
		return massCenter;
	}
	
	public Map<BlockPos, IExtendedBlockState> getStateMap() {
		return destroyData.destroyedBranches;
	}
	
	@Override
	public void onEntityUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		
		handleMotion();
		
		setEntityBoundingBox(getEntityBoundingBox().offset(motionX, motionY, motionZ));
		
		if(ticksExisted > 30) {
			dropPayLoad();
			setDead();
		}
	}

	public void initMotion() {
		
	}
	
	public void handleMotion() {
		//motionY = 0.0;
		//motionY = 0.03;
		motionY -= 0.03;//Gravity
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		rotationYaw += 10;
	}
	
	public void dropPayLoad() {
		BlockPos pos = new BlockPos(posX, posY, posZ);
		payload.forEach(i -> Block.spawnAsEntity(world, pos, i));
		
		for(BlockItemStack bis : destroyData.leavesDrops) {
			BlockPos sPos = pos.add(bis.pos);
			EntityItem itemEntity = new EntityItem(world, sPos.getX() + 0.5, sPos.getY() + 0.5, sPos.getZ() + 0.5, bis.stack);
			world.spawnEntity(itemEntity);
		}
	}
	
	@Override
	protected void entityInit() { }
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) { }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
}
