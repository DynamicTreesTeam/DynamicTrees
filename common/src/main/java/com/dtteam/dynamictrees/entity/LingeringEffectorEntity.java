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

import javax.annotation.Nullable;

public class LingeringEffectorEntity extends Entity {// implements IEntityAdditionalSpawnData {

    public static final EntityDataAccessor<CompoundTag> effectorDataParameter = SynchedEntityData.defineId(LingeringEffectorEntity.class, EntityDataSerializers.COMPOUND_TAG);

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
                    effector.kill(); // Kill old effector if it's the same.
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

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(effectorDataParameter, new CompoundTag());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        CompoundTag effector = (CompoundTag) tag.get("effector");
        if (effector != null)
            setEffectorData(effector);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("effector", getEffectorData());
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
        blockPos = new BlockPos(tag.getInt("posx"), tag.getInt("posy"), tag.getInt("posz"));
        effect = LingeringSubstances.fromIndex(tag.getInt("effect")).get();
        getEntityData().set(effectorDataParameter, tag);
    }

    public CompoundTag getEffectorData() {
        return getEntityData().get(effectorDataParameter);
    }

    private byte invalidTicks = 0;

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() && !clientBuilt){
            setEffectorData(getEffectorData());
            clientBuilt = true;
        }

        if (this.effect == null) {
            // If effect hasn't been set for 20 ticks then kill the entity.
            if (++this.invalidTicks > 20) {
                this.kill();
            }
            return;
        }

        if (this.blockPos == null) return;

        final BlockState blockState = this.level().getBlockState(this.blockPos);

        if (blockState.getBlock() instanceof SoilBlock) {
            if (!this.effect.update(this.level(), this.blockPos, this.tickCount, blockState.getValue(SoilBlock.FERTILITY))) {
                this.kill();
            }
        } else {
            this.kill();
        }
    }

}
