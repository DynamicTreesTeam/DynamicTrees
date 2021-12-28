package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.RootyBlockDecayer;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.cells.Cell;
import com.ferreusveritas.dynamictrees.api.cells.CellNull;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.treedata.TreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockWithDynamicHardness;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.GrowSignal;
import com.ferreusveritas.dynamictrees.tileentity.SpeciesTileEntity;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A version of Rooty Dirt block that holds on to a species with a TileEntity.
 * <p>
 * When to use this: You can't determine a species of a tree family by location alone (e.g. Swamp Oak by biome) The
 * species is rare and you don't want to commit all the resources necessary to make a whole tree family(e.g. Apple Oak)
 * <p>
 * This is a great method for creating numerous fruit species(Pam's Harvestcraft) under one {@link Family} family.
 *
 * @author ferreusveritas
 */
@SuppressWarnings("deprecation")
public class RootyBlock extends BlockWithDynamicHardness implements TreePart {

    public static RootyBlockDecayer rootyBlockDecayer = null;

    public static final IntegerProperty FERTILITY = IntegerProperty.create("fertility", 0, 15);
    public static final BooleanProperty IS_VARIANT = BooleanProperty.create("is_variant");

    private final SoilProperties properties;
    //private ConfiguredSoilProperties<SoilProperties> configuredProperties = ConfiguredSoilProperties.NULL_CONFIGURED_SOIL_PROPERTIES;

    public RootyBlock(SoilProperties properties, Properties blockProperties) {
        super(blockProperties.randomTicks());
        this.properties = properties;
        registerDefaultState(defaultBlockState().setValue(FERTILITY, 0).setValue(IS_VARIANT, false));
    }

    ///////////////////////////////////////////
    // SOIL PROPERTIES
    ///////////////////////////////////////////

    public SoilProperties getSoilProperties() {
        return properties;
    }

    public Block getPrimitiveSoilBlock() {
        return properties.getPrimitiveSoilBlock();
    }

    ///////////////////////////////////////////
    // BLOCK PROPERTIES
    ///////////////////////////////////////////

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getPrimitiveSoilBlock().getSoundType(getPrimitiveSoilBlock().defaultBlockState(), world, pos, entity);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return getPrimitiveSoilBlock().getLightValue(getPrimitiveSoilBlock().defaultBlockState(), world, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) {
        return getPrimitiveSoilBlock().propagatesSkylightDown(getPrimitiveSoilBlock().defaultBlockState(), p_200123_2_, p_200123_3_);
    }

    @Override
    public int getLightBlock(BlockState p_200011_1_, IBlockReader p_200011_2_, BlockPos p_200011_3_) {
        return getPrimitiveSoilBlock().getLightBlock(getPrimitiveSoilBlock().defaultBlockState(), p_200011_2_, p_200011_3_);
    }

    @Nullable
    @Override
    public ToolType getHarvestTool(BlockState state) {
        return getPrimitiveSoilBlock().getHarvestTool(getPrimitiveSoilBlock().defaultBlockState());
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return getPrimitiveSoilBlock().getHarvestLevel(getPrimitiveSoilBlock().defaultBlockState());
    }

    @Override
    public MaterialColor defaultMaterialColor() {
        return getPrimitiveSoilBlock().defaultMaterialColor();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return getPrimitiveSoilBlock().getShape(getDecayBlockState(state, reader, pos), reader, pos, context);
    }

    @Override
    public float getFriction() {
        return getPrimitiveSoilBlock().getFriction();
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return getPrimitiveSoilBlock().getExplosionResistance(getPrimitiveSoilBlock().defaultBlockState(), world, pos, explosion);
    }

    @Override
    public float getSpeedFactor() {
        return getPrimitiveSoilBlock().getSpeedFactor();
    }

    @Override
    public float getJumpFactor() {
        return getPrimitiveSoilBlock().getJumpFactor();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return getPrimitiveSoilBlock().getFireSpreadSpeed(getPrimitiveSoilBlock().defaultBlockState(), world, pos, face);
    }

    @Override
    public boolean isFireSource(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return getPrimitiveSoilBlock().isFireSource(getPrimitiveSoilBlock().defaultBlockState(), world, pos, side);
    }

    @Nonnull
    @Override
    public List<ItemStack> getDrops(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
        return getPrimitiveSoilBlock().defaultBlockState().getDrops(builder);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        return getPrimitiveSoilBlock().getPickBlock(getPrimitiveSoilBlock().defaultBlockState(), target, world, pos, player);
    }

    @Override
    public float getHardness(IBlockReader worldIn, BlockPos pos) {
        return (float) (getPrimitiveSoilBlock().defaultBlockState().getDestroySpeed(worldIn, pos) * DTConfigs.ROOTY_BLOCK_HARDNESS_MULTIPLIER.get());
    }

    ///////////////////////////////////////////
    // BLOCKSTATES
    ///////////////////////////////////////////

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FERTILITY).add(IS_VARIANT);
    }

    ///////////////////////////////////////////
    // INTERACTION
    ///////////////////////////////////////////

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        if (state.getValue(IS_VARIANT)) {
            return new SpeciesTileEntity();
        }
        return null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state.getValue(IS_VARIANT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        if (random.nextInt(DTConfigs.TREE_GROWTH_FOLDING.get()) == 0) {
            updateTree(state, worldIn, pos, random, true);
        }
    }

    public Direction getTrunkDirection(IBlockReader access, BlockPos rootPos) {
        return Direction.UP;
    }

    public void updateTree(BlockState rootyState, World world, BlockPos rootPos, Random random, boolean natural) {

        if (CoordUtils.isSurroundedByLoadedChunks(world, rootPos)) {

            boolean viable = false;

            Species species = getSpecies(rootyState, world, rootPos);

            if (species.isValid()) {
                BlockPos treePos = rootPos.relative(getTrunkDirection(world, rootPos));
                TreePart treeBase = TreeHelper.getTreePart(world.getBlockState(treePos));
                if (treeBase != TreeHelper.NULL_TREE_PART) {
                    viable = species.update(world, this, rootPos, getFertility(rootyState, world, rootPos), treeBase, treePos, random, natural);
                }
            }

            if (!viable) {
                //TODO: Attempt to destroy what's left of the tree before setting rooty to dirt
                world.setBlock(rootPos, getDecayBlockState(rootyState, world, rootPos), 3);
            }

        }

    }

    /**
     * This is the state the rooty dirt returns to once it no longer supports a tree structure.
     *
     * @param access
     * @param pos    The position of the {@link RootyBlock}
     * @return
     */
    public BlockState getDecayBlockState(BlockState state, IBlockReader access, BlockPos pos) {
        return properties.getPrimitiveSoilState(state);
    }

    /**
     * Forces the {@link RootyBlock} to decay if it's there, turning it back to its primitive soil block. Custom decay
     * logic is also supported, see {@link RootyBlockDecayer} for details.
     *
     * @param world      The {@link World} instance.
     * @param rootPos    The {@link BlockPos} of the {@link RootyBlock}.
     * @param rootyState The {@link BlockState} of the {@link RootyBlock}.
     * @param species    The {@link Species} of the tree that was removed.
     */
    public void doDecay(World world, BlockPos rootPos, BlockState rootyState, Species species) {
        if (world.isClientSide || !TreeHelper.isRooty(rootyState)) {
            return;
        }

        this.updateTree(rootyState, world, rootPos, world.random, true); // This will turn the rooty dirt back to it's default soil block.
        final BlockState newState = world.getBlockState(rootPos);

        // Make sure we're not still a rooty block and return if custom decay returns true.
        if (TreeHelper.isRooty(newState) || (rootyBlockDecayer != null && rootyBlockDecayer.decay(world, rootPos, rootyState, species))) {
            return;
        }

        final BlockState primitiveDirt = getDecayBlockState(rootyState, world, rootPos);

        world.setBlock(rootPos, primitiveDirt, Constants.BlockFlags.DEFAULT);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        return getFertility(blockState, world, pos);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        return getFamily(state, worldIn, pos).onTreeActivated(worldIn, pos, state, player, handIn, player.getItemInHand(handIn), hit) ? ActionResultType.SUCCESS : ActionResultType.FAIL;
    }

    public void destroyTree(World world, BlockPos rootPos) {
        Optional<BranchBlock> branch = TreeHelper.getBranchOpt(world.getBlockState(rootPos.above()));

        if (branch.isPresent()) {
            BranchDestructionData destroyData = branch.get().destroyBranchFromNode(world, rootPos.above(), Direction.DOWN, true, null);
            FallingTreeEntity.dropTree(world, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
        }
    }

    @Override
    public void playerWillDestroy(World world, @Nonnull BlockPos pos, BlockState state, @Nonnull PlayerEntity player) {
        this.destroyTree(world, pos);
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        destroyTree(world, pos);
        super.onBlockExploded(state, world, pos, explosion);
    }


    @Nonnull
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    ///////////////////////////////////////////
    // TREE STUFF
    ///////////////////////////////////////////

    public int getFertility(BlockState blockState, IBlockReader blockAccess, BlockPos pos) {
        return blockState.getValue(FERTILITY);
    }

    public void setFertility(World world, BlockPos rootPos, int fertility) {
        final BlockState currentState = world.getBlockState(rootPos);
        final Species species = this.getSpecies(currentState, world, rootPos);

        world.setBlock(rootPos, currentState.setValue(FERTILITY, MathHelper.clamp(fertility, 0, 15)), 3);
        world.updateNeighborsAt(rootPos, this); // Notify all neighbors of NSEWUD neighbors (for comparator).
        this.setSpecies(world, rootPos, species);
    }

    public boolean fertilize(World world, BlockPos pos, int amount) {
        int fertility = this.getFertility(world.getBlockState(pos), world, pos);
        if ((fertility == 0 && amount < 0) || (fertility == 15 && amount > 0)) {
            return false;//Already maxed out
        }
        setFertility(world, pos, fertility + amount);
        return true;
    }

    @Override
    public Cell getHydrationCell(IBlockReader reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesTree) {
        return CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    @Override
    public int getRadius(BlockState state) {
        return 8;
    }

    @Override
    public int getRadiusForConnection(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        return 8;
    }

    @Override
    public int probabilityForBlock(BlockState state, IBlockReader reader, BlockPos pos, BranchBlock from) {
        return 0;
    }

    /**
     * Analysis typically begins with the root node.  This function allows the rootyBlock to direct the analysis in the
     * direction of the tree since trees are not always "up" from the rootyBlock
     *
     * @param world
     * @param rootPos
     * @param signal
     * @return
     */
    public MapSignal startAnalysis(IWorld world, BlockPos rootPos, MapSignal signal) {
        Direction dir = getTrunkDirection(world, rootPos);
        BlockPos treePos = rootPos.relative(dir);
        BlockState treeState = world.getBlockState(treePos);

        TreeHelper.getTreePart(treeState).analyse(treeState, world, treePos, null, signal);

        return signal;
    }

    @Override
    public boolean shouldAnalyse(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public MapSignal analyse(BlockState state, IWorld world, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
        signal.run(state, world, pos, fromDir);//Run inspector of choice

        if (signal.root == null) {
            signal.root = pos;
        } else {
            signal.multiroot = true;
        }

        signal.foundRoot = true;

        return signal;
    }

    @Override
    public int branchSupport(BlockState state, IBlockReader reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return dir == Direction.DOWN ? BranchBlock.setSupport(1, 1) : 0;
    }

    @Override
    public Family getFamily(BlockState state, IBlockReader reader, BlockPos rootPos) {
        BlockPos treePos = rootPos.relative(getTrunkDirection(reader, rootPos));
        BlockState treeState = reader.getBlockState(treePos);
        return TreeHelper.isBranch(treeState) ? TreeHelper.getBranch(treeState).getFamily(treeState, reader, treePos) : Family.NULL_FAMILY;
    }

    @Nullable
    private SpeciesTileEntity getTileEntitySpecies(IWorld world, BlockPos pos) {
<<<<<<< HEAD
        final TileEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof SpeciesTileEntity ? (SpeciesTileEntity) blockEntity : null;
=======
        final TileEntity tileEntity = world.getBlockEntity(pos);
        return tileEntity instanceof SpeciesTileEntity ? (SpeciesTileEntity) tileEntity : null;
>>>>>>> develop/1.16.5
    }

    /**
     * Rooty Dirt can report whatever {@link Family} species it wants to be. We'll use a stored value to determine the
     * species for the {@link TileEntity} version. Otherwise we'll just make it report whatever {@link DynamicTrees} the
     * above {@link BranchBlock} says it is.
     */
    public Species getSpecies(BlockState state, IWorld world, BlockPos rootPos) {

        Family tree = getFamily(state, world, rootPos);

        SpeciesTileEntity rootyDirtTE = getTileEntitySpecies(world, rootPos);

        if (rootyDirtTE != null) {
            Species species = rootyDirtTE.getSpecies();
            if (species.getFamily() == tree) {//As a sanity check we should see if the tree and the stored species are a match
                return rootyDirtTE.getSpecies();
            }
        }

        return tree.getSpeciesForLocation(world, rootPos.relative(getTrunkDirection(world, rootPos)));
    }

    public void setSpecies(World world, BlockPos rootPos, Species species) {
        SpeciesTileEntity rootyDirtTE = getTileEntitySpecies(world, rootPos);
        if (rootyDirtTE != null) {
            rootyDirtTE.setSpecies(species);
        }
    }

    public final TreePartType getTreePartType() {
        return TreePartType.ROOT;
    }

    @Override
    public final boolean isRootNode() {
        return true;
    }

    ///////////////////////////////////////////
    // RENDERING
    ///////////////////////////////////////////

    public int colorMultiplier(BlockColors blockColors, BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tintIndex) {
        final int white = 0xFFFFFFFF;
        switch (tintIndex) {
            case 0:
                return blockColors.getColor(getPrimitiveSoilBlock().defaultBlockState(), world, pos, tintIndex);
            case 1:
                return state.getBlock() instanceof RootyBlock ? rootColor(state, world, pos) : white;
            default:
                return white;
        }
    }

    public boolean getColorFromBark() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public int rootColor(BlockState state, IBlockReader blockAccess, BlockPos pos) {
        return getFamily(state, blockAccess, pos).getRootColor(state, getColorFromBark());
    }

    public boolean fallWithTree(BlockState state, World world, BlockPos pos) {
        return false;
    }

}
