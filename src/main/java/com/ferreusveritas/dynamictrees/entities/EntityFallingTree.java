package com.ferreusveritas.dynamictrees.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class EntityFallingTree extends Entity {
	
	protected Species species;
	protected BlockPos cutPos = BlockPos.ORIGIN;
	protected List<ItemStack> payload;
	protected Map<BlockPos, IExtendedBlockState> stateMap = new HashMap<>();
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
	}
	
	public void setData(Species species, BlockPos cutPos, List<ItemStack> payload, Map<BlockPos, IExtendedBlockState> stateMap) {
		this.species = species;
		this.cutPos = cutPos;
		this.payload = payload;
		this.stateMap = stateMap;
		
		setPosition(cutPos.getX() + 0.5, cutPos.getY() + 0.5, cutPos.getZ() + 0.5);
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

		System.out.println("I'm updating");
		
		if(ticksExisted > 10) {
			payload.forEach(i -> Block.spawnAsEntity(world, cutPos, i));
			setDead();
			System.out.println("I died");
		}
	}
	
	@Override
	protected void entityInit() { }
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) { }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
}
