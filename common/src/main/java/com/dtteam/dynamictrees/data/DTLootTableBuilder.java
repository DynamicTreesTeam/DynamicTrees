package com.dtteam.dynamictrees.data;

import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;

import com.dtteam.dynamictrees.loot.DTLootParameterSets;
import com.dtteam.dynamictrees.loot.condition.SeasonalSeedDropChance;
import com.dtteam.dynamictrees.loot.condition.VoluntarySeedDropChance;
import com.dtteam.dynamictrees.loot.entry.SeedItemLootPoolEntry;
import com.dtteam.dynamictrees.loot.function.MultiplyByLogsCount;
import com.dtteam.dynamictrees.loot.function.MultiplyBySticksCount;
import com.dtteam.dynamictrees.utility.ItemUtils;
import net.minecraft.advancements.predicates.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;

public class DTLootTableBuilder {

    protected static LootItemCondition.Builder hasSilkTouch(HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return MatchTool.toolMatches(
                ItemPredicate.Builder.item()
                        .withComponents(
                                DataComponentMatchers.Builder.components()
                                        .partial(
                                                DataComponentPredicates.ENCHANTMENTS,
                                                EnchantmentsPredicate.enchantments(
                                                        List.of(new EnchantmentPredicate(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                                                )
                                        )
                                        .build()
                        )
        );
    }

    private static LootItemCondition.Builder hasNoShearsOrSilkTouch(HolderLookup.Provider registries){
        return hasShearsOrSilkTouch(registries).invert();
    }

    private static LootItemCondition.Builder hasShearsOrSilkTouch(HolderLookup.Provider registries){
        LootItemCondition.Builder hasShears = MatchTool.toolMatches(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), Items.SHEARS));
        return hasShears.or(hasSilkTouch(registries));
    }

    /** 26.2 item codecs reject {@code minecraft:air}; never emit it as a loot entry. */
    private static Item lootItemOrStick(Item item) {
        return item == null || item == Items.AIR ? Items.STICK : item;
    }

    private static boolean isLootable(Item item) {
        return item != null && item != Items.AIR;
    }

    protected static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder conditionBuilder, LootPoolEntryContainer.Builder<?> alternativeBuilder) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(conditionBuilder).otherwise(alternativeBuilder)));
    }

    public static LootTable.Builder createLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances, Item stickItem, HolderLookup.Provider registries) {
        return createSelfDropDispatchTable(
                primitiveLeavesBlock,
                hasShearsOrSilkTouch(registries),
                SeedItemLootPoolEntry.lootTableSeedItem()
                        .when(ExplosionCondition.survivesExplosion())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), seedChances))
                        .when(SeasonalSeedDropChance.seasonalSeedDropChance())
        ).withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(hasNoShearsOrSilkTouch(registries))
                        .add(LootItem.lootTableItem(lootItemOrStick(stickItem))
                                .apply(SetItemCountFunction.setCount(
                                        UniformGenerator.between(1.0F, 2.0F)
                                ))
                                .apply(ApplyExplosionDecay.explosionDecay())
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), 0.02F,
                                        0.022222223F, 0.025F, 0.033333335F, 0.1F)))
        ).setParamSet(DTLootParameterSets.LEAVES_BLOCK);
    }

    public static LootTable.Builder createPalmLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances, HolderLookup.Provider registries) {
        return createSelfDropDispatchTable(
                primitiveLeavesBlock,
                hasShearsOrSilkTouch(registries),
                SeedItemLootPoolEntry.lootTableSeedItem()
                        .when(ExplosionCondition.survivesExplosion())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), seedChances))
                        .when(SeasonalSeedDropChance.seasonalSeedDropChance())
        ).setParamSet(DTLootParameterSets.LEAVES_BLOCK);
    }

    public static LootTable.Builder createWartBlockDrops(Block primitiveWartBlock, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(primitiveWartBlock))
                        .when(ExplosionCondition.survivesExplosion())
        );
    }

    public static LootTable.Builder createLeavesDrops(float[] seedChances, ContextKeySet parameterSet, HolderLookup.Provider registries) {
        return createLeavesDrops(seedChances, parameterSet, Items.STICK, registries);
    }
    public static LootTable.Builder createLeavesDrops(float[] seedChances, ContextKeySet parameterSet, Item stickItem, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        SeedItemLootPoolEntry.lootTableSeedItem()
                                .when(ExplosionCondition.survivesExplosion())
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), seedChances))
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
                                        ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
                                ))
                )
        ).setParamSet(parameterSet);
    }

    public static LootTable.Builder createPalmLeavesDrops(float[] seedChances, ContextKeySet parameterSet, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        SeedItemLootPoolEntry.lootTableSeedItem()
                                .when(ExplosionCondition.survivesExplosion())
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), seedChances))
                                .when(SeasonalSeedDropChance.seasonalSeedDropChance())
                )
        ).setParamSet(parameterSet);
    }

    public static LootTable.Builder createWartDrops(Block primitiveWartBlock, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(primitiveWartBlock))
                        .when(ExplosionCondition.survivesExplosion())
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                ItemUtils.getEnchantment(Enchantments.FORTUNE, registries), 0.1F, 0.1333333F, 0.1666666F, 0.2F
                        ))
        );
    }

    public static LootTable.Builder createVoluntaryDrops(Item seedItem, HolderLookup.Provider registries) {
        if (!isLootable(seedItem)) {
            return LootTable.lootTable().setParamSet(DTLootParameterSets.VOLUNTARY);
        }
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(seedItem)
                                .when(VoluntarySeedDropChance.voluntarySeedDropChance())
                )
        ).setParamSet(DTLootParameterSets.VOLUNTARY);
    }

    public static LootTable.Builder createBranchDrops(Block primitiveLogBlock, Item stickItem, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(primitiveLogBlock)
                                .apply(MultiplyByLogsCount.multiplyByLogsCount())
                                .apply(ApplyExplosionDecay.explosionDecay())
                )
        ).withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(lootItemOrStick(stickItem))
                                .apply(MultiplyBySticksCount.multiplyBySticksCount())
                                .apply(ApplyExplosionDecay.explosionDecay())
                )
        ).setParamSet(DTLootParameterSets.BRANCHES);
    }

    public static LootTable.Builder createCreakingHeartDrops(Block primitiveLogBlock, Item resinItem, int minResin, int maxResin, HolderLookup.Provider registries) {
        return createSelfDropDispatchTable(
                primitiveLogBlock,
                hasSilkTouch(registries),
                LootItem.lootTableItem(resinItem)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minResin, maxResin)))
                        .apply(com.dtteam.dynamictrees.loot.function.MultiplyByTotalVolume.multiplyByTotalVolume())
                        .apply(ApplyExplosionDecay.explosionDecay())
        ).setParamSet(DTLootParameterSets.BRANCHES);
    }

    public static LootTable.Builder createResinBranchDrops(Block primitiveLogBlock, Item stickItem, Item resinItem, int minResin, int maxResin, HolderLookup.Provider registries) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(primitiveLogBlock)
                                .apply(MultiplyByLogsCount.multiplyByLogsCount())
                                .apply(ApplyExplosionDecay.explosionDecay())
                )
        ).withPool(
                LootPool.lootPool().setRolls(UniformGenerator.between(minResin, maxResin)).add(
                        LootItem.lootTableItem(resinItem)
                                .apply(com.dtteam.dynamictrees.loot.function.MultiplyByTotalVolume.multiplyByTotalVolume())
                                .apply(ApplyExplosionDecay.explosionDecay())
                )
        ).withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        LootItem.lootTableItem(lootItemOrStick(stickItem))
                                .apply(MultiplyBySticksCount.multiplyBySticksCount())
                                .apply(ApplyExplosionDecay.explosionDecay())
                )
        ).setParamSet(DTLootParameterSets.BRANCHES);
    }

    public static LootTable.Builder createFruitPodDrops(Block fruitBlock, Item fruitItem, IntegerProperty ageProperty, int matureAge, int count, HolderLookup.Provider registries) {
        return createFruitPodDrops(fruitBlock, fruitItem, ageProperty, matureAge, count, count, registries);
    }
    public static LootTable.Builder createFruitPodDrops(Block fruitBlock, Item fruitItem, IntegerProperty ageProperty, int matureAge, int countMin, int countMax, HolderLookup.Provider registries) {
        //Select a number provider depending on the range.
        // If both numbers are the same then use a constant value, otherwise use an uniform range.
        NumberProvider numberProvider = (countMin == countMax) ?
                ConstantValue.exactly(countMax) :
                UniformGenerator.between(countMin, countMax);
        //Apply the count to the item builder only if it's not just 1.
        if (!isLootable(fruitItem)) {
            return LootTable.lootTable().setParamSet(LootContextParamSets.BLOCK);
        }
        LootPoolSingletonContainer.Builder<?> itemBuilder = LootItem.lootTableItem(fruitItem);
        if (!(countMin == countMax && countMax == 1)){
            itemBuilder.apply(SetItemCountFunction.setCount(numberProvider));
            //If the min count is negative, then cap it up to 0.
            if (countMin < 0)
                itemBuilder.apply(LimitCount.limitCount(IntRange.lowerBound(0)));
        }
        //finally, return the table builder
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(
                        itemBuilder
                                .apply(ApplyExplosionDecay.explosionDecay())
                                .when(LootItemBlockStatePropertyCondition
                                        .hasBlockStateProperties(fruitBlock)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(ageProperty, matureAge))
                                )
                )
        ).setParamSet(LootContextParamSets.BLOCK);
    }

}
