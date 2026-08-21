package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlockEntity;
import com.dtteam.dynamictrees.block.branch.ResinBranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.compat.DeferredItemStacks;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.tags.TagAppender;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.utility.ResourceLocationUtils.suffix;

public class CreakingHeartFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CreakingHeartFamily::new);

    protected float treeHeartHardnessMultiplier = 10;
    protected float hiddenHeartHardnessMultiplier = 0.4f;
    protected Item resinItem = Items.RESIN_CLUMP;
    protected Block resinBlock = Blocks.RESIN_CLUMP;
    protected Block primitiveHeartLog = Blocks.AIR;
    private Supplier<BranchBlock> heartBranch;
    private Supplier<BranchBlock> resinBranch;

    public CreakingHeartFamily(Identifier name) {
        super(name);
    }

    @Override
    public void setupBlocks() {
        super.setupBlocks();
        this.resinBranch = this.setupBranch(this.createResinBranch(), true);
        this.heartBranch = this.setupBranch(this.createHeartBranch(), true);
    }

    protected Supplier<BranchBlock> createHeartBranch() {
        Identifier name = this.getRegistryName().withSuffix("_creaking_heart");
        return RegistryHandler.addBlock(suffix(name, BranchBlock.NAME_SUFFIX),
                () -> new CreakingHeartBranchBlock(name, this.getProperties()));
    }

    protected Supplier<BranchBlock> createResinBranch() {
        Identifier name = this.getBranchName("resin_");
        return RegistryHandler.addBlock(suffix(name, BranchBlock.NAME_SUFFIX),
                () -> new ResinBranchBlock(name, this.getProperties()));
    }

    @Override
    protected BranchBlock createBranchBlock(Identifier name) {
        return this.isThick() ? new ThickBranchBlock(name, this.getProperties()) {
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return CreakingHeartFamily.this.getTreeHardness(state, level, pos, super.getHardness(state, level, pos));
            }
        } : new BasicBranchBlock(name, this.getProperties()) {
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return CreakingHeartFamily.this.getTreeHardness(state, level, pos, super.getHardness(state, level, pos));
            }
        };
    }

    public Family setPrimitiveHeartLog(Block primitiveLog) {
        this.primitiveHeartLog = primitiveLog;
        getHeartBranch().ifPresent(branch ->
                DeferredItemStacks.setWhenBound(stack -> branch.setPrimitiveLogDrops(stack), primitiveLog));
        return this;
    }

    public Optional<BranchBlock> getHeartBranch() {
        return Optionals.ofBlock(this.heartBranch);
    }

    public Optional<BranchBlock> getResinBranch() {
        return Optionals.ofBlock(this.resinBranch);
    }

    public Optional<Block> getPrimitiveHeartLog() {
        return primitiveHeartLog == Blocks.AIR ? Optional.empty() : Optional.of(this.primitiveHeartLog);
    }

    @Override
    public Family setPrimitiveLog(Block primitiveLog) {
        getResinBranch().ifPresent(branch ->
                DeferredItemStacks.setWhenBound(stack -> branch.setPrimitiveLogDrops(stack), primitiveLog));
        return super.setPrimitiveLog(primitiveLog);
    }

    @Override
    public void generateStateData(DTDataProvider.BlockState provider) {
        super.generateStateData(provider);
        var heart = blockStateGenerators.get(DynamicTrees.location("creaking_heart"));
        if (heart != null) {
            heart.get().generate(provider, this);
        }
        var resin = blockStateGenerators.get(DynamicTrees.location("resin_branch"));
        if (resin != null) {
            resin.get().generate(provider, this);
        }
    }

    public void addHeartTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, String state) {
        String suffixText = state.isEmpty() ? "" : "_" + state;
        Identifier bark = primitiveLogLocation.withPath(path -> path.replace("creaking_heart", "creaking_heart" + suffixText));
        Identifier rings = Identifier.fromNamespaceAndPath(primitiveLogLocation.getNamespace(),
                primitiveLogLocation.getPath().replace("creaking_heart", "creaking_heart" + suffixText) + "_top");
        if (this.textureOverrides.containsKey(state + (state.isEmpty() ? "" : "_") + "heart_branch")) {
            bark = this.textureOverrides.get(state + (state.isEmpty() ? "" : "_") + "heart_branch");
        }
        textureConsumer.accept("bark", bark);
        textureConsumer.accept("rings", rings);
    }

    public void addResinTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveResinLocation) {
        Identifier resin = this.textureOverrides.getOrDefault("resin", primitiveResinLocation);
        textureConsumer.accept("bark", resin);
        textureConsumer.accept("rings", DynamicTrees.location("block/air"));
    }

    public boolean registerDefaultBlockEntity() {
        return true;
    }

    public boolean hasHeart(BlockState state, BlockGetter level, BlockPos pos) {
        return getHeartPos(state, level, pos) != null;
    }

    private @Nullable BlockPos getHeartPos(BlockState state, BlockGetter level, BlockPos pos) {
        return CreakingHeartBranchBlock.findFromBranch(state, level, pos, this.getMaxSignalDepth());
    }

    public float getTreeHardness(BlockState state, BlockGetter level, BlockPos pos, float baseHardness) {
        if (hasHeart(state, level, pos)) {
            return baseHardness * treeHeartHardnessMultiplier;
        }
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

    public static void sendParticlesFromHeart(Level level, BlockPos branchPos, BlockState branchState, CreakingHeartFamily family) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos heartPos = family.getHeartPos(branchState, level, branchPos);
        if (heartPos == null) {
            return;
        }

        int rad = TreeHelper.getTreePart(branchState).getRadius(branchState);
        AABB source = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(branchPos)).deflate(0.375).inflate(rad / 12f);
        AABB destination = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(heartPos));

        CreakingHeartBranchBlockEntity.emitParticlesToPosition(serverLevel, rad * 2, false, destination, source);
        CreakingHeartBranchBlockEntity.emitParticlesToPosition(serverLevel, rad * 2, true, destination, source);

        serverLevel.playSound(null, branchPos, SoundEvents.CREAKING_HEART_HURT, SoundSource.BLOCKS, 0.5f, 1.0F);
    }

    @Override
    public void addGeneratedBlockTags(Function<TagKey<Block>, TagAppender<Block>> tagAppender) {
        super.addGeneratedBlockTags(tagAppender);
        getHeartBranch().ifPresent(branch -> {
            tierTag(getDefaultBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(branch));
            defaultBranchTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(branch);
                } else {
                    tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(branch));
                }
            });
        });
        getResinBranch().ifPresent(branch -> {
            tierTag(getDefaultBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(branch));
            defaultBranchTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(branch);
                } else {
                    tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(branch));
                }
            });
        });
    }

}
