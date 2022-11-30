package com.ferreusveritas.dynamictrees.systems.fruit;

import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.FruitBlock;
import com.ferreusveritas.dynamictrees.block.GrowableBlock;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.data.provider.DTLootTableProvider;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.tree.Resettable;
import com.ferreusveritas.dynamictrees.util.AgeProperties;
import com.ferreusveritas.dynamictrees.util.LazyValue;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.function.Supplier;

import static com.ferreusveritas.dynamictrees.compat.season.SeasonHelper.isSeasonBetween;

/**
 * Stores properties and implements functionality of fruits which grow from the leaves of a tree.
 *
 * @author Harley O'Connor
 */
public class Fruit extends RegistryEntry<Fruit> implements Resettable<Fruit> {

    public static final TypedRegistry.EntryType<Fruit> TYPE = TypedRegistry.newType(Fruit::new);

    public static final Fruit NULL = new Fruit(DTTrees.NULL);

    /**
     * Central registry for all {@link Fruit} objects.
     */
    public static final TypedRegistry<Fruit> REGISTRY = new TypedRegistry<>(Fruit.class, NULL, TYPE);

    private Supplier<FruitBlock> block;


    private int maxAge = 3;

    /**
     * The block's age state property. Changes with {@link #maxAge} to allow for customisation of the maximum age
     * of the fruit.
     */
    private IntegerProperty ageProperty = BlockStateProperties.AGE_3;

    private VoxelShape[] blockShapes = {
            Shapes.block(), Shapes.block(), Shapes.block(), Shapes.block()
    };

    /**
     * Sets whether the fruit can be bone-mealed to accelerate growth. Defaults to {@link DTConfigs#CAN_BONE_MEAL_FRUIT}.
     */
    private boolean canBoneMeal;

    /**
     * The item stack for this fruit. Note that this is only used for the pick block functionality, drops must be set
     * up using vanilla loot tables.
     */
    private ItemStack itemStack;

    private float growthChance = 0.2F;

    @Nullable
    private Float seasonOffset = 0f;

    private float flowerHoldPeriodLength = 0.5F;

    private float minProductionFactor = 0.3F;

    private GrowableBlock.MatureAction matureAction = GrowableBlock.MatureAction.DEFAULT;

    public Fruit(ResourceLocation registryName) {
        super(registryName);
    }

    /**
     * @throws IllegalStateException if this was called before the block has been created
     */
    public final FruitBlock getBlock() {
        if (block == null) {
            throw new IllegalStateException("Invoked too early (before the block was created).");
        }
        return block.get();
    }

    /**
     * Creates and sets the {@link FruitBlock} for this fruit.
     *
     * @param name       the name to set for the block, or {@code null} to use the fruit's name
     * @param properties the properties of the block. May be the {@linkplain #getDefaultBlockProperties default
     *                   properties} or a modification of them.
     */
    public final void createBlock(@Nullable ResourceLocation name, Block.Properties properties) {
        block = RegistryHandler.addBlock(name == null ? this.getRegistryName() : name, () -> createBlock(properties));
    }

    protected FruitBlock createBlock(Block.Properties properties) {
        return new FruitBlock(properties, this);
    }

    public Material getDefaultMaterial() {
        return Material.PLANT;
    }

    public MaterialColor getDefaultMaterialColor() {
        return getDefaultMaterial().getColor();
    }

    public BlockBehaviour.Properties getDefaultBlockProperties() {
        return getDefaultBlockProperties(getDefaultMaterial(), getDefaultMaterialColor());
    }

    public BlockBehaviour.Properties getDefaultBlockProperties(Material material, MaterialColor materialColor) {
        return BlockBehaviour.Properties.of(material, materialColor)
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

    public final VoxelShape getBlockShape(int age) {
        return blockShapes[age];
    }

    /**
     * @param blockShapes the block shapes to set; indexed by the age the respective shape is for
     * @throws IllegalArgumentException if the specified {@code blockShapes} array does not contain a shape for each age
     */
    public void setBlockShapes(VoxelShape[] blockShapes) {
        if (blockShapes.length <= maxAge) {
            throw new IllegalArgumentException("Insufficient number of block shapes provided for the maximum age " + maxAge + " on fruit " + this);
        }
        this.blockShapes = blockShapes;
    }

    public final boolean canBoneMeal() {
        return canBoneMeal;
    }

    public void setCanBoneMeal(boolean canBoneMeal) {
        this.canBoneMeal = canBoneMeal;
    }

    /**
     * @return a copy of this fruit's item stack
     */
    public final ItemStack getItemStack() {
        return itemStack.copy();
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

    public final boolean isInFlowerHoldPeriod(LevelAccessor level, BlockPos rootPos, Float seasonValue) {
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

    public float seasonalFruitProductionFactor(LevelContext levelContext, BlockPos pos) {
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

    public boolean isOutOfSeason(Level level, BlockPos pos) {
        return seasonalFruitProductionFactor(LevelContext.create(level), pos) < minProductionFactor;
    }

    public void place(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue) {
        BlockState state = getStateForAge(0);
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    public void placeDuringWorldGen(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue) {
        BlockState state = getStateForAge(getAgeForWorldGen(level, pos, seasonValue));
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    protected BlockState getStateForAge(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Cannot get state for negative fruit age.");
        }
        if (age > maxAge) {
            throw new IllegalArgumentException(
                    "Cannot get state for age " + age + " as it is greater than maximum " + maxAge + " for fruit \""
                            + getRegistryName() + "\"."
            );
        }
        return this.block.get().defaultBlockState().setValue(ageProperty, age);
    }

    protected int getAgeForWorldGen(LevelAccessor level, BlockPos pos, @Nullable Float seasonValue) {
        // If seasons are enabled and in flower period, set to flower age (0).
        if (seasonValue != null && this.isInFlowerHoldPeriod(level, pos, seasonValue)) {
            return 0;
        }
        // Half the time the fruit should be fully mature.
        return Math.min(level.getRandom().nextInt(maxAge * 2), maxAge);
    }

    public void performMatureAction(GrowableBlock.Info blockInfo) {
        matureAction.perform(block.get(), blockInfo);
    }

    public GrowableBlock.MatureAction getMatureAction() {
        return matureAction;
    }

    public void setMatureAction(GrowableBlock.MatureAction matureAction) {
        this.matureAction = matureAction;
    }

    public boolean shouldGenerateBlockDrops() {
        return true;
    }

    private final LazyValue<ResourceLocation> blockDropsPath = LazyValue.supplied(() ->
            ResourceLocationUtils.prefix(block.get().getRegistryName(), "blocks/"));

    public ResourceLocation getBlockDropsPath() {
        return blockDropsPath.get();
    }

    public LootTable.Builder createBlockDrops() {
        return DTLootTableProvider.createFruitDrops(block.get(), itemStack.getItem(), ageProperty, maxAge);
    }

    @Nonnull
    @Override
    public Fruit reset() {
        canBoneMeal = DTConfigs.CAN_BONE_MEAL_FRUIT.get();
        seasonOffset = 0.0F;
        flowerHoldPeriodLength = 0.5F;
        minProductionFactor = 0.3F;
        matureAction = GrowableBlock.MatureAction.DEFAULT;
        return this;
    }

}
