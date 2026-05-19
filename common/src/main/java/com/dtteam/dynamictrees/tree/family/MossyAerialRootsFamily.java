package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.MossyRootsBlock;
import com.dtteam.dynamictrees.tree.BranchEntry;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.data.tags.TagAppender;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class MossyAerialRootsFamily extends AerialRootsFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MossyAerialRootsFamily::new);

    /**
     * Must be a block item to be placed and clicked on
     */
    private BlockItem mossCarpet;
    public static final int MOSSY_ROOTS_INDEX = 3;

    public MossyAerialRootsFamily(Identifier name) {
        super(name);
    }

    ///////////////////////////////////////////
    // MOSSY ROOTS
    ///////////////////////////////////////////

    @Override
    public void setupBlocks() {
        super.setupBlocks();

        branches.add(MOSSY_ROOTS_INDEX, new BranchEntry(this, getRootsName("mossy_"))
                .setCanBeStripped(false)
                .CreateBlock(this::createMossyRoots));
    }

    protected BranchBlock createMossyRoots(Identifier name, BlockBehaviour.Properties properties) {
        return new MossyRootsBlock(name, properties);
    }

    @Override
    protected BranchBlock createRoots(Identifier name, BlockBehaviour.Properties properties) {
        return new BasicRootsBlock(name, properties){
            @Override
            protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
                if (stack.is(getMossCarpetItem())
                        && placeMossCarpet(state, level, pos)) {
                    stack.consume(1, player);
                    return InteractionResult.SUCCESS;
                }
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }
        };
    }

    public Optional<BranchBlock> getMossyRoots() {
        return Optionals.ofBlock(branches.get(3).getBlock());
    }

    public void setMossCarpet(Item mossCarpet) {
        if (mossCarpet instanceof BlockItem mossCarpetItem)
            this.mossCarpet = mossCarpetItem;
        DynamicTrees.LOG.error("Could not register mossCarpet for {}. Item {} is not of BlockItem type.", this, mossCarpet);
    }

    public Block getMossCarpetBlock() {
        return mossCarpet.getBlock();
    }

    public Item getMossCarpetItem() {
        return mossCarpet;
    }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> list = new LinkedList<>(super.getBlockModelGenerators());
        list.add(DynamicTrees.location("mossy_roots"));
        return list;
    }

    @Override
    public void addGeneratedBlockTags (Function<TagKey<Block>, TagAppender<Block, Block>> tagAppender){
        super.addGeneratedBlockTags(tagAppender);
        //Create roots tag and root harvest tag if the family is mangrove-like.
        getMossyRoots().ifPresent(roots -> {
            this.tierTag(getDefaultRootsHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(roots));
            defaultRootsTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(roots);
                } else {
                    tagAppender.apply(tag).addOptional(roots);
                }
            });
        });
    }

    @Override
    public void setPrimitiveRoots(Block primitiveRoots) {
        super.setPrimitiveRoots(primitiveRoots);
        branches.get(MOSSY_ROOTS_INDEX).setPrimitiveBlock(primitiveRoots);
    }

    ///////////////////////////////////////////
    // BEHAVIOR
    ///////////////////////////////////////////

    public SoundType getMossCarpetSoundType(){
        return getMossCarpetBlock().defaultBlockState().getSoundType();
    }

    public boolean placeMossCarpet(BlockState state, Level level, BlockPos pos){
        if (BasicRootsBlock.isFullBlock(state)) return false;
        int currentRadius = TreeHelper.getRadius(state);
        if (currentRadius == 0 || TreeHelper.isBranch(level.getBlockState(pos.above()))) return false;
        getMossyRoots().ifPresent(roots -> roots.setRadius(level, pos, currentRadius, null, 3));
        level.playSound(null, pos, getMossCarpetSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public void removeMossCarpet(BlockState state, Level level, BlockPos pos){
        int currentRadius = TreeHelper.getRadius(state);
        if (currentRadius == 0) return;
        getRoots().ifPresent(roots -> roots.setRadius(level, pos, currentRadius, null, 3));
        Block.popResource(level, pos, new ItemStack(getMossCarpetItem()));
        level.playSound(null, pos, getMossCarpetSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

}
