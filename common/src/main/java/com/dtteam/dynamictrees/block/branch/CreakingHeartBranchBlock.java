package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.data.DTLootTableBuilder;
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
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
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
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;

public class CreakingHeartBranchBlock extends BasicBranchBlock implements EntityBlock {

    public static final EnumProperty<CreakingHeartState> STATE = BlockStateProperties.CREAKING_HEART_STATE;

    public CreakingHeartBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    @Override
    public BlockState[] createBranchStates(IntegerProperty radiusProperty, int maxRadius) {
        registerDefaultState(defaultBlockState().setValue(STATE, CreakingHeartState.DORMANT));
        return super.createBranchStates(radiusProperty, maxRadius);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STATE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
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
        CreakingHeartState wakeState = level.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, pos) ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT;
        return hasLogs && disabled ? state.setValue(STATE, wakeState) : state;
    }

    public static boolean hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()){
            if (level.getBlockState(pos.offset(dir.getUnitVec3i())).getBlock() instanceof BranchBlock)
                count++;
            if (count >= 2) return true;
        }
        return false;
    }

    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    @Override
    public BlockState getStateForRadius(int radius, BlockState previousState) {
        BlockState state = super.getStateForRadius(radius, previousState);
        if (previousState.hasProperty(CreakingHeartBranchBlock.STATE)){
            CreakingHeartState heartState = previousState.getValue(CreakingHeartBranchBlock.STATE);
            if (heartState == CreakingHeartState.UPROOTED) heartState = CreakingHeartState.DORMANT;
            return state.setValue(CreakingHeartBranchBlock.STATE, heartState);
        }
        return state;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreakingHeartBranchBlockEntity(blockPos, blockState);
    }

    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int b0, int b1) {
        super.triggerEvent(state, level, pos, b0, b1);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null ? false : blockEntity.triggerEvent(b0, b1);
    }

    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> @Nullable BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> actual, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker) {
        return expected == actual ? (BlockEntityTicker<A>) ticker : null;
    }

    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;

        return blockState.getValue(STATE) != CreakingHeartState.UPROOTED
                ? createTickerHelper(type, DTRegistries.CREAKING_HEART_BLOCK_ENTITY.get(), CreakingHeartBranchBlockEntity::serverTick)
                : null;
    }

    @Override
    public void futureBreak(BlockState state, Level level, BlockPos cutPos, LivingEntity entity) {
        BlockEntity var6 = level.getBlockEntity(cutPos);
        if (var6 instanceof CreakingHeartBranchBlockEntity creakingHeartBlockEntity && entity instanceof Player player) {
            creakingHeartBlockEntity.removeProtector(player.damageSources().playerAttack(player));
            this.tryAwardExperience(player, level, cutPos);
        }

        super.futureBreak(state, level, cutPos, entity);
    }

    @Override
    public void onBlockExploded(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof CreakingHeartBlockEntity creakingHeartBlockEntity
                && explosion instanceof ServerExplosion serverExplosion
                && explosion.getBlockInteraction().shouldAffectBlocklikeEntities()) {
            creakingHeartBlockEntity.removeProtector(serverExplosion.getDamageSource());

            if (explosion.getIndirectSourceEntity() instanceof Player player) {
                this.tryAwardExperience(player, level, pos);
            }
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    private void tryAwardExperience(Player player, Level level, BlockPos pos) {
        if (!player.preventsBlockDrops() && !player.isSpectator() && level instanceof ServerLevel serverLevel) {
            this.popExperience(serverLevel, pos, level.getRandom().nextIntBetweenInclusive(20, 24));
        }
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (state.getValue(STATE) == CreakingHeartState.UPROOTED) return 0;

        if (level.getBlockEntity(pos) instanceof CreakingHeartBranchBlockEntity creakingHeartBlockEntity)
            return creakingHeartBlockEntity.getAnalogOutputSignal();

        return 0;
    }

    @Override
    public Optional<Block> getPrimitiveLog() {
        return ((CreakingHeartFamily)getFamily()).getPrimitiveHeartLog();
    }

    @Override
    protected SoundType getSoundType(BlockState state) {
        return getPrimitiveLog().map(block -> block.defaultBlockState().getSoundType()).orElseGet(() -> super.getSoundType(state));
    }

    /**
     * We unfortunately cannot use {@link BranchBlock#analyse(BlockState, LevelAccessor, BlockPos, Direction, MapSignal)}
     * As it requires a {@link LevelAccessor} and we only have a {@link BlockGetter}.
     * We use BFS instead. Should be fine as long as the trees remain small
     */
    @Nullable
    public static BlockPos findFromBranch(BlockState state, BlockGetter level, BlockPos pos, int stepsLeft, HashSet<BlockPos> explored, @Nullable Direction from){
        if (state.getBlock() instanceof CreakingHeartBranchBlock) {
            //Deactivated hearts don't count.
            if (state.getValue(STATE) == CreakingHeartState.UPROOTED) return null;
            return pos;
        }
        if (stepsLeft <= 0) return null;
        explored.add(pos);
        for (Direction dir : Direction.values()){
            if (dir == from) continue;
            BlockPos sidePos = pos.offset(dir.getUnitVec3i());
            if (explored.contains(sidePos)) continue;
            BlockState sideState = level.getBlockState(sidePos);
            if (TreeHelper.isBranch(sideState)){
                BlockPos foundPos = findFromBranch(sideState, level, sidePos, stepsLeft-1, explored, dir.getOpposite());
                if (foundPos != null) return foundPos;
            }
        }
        return null;
    }
    @Nullable
    public static BlockPos findFromBranch(BlockState state, BlockGetter level, BlockPos pos, int stepsLeft){
        return findFromBranch(state, level, pos, stepsLeft, new HashSet<>(), null);
    }

    @Override
    public LootTable.Builder createBranchDrops(HolderLookup.Provider registries) {
        return DTLootTableBuilder.createCreakingHeartDrops(getPrimitiveLog().get(),
                ((CreakingHeartFamily)getFamily()).getResinItem(), 1, 3, registries);
    }

    public void addResinToBranch(BlockState state, Level level, BlockPos pos){
        CreakingHeartFamily family = (CreakingHeartFamily)getFamily();
        if (family.getAltBranch().isEmpty() || family.getBranch().isEmpty()) return;
        BranchBlock branchBlock = TreeHelper.getBranch(state);
        if (branchBlock == null || branchBlock != family.getBranch().get()) return;
        int radius = TreeHelper.getRadius(state);
        family.getAltBranch().get().setRadius(level, pos, radius, null, 3);
    }
}
