package com.ferreusveritas.dynamictrees.entities.animation;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.entity.item.ItemEntity;
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

    class HandlerData extends DataAnimationHandler {
        float fallSpeed = 0;
        int bounces = 0;
        HashSet<LivingEntity> entitiesHit = new HashSet<>();//A record of the entities that have taken damage to ensure they are only damaged a single time
    }

    HandlerData getData(FallingTreeEntity entity) {
        return entity.dataAnimationHandler != null ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
    }

    @Override
    public void initMotion(FallingTreeEntity entity) {
        entity.dataAnimationHandler = new HandlerData();
        FallingTreeEntity.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over

        BlockPos belowBlock = entity.getDestroyData().cutPos.below();
        if (entity.level.getBlockState(belowBlock).isFaceSturdy(entity.level, belowBlock, Direction.UP)) {
            entity.setOnGround(true);
            return;
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
            Level world = entity.level;
            int radius = 8;
            BlockState state = entity.getDestroyData().getBranchBlockState(0);
            if (TreeHelper.isBranch(state)) {
                radius = ((BranchBlock) state.getBlock()).getRadius(state);
            }
            AABB fallBox = new AABB(entity.getX() - radius, entity.getY(), entity.getZ() - radius, entity.getX() + radius, entity.getY() + 1.0, entity.getZ() + radius);
            BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
            BlockState collState = world.getBlockState(pos);

            VoxelShape shape = collState.getBlockSupportShape(world, pos);
            AABB collBox = new AABB(0, 0, 0, 0, 0, 0);
            if (!shape.isEmpty()) {
                collBox = collState.getBlockSupportShape(world, pos).bounds();
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
            addRotation(entity, -fallSpeed);//pull back to before the collision
            getData(entity).bounces++;
            fallSpeed *= -AnimationConstants.TREE_ELASTICITY;//bounce with elasticity
            entity.landed = Math.abs(fallSpeed) < 0.02f;//The entity has landed if after a bounce it has little velocity
        }

        //Crush living things with clumsy dead trees
        Level world = entity.level;
        if (DTConfigs.ENABLE_FALLING_TREE_DAMAGE.get() && !world.isClientSide) {
            List<LivingEntity> elist = testEntityCollision(entity);
            for (LivingEntity living : elist) {
                if (!getData(entity).entitiesHit.contains(living)) {
                    getData(entity).entitiesHit.add(living);
                    float damage = entity.getDestroyData().woodVolume.getVolume() * Math.abs(fallSpeed) * 3f;
                    if (getData(entity).bounces == 0 && damage > 2) {
                        //System.out.println("damage: " + damage);
                        living.setDeltaMovement(
                                living.getDeltaMovement().x + (world.random.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getStepX() * damage * 0.2f),
                                living.getDeltaMovement().y + (world.random.nextFloat() * fallSpeed * 0.25f),
                                living.getDeltaMovement().z + (world.random.nextFloat() * entity.getDestroyData().toolDir.getOpposite().getStepZ() * damage * 0.2f));
                        living.setDeltaMovement(living.getDeltaMovement().x + (world.random.nextFloat() - 0.5), living.getDeltaMovement().y, living.getDeltaMovement().z + (world.random.nextFloat() - 0.5));
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
     * number of {@link ItemEntity}s in the world.
     *
     * @param entity
     * @return true if collision is detected
     */
    private boolean testCollision(FallingTreeEntity entity) {
        Direction toolDir = entity.getDestroyData().toolDir;

        float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.yRot : entity.xRot;

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
                entity.xRot += delta;
                break;
            case SOUTH:
                entity.xRot -= delta;
                break;
            case WEST:
                entity.yRot += delta;
                break;
            case EAST:
                entity.yRot -= delta;
                break;
            default:
                break;
        }

        entity.xRot = Mth.wrapDegrees(entity.xRot);
        entity.yRot = Mth.wrapDegrees(entity.yRot);
    }

    public List<LivingEntity> testEntityCollision(FallingTreeEntity entity) {

        Level world = entity.level;

        Direction toolDir = entity.getDestroyData().toolDir;

        float actingAngle = toolDir.getAxis() == Direction.Axis.X ? entity.yRot : entity.xRot;

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

        return world.getEntities(entity, new AABB(vec3d1.x, vec3d1.y, vec3d1.z, vec3d2.x, vec3d2.y, vec3d2.z),
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
     * A client side version of {@link AxisAlignedBB#intersects(Vector3d, Vector3d)}.
     */
    public static boolean intersects(AABB axisAlignedBB, Vec3 vec3d, Vec3 otherVec3d) {
        return axisAlignedBB.intersects(Math.min(vec3d.x, otherVec3d.x), Math.min(vec3d.y, otherVec3d.y), Math.min(vec3d.z, otherVec3d.z), Math.max(vec3d.x, otherVec3d.x), Math.max(vec3d.y, otherVec3d.y), Math.max(vec3d.z, otherVec3d.z));
    }

    @Override
    public void dropPayload(FallingTreeEntity entity) {
        Level world = entity.level;
        BlockPos cutPos = entity.getDestroyData().cutPos;
        entity.getPayload().forEach(i -> Block.popResource(world, cutPos, i));
    }

    @Override
    public boolean shouldDie(FallingTreeEntity entity) {

        boolean dead =
                Math.abs(entity.xRot) >= 160 ||
                        Math.abs(entity.yRot) >= 160 ||
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
    public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack) {

        float yaw = Mth.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.yRotO, entity.yRot, partialTicks));
        float pit = Mth.wrapDegrees(com.ferreusveritas.dynamictrees.util.MathHelper.angleDegreesInterpolate(entity.xRotO, entity.xRot, partialTicks));

        //Vec3d mc = entity.getMassCenter();

        int radius = entity.getDestroyData().getBranchRadius(0);

        Direction toolDir = entity.getDestroyData().toolDir;
        Vec3 toolVec = new Vec3(toolDir.getStepX(), toolDir.getStepY(), toolDir.getStepZ()).scale(radius / 16.0f);

        matrixStack.translate(-toolVec.x, -toolVec.y, -toolVec.z);
        matrixStack.mulPose(new Quaternion(new Vector3f(0, 0, 1), -yaw, true));
        matrixStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), pit, true));
        matrixStack.translate(toolVec.x, toolVec.y, toolVec.z);

        matrixStack.translate(-0.5, 0, -0.5);

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(FallingTreeEntity entity) {
        return true;
    }

}
