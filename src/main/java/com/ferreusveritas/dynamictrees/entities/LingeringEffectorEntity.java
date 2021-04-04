package com.ferreusveritas.dynamictrees.entities;

import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.substances.GrowthSubstance;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class LingeringEffectorEntity extends Entity {

	private BlockPos blockPos;
	private ISubstanceEffect effect;

	public LingeringEffectorEntity(EntityType<?> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		this.blockPos = BlockPos.ZERO;
		this.effect = new GrowthSubstance();
	}

	public LingeringEffectorEntity(World world, BlockPos pos, ISubstanceEffect effect) {
		this(DTRegistries.lingeringEffector, world);
		this.maxUpStep = 1f;
		this.noPhysics = true;
		this.setBlockPos(pos);
		this.effect = effect;

		if(this.effect != null) {
			//Search for existing effectors with the same effect in the same place
			for(LingeringEffectorEntity effector : world.getEntitiesOfClass(LingeringEffectorEntity.class, new AxisAlignedBB(pos))) {
				if(effector.getBlockPos().equals(pos) && effector.getEffect().getName().equals(effect.getName())) {
					effector.kill();//Kill old effector if it's the same
				}
			}
		}
	}

	public void setBlockPos(BlockPos pos) {
		blockPos = pos;
		setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}

	public ISubstanceEffect getEffect() {
		return this.effect;
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	protected void readAdditionalSaveData(CompoundNBT compound) {}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) { }

	@Override
	public void tick() {
		super.tick();

		if(effect != null) {
			BlockState blockState = level.getBlockState(blockPos);

			if(blockState.getBlock() instanceof RootyBlock) {
				if(!effect.update(level, blockPos, tickCount)) {
					kill();
				}
			} else {
				kill();
			}
		}
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

//	@Override
//	public boolean isInRangeToRenderDist(double distance) {
//		return false; // Effectively make this entity invisible
//	}

}
