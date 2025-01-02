package com.dtteam.dynamictrees.entity;

import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.block.soil.RootyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LingeringEffectorEntity extends Entity {// implements IEntityAdditionalSpawnData {

    private BlockPos blockPos;
    private SubstanceEffect effect;

    public LingeringEffectorEntity(EntityType<? extends LingeringEffectorEntity> entityTypeIn, Level level) {
        super(entityTypeIn, level);
        this.blockPos = BlockPos.ZERO;
    }

//    public LingeringEffectorEntity(Level level, BlockPos pos, SubstanceEffect effect) {
//        this(level);
//        this.setMaxUpStep(1f);
//        this.noPhysics = true;
//        this.setBlockPos(pos);
//        this.effect = effect;
//
//        if (this.effect != null) {
//            // Search for existing effectors with the same effect in the same place.
//            for (final LingeringEffectorEntity effector : level.getEntitiesOfClass(LingeringEffectorEntity.class, new AABB(pos))) {
//                if (effector.getEffect() != null && effector.getEffect().getName().equals(effect.getName())) {
//                    effector.kill(); // Kill old effector if it's the same.
//                }
//            }
//        }
//    }

    public static boolean treeHasEffectorForEffect(LevelAccessor level, BlockPos pos, SubstanceEffect effect) {
        for (final LingeringEffectorEntity effector : level.getEntitiesOfClass(LingeringEffectorEntity.class, new AABB(pos))) {
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

    public SubstanceEffect getEffect() {
        return this.effect;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

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

        final BlockState blockState = this.level().getBlockState(this.blockPos);

        if (blockState.getBlock() instanceof RootyBlock) {
            if (!this.effect.update(this.level(), this.blockPos, this.tickCount, blockState.getValue(RootyBlock.FERTILITY))) {
                this.kill();
            }
        } else {
            this.kill();
        }
    }

//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }
//
//    @Override
//    public void writeSpawnData(FriendlyByteBuf buffer) {
//        // We'll assume there aren't more than 128 lingering substance effects, so send a byte.
//        buffer.writeByte(this.effect == null ? -1 : LingeringSubstances.indexOf(this.effect.getClass()));
//    }
//
//    @Override
//    public void readSpawnData(FriendlyByteBuf additionalData) {
//        // We'll assume there aren't more than 128 lingering substance effects, so send a byte.
//        final byte index = additionalData.readByte();
//        this.effect = index < 0 ? null : LingeringSubstances.fromIndex(index).get();
//
//        if (this.effect != null && this.level() != null) {
//            this.effect.apply(this.level(), this.blockPos);
//        }
//    }

}
