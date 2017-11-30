package com.ferreusveritas.dynamictrees.entities;

import java.util.List;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.WorldDec;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class EntityLingeringEffector extends Entity {

	public BlockPos blockPos;
	public ISubstanceEffect effect;
	public boolean extended;
	private WorldDec world;
	
	public EntityLingeringEffector(WorldDec world, BlockPos pos, ISubstanceEffect effect) {
		super(world.getWorld());
		this.world = world;
		width = 1.0f;
		height = 1.0f;
		noClip = true;
		setBlockPos(pos);
		setEffect(effect);

		if(this.effect != null) {
			//Search for existing effectors with the same effect in the same place
			for(EntityLingeringEffector effector : (List<EntityLingeringEffector>)world.getWorld().getEntitiesWithinAABB(EntityLingeringEffector.class, pos.getAxisAlignedBB()) ) {
				if(effector.getBlockPos().equals(pos) && effector.getEffect().getName().equals(effect.getName())) {
					effector.setDead();//Kill old effector if it's the same
				}
			}
		}
	}

	public void setBlockPos(BlockPos pos) {
		blockPos = pos;
		setPosition(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
	}
	
	public BlockPos getBlockPos() {
		return blockPos;
	}
	
	public void setEffect(ISubstanceEffect effect) {
		this.effect = effect;
	}
	
	public ISubstanceEffect getEffect() {
		return this.effect;
	}
	
	@Override
	protected void entityInit() {}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(effect != null) {
			IBlockState blockState = world.getBlockState(blockPos);

			if(blockState.getBlock() instanceof BlockRootyDirt) {
				BlockRootyDirt rootyDirt = (BlockRootyDirt) blockState.getBlock();
				if(!effect.update(world, rootyDirt, blockPos, ticksExisted)) {
					setDead();
				}
			} else {
				setDead();
			}
		}	

	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return false;//Effectively make this entity invisible
	}
	
}
