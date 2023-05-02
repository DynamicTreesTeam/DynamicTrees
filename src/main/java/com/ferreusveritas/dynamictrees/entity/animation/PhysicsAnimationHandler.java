package com.ferreusveritas.dynamictrees.entity.animation;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

public class PhysicsAnimationHandler implements AnimationHandler {
    @Override
    public String getName() {
        return "physics";
    }

    static class HandlerData extends DataAnimationHandler {
        float rotYaw = 0;
        float rotPit = 0;
        boolean endSoundPlayed = false;
        SoundInstance fallingSoundInstance;
    }

    HandlerData getData(FallingTreeEntity entity) {
        return entity.dataAnimationHandler instanceof HandlerData ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
    }

    protected void playEndSound(FallingTreeEntity entity, boolean onWater){
        if (!getData(entity).endSoundPlayed && !entity.level.isClientSide()){
            SoundInstance fallingInstance = getData(entity).fallingSoundInstance;
            if (fallingInstance != null)
                Minecraft.getInstance().getSoundManager().stop(fallingInstance);
            Species species = entity.getSpecies();
            SoundEvent sound = species.getFallingBranchEndSound(entity.getVolume(), entity.hasLeaves(), onWater);
            entity.playSound(sound, species.getFallingBranchPitch(entity.getVolume()), 1);
            getData(entity).endSoundPlayed = true;
        }
    }

    @Override
    public void initMotion(FallingTreeEntity entity) {
        entity.dataAnimationHandler = new HandlerData();
        final BlockPos cutPos = entity.getDestroyData().cutPos;

        //playStartSound(entity);

        final long seed = entity.level.random.nextLong();
        final Random random = new Random(seed ^ (((long) cutPos.getX()) << 32 | ((long) cutPos.getZ())));
        final float mass = entity.getDestroyData().woodVolume.getVolume();
        final float inertialMass = Mth.clamp(mass, 1, 3);
        entity.setDeltaMovement(entity.getDeltaMovement().x / inertialMass,
                entity.getDeltaMovement().y / inertialMass, entity.getDeltaMovement().z / inertialMass);

        this.getData(entity).rotPit = (random.nextFloat() - 0.5f) * 4 / inertialMass;
        this.getData(entity).rotYaw = (random.nextFloat() - 0.5f) * 4 / inertialMass;

        final double motionToAdd = entity.getDestroyData().cutDir.getOpposite().getStepX() * 0.1;
        entity.setDeltaMovement(entity.getDeltaMovement().add(motionToAdd, motionToAdd, motionToAdd));

        FallingTreeEntity.standardDropLeavesPayLoad(entity); // Seeds and stuff fall out of the tree before it falls over.
    }

    @Override
    public void handleMotion(FallingTreeEntity entity) {
        if (entity.landed) {
            return;
        }

        entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y - AnimationConstants.TREE_GRAVITY, entity.getDeltaMovement().z);

        // Create drag in air.
        entity.setDeltaMovement(entity.getDeltaMovement().x * 0.98f, entity.getDeltaMovement().y * 0.98f,
                entity.getDeltaMovement().z * 0.98f);
        this.getData(entity).rotYaw *= 0.98f;
        this.getData(entity).rotPit *= 0.98f;

        // Apply motion.
        entity.setPos(entity.getX() + entity.getDeltaMovement().x, entity.getY() + entity.getDeltaMovement().y,
                entity.getZ() + entity.getDeltaMovement().z);
        entity.setXRot(Mth.wrapDegrees(entity.getXRot() + getData(entity).rotPit));
        entity.setYRot(Mth.wrapDegrees(entity.getYRot() + getData(entity).rotYaw));

        int radius = 8;
        if (entity.getDestroyData().getNumBranches() <= 0) {
            return;
        }
        final BlockState state = entity.getDestroyData().getBranchBlockState(0);
        if (TreeHelper.isBranch(state)) {
            radius = ((BranchBlock) state.getBlock()).getRadius(state);
        }

        final Level level = entity.level;
        final AABB fallBox = new AABB(entity.getX() - radius, entity.getY(), entity.getZ() - radius, entity.getX() + radius, entity.getY() + 1.0, entity.getZ() + radius);
        final BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
        final BlockState collState = level.getBlockState(pos);

        if (!TreeHelper.isLeaves(collState) && !TreeHelper.isBranch(collState) && !(collState.getBlock() instanceof TrunkShellBlock)) {
            if (collState.getBlock() instanceof LiquidBlock) {
                // Play the water version of the sound
                playEndSound(entity, true);
                // Undo the gravity.
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, AnimationConstants.TREE_GRAVITY, 0));
                // Create drag in liquid.
                entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.8f, 0.8f, 0.8f));
                this.getData(entity).rotYaw *= 0.8f;
                this.getData(entity).rotPit *= 0.8f;
                // Add a little buoyancy.
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.01, 0));
                entity.onFire = false;
            } else {
                final VoxelShape shape = collState.getBlockSupportShape(level, pos);
                AABB collBox = new AABB(0, 0, 0, 0, 0, 0);
                if (!shape.isEmpty()) {
                    collBox = collState.getBlockSupportShape(level, pos).bounds();
                }

                collBox = collBox.move(pos);
                if (fallBox.intersects(collBox)) {
                    playEndSound(entity, false);
                    entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
                    entity.setPos(entity.getX(), collBox.maxY, entity.getZ());
                    entity.yo = entity.getY();
                    entity.landed = true;
                    entity.setOnGround(true);
                    if (entity.onFire) {
                        if (entity.level.isEmptyBlock(pos.above())) {
                            entity.level.setBlockAndUpdate(pos.above(), Blocks.FIRE.defaultBlockState());
                        }
                    }
                }
            }
        }

    }

    @Override
    public void dropPayload(FallingTreeEntity entity) {
        final Level level = entity.level;
        entity.getPayload().forEach(i -> Block.popResource(level, new BlockPos(entity.getX(), entity.getY(), entity.getZ()), i));
        entity.getDestroyData().leavesDrops.forEach(bis -> Block.popResource(level, entity.getDestroyData().cutPos.offset(bis.pos), bis.stack));
    }

    public boolean shouldDie(FallingTreeEntity entity) {
        final boolean dead = entity.landed || entity.tickCount > 120;

        if (dead) {
            entity.cleanupRootyDirt();
        }

        return dead;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack) {
        final float yaw = Mth.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.yRotO, entity.getYRot(), partialTick));
        final float pit = Mth.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.xRotO, entity.getXRot(), partialTick));

        final Vec3 mc = entity.getMassCenter();
        poseStack.translate(mc.x, mc.y, mc.z);
        poseStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), -yaw, true));
        poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), pit, true));
        poseStack.translate(-mc.x - 0.5, -mc.y, -mc.z - 0.5);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(FallingTreeEntity entity) {
        return true;
    }
}
