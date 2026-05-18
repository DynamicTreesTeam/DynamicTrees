package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.MossyRootsBlock;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.utility.IdentifierUtils.suffix;

public class MossyAerialRootsFamily extends AerialRootsFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MossyAerialRootsFamily::new);

    private Supplier<BranchBlock> mossyRoots;
    /**
     * Must be a block item to be placed and clicked on
     */
    private BlockItem mossCarpet;

    public MossyAerialRootsFamily(Identifier name) {
        super(name);
    }

    ///////////////////////////////////////////
    // MOSSY ROOTS
    ///////////////////////////////////////////

    @Override
    public void setupBlocks() {
        super.setupBlocks();
        this.setMossyRoots(this.createMossyRoots(this.getBranchName("mossy_")));
        this.createRootsItem(this.getBranchName().withPrefix("mossy_"), this.mossyRoots);
    }

    public Family setMossyRoots(final Supplier<BranchBlock> branchSup) {
        this.mossyRoots = setupBranch(branchSup, false);
        return this;
    }

    public Optional<BranchBlock> getMossyRoots() {
        return Optionals.ofBlock(mossyRoots);
    }

    protected Supplier<BranchBlock> createMossyRoots(final Identifier id) {
        Identifier name = suffix(id, getRootsNameSuffix());
        return RegistryHandler.addBlock(name, () -> createMossyRootsBlock(name));
    }

    protected BranchBlock createMossyRootsBlock(Identifier name) {
        final BasicRootsBlock branch = new MossyRootsBlock(name, this.getProperties());
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
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
        if (this.mossyRoots != null) {
            this.mossyRoots.get().setPrimitiveLogDrops(List.of(()->new ItemStack(primitiveRoots)));
        }
    }

    ///////////////////////////////////////////
    // BEHAVIOR
    ///////////////////////////////////////////

    public SoundType getMossCarpetSoundType(){
        return getMossCarpetBlock().defaultBlockState().getSoundType();
    }

    @Override
    protected BranchBlock createRootsBlock(Identifier name) {
        final BasicRootsBlock branch = new BasicRootsBlock(name, this.getProperties()){
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
        if (this.isFireProof()) branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
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
