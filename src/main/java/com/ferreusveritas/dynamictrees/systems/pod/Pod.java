package com.ferreusveritas.dynamictrees.systems.pod;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.GrowableBlock;
import com.ferreusveritas.dynamictrees.block.OffsetablePodBlock;
import com.ferreusveritas.dynamictrees.block.PodBlock;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.tree.Resettable;
import com.ferreusveritas.dynamictrees.util.AgeProperties;
import com.ferreusveritas.dynamictrees.util.LazyValue;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.OffsetProperties;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

import static com.ferreusveritas.dynamictrees.compat.season.SeasonHelper.isSeasonBetween;

/**
 * Stores properties and implements functionality of pods which grow from the branches of a tree.
 *
 * @author Harley O'Connor
 */
public class Pod extends RegistryEntry<Pod> implements Resettable<Pod> {

    public final class BlockShapeData {
        private final Map<Direction, VoxelShape[]> facingShapes = Util.make(Maps.newHashMap(), facingShapes -> {
            facingShapes.put(Direction.NORTH, new VoxelShape[] {
                    Shapes.block(), Shapes.block(), Shapes.block(), Shapes.block()
            });
            facingShapes.put(Direction.SOUTH, new VoxelShape[] {
                    Shapes.block(), Shapes.block(), Shapes.block(), Shapes.block()
            });
            facingShapes.put(Direction.WEST, new VoxelShape[] {
                    Shapes.block(), Shapes.block(), Shapes.block(), Shapes.block()
            });
            facingShapes.put(Direction.EAST, new VoxelShape[] {
                    Shapes.block(), Shapes.block(), Shapes.block(), Shapes.block()
            });
        });

        public VoxelShape getShapeFor(Direction facing, int age) {
            return facingShapes.get(facing)[age];
        }

        public void setShapesFor(Direction facing, VoxelShape[] shapes) {
            if (shapes.length <= maxAge) {
                throw new IllegalArgumentException("Insufficient number of block shapes provided for the maximum age "+maxAge+" on pod "+this);
            }
            facingShapes.put(facing, shapes);
        }
    }

    public static final TypedRegistry.EntryType<Pod> TYPE = TypedRegistry.newType(Pod::new);

    public static final Pod NULL = new Pod(DTTrees.NULL);

    /**
     * Central registry for all {@link Pod} objects.
     */
    public static final TypedRegistry<Pod> REGISTRY = new TypedRegistry<>(Pod.class, NULL, TYPE);

    private Supplier<PodBlock> block;


    private int maxAge = 2;

    /**
     * The block's age state property. Changes with {@link #maxAge} to allow for customisation of the maximum age
     * of the pod.
     */
    private IntegerProperty ageProperty = BlockStateProperties.AGE_2;

    private IntegerProperty offsetProperty;

    private final BlockShapeData blockShapeData = new BlockShapeData();

    /**
     * Sets whether the pod can be bone-mealed to accelerate growth. Defaults to {@link DTConfigs#CAN_BONE_MEAL_PODS}.
     */
    private boolean canBoneMeal = true;

    /**
     * The item stack for this pod. Note that this is only used for the pick block functionality, drops must be set
     * up using vanilla loot tables.
     */
    private ItemStack itemStack;

    private float growthChance = 0.2F;

    @Nullable
    private Float seasonOffset = 0f;

    private float flowerHoldPeriodLength = 0.5F;

    private float minProductionFactor = 0.3F;

    private int minRadius = 8;
    private int maxRadius = 8;

    private GrowableBlock.MatureAction matureAction = GrowableBlock.MatureAction.DEFAULT;

    public Pod(ResourceLocation registryName) {
        super(registryName);
    }

    /**
     * @throws IllegalStateException if this was called before the block has been created
     */
    public final PodBlock getBlock() {
        if (block == null) {
            throw new IllegalStateException("Invoked too early (before the block was created).");
        }
        return block.get();
    }

    /**
     * Creates and sets the {@link PodBlock} for this pod.
     *
     * @param name       the name to set for the block, or {@code null} to use the pod's name
     * @param properties the properties of the block. May be the {@linkplain #getDefaultBlockProperties default
     *                   properties} or a modification of them.
     */
    public final void createBlock(@Nullable ResourceLocation name, Block.Properties properties) {
        block = RegistryHandler.addBlock(name == null ? this.getRegistryName() : name, () -> createBlock(properties));
    }

    protected PodBlock createBlock(Block.Properties properties) {
        if (hasVariableOffset())
            return new OffsetablePodBlock(properties, this);
        else return new PodBlock(properties, this);
    }

    public MapColor getDefaultMapColor() {
        return MapColor.PLANT;
    }

    public BlockBehaviour.Properties getDefaultBlockProperties() {
        return getDefaultBlockProperties(this.getDefaultMapColor());
    }

    public BlockBehaviour.Properties getDefaultBlockProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .noCollission()
                .pushReaction(PushReaction.DESTROY)
                .sound(SoundType.CROP)
                .randomTicks()
                .strength(0.3F);
    }

    public final IntegerProperty getAgeProperty() {
        return ageProperty;
    }

    public final int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        this.ageProperty = AgeProperties.getOrCreate(maxAge);
    }

    public final VoxelShape getBlockShape(Direction facing, int age) {
        return blockShapeData.getShapeFor(facing, age);
    }

    /**
     * @param blockShapes the block shapes to set; indexed by the age the respective shape is for
     * @throws IllegalArgumentException if the specified {@code blockShapes} array does not contain a shape for each age
     */
    public void setBlockShapes(Direction facing, VoxelShape[] blockShapes) {
        blockShapeData.setShapesFor(facing, blockShapes);
    }

    public final boolean canBoneMeal() {
        return canBoneMeal;
    }

    public void setCanBoneMeal(boolean canBoneMeal) {
        this.canBoneMeal = canBoneMeal;
    }

    /**
     * @throws IllegalStateException if the pod does not have an item set
     * @return a copy of this pod's item stack
     */
    public final ItemStack getItemStack() {
        if (itemStack == null) {
            throw new IllegalStateException("Invoked too early or item was not set on \"" + getRegistryName() + "\".");
        }
        return itemStack.copy();
    }

    /**
     * @return {@code true} if the given {@code itemStack} matches this Pod's item
     */
    public boolean isItem(ItemStack itemStack) {
        return ItemStack.matches(this.itemStack, itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public final float getGrowthChance() {
        return growthChance;
    }

    public void setGrowthChance(float growthChance) {
        this.growthChance = growthChance;
    }

    public boolean isInFlowerHoldPeriod(LevelAccessor level, BlockPos rootPos, Float seasonValue) {
        if (seasonOffset == null) {
            return false;
        }
        final Float peakSeasonValue = SeasonHelper.getSeasonManager()
                .getPeakFruitProductionSeasonValue(LevelContext.create(level).level(), rootPos, seasonOffset);
        if (peakSeasonValue == null || flowerHoldPeriodLength == 0.0F) {
            return false;
        }
        final float min = peakSeasonValue - 1.5F;
        final float max = min + flowerHoldPeriodLength;
        return isSeasonBetween(seasonValue, min, max);
    }

    @Nullable
    public final Float getSeasonOffset() {
        return seasonOffset;
    }

    /**
     * Sets the season offset for fruit production. By default, this will peak in the middle of summer, starting at the
     * middle of spring and ending at the middle of fall. This offset will move the fruiting by a factor of one season.
     * For example, an offset of 2.0 would cause fruiting to peak in winter.
     *
     * @param offset the offset for fruit production, or {@code null} for it to peak all year round
     */
    public void setSeasonOffset(@Nullable Float offset) {
        seasonOffset = offset;
    }

    public float seasonalProductionFactor(LevelContext levelContext, BlockPos pos) {
        return seasonOffset != null ?
                SeasonHelper.globalSeasonalFruitProductionFactor(levelContext, pos, -seasonOffset, false)
                : 1.0F;
    }

    public float getFlowerHoldPeriodLength() {
        return flowerHoldPeriodLength;
    }

    public void setFlowerHoldPeriodLength(float flowerHoldPeriodLength) {
        this.flowerHoldPeriodLength = flowerHoldPeriodLength;
    }

    public final float getMinProductionFactor() {
        return minProductionFactor;
    }

    public void setMinProductionFactor(float minProductionFactor) {
        this.minProductionFactor = minProductionFactor;
    }

    public boolean isOutOfSeason(LevelContext levelContext, BlockPos pos) {
        return seasonalProductionFactor(levelContext, pos) < minProductionFactor;
    }

    public void place(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue, Direction facing, int radius) {
        BlockState state = getStateFor(facing, 0, radius);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    public void placeDuringWorldGen(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue, Direction facing, int radius) {
        BlockState state = getStateFor(facing, getAgeForWorldGen(level, pos, seasonValue), radius);
        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }

    protected BlockState getStateFor(Direction facing, int age, int radius) {
        if (age < 0) {
            throw new IllegalArgumentException("Cannot get state for negative pod age.");
        }
        if (age > maxAge) {
            throw new IllegalArgumentException(
                    "Cannot get state for age " + age + " as it is greater than maximum " + maxAge + " for pod \""
                            + getRegistryName() + "\"."
            );
        }
        BlockState state = this.block.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing).setValue(ageProperty, age);
        if (hasVariableOffset() && state.hasProperty(getOffsetProperty()))
            state = state.setValue(getOffsetProperty(), radius);
        return state;
    }

    protected int getAgeForWorldGen(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue) {
        // If seasons are enabled and in flower period, set to flower age (0).
        if (seasonValue != null && this.isInFlowerHoldPeriod(level, pos, seasonValue)) {
            return 0;
        }
        // Half the time the pod should be fully mature.
        return Math.min(level.getRandom().nextInt(maxAge * 2), maxAge);
    }

    public void performMatureAction(GrowableBlock.Info blockInfo) {
        matureAction.perform(block.get(), blockInfo);
    }

    public void setMatureAction(GrowableBlock.MatureAction matureAction) {
        this.matureAction = matureAction;
    }

    public boolean shouldGenerateBlockDrops() {
        return true;
    }

    private final LazyValue<ResourceLocation> blockDropsPath = LazyValue.supplied(() ->
            ResourceLocationUtils.prefix(ForgeRegistries.BLOCKS.getKey(block.get()), "blocks/"));

    public ResourceLocation getBlockDropsPath() {
        return blockDropsPath.get();
    }

    public LootTable.Builder createBlockDrops() {
        return DTLootTableProvider.BlockLoot.createPodDrops(block.get(), itemStack.getItem(), ageProperty, maxAge);
    }

    public void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    public void setMinRadius(int minRadius) {
        this.minRadius = minRadius;
    }

    public boolean hasVariableOffset(){
        return minRadius!=maxRadius;
    }

    public boolean isValidRadius (int radius){
        return radius >= minRadius && radius <= maxRadius;
    }

    public IntegerProperty getOffsetProperty (){
        if (offsetProperty == null)
            offsetProperty = OffsetProperties.getOrCreate(minRadius, maxRadius);
        return offsetProperty;
    }

    @Nonnull
    @Override
    public Pod reset() {
        canBoneMeal =  DTConfigs.SERVER_CONFIG.isLoaded() && DTConfigs.CAN_BONE_MEAL_PODS.get();
        seasonOffset = 0.0F;
        flowerHoldPeriodLength = 0.5F;
        minProductionFactor = 0.3F;
        matureAction = GrowableBlock.MatureAction.DEFAULT;
        return this;
    }

}
