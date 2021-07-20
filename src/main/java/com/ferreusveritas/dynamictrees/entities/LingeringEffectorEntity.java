package com.ferreusveritas.dynamictrees.entities;

import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.substances.LingeringSubstances;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class LingeringEffectorEntity extends Entity implements IEntityAdditionalSpawnData {

	private BlockPos blockPos;
	private ISubstanceEffect effect;

	public LingeringEffectorEntity(EntityType<? extends LingeringEffectorEntity> entityTypeIn, World worldIn) {
		super(entityTypeIn, worldIn);
		this.blockPos = BlockPos.ZERO;
	}

	@SuppressWarnings("unused")
	private LingeringEffectorEntity(World world) {
		super(DTRegistries.LINGERING_EFFECTOR, world);
	}

	public LingeringEffectorEntity(World world, BlockPos pos, ISubstanceEffect effect) {
		this(DTRegistries.LINGERING_EFFECTOR, world);
		this.maxUpStep = 1f;
		this.noPhysics = true;
		this.setBlockPos(pos);
		this.effect = effect;

		if (this.effect != null) {
			// Search for existing effectors with the same effect in the same place.
			for (final LingeringEffectorEntity effector : world.getEntitiesOfClass(LingeringEffectorEntity.class, new AxisAlignedBB(pos))) {
				if (effector.getEffect() != null && effector.getEffect().getName().equals(effect.getName())) {
					effector.kill(); // Kill old effector if it's the same.
				}
			}
		}
	}

	public static boolean treeHasEffectorForEffect(IWorld world, BlockPos pos, ISubstanceEffect effect) {
		for (final LingeringEffectorEntity effector : world.getEntitiesOfClass(LingeringEffectorEntity.class, new AxisAlignedBB(pos))) {
			if (effector.getEffect() != null && effector.getEffect().getName().equals(effect.getName())) {
				return true;
			}
		}
		return false;
	}

	public void setBlockPos(BlockPos pos) {
		this.blockPos = pos;
		setPos(this.blockPos.getX() + 0.5, this.blockPos.getY(), this.blockPos.getZ() + 0.5);
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

	private byte invalidTicks = 0;

	@Override
	public void tick() {
		super.tick();

		if (this.effect == null) {
			// If effect hasn't been set for 20 ticks then kill the entity.
			if (++this.invalidTicks > 20) {
				this.kill();
			}
			return;
		}

		final BlockState blockState = this.level.getBlockState(this.blockPos);

		if (blockState.getBlock() instanceof RootyBlock) {
			if (!this.effect.update(this.level, this.blockPos, this.tickCount, blockState.getValue(RootyBlock.FERTILITY))) {
				this.kill();
			}
		} else {
			this.kill();
		}
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		// We'll assume there aren't more than 128 lingering substance effects, so send a byte.
		buffer.writeByte(this.effect == null ? -1 : LingeringSubstances.indexOf(this.effect.getClass()));
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		// We'll assume there aren't more than 128 lingering substance effects, so send a byte.
		final byte index = additionalData.readByte();
		this.effect = index < 0 ? null : LingeringSubstances.fromIndex(index).get();

		if (this.effect != null && this.level != null) {
			this.effect.apply(this.level, this.blockPos);
		}
	}

}
