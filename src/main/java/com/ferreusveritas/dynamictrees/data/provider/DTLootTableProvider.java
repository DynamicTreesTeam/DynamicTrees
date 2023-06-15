package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.loot.condition.SeasonalSeedDropChance;
import com.ferreusveritas.dynamictrees.loot.condition.VoluntarySeedDropChance;
import com.ferreusveritas.dynamictrees.loot.entry.SeedItemLootPoolEntry;
import com.ferreusveritas.dynamictrees.loot.function.MultiplyLogsCount;
import com.ferreusveritas.dynamictrees.loot.function.MultiplySticksCount;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

/**
 * @author Harley O'Connor
 */
public class DTLootTableProvider extends LootTableProvider {
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
            .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
    private static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    private static final LootItemCondition.Builder HAS_SHEARS =
            MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();

    private final String modId;
    private final ExistingFileHelper existingFileHelper;

    public DTLootTableProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, Set.of(), List.of());
        this.modId = modId;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public List<SubProviderEntry> getTables() {
        return List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK));
    }

    public class BlockLoot extends VanillaBlockLoot {
        @Override
        protected void generate() {
            Species.REGISTRY.dataGenerationStream(modId).forEach(this::addVoluntaryTable);

            ForgeRegistries.BLOCKS.getValues().stream()
                    .filter(block -> block instanceof BranchBlock)
                    .map(block -> (BranchBlock) block)
                    .filter(block -> ForgeRegistries.BLOCKS.getKey(block).getNamespace().equals(modId))
                    .forEach(this::addBranchTable);

            LeavesProperties.REGISTRY.dataGenerationStream(modId).forEach(leavesProperties -> {
                addLeavesBlockTable(leavesProperties);
                addLeavesTable(leavesProperties);
            });

            Fruit.REGISTRY.dataGenerationStream(modId).forEach(this::addFruitBlockTable);
            Pod.REGISTRY.dataGenerationStream(modId).forEach(this::addPodBlockTable);
        }

        private void addVoluntaryTable(Species species) {
            if (species.shouldGenerateVoluntaryDrops()) {
                final ResourceLocation leavesTablePath = species.getVoluntaryDropsPath();
                if (!existingFileHelper.exists(leavesTablePath, PackType.SERVER_DATA)) {
                    this.map.put(leavesTablePath, species.createVoluntaryDrops());
                }
            }
        }

        private void addBranchTable(BranchBlock branchBlock) {
            if (branchBlock.shouldGenerateBranchDrops()) {
                final ResourceLocation branchTablePath = branchBlock.getLootTableName();
                if (!existingFileHelper.exists(branchTablePath, PackType.SERVER_DATA)) {
                    this.map.put(branchTablePath, branchBlock.createBranchDrops());
                }
            }
        }

        private void addLeavesBlockTable(LeavesProperties leavesProperties) {
            if (leavesProperties.shouldGenerateBlockDrops()) {
                final ResourceLocation leavesBlockTablePath = leavesProperties.getBlockLootTableName();
                if (!existingFileHelper.exists(leavesBlockTablePath, PackType.SERVER_DATA)) {
                    this.map.put(leavesBlockTablePath, leavesProperties.createBlockDrops());
                }
            }
        }

        private void addLeavesTable(LeavesProperties leavesProperties) {
            if (leavesProperties.shouldGenerateDrops()) {
                final ResourceLocation leavesTablePath = leavesProperties.getLootTableName();
                if (!existingFileHelper.exists(leavesTablePath, PackType.SERVER_DATA)) {
                    this.map.put(leavesTablePath, leavesProperties.createDrops());
                }
            }
        }

        private void addFruitBlockTable(Fruit fruit) {
            if (fruit.shouldGenerateBlockDrops()) {
                final ResourceLocation fruitBlockTablePath = fruit.getBlockDropsPath();
                if (!existingFileHelper.exists(fruitBlockTablePath, PackType.SERVER_DATA)) {
                    this.map.put(fruitBlockTablePath, fruit.createBlockDrops());
                }
            }
        }

        private void addPodBlockTable(Pod pod) {
            if (pod.shouldGenerateBlockDrops()) {
                final ResourceLocation fruitBlockTablePath = pod.getBlockDropsPath();
                if (!existingFileHelper.exists(fruitBlockTablePath, PackType.SERVER_DATA)) {
                    this.map.put(fruitBlockTablePath, pod.createBlockDrops());
                }
            }
        }

        public static LootTable.Builder createLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances) {
            return createSilkTouchOrShearsDispatchTable(
                    primitiveLeavesBlock,
                    SeedItemLootPoolEntry.lootTableSeedItem()
                            .when(ExplosionCondition.survivesExplosion())
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                            .when(SeasonalSeedDropChance.seasonalSeedDropChance())
            ).withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                            .add(LootItem.lootTableItem(Items.STICK)
                                    .apply(SetItemCountFunction.setCount(
                                            UniformGenerator.between(1.0F, 2.0F)
                                    ))
                                    .apply(ApplyExplosionDecay.explosionDecay())
                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F,
                                            0.022222223F, 0.025F, 0.033333335F, 0.1F)))
            ).setParamSet(LootContextParamSets.BLOCK);
        }

        public static LootTable.Builder createPalmLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances) {
            return createSilkTouchOrShearsDispatchTable(
                    primitiveLeavesBlock,
                    SeedItemLootPoolEntry.lootTableSeedItem()
                            .when(ExplosionCondition.survivesExplosion())
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                            .when(SeasonalSeedDropChance.seasonalSeedDropChance())
            ).setParamSet(LootContextParamSets.BLOCK);
        }

        public static LootTable.Builder createWartBlockDrops(Block primitiveWartBlock) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(primitiveWartBlock))
                            .when(ExplosionCondition.survivesExplosion())
            );
        }

        public static LootTable.Builder createLeavesDrops(float[] seedChances, LootContextParamSet parameterSet) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            SeedItemLootPoolEntry.lootTableSeedItem()
                                    .when(ExplosionCondition.survivesExplosion())
                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                                    .when(SeasonalSeedDropChance.seasonalSeedDropChance())
                    )
            ).withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            LootItem.lootTableItem(Items.STICK)
                                    .apply(SetItemCountFunction.setCount(
                                            UniformGenerator.between(1.0F, 2.0F)
                                    ))
                                    .apply(ApplyExplosionDecay.explosionDecay())
                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                            Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
                                    ))
                    )
            ).setParamSet(parameterSet);
        }

        public static LootTable.Builder createPalmLeavesDrops(float[] seedChances, LootContextParamSet parameterSet) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            SeedItemLootPoolEntry.lootTableSeedItem()
                                    .when(ExplosionCondition.survivesExplosion())
                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                                    .when(SeasonalSeedDropChance.seasonalSeedDropChance())
                    )
            ).setParamSet(parameterSet);
        }

        public static LootTable.Builder createWartDrops(Block primitiveWartBlock) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(primitiveWartBlock))
                            .when(ExplosionCondition.survivesExplosion())
                            .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                    Enchantments.BLOCK_FORTUNE, 0.1F, 0.1333333F, 0.1666666F, 0.2F
                            ))
            );
        }

        public static LootTable.Builder createVoluntaryDrops(Item seedItem) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            LootItem.lootTableItem(seedItem)
                                    .when(VoluntarySeedDropChance.voluntarySeedDropChance())
                    )
            ).setParamSet(DTLootParameterSets.VOLUNTARY);
        }

        public static LootTable.Builder createBranchDrops(Block primitiveLogBlock, Item stickItem) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            LootItem.lootTableItem(primitiveLogBlock)
                                    .apply(MultiplyLogsCount.multiplyLogsCount())
                                    .apply(ApplyExplosionDecay.explosionDecay())
                    )
            ).withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            LootItem.lootTableItem(stickItem)
                                    .apply(MultiplySticksCount.multiplySticksCount())
                                    .apply(ApplyExplosionDecay.explosionDecay())
                    )
            ).setParamSet(DTLootParameterSets.BRANCHES);
        }

        public static LootTable.Builder createFruitDrops(Block fruitBlock, Item fruitItem, IntegerProperty ageProperty, int matureAge) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            LootItem.lootTableItem(fruitItem)
                                    .when(
                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(fruitBlock)
                                                    .setProperties(
                                                            StatePropertiesPredicate.Builder.properties()
                                                                    .hasProperty(ageProperty, matureAge)
                                                    )
                                    )
                    )
            ).apply(ApplyExplosionDecay.explosionDecay()).setParamSet(LootContextParamSets.BLOCK);
        }

        public static LootTable.Builder createPodDrops(Block podBlock, Item podItem, IntegerProperty ageProperty, int matureAge) {
            return LootTable.lootTable().withPool(
                    LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                            LootItem.lootTableItem(podItem)
                                    .apply(
                                            SetItemCountFunction.setCount(ConstantValue.exactly(3))
                                                    .when(
                                                            LootItemBlockStatePropertyCondition.hasBlockStateProperties(podBlock)
                                                                    .setProperties(
                                                                            StatePropertiesPredicate.Builder.properties()
                                                                                    .hasProperty(ageProperty, matureAge)
                                                                    )
                                                    )
                                    )
                                    .apply(ApplyExplosionDecay.explosionDecay())
                    )
            ).setParamSet(LootContextParamSets.BLOCK);
        }
    }
}
