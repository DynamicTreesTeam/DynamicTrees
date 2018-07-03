package com.ferreusveritas.dynamictrees.entities;

import java.util.HashMap;
import java.util.List;

import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;

public class EntityFallingTree extends Entity {
	
	protected Species species;
	protected BlockPos cutPos;
	protected List<ItemStack> payload;

	public HashMap<BlockPos, IExtendedBlockState> stateMap = new HashMap<>();
	
	public EntityFallingTree(World worldIn) {
		super(worldIn);
		this.species = null;
		this.payload = null;
		cutPos = BlockPos.ORIGIN;//TODO: Obviously not right
	}
	
	public void setData(Species species, BlockPos cutPos, List<ItemStack> payload) {
		this.species = species;
		this.cutPos = cutPos;
		this.payload = payload;
	}
	
	public BlockPos getCutPos() {
		return cutPos;
	}
	
	@Override
	public void onEntityUpdate() {
		super.onEntityUpdate();

		System.out.println("I exist");
		
		if(ticksExisted > 10) {
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
