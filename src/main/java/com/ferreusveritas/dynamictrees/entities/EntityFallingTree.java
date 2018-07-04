package com.ferreusveritas.dynamictrees.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.BlockBranchBasic;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class EntityFallingTree extends Entity {
	
	protected Species species;
	protected BlockPos cutPos = BlockPos.ORIGIN;
	protected List<ItemStack> payload = new ArrayList<>();
	protected Map<BlockPos, IExtendedBlockState> stateMap = new HashMap<>();
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
	}
	
	public void setData(Species species, BlockPos cutPos, List<ItemStack> payload, Map<BlockPos, IExtendedBlockState> stateMap) {
		this.species = species;
		this.cutPos = cutPos;
		this.payload = payload;
		this.stateMap = stateMap;
		
		this.motionY = 0.5;
		
		setPosition(cutPos.getX() + 0.5, cutPos.getY() + 0.5, cutPos.getZ() + 0.5);
	}

	public Vec3d calcCenterOfMass() {

		Vec3d c = new Vec3d(0, 0, 0);
		
		for( Map.Entry<BlockPos, IExtendedBlockState> entry : stateMap.entrySet()) {
			BlockPos relPos = entry.getKey().subtract(getCutPos()); //Get the relative position of the block
			IExtendedBlockState exState = entry.getValue();
			
			int radius = 1;
			
			if(exState.getBlock() instanceof BlockBranchBasic) {
				BlockBranchBasic bbb = (BlockBranchBasic) exState.getBlock();
				radius = bbb.getRawRadius(exState); //This needs to be better
			}
			
			float weight = (radius * radius * radius) / 512f;
			
			c.addVector(relPos.getX() + 0.5, relPos.getY() + 0.5, relPos.getZ() + 0.5);
		}
		
		int numBlocks = stateMap.size();		
		return new Vec3d(c.x / numBlocks, c.y / numBlocks, c.z / numBlocks);
	}
	
	public BlockPos getCutPos() {
		return cutPos;
	}
	
	public Map<BlockPos, IExtendedBlockState> getStateMap() {
		return stateMap;
	}
	
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();
		
		motionY -= 0.03;
		posY += motionY;
		rotationYaw += motionY * 10;
		
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
