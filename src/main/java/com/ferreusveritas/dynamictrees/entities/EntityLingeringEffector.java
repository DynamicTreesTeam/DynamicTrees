package com.ferreusveritas.dynamictrees.entities;

import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityLingeringEffector extends Entity {

	public EntityLingeringEffector(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
	}

	@Override
	protected void registerData() {

	}

	@Override
	protected void readAdditional(CompoundNBT compound) {

	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {

	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return null;
	}

//	public BlockPos blockPos;
//	public ISubstanceEffect effect;
//	public boolean extended;
//
//	public EntityLingeringEffector(World world, BlockPos pos, ISubstanceEffect effect) {
//		super(world);
//		width = 1.0f;
//		height = 1.0f;
//		noClip = true;
//		setBlockPos(pos);
//		setEffect(effect);
//
//		if(this.effect != null) {
//			//Search for existing effectors with the same effect in the same place
//			for(EntityLingeringEffector effector : world.getEntitiesWithinAABB(EntityLingeringEffector.class, new AxisAlignedBB(pos))) {
//				if(effector.getBlockPos().equals(pos) && effector.getEffect().getName().equals(effect.getName())) {
//					effector.setDead();//Kill old effector if it's the same
//				}
//			}
//		}
//	}
//
//	public void setBlockPos(BlockPos pos) {
//		blockPos = pos;
//		setPosition(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
//	}
//
//	public BlockPos getBlockPos() {
//		return blockPos;
//	}
//
//	public void setEffect(ISubstanceEffect effect) {
//		this.effect = effect;
//	}
//
//	public ISubstanceEffect getEffect() {
//		return this.effect;
//	}
//
//	@Override
//	protected void entityInit() {}
//
//	@Override
//	protected void readEntityFromNBT(NBTTagCompound compound) {}
//
//	@Override
//	protected void writeEntityToNBT(NBTTagCompound compound) {}
//
//	@Override
//	public void onUpdate() {
//		super.onUpdate();
//
//		if(effect != null) {
//			BlockState blockState = world.getBlockState(blockPos);
//
//			if(blockState.getBlock() instanceof BlockRooty) {
//				if(!effect.update(world, blockPos, ticksExisted)) {
//					setDead();
//				}
//			} else {
//				setDead();
//			}
//		}
//
//	}
//
//	@Override
//	public boolean shouldRenderInPass(int pass) {
//		return false;//Effectively make this entity invisible
//	}
//
}
