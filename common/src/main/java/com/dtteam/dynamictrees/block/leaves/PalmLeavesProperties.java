package com.dtteam.dynamictrees.block.leaves;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.function.BiConsumer;

public class PalmLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(PalmLeavesProperties::new);

    public PalmLeavesProperties(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(BlockBehaviour.Properties properties) {
        return new DynamicPalmLeavesBlock(getBlockRegistryName(),this, properties);
    }

    Identifier frondLoader = DynamicTrees.location("large_palm_fronds");
    public void setFrondLoader(Identifier frondLoader) {
        this.frondLoader = frondLoader;
    }
    public Identifier getFrondLoader () { return frondLoader; }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        return List.of(DynamicTrees.location("palm_fronds"));
    }

    public String getFrondsModelName(){
        return "block/palm_leaves/" + getRegistryName().getPath() + "_frond";
    }
    public String getCoreTopModelName(){
        return "block/palm_leaves/" + getRegistryName().getPath() + "_core_top";
    }
    public String getCoreBottomModelName(){
        return "block/palm_leaves/" + getRegistryName().getPath() + "_core_bottom";
    }

    public static final String FROND = "frond";
    public static final String CORE_TOP = "core_top";
    public static final String CORE_BOTTOM = "core_bottom";
    public void addFrondTextures(BiConsumer<String, Identifier> textureConsumer, Identifier leavesTextureLocation) {
        Identifier leavesLoc = getTexturePath(FROND).orElse(leavesTextureLocation);
        textureConsumer.accept("frond", leavesLoc);
    }

    public void addCoreTextures(BiConsumer<String, Identifier> textureConsumer,
                                   Identifier coreTextureLocation) {
        Identifier coreLoc = getTexturePath(CORE_BOTTOM).orElse(coreTextureLocation);
        textureConsumer.accept("core_bottom", coreLoc);
    }

    public Identifier getCoreTopSmartModelLocation() {
        if (modelOverrides.containsKey(CORE_TOP)) return modelOverrides.get(CORE_TOP);
        return DynamicTrees.location("block/smartmodel/palm/core_top");
    }
    public Identifier getCoreBottomSmartModelLocation() {
        if (modelOverrides.containsKey(CORE_BOTTOM)) return modelOverrides.get(CORE_BOTTOM);
        return DynamicTrees.location("block/smartmodel/palm/core_bottom");
    }

    public static class DynamicPalmLeavesBlock extends DynamicLeavesBlock {

        public static final IntegerProperty DIRECTION = IntegerProperty.create("direction", 0, 8);

        public static final CoordUtils.Surround[][] hydroSurroundMap = new CoordUtils.Surround[][]{
                {}, //distance 0
                {CoordUtils.Surround.NE, CoordUtils.Surround.SE, CoordUtils.Surround.SW, CoordUtils.Surround.NW}, //distance 1
                {CoordUtils.Surround.N, CoordUtils.Surround.E, CoordUtils.Surround.S, CoordUtils.Surround.W}, //distance 2
                {}, //distance 3
                {}, //distance 4
                {}, //distance 5
                {}, //distance 6
                {}  //distance 7
        };

        @Override
        public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
            if (state.getBlock() == this) {
                int dist = state.getValue(DISTANCE);
                if ((dist == 1 || dist == 2) && state.getValue(DIRECTION) == 0) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    return;
                }
            }
            super.randomTick(state, level, pos, rand);
        }

        public DynamicPalmLeavesBlock(Identifier id, LeavesProperties leavesProperties, Properties properties) {
            super(id, leavesProperties, properties);
            registerDefaultState(defaultBlockState().setValue(DIRECTION, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(DIRECTION);
        }

        public static BlockState getDirectionState(BlockState state, CoordUtils.Surround surround) {
            if (state == null) {
                return Blocks.AIR.defaultBlockState();
            }
            if (!state.hasProperty(DIRECTION)) return state;
            return state.setValue(DIRECTION, surround == null ? 0 : surround.ordinal() + 1);
        }

        @Override
        public int getRadiusForConnection(BlockState state, BlockGetter level, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
            return side == Direction.UP && from.getFamily().isCompatibleDynamicLeaves(Species.NULL_SPECIES, state, level, pos) ? fromRadius : 0;
        }

        @Override
        public int branchSupport(BlockState state, BlockGetter level, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
            return branch.getFamily() == getFamily(state, level, pos) ? BranchBlock.setSupport(0, 1) : 0;
        }

        @Override
        public boolean appearanceChangesWithHydro(int oldHydro, int newHydro) {
            return true;
        }

        @Override
        public BlockState getLeavesBlockStateForPlacement(LevelAccessor level, BlockPos pos, BlockState leavesStateWithHydro, int oldHydro, boolean worldGen) {
            for (CoordUtils.Surround surround : CoordUtils.Surround.values()) {
                BlockState offstate = level.getBlockState(pos.offset(surround.getOffset()));
                if (offstate.getBlock() == this && offstate.getValue(DISTANCE) == 3) {
                    return getDirectionState(leavesStateWithHydro, surround);
                }
            }
            return leavesStateWithHydro;
        }

        @Override
        protected VoxelShape getOcclusionShape(BlockState state) {
            AABB base = super.getOcclusionShape(state).bounds();
            base.inflate(1, 0, 1);
            base.inflate(-1, -0, -1);
            return Shapes.create(base);
        }

    }

//    @Override
//    protected String getBlockRegistryNameSuffix() {
//        return "_fronds";
//    }

}
