package com.ferreusveritas.dynamictrees.entity.animation;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FalloverAnimationHandler implements AnimationHandler {

    @Override
    public String getName() {
        return "fallover";
    }

    static class HandlerData extends DataAnimationHandler {
        float fallSpeed = 0;
        int bounces = 0;
        boolean startSoundPlayed = false;
        boolean fallThroughWaterSoundPlayed = false;
        boolean endSoundPlayed = false;
        SoundInstance fallingSoundInstance;
        HashSet<LivingEntity> entitiesHit = new HashSet<>();//A record of the entities that have taken damage to ensure they are only damaged a single time
    }

    HandlerData getData(FallingTreeEntity entity) {
        return entity.dataAnimationHandler != null ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
    }

    protected void playStartSound(FallingTreeEntity entity){
        //we play on the server side so everyone can hear it
        if (!getData(entity).startSoundPlayed && !entity.level.isClientSide()){
            Species species = entity.getSpecies();
            SoundEvent sound = species.getFallingTreeStartSound(entity.getVolume(), entity.hasLeaves());
            SoundInstance fallingInstance = species.getSoundInstance(sound, species.getFallingTreePitch(entity.getVolume()), entity.position());
            Minecraft.getInstance().getSoundManager().play(fallingInstance);
            getData(entity).fallingSoundInstance = fallingInstance;
            getData(entity).startSoundPlayed = true;
        }
    }
    protected void playEndSound(FallingTreeEntity entity){
        if (!getData(entity).endSoundPlayed && !entity.level.isClientSide()){
            Species species = entity.getSpecies();
            SoundInstance fallingInstance = getData(entity).fallingSoundInstance;
            if (fallingInstance != null)
                Minecraft.getInstance().getSoundManager().stop(fallingInstance);
            SoundEvent sound = species.getFallingTreeEndSound(entity.getVolume(), entity.hasLeaves());
            entity.playSound(sound, 3, species.getFallingTreePitch(entity.getVolume()));
            getData(entity).endSoundPlayed = true;
        }
    }

    protected void playFallThroughWaterSound(FallingTreeEntity entity){
        if (!getData(entity).fallThroughWaterSoundPlayed && !entity.level.isClientSide()){
            entity.playSound(entity.getSpecies().getFallingTreeHitWaterSound(entity.getVolume(), entity.hasLeaves()), 2, 1);
            getData(entity).fallThroughWaterSoundPlayed = true;
        }
    }

    @Override
    public void initMotion(FallingTreeEntity entity) {
        entity.dataAnimationHandler = new HandlerData();
        FallingTreeEntity.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over

        playStartSound(entity);

        BlockPos belowBlock = entity.getDestroyData().cutPos.below();
        if (entity.level.getBlockState(belowBlock).isFaceSturdy(entity.level, belowBlock, Direction.UP)) {
            entity.setOnGround(true);
        }
    }

    @Override
    public void handleMotion(FallingTreeEntity entity) {

        float fallSpeed = getData(entity).fallSpeed;

        if (entity.isOnGround()) {
            float height = (float) entity.getMassCenter().y * 2;
            fallSpeed += (0.2 / height);
            addRotation(entity, fallSpeed);
        }

        entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y - AnimationConstants.TREE_GRAVITY, entity.getDeltaMovement().z);
        entity.setPos(entity.getX(), entity.getY() + entity.getDeltaMovement().y, entity.getZ());

        {//Handle entire entity falling and collisions with it's base and the ground
            Level level = entity.level;
            int radius = 8;
            BlockState state = entity.getDestroyData().getBranchBlockState(0);
            if (TreeHelper.isBranch(state)) {
                radius = ((BranchBlock) state.getBlock()).getRadius(state);
            }
            AABB fallBox = new AABB(entity.getX() - radius, entity.getY(), entity.getZ() - radius, entity.getX() + radius, entity.getY() + 1.0, entity.getZ() + radius);
            BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
            BlockState collState = level.getBlockState(pos);

            VoxelShape shape = collState.getBlockSupportShape(level, pos);
            AABB collBox = new AABB(0, 0, 0, 0, 0, 0);
            if (!shape.isEmpty()) {
                collBox = collState.getBlockSupportShape(level, pos).bounds();
            }

            collBox = collBox.move(pos);
            if (fallBox.intersects(collBox)) {
                entity.setDeltaMovement(entity.getDeltaMovement().x, 0, entity.getDeltaMovement().z);
                entity.setPos(entity.getX(), collBox.maxY, entity.getZ());
                entity.yo = entity.getY();
                entity.setOnGround(true);
            }
        }

        if (fallSpeed > 0 && testCollision(entity)) {
            playEndSound(entity);
            addRotation(entity, -fallSpeed);//pull back to before the collision
            getData(entity).bounces++;
            fallSpeed *= -AnimationConstants.TREE_ELASTICITY;//bounce with elasticity
            entity.landed = Math.abs(fallSpeed) < 0.02f;//The entity has landed if after a bounce it has little velocity
        }

        //Crush living things with clumsy dead trees
        Level level = entity.level;
        if (DTConfigs.ENABLE_FALLING_TREE_DAMAGE.get() && !level.isClientSide) {
            List<LivingEntity> elist = testEntityCollision(entity);
            for (LivingEntity living : elist) {
                if (!getData(entity).entitiesHit.contains(living)) {
                    getData(entity).entitiesHit.add(living);
                    float damage = entity.getDestroyData().woodVolume.getVolume() * Math.abs(fallSpeed) * 3f;
                    if (getData(entity).bounces == 0 && damage > 2) {
                        //System.out.println("damage: " + damage);
                        living.setDeltaMovement(
                                living.getDeltaMovement().x + (level.random.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getStepX() * damage * 0.2f),
                                living.getDeltaMovement().y + (level.random.nextFloat() * fallSpeed * 0.25f),
                                living.getDeltaMovement().z + (level.random.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getStepZ() * damage * 0.2f));
                        living.setDeltaMovement(living.getDeltaMovement().x + (level.random.nextFloat() - 0.5), living.getDeltaMovement().y, living.getDeltaMovement().z + (level.random.nextFloat() - 0.5));
                        damage *= DTConfigs.FALLING_TREE_DAMAGE_MULTIPLIER.get();
                        //System.out.println("Tree Falling Damage: " + damage + "/" + living.getHealth());
                        living.hurt(AnimationConstants.TREE_DAMAGE, damage);
                    }
                }
            }
        }

        getData(entity).fallSpeed = fallSpeed;
    }

    /**
     * This tests a bounding box cube for each block of the trunk. Processing is approximately equivalent to the same
     * number of {@link net.minecraft.world.entity.item.ItemEntity}s in the world.
     *
     * @param entity the falling tree entity
     * @return true if collision is detected
     */
    private boolean testCollision(FallingTreeEntity entity) {
        Direction toolDir = entity.getDestroyData().toolDir;

        float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.getYRot() : entity.getXRot();

        int offsetX = toolDir.getStepX();
        int offsetZ = toolDir.getStepZ();
        float h = Mth.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
        float v = Mth.cos((float) Math.toRadians(actingAngle));
        float xbase = (float) (entity.getX() + offsetX * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));
        float ybase = (float) (entity.getY() - (h * 0.5f) + (v * 0.5f));
        float zbase = (float) (entity.getZ() + offsetZ * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));

        int trunkHeight = entity.getDestroyData().trunkHeight;
        float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;

        trunkHeight = Math.min(trunkHeight, 24);

        for (int segment = 0; segment < trunkHeight; segment++) {
            float segX = xbase + h * segment * offsetX;
            float segY = ybase + v * segment;
            float segZ = zbase + h * segment * offsetZ;
            float tex = 0.0625f;
            float half = Mth.clamp(tex * (segment + 1) * 2, tex, maxRadius);
            AABB testBB = new AABB(segX - half, segY - half, segZ - half, segX + half, segY + half, segZ + half);

            if (entity.level.containsAnyLiquid(testBB)){
                playFallThroughWaterSound(entity);
            }

            if (!entity.level.noCollision(entity, testBB)) {
                return true;
            }
        }

        return false;
    }

    private void addRotation(FallingTreeEntity entity, float delta) {
        Direction toolDir = entity.getDestroyData().toolDir;

        switch (toolDir) {
            case NORTH:
                entity.setXRot(entity.getXRot() + delta);
                break;
            case SOUTH:
                entity.setXRot(entity.getXRot() - delta);
                break;
            case WEST:
                entity.setYRot(entity.getYRot() + delta);
                break;
            case EAST:
                entity.setYRot(entity.getYRot() - delta);
                break;
            default:
                break;
        }

        entity.setXRot(Mth.wrapDegrees(entity.getXRot()));
        entity.setYRot(Mth.wrapDegrees(entity.getYRot()));
    }

    public List<LivingEntity> testEntityCollision(FallingTreeEntity entity) {

        Level level = entity.level;

        Direction toolDir = entity.getDestroyData().toolDir;

        float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.getYRot() : entity.getXRot();

        int offsetX = toolDir.getStepX();
        int offsetZ = toolDir.getStepZ();
        float h = Mth.sin((float) Math.toRadians(actingAngle)) * (offsetX | offsetZ);
        float v = Mth.cos((float) Math.toRadians(actingAngle));
        float xbase = (float) (entity.getX() + offsetX * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));
        float ybase = (float) (entity.getY() - (h * 0.5f) + (v * 0.5f));
        float zbase = (float) (entity.getZ() + offsetZ * (-(0.5f) + (v * 0.5f) + (h * 0.5f)));
        int trunkHeight = entity.getDestroyData().trunkHeight;
        float segX = xbase + h * (trunkHeight - 1) * offsetX;
        float segY = ybase + v * (trunkHeight - 1);
        float segZ = zbase + h * (trunkHeight - 1) * offsetZ;

        float maxRadius = entity.getDestroyData().getBranchRadius(0) / 16.0f;

        Vec3 vec3d1 = new Vec3(xbase, ybase, zbase);
        Vec3 vec3d2 = new Vec3(segX, segY, segZ);

        return level.getEntities(entity, new AABB(vec3d1.x, vec3d1.y, vec3d1.z, vec3d2.x, vec3d2.y, vec3d2.z),
                entity1 -> {
                    if (entity1 instanceof LivingEntity && entity1.isPickable()) {
                        AABB axisalignedbb = entity1.getBoundingBox().inflate(maxRadius);
                        return axisalignedbb.contains(vec3d1) || intersects(axisalignedbb, vec3d1, vec3d2);
                    }
                    return false;
                }
        ).stream().map(a -> (LivingEntity) a).collect(Collectors.toList());

    }

    /**
     */
    public static boolean intersects(AABB axisAlignedBB, Vec3 vec3d, Vec3 otherVec3d) {
        return axisAlignedBB.intersects(Math.min(vec3d.x, otherVec3d.x), Math.min(vec3d.y, otherVec3d.y), Math.min(vec3d.z, otherVec3d.z), Math.max(vec3d.x, otherVec3d.x), Math.max(vec3d.y, otherVec3d.y), Math.max(vec3d.z, otherVec3d.z));
    }

    @Override
    public void dropPayload(FallingTreeEntity entity) {
        Level level = entity.level;
        BlockPos cutPos = entity.getDestroyData().cutPos;
        entity.getPayload().forEach(i -> Block.popResource(level, cutPos, i));
    }

    @Override
    public boolean shouldDie(FallingTreeEntity entity) {

        boolean dead =
                Math.abs(entity.getXRot()) >= 160 ||
                        Math.abs(entity.getYRot()) >= 160 ||
                        entity.landed ||
                        entity.tickCount > 120 + (entity.getDestroyData().trunkHeight);

        //Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
        if (dead) {
            entity.cleanupRootyDirt();
        }

        return dead;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack) {

        float yaw = Mth.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.yRotO, entity.getYRot(), partialTick));
        float pit = Mth.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.xRotO, entity.getXRot(), partialTick));

        //Vec3d mc = entity.getMassCenter();

        int radius = entity.getDestroyData().getBranchRadius(0);

        Direction toolDir = entity.getDestroyData().toolDir;
        Vec3 toolVec = new Vec3(toolDir.getStepX(), toolDir.getStepY(), toolDir.getStepZ()).scale(radius / 16.0f);

        poseStack.translate(-toolVec.x, -toolVec.y, -toolVec.z);
        poseStack.mulPose(new Quaternion(new Vector3f(0, 0, 1), -yaw, true));
        poseStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), pit, true));
        poseStack.translate(toolVec.x, toolVec.y, toolVec.z);

        poseStack.translate(-0.5, 0, -0.5);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(FallingTreeEntity entity) {
        return true;
    }

}
