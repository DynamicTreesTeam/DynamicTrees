package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.*;
import com.dtteam.dynamictrees.tree.BranchEntry;
import com.dtteam.dynamictrees.tree.TreeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CreakingHeartFamily extends AltBranchFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CreakingHeartFamily::new);

    protected float treeHeartHardnessMultiplier = 10;
    protected float hiddenHeartHardnessMultiplier = 0.25f;
    protected Item resinItem = Items.RESIN_CLUMP;
    protected Block resinBlock = Blocks.RESIN_CLUMP;
    public static final int HEART_BRANCH_INDEX = 3;


    public CreakingHeartFamily(Identifier name) {
        super(name);
    }

    ///////////////////////////////////////////
    // HEART BRANCH
    ///////////////////////////////////////////

    @Override
    public void setupBlocks() {
        super.setupBlocks();

        addBranch(HEART_BRANCH_INDEX, new BranchEntry(this,getHeartBranchName())
                .setCanBeStripped(true)
                .CreateBlock(this::createHeartBranch));
    }

    protected Identifier getHeartBranchName(){
        return this.getRegistryName().withSuffix("_creaking_heart").withSuffix(BranchBlock.NAME_SUFFIX);
    }

    protected BranchBlock createHeartBranch(Identifier name, BlockBehaviour.Properties properties) {
        return new CreakingHeartBranchBlock(name, properties);
    }

    public Family setPrimitiveHeartLog(Block primitiveLog) {
        branches.get(HEART_BRANCH_INDEX).setPrimitiveBlock(primitiveLog);
        return this;
    }

    public Optional<BranchBlock> getHeartBranch() {
        return getBranchBlock(HEART_BRANCH_INDEX);
    }

    public Optional<Block> getPrimitiveHeartLog() {
        return getPrimitiveLog(HEART_BRANCH_INDEX);
    }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        generators.add(heartBranchModelGenerator());
        return generators;
    }

    protected Identifier heartBranchModelGenerator() {
        return DynamicTrees.location("creaking_heart");
    }

    public Identifier getHeartBranchLoader() {
        return DynamicTrees.location("creaking_heart");
    }

    public void addHeartTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock, String state) {
        Optional<Block> primHeart = getPrimitiveHeartLog();
        if (primHeart.isPresent() && primHeart.get() == sourceBlock){
            String u = state.isEmpty() ? "" : "_";
            Identifier barkAwake = primitiveLogLocation.withSuffix(u+state);
            Identifier ringsAwake = primitiveLogLocation.withSuffix("_top"+u+state);
            String textureName = state+u+"heart_branch";
            if (this.textureOverrides.containsKey(textureName))
                barkAwake = this.textureOverrides.get(textureName);
            if (this.textureOverrides.containsKey(textureName+"_top"))
                ringsAwake = this.textureOverrides.get(textureName+"_top");

            textureConsumer.accept("heart_bark", barkAwake);
            textureConsumer.accept("heart_rings", ringsAwake);
        } else {
            DynamicTrees.LOG.error("Attempted to load heart branch textures for family {} but the provided block {} was not it's heart branch.", getRegistryName(), primHeart);
        }
    }

    public void addGeneratedBlockTags (Function<TagKey<Block>, TagAppender<Block, Block>> tagAppender){
        super.addGeneratedBlockTags(tagAppender);
        getHeartBranch().ifPresent(branch -> {
            tierTag(getDefaultBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(branch));
            defaultBranchTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(branch);
                } else {
                    tagAppender.apply(tag).addOptional(branch);
                }
            });
        });
    }

    @Override
    public List<Identifier> topBranchTextureLocations(){
        List<Identifier> locations = super.topBranchTextureLocations();
        if (getPrimitiveHeartLog().isPresent()){
            locations.add(topBranchTextureLocation(getPrimitiveHeartLog().get(), "heart_branch_top"));
        }

        return locations;
    }

    ///////////////////////////////////////////
    // OTHER BRANCHES
    ///////////////////////////////////////////


    @Override
    protected BranchBlock createBranch(Identifier name, BlockBehaviour.Properties properties) {
        return this.isThick() ? new ThickBranchBlock(name, properties){
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return ((CreakingHeartFamily)getFamily()).getTreeHardness(state, level, pos, super.getHardness(state, level, pos));
            }

            @Override
            protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
                sendParticlesFromHeart(level, pos, state,(CreakingHeartFamily)getFamily());
                super.attack(state, level, pos, player);
            }
        } : new BasicBranchBlock(name, properties){
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return ((CreakingHeartFamily)getFamily()).getTreeHardness(state, level, pos, super.getHardness(state, level, pos));
            }

            @Override
            protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
                sendParticlesFromHeart(level, pos, state,(CreakingHeartFamily)getFamily());
                super.attack(state, level, pos, player);
            }
        };
    }

    private static void sendParticlesFromHeart(Level level, BlockPos branchPos, BlockState branchState, CreakingHeartFamily family) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        BlockPos heartPos = family.getHeartPos(branchState, level, branchPos);
        if (heartPos == null) return;

        int rad = TreeHelper.getRadius(branchState);
        AABB source = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(branchPos)).deflate(0.375).inflate(rad/12f);
        AABB destination = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(heartPos));

        CreakingHeartBranchBlockEntity.emitParticlesToPosition(serverLevel, rad*2, false, destination, source);
        CreakingHeartBranchBlockEntity.emitParticlesToPosition(serverLevel, rad*2, true, destination, source);

        serverLevel.playSound(null, branchPos, SoundEvents.CREAKING_HEART_HURT, SoundSource.BLOCKS, 0.5f, 1.0F);
    }

    @Override
    protected BranchBlock createAltBranch(Identifier name, BlockBehaviour.Properties properties) {
        return new ResinBranchBlock(name, properties);
    }

    @Override
    protected Identifier getAltBranchName() {
        return getBranchName("resin_");
    }

    @Override
    protected Identifier altBranchModelGenerator() {
        return DynamicTrees.location("resin_branch");
    }

    @Override
    public Family setPrimitiveLog(Block primitiveLog) {
        setPrimitiveAltLog(primitiveLog);
        return super.setPrimitiveLog(primitiveLog);
    }

    public void addResinTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveResinLocation) {
        Identifier resin = primitiveResinLocation;
        if (this.textureOverrides.containsKey("resin")) {
            resin = this.textureOverrides.get("resin");
        }

        textureConsumer.accept("bark", resin);
        textureConsumer.accept("rings", DynamicTrees.location("block/air"));
    }

    ///////////////////////////////////////////
    // HEART LOGIC
    ///////////////////////////////////////////

    /**
     * @return false if you are using a custom block entity for the heart.
     * Otherwise, DT will register {@link com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlockEntity}
     */
    public boolean registerDefaultBlockEntity(){
        return true;
    }

    /**
     * This performs a DFS every time its called, avoid using it too often.
     */
    public boolean hasHeart(BlockState state, BlockGetter level, BlockPos pos){
        return getHeartPos(state, level, pos) != null;
    }

    /**
     * This performs a DFS every time its called, avoid using it too often.
     */
    private @Nullable BlockPos getHeartPos(BlockState state, BlockGetter level, BlockPos pos) {
        return CreakingHeartBranchBlock.findFromBranch(state, level, pos, this.getMaxSignalDepth());
    }

    public float getTreeHardness(BlockState state, BlockGetter level, BlockPos pos, float baseHardness){
        if (hasHeart(state, level, pos))
            return baseHardness * treeHeartHardnessMultiplier;
        return baseHardness;
    }

    public void setTreeHeartHardnessMultiplier(float treeWhenHeartHardnessMultiplier) {
        this.treeHeartHardnessMultiplier = treeWhenHeartHardnessMultiplier;
    }

    public float getHiddenHeartHardnessMultiplier() {
        return hiddenHeartHardnessMultiplier;
    }

    public void setHiddenHeartHardnessMultiplier(float hiddenHeartHardnessMultiplier) {
        this.hiddenHeartHardnessMultiplier = hiddenHeartHardnessMultiplier;
    }

    public void setResinItem(Item resinItem) {
        this.resinItem = resinItem;
    }

    public Item getResinItem() {
        return resinItem;
    }

    public void setResinBlock(Block resinBlock) {
        this.resinBlock = resinBlock;
    }

    public Block getResinBlock() {
        return resinBlock;
    }

}
