package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.MossyRootsBlock;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.tags.TagAppender;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.utility.ResourceLocationUtils.suffix;

public class MossyAerialRootsFamily extends UndergroundRootsFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MossyAerialRootsFamily::new);

    protected BlockItem mossCarpet;
    protected float mossyRootsChance = 0.3f;
    private Supplier<BranchBlock> mossyRoots;
    private Supplier<Item> mossyRootsItem;

    public MossyAerialRootsFamily(Identifier name) {
        super(name);
    }

    @Override
    public void setupBlocks() {
        super.setupBlocks();
        this.mossyRoots = this.setupBranch(this.createMossyRoots(this.getBranchName("mossy_")), false);
        this.setMossyRootsItem(this.createRootsItem(this.getBranchName("mossy_"), this.mossyRoots));
    }

    protected Supplier<BranchBlock> createMossyRoots(final Identifier name) {
        return RegistryHandler.addBlock(suffix(name, getRootsNameSuffix()), () -> createMossyRootsBlock(name));
    }

    protected BranchBlock createMossyRootsBlock(Identifier name) {
        final MossyRootsBlock branch = new MossyRootsBlock(name, this.getProperties());
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }

    @Override
    protected BranchBlock createRootsBlock(Identifier name) {
        final BasicRootsBlock branch = new BasicRootsBlock(name, this.getProperties()) {
            @Override
            protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
                if (getMossCarpetItem() != null && stack.is(getMossCarpetItem())
                        && placeMossCarpet(state, level, pos)) {
                    stack.consume(1, player);
                    return InteractionResult.SUCCESS;
                }
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }
        };
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Item> Family setMossyRootsItem(Supplier<T> mossyRootsItemSup) {
        this.mossyRootsItem = (Supplier<Item>) mossyRootsItemSup;
        return this;
    }

    public Optional<BranchBlock> getMossyRoots() {
        return Optionals.ofBlock(this.mossyRoots);
    }

    public Optional<Item> getMossyRootsItem() {
        return Optionals.ofItem(this.mossyRootsItem);
    }

    public void setMossCarpet(Item mossCarpet) {
        if (mossCarpet instanceof BlockItem mossCarpetItem) {
            this.mossCarpet = mossCarpetItem;
        } else {
            DynamicTrees.LOG.error("Could not register mossCarpet for {}. Item {} is not of BlockItem type.", this, mossCarpet);
        }
    }

    public Block getMossCarpetBlock() {
        return mossCarpet != null ? mossCarpet.getBlock() : net.minecraft.world.level.block.Blocks.MOSS_CARPET;
    }

    public Item getMossCarpetItem() {
        return mossCarpet;
    }

    @Override
    public void generateStateData(DTDataProvider.BlockState provider) {
        super.generateStateData(provider);
        var mossy = blockStateGenerators.get(DynamicTrees.location("mossy_roots"));
        if (mossy != null) {
            mossy.get().generate(provider, this);
        }
    }

    @Override
    public void generateItemModelData(DTDataProvider.ItemModel provider) {
        super.generateItemModelData(provider);
        var mossy = itemModelGenerators.get(DynamicTrees.location("mossy_roots_item"));
        if (mossy != null) {
            mossy.get().generate(provider, this);
        }
    }

    @Override
    public void addGeneratedBlockTags(Function<TagKey<Block>, TagAppender<Block>> tagAppender) {
        super.addGeneratedBlockTags(tagAppender);
        getMossyRoots().ifPresent(roots -> {
            this.tierTag(getDefaultRootsHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(roots));
            defaultRootsTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(roots);
                } else {
                    tagAppender.apply(tag).addOptional(BuiltInRegistries.BLOCK.getKey(roots));
                }
            });
        });
    }

    @Override
    public void setPrimitiveRoots(Block primitiveRoots) {
        super.setPrimitiveRoots(primitiveRoots);
        if (this.mossyRoots != null) {
            com.dtteam.dynamictrees.compat.DeferredItemStacks.setWhenBound(
                    stack -> this.mossyRoots.get().setPrimitiveLogDrops(stack), primitiveRoots);
        }
    }

    public SoundType getMossCarpetSoundType() {
        return getMossCarpetBlock().defaultBlockState().getSoundType();
    }

    public boolean placeMossCarpet(BlockState state, Level level, BlockPos pos) {
        if (mossCarpet == null || state.getValue(BasicRootsBlock.LAYER) == BasicRootsBlock.Layer.COVERED) {
            return false;
        }
        int currentRadius = TreeHelper.getTreePart(state).getRadius(state);
        if (currentRadius == 0 || TreeHelper.isBranch(level.getBlockState(pos.above()))) {
            return false;
        }
        getMossyRoots().ifPresent(roots -> roots.setRadius(level, pos, currentRadius, null, 3));
        level.playSound(null, pos, getMossCarpetSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public void removeMossCarpet(BlockState state, Level level, BlockPos pos, @Nullable Player player) {
        int currentRadius = TreeHelper.getTreePart(state).getRadius(state);
        if (currentRadius == 0) {
            return;
        }
        getRoots().ifPresent(roots -> roots.setRadius(level, pos, currentRadius, null, 3));
        if (mossCarpet != null && (player == null || !player.isCreative())) {
            Block.popResource(level, pos, new ItemStack(getMossCarpetItem()));
        }
        level.playSound(null, pos, getMossCarpetSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public void setMossyRootsChance(float mossyRootsChance) {
        this.mossyRootsChance = mossyRootsChance;
    }

    @Override
    public Optional<BranchBlock> getBranchForRootsPlacement(LevelAccessor level, Species species, BlockPos pos) {
        if (level.getRandom().nextFloat() < mossyRootsChance
                && !TreeHelper.isTreePart(level.getBlockState(pos.above()))) {
            return getMossyRoots();
        }
        return getRoots();
    }

}
