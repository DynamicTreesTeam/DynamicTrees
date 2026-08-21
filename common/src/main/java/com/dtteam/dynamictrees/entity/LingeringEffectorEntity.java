package com.dtteam.dynamictrees.entity;

import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.systems.substance.LingeringSubstances;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class LingeringEffectorEntity extends Entity {
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
// implements IEntityAdditionalSpawnData {

    public static final EntityDataAccessor<String> effectorDataParameter = SynchedEntityData.defineId(LingeringEffectorEntity.class, EntityDataSerializers.STRING);

    private BlockPos blockPos;
    private SubstanceEffect effect;
    protected boolean clientBuilt = false;

    public LingeringEffectorEntity(EntityType<? extends LingeringEffectorEntity> entityTypeIn, Level level) {
        super(entityTypeIn, level);
    }

    public void setData (Level level, BlockPos pos, SubstanceEffect effect){
        this.noPhysics = true;
        this.setBlockPos(pos);
        this.effect = effect;

        if (this.effect != null) {
            // Search for existing effectors with the same effect in the same place.
            for (final LingeringEffectorEntity effector : level.getEntitiesOfClass(LingeringEffectorEntity.class, new AABB(pos))) {
                if (effector.getEffect() != null && effector.getEffect().getName().equals(effect.getName())) {
                    effector.discard(); // Kill old effector if it's the same.
                }
            }
        }

        setEffectorData(buildEffectorData(pos, effect));
    }

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

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(effectorDataParameter, "");
    }

    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        input.read("effector", CompoundTag.CODEC).ifPresent(this::setEffectorData);
    }

    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        output.store("effector", CompoundTag.CODEC, getEffectorData());
    }

    public CompoundTag buildEffectorData(BlockPos pos, @Nullable SubstanceEffect substance) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("posx", pos.getX());
        tag.putInt("posy", pos.getY());
        tag.putInt("posz", pos.getZ());
        tag.putInt("effect", substance == null ? 0 : LingeringSubstances.indexOf(substance.getClass()));

        return tag;
    }

    public void setEffectorData(CompoundTag tag) {
        blockPos = new BlockPos(tag.getIntOr("posx", 0), tag.getIntOr("posy", 0), tag.getIntOr("posz", 0));
        effect = LingeringSubstances.fromIndex(tag.getIntOr("effect", 0)).get();
        getEntityData().set(effectorDataParameter, tag.toString());
    }

    public CompoundTag getEffectorData() {
        try {
            String snbt = getEntityData().get(effectorDataParameter);
            return snbt == null || snbt.isEmpty() ? new CompoundTag() : net.minecraft.nbt.TagParser.parseCompoundFully(snbt);
        } catch (Exception e) {
            return new CompoundTag();
        }
    }

    private byte invalidTicks = 0;

    public void tick() {
        super.tick();

        if (level().isClientSide() && !clientBuilt) {
            String snbt = getEntityData().get(effectorDataParameter);
            if (snbt != null && !snbt.isEmpty()) {
                setEffectorData(getEffectorData());
                clientBuilt = true;
            }
        }

        if (this.effect == null) {
            // If effect hasn't been set for 20 ticks then kill the entity.
            if (++this.invalidTicks > 20) {
                this.discard();
            }
            return;
        }

        if (this.blockPos == null) return;

        final BlockState blockState = this.level().getBlockState(this.blockPos);

        if (blockState.getBlock() instanceof SoilBlock) {
            if (!this.effect.update(this.level(), this.blockPos, this.tickCount, blockState.getValue(SoilBlock.FERTILITY))) {
                this.discard();
            }
        } else {
            this.discard();
        }
    }

}
