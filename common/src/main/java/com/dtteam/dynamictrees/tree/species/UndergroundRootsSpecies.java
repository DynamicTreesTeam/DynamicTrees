package com.dtteam.dynamictrees.tree.species;

import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.block.soil.SpeciesBlockEntity;
import com.dtteam.dynamictrees.systems.GrowSignal;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKitConfiguration;
import com.dtteam.dynamictrees.systems.growthlogic.context.PositionalSpeciesContext;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.dtteam.dynamictrees.worldgen.JoCodeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UndergroundRootsSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(UndergroundRootsSpecies::new);

    protected GrowthLogicKitConfiguration rootLogicKit = GrowthLogicKitConfiguration.getDefault();
    private int minWorldGenHeightOffset = 2;
    private int maxWorldGenHeightOffset = 6;
    protected float rootSignalEnergy = 16.0f;
    protected float rootTapering = 0.3f;
    protected int rootGrowthMultiplier = 15;
    protected int updateSoilOnWaterRadius = 5;
    public void setMinWorldGenHeightOffset(int minWorldGenHeightOffset) {
        this.minWorldGenHeightOffset = minWorldGenHeightOffset;
    }

    public void setMaxWorldGenHeightOffset(int maxWorldGenHeightOffset) {
        this.maxWorldGenHeightOffset = maxWorldGenHeightOffset;
    }

    public void setRootGrowthMultiplier(int rootGrowthMultiplier) {
        this.rootGrowthMultiplier = rootGrowthMultiplier;
    }

    public void setUpdateSoilOnWaterRadius(int updateSoilOnWaterRadius) {
        this.updateSoilOnWaterRadius = updateSoilOnWaterRadius;
    }

    public UndergroundRootsSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        if (!(family instanceof UndergroundRootsFamily)) {
            throw new RuntimeException("Family " + family.getRegistryName() + " for Underground Roots Species " + getRegistryName() + " is not of type "+ UndergroundRootsFamily.class);
        }
    }

    public UndergroundRootsFamily getFamily() {
        return (UndergroundRootsFamily) family;
    }

    //////////////////////
    // ROOTY SOIL
    //////////////////////

    public boolean placeRootyDirtBlock(LevelAccessor level, BlockPos rootPos, int fertility) {
        BlockState dirtState = level.getBlockState(rootPos);
        Block dirt = dirtState.getBlock();
        boolean worldGenOnWater = (isWater(dirtState) && fertility == 0);
        if (!SoilHelper.isSoilRegistered(dirt) && !(dirt instanceof SoilBlock) || worldGenOnWater) {
            //soil is not valid so we place default roots
            level.setBlock(rootPos, getFamily().getDefaultSoil().getSoilState(dirtState, fertility, this.doesRequireTileEntity(level, rootPos)), 3);

            BlockEntity tileEntity = level.getBlockEntity(rootPos);
            if (tileEntity instanceof SpeciesBlockEntity speciesTE) {
                speciesTE.setSpecies(this);
            }
            return true;
        }

        return super.placeRootyDirtBlock(level, rootPos, fertility);
    }

    private boolean replaceSoilBlock (BlockState soilState, Level level, BlockPos rootPos, int fertility){
        if (soilState.getBlock() instanceof SoilBlock soilBlock
                && !soilBlock.getSoilProperties().equals(getFamily().getDefaultSoil())){
            BlockEntity TE = level.getBlockEntity(rootPos);
            BlockState rootCollarState = getFamily().getDefaultSoil().getSoilState(soilBlock.getPrimitiveSoilState(soilState), fertility, soilState.getValue(SoilBlock.IS_VARIANT));
            getFamily().getDefaultSoil().getBlock().ifPresent(root -> root.updateRadius(level, rootCollarState, rootPos, 3, true));
            if (TE != null){
                level.setBlockEntity(TE);
                if (TE instanceof SpeciesBlockEntity speciesTE) {
                    speciesTE.setSpecies(this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean postGrow(Level level, BlockPos soilPos, BlockPos treePos, int fertility, boolean natural) {
        int radius = TreeHelper.getRadius(level, treePos);
        BlockState soilState = level.getBlockState(soilPos);

        if (radius >= 8 || (isWater(soilState) && radius >= updateSoilOnWaterRadius)) {
            SoilBlock soilBlock = TreeHelper.getRooty(soilState);
            if (soilBlock == null) return false;
            BlockPos rootPos = soilPos.relative(soilBlock.getTrunkDirection(level, soilPos).getOpposite());
            boolean hasRoots = TreeHelper.isBranch(level.getBlockState(rootPos));
            if (hasRoots){
                replaceSoilBlock(soilState, level, soilPos, fertility);
            }
        }
        return super.postGrow(level, soilPos, treePos, fertility, natural);
    }

    public boolean soilDestroyAction(Level level, @NotNull BlockPos rootPos, BlockState state, @NotNull Player player){
        if (state.hasProperty(SoilBlock.FERTILITY)) {
            return replaceSoilBlock(state, level, rootPos, state.getValue(SoilBlock.FERTILITY));
        }
        return false;
    }

    //////////////////////
    // ROT
    //////////////////////

    public float rotChance(LevelAccessor level, BlockPos pos, RandomSource rand, int radius) {
        BlockState branchState = level.getBlockState(pos);
        if (branchState.getBlock() instanceof BasicRootsBlock){
            if (radius == 0) return 0;
            if (branchState.getValue(BlockStateProperties.WATERLOGGED)) return 0;
            return 0.2f + ((1f / (8 + radius * 4f)));
        }
        return super.rotChance(level, pos, rand, radius);
    }
    public boolean update(Level level, SoilBlock rootyDirt, BlockPos rootPos, int fertility, TreePart treeBase, BlockPos treePos, RandomSource random, boolean natural) {

        //Analyze structure to gather all the root's endpoints.
        BlockPos rootCrownPos = rootPos.below();
        List<BlockPos> rootEnds = getEnds(level, rootCrownPos, TreeHelper.getTreePart(level.getBlockState(rootCrownPos)));

        //Rot roots
        handleRot(level, rootEnds, rootPos, rootCrownPos, fertility, false);

        return super.update(level, rootyDirt, rootPos, fertility, treeBase, treePos, random, natural);
    }

    //////////////////////
    // GENERATION
    //////////////////////

    @Override
    public boolean generate(DynamicTreeGenerationContext context) {
        int yOffset = context.random().nextIntBetweenInclusive(minWorldGenHeightOffset, maxWorldGenHeightOffset)
                - countWaterBlocksBelow(context.level(), context.rootPos(), getAllowedWaterHeightForWorldgen());
        context.rootPos().move(Direction.UP, yOffset);

            if (super.generate(context)
                    && !JoCodeRegistry.getCodes(this.getRegistryName(), true).isEmpty()) {
                final JoCode code = JoCodeRegistry.getRandomCode(this.getRegistryName(), context.radius(), context.random(), true);
                if (code != null) {
                    code.generate(context);
                    return true;
                }
            }

        return false;
    }

    //////////////////////
    // GROWTH
    //////////////////////

    public float getRootSignalEnergy() {
        return rootSignalEnergy;
    }
    public void setRootSignalEnergy(float rootSignalEnergy) {
        this.rootSignalEnergy = rootSignalEnergy;
    }

    public float getRootTapering() {
        return rootTapering;
    }
    public void setRootTapering(float rootTapering) {
        this.rootTapering = rootTapering;
    }

    @Override
    protected GrowSignal sendGrowthSignal(TreePart treeBase, Level level, BlockPos treePos, BlockPos rootPos, Direction defaultDir) {
        GrowSignal treeSignal = super.sendGrowthSignal(treeBase, level, treePos, rootPos, defaultDir);

        for (int i = 0; i< rootGrowthMultiplier; i++){
            BlockPos belowPos = rootPos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (TreeHelper.isBranch(belowState)){
                GrowSignal rootGrowSignal = new GrowSignal(this, rootPos, getRootEnergy(level, rootPos), level.random, defaultDir.getOpposite());
                return TreeHelper.getTreePart(belowState).growSignal(level, belowPos, rootGrowSignal);
            } else if (isAcceptableSoil(belowState)) {
                getFamily().getRoots().ifPresent(branch -> branch.setRadius(level, belowPos, family.getPrimaryThickness(), null));
            }
        }

        return treeSignal;
    }

    public float getRootEnergy(Level level, BlockPos rootPos) {
        return this.rootLogicKit.getEnergy(new PositionalSpeciesContext(level, rootPos, this));
    }

    public Species setRootsGrowthLogicKit(GrowthLogicKit logicKit) {
        this.rootLogicKit = logicKit.getDefaultConfiguration();
        return this;
    }

    /**
     * Set the logic kit used to determine how the tree branch network expands. Provides an alternate and more modular
     * method to override a trees growth logic.
     *
     * @param logicKit A growth logic kit
     * @return this species for chaining
     */
    public Species setRootsGrowthLogicKit(GrowthLogicKitConfiguration logicKit) {
        this.rootLogicKit = logicKit;
        return this;
    }

    public GrowthLogicKitConfiguration getRootsGrowthLogicKit() {
        return rootLogicKit;
    }

}
