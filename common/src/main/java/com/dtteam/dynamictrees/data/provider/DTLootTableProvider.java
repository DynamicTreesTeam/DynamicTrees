//package com.dtteam.dynamictrees.data.provider;
//
//import com.dtteam.dynamictrees.block.branch.BranchBlock;
//import com.dtteam.dynamictrees.block.fruit.Fruit;
//import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
//import com.dtteam.dynamictrees.block.pod.Pod;
//import com.dtteam.dynamictrees.loot.DTLootParameterSets;
//import com.dtteam.dynamictrees.loot.condition.SeasonalSeedDropChance;
//import com.dtteam.dynamictrees.loot.condition.VoluntarySeedDropChance;
//import com.dtteam.dynamictrees.loot.entry.SeedItemLootPoolEntry;
//import com.dtteam.dynamictrees.loot.function.MultiplyLogsCount;
//import com.dtteam.dynamictrees.loot.function.MultiplySticksCount;
//import com.dtteam.dynamictrees.systems.fruit.Fruit;
//import com.dtteam.dynamictrees.systems.pod.Pod;
//import com.dtteam.dynamictrees.tree.species.Species;
//import net.minecraft.advancements.critereon.EnchantmentPredicate;
//import net.minecraft.advancements.critereon.ItemPredicate;
//import net.minecraft.advancements.critereon.MinMaxBounds;
//import net.minecraft.advancements.critereon.StatePropertiesPredicate;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.data.PackOutput;
//import net.minecraft.data.loot.BlockLootSubProvider;
//import net.minecraft.data.loot.LootTableProvider;
//import net.minecraft.resources.ResourceKey;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.PackType;
//import net.minecraft.world.flag.FeatureFlagSet;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.Items;
//import net.minecraft.world.item.enchantment.Enchantments;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.properties.IntegerProperty;
//import net.minecraft.world.level.storage.loot.IntRange;
//import net.minecraft.world.level.storage.loot.LootPool;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraft.world.level.storage.loot.entries.LootItem;
//import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
//import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
//import net.minecraft.world.level.storage.loot.functions.LimitCount;
//import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
//import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
//import net.minecraft.world.level.storage.loot.predicates.*;
//import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
//import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
//import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//import java.util.function.BiConsumer;
//
///**
// * @author Harley O'Connor
// */
//public class DTLootTableProvider extends LootTableProvider {
//    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
//            .hasComponents(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
//    private static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item()
//            .of(Items.SHEARS));
//    private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
//    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
//
//    private final String modId;
//    private final ExistingFileHelper existingFileHelper;
//
//    public DTLootTableProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries) {
//        super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), registries);
//        this.modId = modId;
//        this.existingFileHelper = existingFileHelper;
//    }
//
//    public class BlockLoot extends BlockLootSubProvider {
//        protected BlockLoot() {
//            super(Set.of(), FeatureFlagSet.of());
//        }
//
//        @Override
//        protected void generate() {
//            Species.REGISTRY.dataGenerationStream(modId).forEach(this::addVoluntaryTable);
//
//            BuiltInRegistries.BLOCK.stream()
//                    .filter(block -> block instanceof BranchBlock)
//                    .map(block -> (BranchBlock) block)
//                    .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(modId))
//                    .forEach(this::addBranchTable);
//
//            LeavesProperties.REGISTRY.dataGenerationStream(modId).forEach(leavesProperties -> {
//                addLeavesBlockTable(leavesProperties);
//                addLeavesTable(leavesProperties);
//            });
//
//            Fruit.REGISTRY.dataGenerationStream(modId).forEach(this::addFruitBlockTable);
//            Pod.REGISTRY.dataGenerationStream(modId).forEach(this::addPodBlockTable);
//
//            ModLoader.get().postEvent(new DataGenerationStreamEvent(this, modId, existingFileHelper, map));
//        }
//
//        @Override
//        public void generate(@NotNull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
//            this.generate();
//
//            this.map.forEach(output);
//        }
//
//        private void addVoluntaryTable(Species species) {
//            if (species.shouldGenerateVoluntaryDrops()) {
//                final ResourceLocation leavesTablePath = species.getVoluntaryDropsPath();
//                if (!existingFileHelper.exists(leavesTablePath, PackType.SERVER_DATA)) {
//                    this.map.put(leavesTablePath, species.createVoluntaryDrops());
//                }
//            }
//        }
//
//        private void addBranchTable(BranchBlock branchBlock) {
//            if (branchBlock.shouldGenerateBranchDrops()) {
//                final ResourceLocation branchTablePath = branchBlock.getLootTableName();
//                if (!existingFileHelper.exists(branchTablePath, PackType.SERVER_DATA)) {
//                    this.map.put(branchTablePath, branchBlock.createBranchDrops());
//                }
//            }
//        }
//
//        private void addLeavesBlockTable(LeavesProperties leavesProperties) {
//            if (leavesProperties.shouldGenerateBlockDrops()) {
//                final ResourceLocation leavesBlockTablePath = leavesProperties.getBlockLootTableName();
//                if (!existingFileHelper.exists(leavesBlockTablePath, PackType.SERVER_DATA)) {
//                    this.map.put(leavesBlockTablePath, leavesProperties.createBlockDrops());
//                }
//            }
//        }
//
//        private void addLeavesTable(LeavesProperties leavesProperties) {
//            if (leavesProperties.shouldGenerateDrops()) {
//                final ResourceLocation leavesTablePath = leavesProperties.getLootTableName();
//                if (!existingFileHelper.exists(leavesTablePath, PackType.SERVER_DATA)) {
//                    this.map.put(leavesTablePath, leavesProperties.createDrops());
//                }
//            }
//        }
//
//        private void addFruitBlockTable(Fruit fruit) {
//            if (fruit.shouldGenerateBlockDrops()) {
//                final ResourceLocation fruitBlockTablePath = fruit.getBlockDropsPath();
//                if (!existingFileHelper.exists(fruitBlockTablePath, PackType.SERVER_DATA)) {
//                    this.map.put(fruitBlockTablePath, fruit.createBlockDrops());
//                }
//            }
//        }
//
//        private void addPodBlockTable(Pod pod) {
//            if (pod.shouldGenerateBlockDrops()) {
//                final ResourceLocation fruitBlockTablePath = pod.getBlockDropsPath();
//                if (!existingFileHelper.exists(fruitBlockTablePath, PackType.SERVER_DATA)) {
//                    this.map.put(fruitBlockTablePath, pod.createBlockDrops());
//                }
//            }
//        }
//
//        public static LootTable.Builder createLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances, Item stickItem) {
//            return createSilkTouchOrShearsDispatchTable(
//                    primitiveLeavesBlock,
//                    SeedItemLootPoolEntry.lootTableSeedItem()
//                            .when(ExplosionCondition.survivesExplosion())
//                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
//                            .when(SeasonalSeedDropChance.seasonalSeedDropChance())
//            ).withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH)
//                            .add(LootItem.lootTableItem(stickItem)
//                                    .apply(SetItemCountFunction.setCount(
//                                            UniformGenerator.between(1.0F, 2.0F)
//                                    ))
//                                    .apply(ApplyExplosionDecay.explosionDecay())
//                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F,
//                                            0.022222223F, 0.025F, 0.033333335F, 0.1F)))
//            ).setParamSet(LootContextParamSets.BLOCK);
//        }
//
//        public static LootTable.Builder createPalmLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances) {
//            return createSilkTouchOrShearsDispatchTable(
//                    primitiveLeavesBlock,
//                    SeedItemLootPoolEntry.lootTableSeedItem()
//                            .when(ExplosionCondition.survivesExplosion())
//                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
//                            .when(SeasonalSeedDropChance.seasonalSeedDropChance())
//            ).setParamSet(LootContextParamSets.BLOCK);
//        }
//
//        public static LootTable.Builder createWartBlockDrops(Block primitiveWartBlock) {
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1))
//                            .add(LootItem.lootTableItem(primitiveWartBlock))
//                            .when(ExplosionCondition.survivesExplosion())
//            );
//        }
//
//        public static LootTable.Builder createLeavesDrops(float[] seedChances, LootContextParamSet parameterSet) {
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            SeedItemLootPoolEntry.lootTableSeedItem()
//                                    .when(ExplosionCondition.survivesExplosion())
//                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
//                                    .when(SeasonalSeedDropChance.seasonalSeedDropChance())
//                    )
//            ).withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            LootItem.lootTableItem(Items.STICK)
//                                    .apply(SetItemCountFunction.setCount(
//                                            UniformGenerator.between(1.0F, 2.0F)
//                                    ))
//                                    .apply(ApplyExplosionDecay.explosionDecay())
//                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(
//                                            Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
//                                    ))
//                    )
//            ).setParamSet(parameterSet);
//        }
//
//        public static LootTable.Builder createPalmLeavesDrops(float[] seedChances, LootContextParamSet parameterSet) {
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            SeedItemLootPoolEntry.lootTableSeedItem()
//                                    .when(ExplosionCondition.survivesExplosion())
//                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
//                                    .when(SeasonalSeedDropChance.seasonalSeedDropChance())
//                    )
//            ).setParamSet(parameterSet);
//        }
//
//        public static LootTable.Builder createWartDrops(Block primitiveWartBlock) {
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1))
//                            .add(LootItem.lootTableItem(primitiveWartBlock))
//                            .when(ExplosionCondition.survivesExplosion())
//                            .when(BonusLevelTableCondition.bonusLevelFlatChance(
//                                    Enchantments.BLOCK_FORTUNE, 0.1F, 0.1333333F, 0.1666666F, 0.2F
//                            ))
//            );
//        }
//
//        public static LootTable.Builder createVoluntaryDrops(Item seedItem) {
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            LootItem.lootTableItem(seedItem)
//                                    .when(VoluntarySeedDropChance.voluntarySeedDropChance())
//                    )
//            ).setParamSet(DTLootParameterSets.VOLUNTARY);
//        }
//
//        public static LootTable.Builder createBranchDrops(Block primitiveLogBlock, Item stickItem) {
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            LootItem.lootTableItem(primitiveLogBlock)
//                                    .apply(MultiplyLogsCount.multiplyLogsCount())
//                                    .apply(ApplyExplosionDecay.explosionDecay())
//                    )
//            ).withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            LootItem.lootTableItem(stickItem)
//                                    .apply(MultiplySticksCount.multiplySticksCount())
//                                    .apply(ApplyExplosionDecay.explosionDecay())
//                    )
//            ).setParamSet(DTLootParameterSets.BRANCHES);
//        }
//
//        public static LootTable.Builder createFruitPodDrops(Block fruitBlock, Item fruitItem, IntegerProperty ageProperty, int matureAge, int count) {
//            return createFruitPodDrops(fruitBlock, fruitItem, ageProperty, matureAge, count, count);
//        }
//        public static LootTable.Builder createFruitPodDrops(Block fruitBlock, Item fruitItem, IntegerProperty ageProperty, int matureAge, int countMin, int countMax) {
//            //Select a number provider depending on the range.
//            // If both numbers are the same then use a constant value, otherwise use an uniform range.
//            NumberProvider numberProvider = (countMin == countMax) ?
//                    ConstantValue.exactly(countMax) :
//                    UniformGenerator.between(countMin, countMax);
//            //Apply the count to the item builder only if it's not just 1.
//            LootPoolSingletonContainer.Builder<?> itemBuilder = LootItem.lootTableItem(fruitItem);
//            if (!(countMin == countMax && countMax == 1)){
//                itemBuilder.apply(SetItemCountFunction.setCount(numberProvider));
//                //If the min count is negative, then cap it up to 0.
//                if (countMin < 0)
//                    itemBuilder.apply(LimitCount.limitCount(IntRange.lowerBound(0)));
//            }
//            //finally, return the table builder
//            return LootTable.lootTable().withPool(
//                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
//                            itemBuilder
//                                    .apply(ApplyExplosionDecay.explosionDecay())
//                                    .when(LootItemBlockStatePropertyCondition
//                                            .hasBlockStateProperties(fruitBlock)
//                                            .setProperties(StatePropertiesPredicate.Builder.properties()
//                                                    .hasProperty(ageProperty, matureAge))
//                                    )
//                    )
//            ).setParamSet(LootContextParamSets.BLOCK);
//        }
//
//	}
//}
