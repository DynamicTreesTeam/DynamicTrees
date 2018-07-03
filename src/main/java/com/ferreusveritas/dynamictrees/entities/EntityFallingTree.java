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
	protected List<ItemStack> payload;
	
	public BlockPos cutPos;
	public HashMap<BlockPos, IExtendedBlockState> stateMap = new HashMap<>();
	
	public EntityFallingTree(World worldIn, Species species, List<ItemStack> payload) {
		super(worldIn);
		this.species = species;
		this.payload = payload;
	}
	
	@Override
	protected void entityInit() { }
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) { }
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) { }
	
}
