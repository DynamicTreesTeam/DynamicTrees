package com.dtteam.dynamictrees.block.fruit;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.function.TriPredicate;
import com.dtteam.dynamictrees.api.lazyvalue.LazyValue;
import com.dtteam.dynamictrees.api.registry.RegistryEntry;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.api.season.ClimateZoneType;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.DynamicBlockProperties;
import com.dtteam.dynamictrees.block.Growable;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.data.DTLootTableBuilder;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.treepack.Resettable;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * Stores properties and implements functionality of fruits which grow from the leaves of a tree.
 *
 * @author Harley O'Connor
 */
public class Fruit extends RegistryEntry<Fruit> implements Resettable<Fruit> {

    public static final TypedRegistry.EntryType<Fruit> TYPE = TypedRegistry.newType(Fruit::new);

    public static final Fruit NULL = new Fruit(DynamicTrees.NULL);

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
     * Sets whether the fruit can be bone-mealed to accelerate growth. Defaults to {@link DTConfigs#canBoneMealFruit}.
     */
    private boolean canBoneMeal;

    /**
     * The item stack for this fruit. Note that this is only used for the pick block functionality, drops must be set
     * up using vanilla loot tables.
     */
    private ItemStack itemStack;

    private float growthChance = 0.2F;
    private float requiredProductionFactor = 0.3F;

    private Growable.MatureAction matureAction = Growable.MatureAction.DEFAULT;

    private BiFunction<LevelContext, BlockPos, Float> seasonalFactorGetter = (l,b)-> 1.0f;
    private TriPredicate<LevelContext, BlockPos, Float> floweringPeriodPredicate = (l, b, s)-> false;

    private int minDropCount = 1;
    private int maxDropCount = 1;

    public Fruit(Identifier registryName) {
        super(registryName);
    }

    public void setSeasonalFactorGetter(BiFunction<LevelContext, BlockPos, Float> seasonalFactorGetter) {
        this.seasonalFactorGetter = seasonalFactorGetter;
    }

    public void setFloweringPeriodPredicate(TriPredicate<LevelContext, BlockPos, Float> floweringPeriodPredicate) {
        this.floweringPeriodPredicate = floweringPeriodPredicate;
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
    public final void createBlock(@Nullable Identifier name, Block.Properties properties) {
        block = RegistryHandler.addBlock(name == null ? this.getRegistryName() : name, () -> createBlock(properties));
    }

    protected FruitBlock createBlock(Block.Properties properties) {
        return new FruitBlock(properties, this);
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
        this.ageProperty = DynamicBlockProperties.getOrCreateAge(maxAge);
    }

    public void setDropCount(int dropCount) {
        setMaxDropCount(dropCount);
        setMinDropCount(dropCount);
    }
    public void setMaxDropCount(int maxDropCount) {
        this.maxDropCount = maxDropCount;
    }
    public void setMinDropCount(int minDropCount) {
        this.minDropCount = minDropCount;
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
        if (itemStack == null) {
            LogManager.getLogger().warn("Invoked too early or item was not set on \"{}\".", getRegistryName());
            return new ItemStack(Items.AIR);
        }
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

    public final float getRequiredProductionFactor() {
        return requiredProductionFactor;
    }

    public void setRequiredProductionFactor(float requiredProductionFactor) {
        this.requiredProductionFactor = requiredProductionFactor;
    }

    public Float seasonalFruitProductionFactor(LevelContext level, BlockPos pos){
        return seasonalFactorGetter.apply(level, pos);
    }

    public boolean isOutOfSeason(Level level, BlockPos pos) {
        return seasonalFruitProductionFactor(LevelContext.create(level), pos) < requiredProductionFactor;
    }

    public Boolean isInFlowerHoldPeriod(LevelAccessor level, BlockPos pos, Float seasonValue){
        return floweringPeriodPredicate.test(LevelContext.create(level), pos, seasonValue);
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
        if (seasonValue != null && isInFlowerHoldPeriod(level, pos, seasonValue)) {
            return 0;
        }
        // Half the time the fruit should be fully mature.
        return Math.min(level.getRandom().nextInt(maxAge * 2), maxAge);
    }

    public void performMatureAction(Growable.Info blockInfo) {
        matureAction.perform(block.get(), blockInfo);
    }

    public Growable.MatureAction getMatureAction() {
        return matureAction;
    }

    public void setMatureAction(Growable.MatureAction matureAction) {
        this.matureAction = matureAction;
    }

    public boolean shouldGenerateBlockDrops() {
        return true;
    }

    private final LazyValue<Identifier> blockDropsPath = LazyValue.supplied(() ->
            IdentifierUtils.prefix(BuiltInRegistries.BLOCK.getKey(block.get()),"blocks/"));

    public Identifier getBlockDropsPath() {
        return blockDropsPath.get();
    }

    public LootTable.Builder createBlockDrops(HolderLookup.Provider registries) {
        if (minDropCount > maxDropCount || maxDropCount <= 0)
            throw new IllegalArgumentException("Attempted to create loot tables for "+getRegistryName()+" with an invalid drop count range ["+minDropCount+","+maxDropCount+"].");
        return DTLootTableBuilder.createFruitPodDrops(block.get(), getItemStack().getItem(), ageProperty, maxAge, minDropCount, maxDropCount, registries);
    }

    @NotNull
    @Override
    public Fruit reset() {
        canBoneMeal = DTConfigs.SERVER_CONFIG.isLoaded() && DTConfigs.SERVER.canBoneMealFruit.get();
        requiredProductionFactor = 0.3F;
        matureAction = Growable.MatureAction.DEFAULT;
        seasonalFactorGetter = (l,b)-> 1.0f;
        floweringPeriodPredicate = (l, b, s)-> false;
        return this;
    }

}
