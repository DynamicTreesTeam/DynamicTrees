package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.loot.condition.SeasonalSeedDropChance;
import com.ferreusveritas.dynamictrees.loot.condition.VoluntarySeedDropChance;
import com.ferreusveritas.dynamictrees.loot.function.MultiplyLogsCount;
import com.ferreusveritas.dynamictrees.loot.function.MultiplySticksCount;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.conditions.TableBonus;
import net.minecraft.loot.functions.ExplosionDecay;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class DTLootTableProvider extends LootTableProvider {

    private static final ILootCondition.IBuilder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
            .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))));
    private static final ILootCondition.IBuilder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    private static final ILootCondition.IBuilder HAS_SHEARS =
            MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final ILootCondition.IBuilder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final ILootCondition.IBuilder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, LootTable.Builder> lootTables = new HashMap<>();
    private final DataGenerator generator;
    private final String modId;
    private final ExistingFileHelper existingFileHelper;

    public DTLootTableProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator);
        this.generator = generator;
        this.modId = modId;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public void run(DirectoryCache cache) {
        addTables();
        writeTables(cache);
    }

    private void addTables() {
        Species.REGISTRY.forEach(species -> {
            if (!species.getRegistryName().getNamespace().equals(modId)) {
                return;
            }
            if (species.shouldGenerateLeavesBlockDrops()) {
                final ResourceLocation leavesBlockTablePath = getFullDropsPath(species.getLeavesBlockDropsPath());
                if (!existingFileHelper.exists(leavesBlockTablePath, ResourcePackType.SERVER_DATA)) {
                    lootTables.put(leavesBlockTablePath, species.createLeavesBlockDrops());
                }
            }
            if (species.shouldGenerateLeavesDrops()) {
                final ResourceLocation leavesTablePath = getFullDropsPath(species.getLeavesDropsPath());
                if (!existingFileHelper.exists(leavesTablePath, ResourcePackType.SERVER_DATA)) {
                    lootTables.put(leavesTablePath, species.createLeavesDrops());
                }
            }
            if (species.shouldGenerateVoluntaryDrops()) {
                final ResourceLocation leavesTablePath = getFullDropsPath(species.getVoluntaryDropsPath());
                if (!existingFileHelper.exists(leavesTablePath, ResourcePackType.SERVER_DATA)) {
                    lootTables.put(leavesTablePath, species.createVoluntaryDrops());
                }
            }
        });
        ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> block instanceof BranchBlock)
                .map(block -> (BranchBlock) block)
                .filter(block -> block.getRegistryName().getNamespace().equals(modId))
                .forEach(branchBlock -> {
                    if (branchBlock.shouldGenerateBranchDrops()) {
                        final ResourceLocation branchTablePath = getFullDropsPath(branchBlock.getLootTableName());
                        if (!existingFileHelper.exists(branchTablePath, ResourcePackType.SERVER_DATA)) {
                            lootTables.put(branchTablePath, branchBlock.createBranchDrops());
                        }
                    }
                });
    }

    private ResourceLocation getFullDropsPath(ResourceLocation path) {
        return ResourceLocationUtils.surround(path, "loot_tables/", ".json");
    }

    public static LootTable.Builder createLeavesBlockDrops(Block primitiveLeavesBlock) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1))
                        .add(ItemLootEntry.lootTableItem(primitiveLeavesBlock).when(HAS_SHEARS_OR_SILK_TOUCH))
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(
                        ItemLootEntry.lootTableItem(Items.STICK)
                                .apply(SetCount.setCount(RandomValueRange.between(1.0F, 2.0F)))
                                .apply(ExplosionDecay.explosionDecay())
                                .when(TableBonus.bonusLevelFlatChance(
                                        Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F,
                                        0.1F
                                ))
                )
        ).setParamSet(LootParameterSets.BLOCK);
    }

    public static LootTable.Builder createLeavesBlockDrops(Block primitiveLeavesBlock, Item seedItem,
                                                           float[] seedChances) {
        return BlockLootTables.createSilkTouchOrShearsDispatchTable(
                primitiveLeavesBlock,
                ItemLootEntry.lootTableItem(seedItem)
                        .when(SurvivesExplosion.survivesExplosion())
                        .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                        .when(SeasonalSeedDropChance.seasonalSeedDropChance())
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                        .add(ItemLootEntry.lootTableItem(Items.STICK)
                                .apply(SetCount.setCount(
                                        RandomValueRange.between(1.0F, 2.0F)
                                ))
                                .apply(ExplosionDecay.explosionDecay())
                                .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F,
                                        0.022222223F, 0.025F, 0.033333335F, 0.1F)))
        ).setParamSet(LootParameterSets.BLOCK);
    }

    public static LootTable.Builder createWartBlockDrops(Block primitiveWartBlock) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1))
                        .add(ItemLootEntry.lootTableItem(primitiveWartBlock))
                        .when(SurvivesExplosion.survivesExplosion())
        );
    }

    public static LootTable.Builder createLeavesDrops() {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(Items.STICK)
                                .apply(SetCount.setCount(RandomValueRange.between(1.0F, 2.0F)))
                                .apply(ExplosionDecay.explosionDecay())
                                .when(TableBonus.bonusLevelFlatChance(
                                        Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F,
                                        0.1F
                                ))
                )
        ).setParamSet(LootParameterSets.BLOCK);
    }

    public static LootTable.Builder createLeavesDrops(Item seedItem, float[] seedChances, LootParameterSet parameterSet) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(seedItem)
                                .when(SurvivesExplosion.survivesExplosion())
                                .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                                .when(SeasonalSeedDropChance.seasonalSeedDropChance())
                )
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(Items.STICK)
                                .apply(SetCount.setCount(
                                        RandomValueRange.between(1.0F, 2.0F)
                                ))
                                .apply(ExplosionDecay.explosionDecay())
                                .when(TableBonus.bonusLevelFlatChance(
                                        Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
                                ))
                )
        ).setParamSet(parameterSet);
    }

    public static LootTable.Builder createWartDrops(Block primitiveWartBlock) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1))
                        .add(ItemLootEntry.lootTableItem(primitiveWartBlock))
                        .when(SurvivesExplosion.survivesExplosion())
                        .when(TableBonus.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE, 0.1F, 0.1333333F, 0.1666666F, 0.2F
                        ))
        );
    }

    public static LootTable.Builder createVoluntaryDrops(Item seedItem) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(seedItem)
                                .when(VoluntarySeedDropChance.voluntarySeedDropChance())
                )
        ).setParamSet(DTLootParameterSets.VOLUNTARY);
    }

    public static LootTable.Builder createBranchDrops(Block primitiveLogBlock, Item stickItem) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(primitiveLogBlock)
                                .apply(MultiplyLogsCount.multiplyLogsCount())
                                .apply(ExplosionDecay.explosionDecay())
                )
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(stickItem)
                                .apply(MultiplySticksCount.multiplySticksCount())
                                .apply(ExplosionDecay.explosionDecay())
                )
        ).setParamSet(DTLootParameterSets.WOOD);
    }

    private void writeTables(DirectoryCache cache) {
        Path outputFolder = this.generator.getOutputFolder();
        lootTables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/" + key.getPath());
            try {
                IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable.build()), path);
            } catch (IOException e) {
                LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return modId;
    }
}
