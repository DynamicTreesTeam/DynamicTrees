package com.dtteam.dynamictrees.entity.animation;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.client.SoundInstanceHandler;
import com.dtteam.dynamictrees.client.TintSourceHelper;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.tags.DTEntityTypeTags;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class FalloverAnimationHandler implements AnimationHandler {

    public static final int TICKS_BEFORE_CHECKING_COLLISION = 10;
    public static final float LEAVES_TO_BLOCK_PARTICLE_RATIO = 0.1f;

    @Override
    public String getName() {
        return "fallover";
    }

    static class HandlerData extends DataAnimationHandler {
        float fallSpeed = 0;
        int bounces = 0;
        long touchedGroundTick = -1;
        boolean startSoundPlayed = false;
        boolean fallThroughWaterSoundPlayed = false;
        boolean endSoundPlayed = false;
        HashSet<LivingEntity> entitiesHit = new HashSet<>();//A record of the entities that have taken damage to ensure they are only damaged a single time
    }

    HandlerData getData(FallingTreeEntity entity) {
        return entity.dataAnimationHandler != null ? (HandlerData) entity.dataAnimationHandler : new HandlerData();
    }

    protected void playStartSound(FallingTreeEntity entity){
        if (!getData(entity).startSoundPlayed && entity.level().isClientSide()){
            Species species = entity.getSpecies();
            SoundEvent sound = species.getFallingTreeStartSound(entity.getVolume(), entity.hasLeaves());
            SoundInstanceHandler.playSoundInstance(sound, species.getFallingTreePitch(entity.getVolume()), entity.position(), entity);
        }
    }
    protected void playEndSound(FallingTreeEntity entity){
        if (!getData(entity).endSoundPlayed){
            if (entity.level().isClientSide()){
                SoundInstanceHandler.stopSoundInstance(entity);
            } else {
                Species species = entity.getSpecies();
                SoundEvent sound = species.getFallingTreeEndSound(entity.getVolume(), entity.hasLeaves());
                entity.playSound(sound, 1.5f, species.getFallingTreePitch(entity.getVolume()));
                getData(entity).endSoundPlayed = true;
            }

        }
    }

    protected void playFallThroughWaterSound(FallingTreeEntity entity){
        if (!getData(entity).fallThroughWaterSoundPlayed && !entity.level().isClientSide()){
            entity.playSound(entity.getSpecies().getFallingTreeHitWaterSound(entity.getVolume(), entity.hasLeaves()), 2, 1);
            getData(entity).fallThroughWaterSoundPlayed = true;
        }
    }

    private Vec3 rotateAroundAxis(Vec3 in, Vec3 axis, double theta){
        double x = in.x;
        double y = in.y;
        double z = in.z;
        double u = axis.x;
        double v = axis.y;
        double w = axis.z;
        double v1 = u * x + v * y + w * z;
        double xPrime = u* v1 *(1d - Math.cos(theta))
                + x*Math.cos(theta)
                + (-w*y + v*z)*Math.sin(theta);
        double yPrime = v* v1 *(1d - Math.cos(theta))
                + y*Math.cos(theta)
                + (w*x - u*z)*Math.sin(theta);
        double zPrime = w* v1 *(1d - Math.cos(theta))
                + z*Math.cos(theta)
                + (-v*x + u*y)*Math.sin(theta);
        return new Vec3(xPrime, yPrime, zPrime);
    }

    protected void spawnLeavesParticlesWhileFalling(FallingTreeEntity entity, float fallSpeed){
        if (!entity.level().isClientSide()) return;
        BranchDestructionData data = entity.getDestroyData();
        if (data.getAllLeavesWithPos().isEmpty()) return;

        int particleCount = (int)(fallSpeed * data.species.falloverParticleFlingMultiplier());
        if (particleCount == 0) return;

        RandomSource rand = entity.level().getRandom();
        for (int j=0; j<particleCount; j++){
            Pair<BlockPos, BlockState> leafLoc = data.getAllLeavesWithPos().get(rand.nextInt(data.getAllLeavesWithPos().size()));
            BlockPos leavesPos = leafLoc.getKey().offset(data.basePos);
            BlockState leavesState = leafLoc.getValue();
            if (leavesState == null) return;

            ParticleOptions particle = getParticle(entity, leavesState, leavesPos);

            spawnParticlesAtLeaves(entity, leavesPos, particle, Vec3.ZERO, rand, particleCount, 1);
        }
    }

    protected void flingLeavesParticles(FallingTreeEntity entity, float fallSpeed){
        if (!entity.level().isClientSide()) return;
        int bounces = getData(entity).bounces;
        if (bounces > 1) return;
        int maxParticleBlocks = DTConfigs.COMMON.maxFallingTreeLeavesParticles.get();
        if (maxParticleBlocks == 0) return;
        
        BranchDestructionData data = entity.getDestroyData();
        Direction.Axis toolAxis = data.toolDir.getAxis();
        if (toolAxis == Direction.Axis.Y) return; //this one isn't possible anyways
        
        double limitChance = 1;
        if (data.getNumLeaves() > maxParticleBlocks)
            limitChance = maxParticleBlocks / (double)data.getNumLeaves();
        limitChance *= Math.exp(-bounces);

        RandomSource rand = entity.level().getRandom();
        int particleCount = (int)((bounces == 0 ? (int)(fallSpeed*5) : 1) * data.species.falloverParticleFlingMultiplier());

        if (particleCount == 0) return;
        
        Vec3 angularVel = entity.getForward().scale(fallSpeed * -data.toolDir.getAxisDirection().getStep());
        //on the X axis, the entity forward is rotated, so we rotate the angular velocity back
        if (toolAxis == Direction.Axis.X) angularVel = new Vec3(angularVel.z, angularVel.x, angularVel.y);

        for (Pair<BlockPos, BlockState> leafLoc : data.getAllLeavesWithPos()) {
            BlockPos leavesPos = leafLoc.getKey().offset(data.basePos);
            BlockState leavesState = leafLoc.getValue();
            if (leavesState == null) return;
            double r = leavesPos.getY() - data.basePos.getY();
            Vec3 velocity = angularVel.scale(r);

            ParticleOptions leavesParticle = getParticle(entity, leavesState, leavesPos);
            ParticleOptions blockParticle = new BlockParticleOption(ParticleTypes.BLOCK, leavesState);
            ParticleOptions particle = rand.nextFloat() > LEAVES_TO_BLOCK_PARTICLE_RATIO ? blockParticle : leavesParticle;

            spawnParticlesAtLeaves(entity, leavesPos, particle, velocity, rand, particleCount, limitChance);
        }
    }

    private ParticleOptions getParticle(FallingTreeEntity entity, BlockState leavesState, BlockPos leavesPos){
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            if (leavesState.getBlock() instanceof DynamicLeavesBlock leavesBlock){
                LeavesProperties properties = leavesBlock.getLeavesProperties();
                Integer leavesColor = properties.getForceParticleColor();
                if (leavesColor == null) leavesColor = TintSourceHelper.getLeavesColor(entity.getSpecies(), level, leavesPos);

                //if the chance is 0 it means we do not want leaves particles, so use block particles.
                if (properties.getLeavesParticleChance() > 0){
                    ParticleOptions opts = properties.getLeavesParticle(leavesColor);
                    //if it's not null then we have custom particles, otherwise do regular
                    if (opts != null) return opts;
                    return ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, leavesColor);
                }
            }
        }
        return new BlockParticleOption(ParticleTypes.BLOCK, leavesState);
    }

    protected void spawnParticlesAtLeaves(FallingTreeEntity entity, BlockPos leavesPos, ParticleOptions particle, Vec3 velocity, RandomSource rand, int particleCount, double limitChance){
        Vec3 newPos = getRelativeLeavesPosition(entity, leavesPos.getCenter());
        for (int j=0; j<particleCount; j++){
            if (rand.nextDouble() < limitChance){
                entity.level().addParticle(particle,
                        newPos.x+rand.nextFloat(), newPos.y+rand.nextFloat(), newPos.z+rand.nextFloat(),
                        velocity.x+rand.nextFloat(), velocity.y+rand.nextFloat(), velocity.z+rand.nextFloat());
            }
        }
    }

    protected Vec3 getRelativeLeavesPosition(FallingTreeEntity entity, Vec3 leaves){
        BranchDestructionData data = entity.getDestroyData();
        float angle = (data.toolDir.getAxis() == Direction.Axis.X ? entity.getYRot() : entity.getXRot()) * -data.toolDir.getAxisDirection().getStep() * 0.0174533f;
        return rotateAroundAxis(
                leaves.subtract(data.basePos.getCenter()),
                new Vec3(-data.toolDir.getStepZ(),0, data.toolDir.getStepX()),
                angle).add(data.basePos.getCenter()).subtract(0.5,0.5,0.5);
    }

    @Override
    public void initMotion(FallingTreeEntity entity) {
        entity.dataAnimationHandler = new HandlerData();
        FallingTreeEntity.standardDropLeavesPayLoad(entity);//Seeds and stuff fall out of the tree before it falls over

        playStartSound(entity);

        BlockPos belowBlock = entity.getDestroyData().cutPos.below();
        if (entity.level().getBlockState(belowBlock).isFaceSturdy(entity.level(), belowBlock, Direction.UP)) {
            entity.setOnGround(true);
        }
    }

    @Override
    public void handleMotion(FallingTreeEntity entity) {

        float fallSpeed = getData(entity).fallSpeed;

        if (entity.onGround()) {
            float height = (float) entity.getMassCenter().y * 2;
            fallSpeed += (float) (0.2 / height);
            spawnLeavesParticlesWhileFalling(entity, fallSpeed);
            addRotation(entity, fallSpeed);
        }

        entity.setDeltaMovement(entity.getDeltaMovement().x, entity.getDeltaMovement().y - AnimationConstants.TREE_GRAVITY, entity.getDeltaMovement().z);
        entity.setPos(entity.getX(), entity.getY() + entity.getDeltaMovement().y, entity.getZ());

        {//Handle entire entity falling and collisions with it's base and the ground
            Level level = entity.level();
            int radius = 8;
            BlockState state = entity.getDestroyData().getBranchBlockState(0);
            if (TreeHelper.isBranch(state)) {
                radius = ((BranchBlock) state.getBlock()).getRadius(state);
            }
            AABB fallBox = new AABB(entity.getX() - radius, entity.getY(), entity.getZ() - radius, entity.getX() + radius, entity.getY() + 1.0, entity.getZ() + radius);
            BlockPos pos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
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
                if (getData(entity).touchedGroundTick == -1) getData(entity).touchedGroundTick = entity.tickCount;
            }
        }

        //Wait at least ten ticks before calculating falling stuff, to avoid crashing into dirty blocks from the tree itself.
        if (entity.tickCount - getData(entity).touchedGroundTick > TICKS_BEFORE_CHECKING_COLLISION && fallSpeed > 0 && testCollision(entity)) {
            playEndSound(entity);
            flingLeavesParticles(entity, fallSpeed);
            addRotation(entity, -fallSpeed);//pull back to before the collision
            getData(entity).bounces++;
            fallSpeed *= -AnimationConstants.TREE_ELASTICITY;//bounce with elasticity
            entity.landed = Math.abs(fallSpeed) < 0.02f;//The entity has landed if after a bounce it has little velocity
        }

        //Crush living things with clumsy dead trees
        Level level = entity.level();
        if (DTConfigs.SERVER.enableFallingTreeDamage.get() && !level.isClientSide()) {
            List<LivingEntity> elist = testEntityCollision(entity);
            for (LivingEntity living : elist) {
                if (!getData(entity).entitiesHit.contains(living) && !living.is(DTEntityTypeTags.FALLING_TREE_DAMAGE_IMMUNE)) {
                    getData(entity).entitiesHit.add(living);
                    float damage = entity.getDestroyData().woodVolume.getVolume() * Math.abs(fallSpeed) * 3f;
                    if (getData(entity).bounces == 0 && damage > 2) {
                        living.setDeltaMovement(
                                living.getDeltaMovement().x + (level.getRandom().nextFloat() * entity.getDestroyData().toolDir.getOpposite().getStepX() * damage * 0.2f),
                                living.getDeltaMovement().y + (level.getRandom().nextFloat() * fallSpeed * 0.25f),
                                living.getDeltaMovement().z + (level.getRandom().nextFloat() * entity.getDestroyData().toolDir.getOpposite().getStepZ() * damage * 0.2f));
                        living.setDeltaMovement(living.getDeltaMovement().x + (level.getRandom().nextFloat() - 0.5), living.getDeltaMovement().y, living.getDeltaMovement().z + (level.getRandom().nextFloat() - 0.5));
                        damage *= DTConfigs.SERVER.fallingTreeDamageMultiplier.get();
                        living.hurt(AnimationConstants.treeDamage(level.registryAccess()), damage);
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

            if (entity.level().containsAnyLiquid(testBB)){
                playFallThroughWaterSound(entity);
            }

            if (!entity.level().noCollision(entity, testBB)) {
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

        Level level = entity.level();

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
        Level level = entity.level();
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

        if (dead && entity.level().isClientSide()) {
            SoundInstanceHandler.stopSoundInstance(entity);
        }

        return dead;
    }

    @Override
    public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack) {

        float yRotDiff = Mth.wrapDegrees(MathUtils.angleDegreesInterpolate(entity.yRotO, entity.getYRot(), partialTick));
        float xRotDiff = Mth.wrapDegrees(MathUtils.angleDegreesInterpolate(entity.xRotO, entity.getXRot(), partialTick));

        int radius = entity.getDestroyData().getBranchRadius(0);

        Direction toolDir = entity.getDestroyData().toolDir;
        Vec3 toolVec = new Vec3(toolDir.getStepX(), toolDir.getStepY(), toolDir.getStepZ()).scale(radius / 16.0f);

        poseStack.translate(-toolVec.x, -toolVec.y, -toolVec.z);
        poseStack.mulPose(Axis.ZN.rotationDegrees(yRotDiff));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRotDiff));
        poseStack.translate(toolVec.x, toolVec.y, toolVec.z);

        poseStack.translate(-0.5, 0, -0.5);

    }

    @Override
    public boolean shouldRender(FallingTreeEntity entity, double x, double y, double z) {
        return true;
    }

}
