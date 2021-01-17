package com.ferreusveritas.dynamictrees.entities;

import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.RootyBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class LingeringEffectorEntity extends Entity {

	@Override
	protected void registerData() {

	}

	@Override
	protected void readAdditional(@Nonnull CompoundNBT compound) {

	}

	@Override
	protected void writeAdditional(@Nonnull CompoundNBT compound) {

	}

	@Override
	public IPacket<?> createSpawnPacket() {
		return new SSpawnObjectPacket(this);
	}

	public BlockPos blockPos;
	public ISubstanceEffect effect;
	public boolean extended;

	public LingeringEffectorEntity(World world, BlockPos pos, ISubstanceEffect effect) {
		super(EntityType.AREA_EFFECT_CLOUD, world);
		stepHeight = 1f;
//		submergedHeight = 1f;
		noClip = true;
		setBlockPos(pos);
		setEffect(effect);

		if(this.effect != null) {
			//Search for existing effectors with the same effect in the same place
			for(LingeringEffectorEntity effector : world.getEntitiesWithinAABB(LingeringEffectorEntity.class, new AxisAlignedBB(pos))) {
				if(effector.getBlockPos().equals(pos) && effector.getEffect().getName().equals(effect.getName())) {
					effector.onKillCommand();//Kill old effector if it's the same
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
	public void read(CompoundNBT compound) { }

	@Override
	public void tick() {
		super.tick();

		if(effect != null) {
			BlockState blockState = world.getBlockState(blockPos);

			if(blockState.getBlock() instanceof RootyBlock) {
				if(!effect.update(world, blockPos, ticksExisted)) {
					onKillCommand();
				}
			} else {
				onKillCommand();
			}
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return false;//Effectively make this entity invisible
	}

}
