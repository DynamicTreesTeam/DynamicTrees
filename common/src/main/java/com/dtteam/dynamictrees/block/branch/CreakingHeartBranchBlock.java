package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.DTLootTableBuilder;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.function.BiConsumer;

public class CreakingHeartBranchBlock extends BasicBranchBlock implements EntityBlock {

    public static final EnumProperty<CreakingHeartState> STATE = BlockStateProperties.CREAKING_HEART_STATE;
    public static final BooleanProperty HIDDEN = BooleanProperty.create("hidden");

    public CreakingHeartBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    @Override
    public BlockState[] createBranchStates(IntegerProperty radiusProperty, int maxRadius) {
        registerDefaultState(defaultBlockState().setValue(STATE, CreakingHeartState.DORMANT).setValue(HIDDEN, true));
        return super.createBranchStates(radiusProperty, maxRadius);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE, HIDDEN);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        ticks.scheduleTick(pos, this, 1);
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState newState = updateState(state, level, pos);
        if (newState != state) {
            level.setBlock(pos, newState, 3);
        }
    }

    private static BlockState updateState(BlockState state, Level level, BlockPos pos) {
        boolean hasLogs = CreakingHeartBlock.hasRequiredLogs(state, level, pos);
        boolean disabled = state.getValue(STATE) == CreakingHeartState.UPROOTED;
        CreakingHeartState wakeState = level.environmentAttributes().getValue(net.minecraft.world.attribute.EnvironmentAttributes.CREAKING_ACTIVE, pos)
                ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT;
        return hasLogs && disabled ? state.setValue(STATE, wakeState) : state;
    }

    public static boolean hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).getBlock() instanceof BranchBlock) {
                count++;
            }
            if (count >= 2) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    public int setRadius(LevelAccessor level, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
        BlockState previous = level.getBlockState(pos);
        BlockState next = getStateForRadius(radius);
        if (previous.hasProperty(STATE) && next.hasProperty(STATE)) {
            CreakingHeartState heartState = previous.getValue(STATE);
            if (heartState == CreakingHeartState.UPROOTED) {
                heartState = CreakingHeartState.DORMANT;
            }
            next = next.setValue(STATE, heartState);
        }
        if (previous.hasProperty(HIDDEN) && next.hasProperty(HIDDEN)) {
            next = next.setValue(HIDDEN, previous.getValue(HIDDEN));
        } else if (next.hasProperty(HIDDEN) && !DTConfigs.SERVER.hideCreakingHeart.get()) {
            next = next.setValue(HIDDEN, false);
        }
        boolean replacingWater = previous.getFluidState() == net.minecraft.world.level.material.Fluids.WATER.getSource(false);
        if (next.hasProperty(WATERLOGGED)) {
            next = next.setValue(WATERLOGGED, replacingWater && radius <= 7);
        }
        level.setBlock(pos, next, flags);
        return radius;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreakingHeartBranchBlockEntity(blockPos, blockState);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int b0, int b1) {
        super.triggerEvent(state, level, pos, b0, b1);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(b0, b1);
    }

    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> actual, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
        return expected == actual ? (BlockEntityTicker<A>) ticker : null;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return blockState.getValue(STATE) != CreakingHeartState.UPROOTED
                ? createTickerHelper(type, DTRegistries.CREAKING_HEART_BLOCK_ENTITY.get(), CreakingHeartBranchBlockEntity::serverTick)
                : null;
    }

    @Override
    public void futureBreak(BlockState state, Level level, BlockPos cutPos, LivingEntity entity) {
        if (level.getBlockEntity(cutPos) instanceof CreakingHeartBranchBlockEntity creakingHeartBlockEntity
                && entity instanceof Player player) {
            creakingHeartBlockEntity.removeProtector(player.damageSources().playerAttack(player));
            this.tryAwardExperience(player, level, cutPos);
        }
        super.futureBreak(state, level, cutPos, entity);
    }

    @Override
    protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> onHit) {
        if (level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity
                && explosion instanceof ServerExplosion serverExplosion
                && explosion.getBlockInteraction().shouldAffectBlocklikeEntities()) {
            creakingHeartBlockEntity.removeProtector(serverExplosion.getDamageSource());
            if (explosion.getIndirectSourceEntity() instanceof Player player) {
                this.tryAwardExperience(player, level, pos);
            }
        }
        super.onExplosionHit(state, level, pos, explosion, onHit);
    }

    private void tryAwardExperience(Player player, Level level, BlockPos pos) {
        if (!player.preventsBlockDrops() && !player.isSpectator() && level instanceof ServerLevel serverLevel) {
            this.popExperience(serverLevel, pos, level.getRandom().nextIntBetweenInclusive(20, 24));
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (state.getValue(STATE) == CreakingHeartState.UPROOTED) {
            return 0;
        }
        if (level.getBlockEntity(pos) instanceof CreakingHeartBranchBlockEntity creakingHeartBlockEntity) {
            return creakingHeartBlockEntity.getAnalogOutputSignal();
        }
        return 0;
    }

    @Override
    public Optional<Block> getPrimitiveLog() {
        return ((CreakingHeartFamily) getFamily()).getPrimitiveHeartLog();
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        if (state.getValue(HIDDEN) && getFamily().getBranch().isPresent()) {
            return getFamily().getBranch().map(block -> block.defaultBlockState().getSoundType()).orElseGet(() -> super.getSoundType(state));
        }
        return super.getSoundType(state);
    }

    @Nullable
    public static BlockPos findFromBranch(BlockState state, BlockGetter level, BlockPos pos, int stepsLeft, HashSet<BlockPos> explored, @Nullable Direction from) {
        if (state.getBlock() instanceof CreakingHeartBranchBlock) {
            if (state.getValue(STATE) == CreakingHeartState.UPROOTED) {
                return null;
            }
            return pos;
        }
        if (stepsLeft <= 0) {
            return null;
        }
        explored.add(pos);
        for (Direction dir : Direction.values()) {
            if (dir == from) {
                continue;
            }
            BlockPos sidePos = pos.relative(dir);
            if (explored.contains(sidePos)) {
                continue;
            }
            BlockState sideState = level.getBlockState(sidePos);
            if (TreeHelper.isBranch(sideState)) {
                BlockPos foundPos = findFromBranch(sideState, level, sidePos, stepsLeft - 1, explored, dir.getOpposite());
                if (foundPos != null) {
                    return foundPos;
                }
            }
        }
        return null;
    }

    @Nullable
    public static BlockPos findFromBranch(BlockState state, BlockGetter level, BlockPos pos, int stepsLeft) {
        return findFromBranch(state, level, pos, stepsLeft, new HashSet<>(), null);
    }

    @Override
    public LootTable.Builder createBranchDrops(HolderLookup.Provider registries) {
        return DTLootTableBuilder.createCreakingHeartDrops(getPrimitiveLog().get(),
                ((CreakingHeartFamily) getFamily()).getResinItem(), 1, 3, registries);
    }

    public void addResinToBranch(BlockState state, Level level, BlockPos pos) {
        CreakingHeartFamily family = (CreakingHeartFamily) getFamily();
        if (family.getResinBranch().isEmpty() || family.getBranch().isEmpty()) {
            return;
        }
        BranchBlock branchBlock = TreeHelper.getBranch(state);
        if (branchBlock == null || branchBlock != family.getBranch().get()) {
            return;
        }
        int radius = TreeHelper.getTreePart(state).getRadius(state);
        family.getResinBranch().get().setRadius(level, pos, radius, null, 3);
    }

    @Override
    public boolean canBeStripped(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        return state.getValue(HIDDEN) && Services.INTERACTION.canToolAxeStrip(heldItem);
    }

    @Override
    public void stripBranch(BlockState state, LevelAccessor level, BlockPos pos, int radius) {
        level.setBlock(pos, state.setValue(HIDDEN, false), 3);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (state.getValue(HIDDEN)) {
            if (!level.isClientSide()) {
                level.levelEvent(null, 2001, pos, Block.getId(state));
            }
            level.setBlock(pos, state.setValue(HIDDEN, false), 3);
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        float hardness = super.getHardness(state, level, pos);
        if (state.getValue(HIDDEN) && getFamily() instanceof CreakingHeartFamily heartFamily) {
            return hardness * heartFamily.getHiddenHeartHardnessMultiplier();
        }
        return hardness;
    }

}
