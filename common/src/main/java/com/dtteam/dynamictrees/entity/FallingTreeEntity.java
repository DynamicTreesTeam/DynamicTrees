package com.dtteam.dynamictrees.entity;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.entity.animation.AnimationHandler;
import com.dtteam.dynamictrees.entity.animation.AnimationHandlers;
import com.dtteam.dynamictrees.entity.animation.DataAnimationHandler;
import com.dtteam.dynamictrees.model.FallingTreeEntityModelTrackerCache;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.model.ModelTracker;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.BlockBounds;
import com.dtteam.dynamictrees.util.BranchDestructionData;
import com.dtteam.dynamictrees.util.CoordUtils.Surround;
import com.dtteam.dynamictrees.util.TreeHelper;
import com.google.common.collect.Iterables;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * @author ferreusveritas
 */
public class FallingTreeEntity extends Entity implements ModelTracker {

    public static final EntityDataAccessor<CompoundTag> voxelDataParameter = SynchedEntityData.defineId(FallingTreeEntity.class, EntityDataSerializers.COMPOUND_TAG);

    //Not needed in client
    protected List<ItemStack> payload = new ArrayList<>(0);
    protected float volume = 0;
    protected boolean hasLeaves = false;

    //Needed in client and server
    protected BranchDestructionData destroyData = new BranchDestructionData();
    protected Vec3 geomCenter = Vec3.ZERO;
    protected Vec3 massCenter = Vec3.ZERO;
    protected AABB normalBB = new AABB(BlockPos.ZERO);
    protected AABB cullingNormalBB = new AABB(BlockPos.ZERO);
    protected boolean clientBuilt = false;
    protected boolean firstUpdate = true;
    public boolean landed = false;
    public DestroyType destroyType = DestroyType.HARVEST;
    public boolean onFire = false;
    protected AABB cullingBB;
    protected Species species;

    public static AnimationHandler AnimHandlerFall = AnimationHandlers.falloverAnimationHandler;
    public static AnimationHandler AnimHandlerDrop = AnimationHandlers.defaultAnimationHandler;
    public static AnimationHandler AnimHandlerBurn = AnimationHandlers.defaultAnimationHandler;
    public static AnimationHandler AnimHandlerFling = AnimationHandlers.defaultAnimationHandler;
    public static AnimationHandler AnimHandlerBlast = AnimationHandlers.blastAnimationHandler;

    public AnimationHandler currentAnimationHandler = AnimationHandlers.voidAnimationHandler;
    public DataAnimationHandler dataAnimationHandler = null;

    public enum DestroyType {
        VOID,
        HARVEST,
        EXPLODE,
        FIRE,
        ROOT
    }

    public FallingTreeEntity(EntityType<? extends FallingTreeEntity> type, Level level) {
        super(type, level);
    }

    public boolean isClientBuilt() {
        return clientBuilt;
    }

    /**
     * This is only run by the server to set up the object data
     *
     * @param destroyData
     * @param payload
     */
    public FallingTreeEntity setData(BranchDestructionData destroyData, List<ItemStack> payload, DestroyType destroyType) {
        this.destroyData = destroyData;
        if (destroyData.getNumBranches() == 0) { //If the entity contains no branches there's no reason to create it at all
            DynamicTrees.LOG.error("Warning: Tried to create a EntityFallingTree with no branch blocks. This shouldn't be possible.");
            new Exception().printStackTrace();
            kill();
            return this;
        }
        BlockPos basePos = destroyData.basePos;
        this.payload = payload;
        this.destroyType = destroyType;
        this.onFire = destroyType == DestroyType.FIRE;

        //these variables are used for the falling tree sound
        this.volume = destroyData.woodVolume.getVolume();
        this.hasLeaves = destroyData.getNumLeaves() > 0;

        this.species = destroyData.species;

        this.setPosRaw(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5);

        int numBlocks = destroyData.getNumBranches();
        geomCenter = new Vec3(0, 0, 0);
        double totalMass = 0;

        //Calculate center of geometry, center of mass and bounding box, remap to relative coordinates
        for (int index = 0; index < destroyData.getNumBranches(); index++) {
            BlockPos relPos = destroyData.getBranchRelPos(index);

            int radius = destroyData.getBranchRadius(index);
            float mass = (radius * radius * 64) / 4096f;//Assume full height cuboids for simplicity
            totalMass += mass;

            Vec3 relVec = new Vec3(relPos.getX(), relPos.getY(), relPos.getZ());
            geomCenter = geomCenter.add(relVec);
            massCenter = massCenter.add(relVec.scale(mass));
        }

        geomCenter = geomCenter.scale(1.0 / numBlocks);
        massCenter = massCenter.scale(1.0 / totalMass);

        setVoxelData(buildVoxelData(destroyData));

        return this;
    }

    public CompoundTag buildVoxelData(BranchDestructionData destroyData) {
        CompoundTag tag = new CompoundTag();
        destroyData.writeToNBT(tag);

        tag.putDouble("geomx", geomCenter.x);
        tag.putDouble("geomy", geomCenter.y);
        tag.putDouble("geomz", geomCenter.z);
        tag.putDouble("massx", massCenter.x);
        tag.putDouble("massy", massCenter.y);
        tag.putDouble("massz", massCenter.z);
        tag.putInt("destroytype", destroyType.ordinal());
        tag.putBoolean("onfire", onFire);
        tag.putFloat("volume", volume);
        tag.putBoolean("hasleaves", hasLeaves);
        tag.putString("species", species.getRegistryName().toString());

        return tag;
    }

    public void setupFromNBT(CompoundTag tag) {
        destroyData = new BranchDestructionData(tag);
        if (destroyData.getNumBranches() == 0) {
            kill();
        }
        destroyType = DestroyType.values()[tag.getInt("destroytype")];
        geomCenter = new Vec3(tag.getDouble("geomx"), tag.getDouble("geomy"), tag.getDouble("geomz"));
        massCenter = new Vec3(tag.getDouble("massx"), tag.getDouble("massy"), tag.getDouble("massz"));

        this.setBoundingBox(this.buildAABBFromDestroyData(this.destroyData).move(this.getX(), this.getY(), this.getZ()));
        this.cullingBB = this.cullingNormalBB.move(this.getX(), this.getY(), this.getZ());

        volume = tag.getFloat("volume");
        hasLeaves = tag.getBoolean("hasleaves");
        species = Species.REGISTRY.get(tag.getString("species"));

        onFire = tag.getBoolean("onfire");
    }

    public void buildClient() {

        CompoundTag tag = getVoxelData();

        if (tag.contains("species")) {
            setupFromNBT(tag);
            clientBuilt = true;
        } else {
            System.out.println("Error: No species tag has been set");
        }

        BlockBounds renderBounds = new BlockBounds(destroyData.cutPos);

        cleanupShellBlocks(destroyData);

        Minecraft.getInstance().levelRenderer.setBlocksDirty(renderBounds.getMin().getX(), renderBounds.getMin().getY(), renderBounds.getMin().getZ(), renderBounds.getMax().getX(), renderBounds.getMax().getY(), renderBounds.getMax().getZ());//This forces the client to rerender the chunks
    }

    protected void cleanupShellBlocks(BranchDestructionData destroyData) {
        BlockPos cutPos = destroyData.cutPos;
        for (int i = 0; i < destroyData.getNumBranches(); i++) {
            if (destroyData.getBranchRadius(i) > 8) {
                BlockPos pos = destroyData.getBranchRelPos(i).offset(cutPos);
                for (Surround dir : Surround.values()) {
                    BlockPos dPos = pos.offset(dir.getOffset());
                    if (level().getBlockState(dPos).getBlock() instanceof TrunkShellBlock) {
                        level().removeBlock(dPos, false);
                    }
                }
            }
        }
    }

    public AABB buildAABBFromDestroyData(BranchDestructionData destroyData) {

        normalBB = new AABB(BlockPos.ZERO);

        for (BlockPos relPos : destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, false)) {
            normalBB = normalBB.minmax(new AABB(relPos));
        }
        if (destroyData.species.leavesAreSolid()){
            for (BlockPos relPos : destroyData.getPositions(BranchDestructionData.PosType.LEAVES, false)) {
                normalBB = normalBB.minmax(new AABB(relPos));
            }
        }

        //Adjust the bounding box to account for the tree falling over
        double height = normalBB.maxY - normalBB.minY;
        double width = Mth.absMax(normalBB.maxX - normalBB.minX, normalBB.maxZ - normalBB.minZ);
        double grow = Math.max(0, height - (width / 2)) + 2;
        cullingNormalBB = normalBB.inflate(grow + 4, 4, grow + 4);

        return normalBB;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.cullingBB;
    }

    public BranchDestructionData getDestroyData() {
        return destroyData;
    }

    public List<ItemStack> getPayload() {
        return payload;
    }

    public Vec3 getGeomCenter() {
        return geomCenter;
    }

    public Vec3 getMassCenter() {
        return massCenter;
    }

    public float getVolume() {
        return volume;
    }

    public boolean hasLeaves() {
        return hasLeaves;
    }

    public Species getSpecies() {
        return species;
    }

    @Override
    public void setPos(double x, double y, double z) {
        //This comes to the client as a packet from the server. But it doesn't set up the bounding box correctly
        this.setPosRaw(x, y, z);
        //This function is called by the Entity constructor during which normAABB hasn't yet been assigned.
        this.setBoundingBox(this.normalBB != null ? this.normalBB.move(x, y, z) : new AABB(BlockPos.ZERO));
        this.cullingBB = cullingNormalBB != null ? cullingNormalBB.move(x, y, z) : new AABB(BlockPos.ZERO);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide && !this.clientBuilt) {
            this.buildClient();
            if (!isAlive()) {
                return;
            }
        }

        if (!this.level().isClientSide && this.firstUpdate) {
            this.updateNeighbors();
        }

        this.handleMotion();

        this.setBoundingBox(this.normalBB.move(this.getX(), this.getY(), this.getZ()));
        this.cullingBB = cullingNormalBB.move(this.getX(), this.getY(), this.getZ());

        if (this.shouldDie()) {
            this.dropPayLoad();
            this.kill();
            this.modelCleanup();
        }

        this.firstUpdate = false;
    }

    /**
     * This is run server side to update all of the neighbors
     */
    protected void updateNeighbors() {
        HashSet<BlockPos> destroyed = new HashSet<>();
        HashSet<BlockPos> toUpdate = new HashSet<>();

        //Gather a set of all of the block positions that were recently destroyed
        Iterables.concat(destroyData.getPositions(BranchDestructionData.PosType.BRANCHES), destroyData.getPositions(BranchDestructionData.PosType.LEAVES)).forEach(destroyed::add);

        //Gather a list of all of the non-destroyed blocks surrounding each destroyed block
        for (BlockPos d : destroyed) {
            for (Direction dir : Direction.values()) {
                BlockPos dPos = d.relative(dir);
                if (!destroyed.contains(dPos)) {
                    toUpdate.add(dPos);
                }
            }
        }

        //Update each of the blocks that need to be updated
        toUpdate.forEach(pos -> level().neighborChanged(pos, Blocks.AIR, pos));
    }

    protected AnimationHandler selectAnimationHandler() {
        return Services.CONFIG.getBoolConfig(IConfigHelper.ENABLE_FALLING_TREES) ? destroyData.species.selectAnimationHandler(this) : AnimationHandlers.voidAnimationHandler;
    }

    public AnimationHandler defaultAnimationHandler() {
        if (destroyType == DestroyType.VOID || destroyType == DestroyType.ROOT) {
            return AnimationHandlers.voidAnimationHandler;
        }

        if (destroyType == DestroyType.EXPLODE) {
            return AnimHandlerBlast;
        }

        if (destroyType == DestroyType.FIRE) {
            return AnimHandlerBurn;
        }

        if (getDestroyData().cutDir == Direction.DOWN) {
            if (getMassCenter().y >= 1.0) {
                return AnimHandlerFall;
            } else {
                return AnimHandlerFling;
            }
        }

        return AnimHandlerDrop;
    }

    @Override
    public void modelCleanup() {
        FallingTreeEntityModelTrackerCache.cleanupModels(level(), this);
    }

    public void handleMotion() {
        if (firstUpdate) {
            currentAnimationHandler = selectAnimationHandler();
            currentAnimationHandler.initMotion(this);
        } else {
            currentAnimationHandler.handleMotion(this);
        }
    }

    public void dropPayLoad() {
        if (!level().isClientSide) {
            currentAnimationHandler.dropPayload(this);
        }
    }

    public boolean shouldDie() {
        return tickCount > 20 && currentAnimationHandler.shouldDie(this); //Give the entity 20 ticks to receive it's data from the server.
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return currentAnimationHandler.shouldRender(this, x, y, z);
    }

    /**
     * Same style payload droppers that have always existed in Dynamic Trees.
     * <p>
     * Drops wood materials at the cut position Leaves drops fall from their original location
     *
     * @param entity The {@link FallingTreeEntity} object.
     */
    public static void standardDropLogsPayload(FallingTreeEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide) {
            BlockPos cutPos = entity.getDestroyData().cutPos;
            entity.getPayload().forEach(i -> spawnItemAsEntity(level, cutPos, i));
        }
    }

    public static void standardDropLeavesPayLoad(FallingTreeEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide) {
            BlockPos cutPos = entity.getDestroyData().cutPos;
            entity.getDestroyData().leavesDrops.forEach(bis -> Block.popResource(level, cutPos.offset(bis.pos), bis.stack));
        }
    }

    /**
     * Same as Block.spawnAsEntity only this arrests the entityItem's random motion. Useful for CC turtles to pick up
     * the loot.
     */
    public static void spawnItemAsEntity(Level level, BlockPos pos, ItemStack stack) {
        if (!level.isClientSide && !stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !Services.MISC.isLevelRestoringBlockSnapshots(level)) { // do not drop items while restoring blockstates, prevents item dupe
            ItemEntity entityitem = new ItemEntity(level, (double) pos.getX() + 0.5F, (double) pos.getY() + 0.5F, (double) pos.getZ() + 0.5F, stack);
            entityitem.setDeltaMovement(0, 0, 0);
            entityitem.setDefaultPickUpDelay();
            level.addFreshEntity(entityitem);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(voxelDataParameter, new CompoundTag());
    }

    public void cleanupRootyDirt() {
        // Force the Rooty Dirt to update if it's there.  Turning it back to dirt.
        if (!this.level().isClientSide) {
            final BlockPos rootPos = getDestroyData().cutPos.below();
            final BlockState belowState = this.level().getBlockState(rootPos);

            if (TreeHelper.isRooty(belowState)) {
                final SoilBlock soilBlock = (SoilBlock) belowState.getBlock();
                soilBlock.doDecay(this.level(), rootPos, belowState, getDestroyData().species);
            }
        }
    }


    //This is shipped off to the clients
    public void setVoxelData(CompoundTag tag) {
        this.setBoundingBox(this.buildAABBFromDestroyData(this.destroyData).move(this.getX(), this.getY(), this.getZ()));
        this.cullingBB = this.cullingNormalBB.move(this.getX(), this.getY(), this.getZ());
        getEntityData().set(voxelDataParameter, tag);
    }

    public CompoundTag getVoxelData() {
        return getEntityData().get(voxelDataParameter);
    }



    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        CompoundTag vox = (CompoundTag) compound.get("vox");
        setupFromNBT(vox);
        setVoxelData(vox);

        if (compound.contains("payload")) {
            final ListTag nbtList = (ListTag) compound.get("payload");

            for (Tag tag : Objects.requireNonNull(nbtList)) {
                if (tag instanceof CompoundTag compTag) {
                    ItemStack.parse(level().registryAccess(),compTag).ifPresent(t->this.payload.add(t)); ;
                }
            }
        }

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("vox", getVoxelData());

        if (!payload.isEmpty()) {
            ListTag list = new ListTag();

            for (ItemStack stack : payload) {
                list.add(stack.save(level().registryAccess(), compound));
            }

            compound.put("payload", list);
        }
    }

//    @Override
//    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }

    public static FallingTreeEntity dropTree(Level level, BranchDestructionData destroyData, List<ItemStack> woodDropList, DestroyType destroyType) {
        //Spawn the appropriate item entities into the level
        if (!level.isClientSide()) {// Only spawn entities server side
            FallingTreeEntity entity = DTRegistries.FALLING_TREE.get().create(level);
            if (entity == null) return null;
            entity.setData(destroyData, woodDropList, destroyType);
            if (entity.isAlive()) {
                level.addFreshEntity(entity);
            }
            return entity;
        }

        return null;
    }

}
