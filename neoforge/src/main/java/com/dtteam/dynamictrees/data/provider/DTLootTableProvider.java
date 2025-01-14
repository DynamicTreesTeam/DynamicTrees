package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.event.DataGenerationStreamEvent;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * @author Harley O'Connor
 */
public class DTLootTableProvider extends LootTableProvider {

    private final String modId;
    private final ExistingFileHelper fileHelper;

    public DTLootTableProvider(PackOutput output, String modId, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(), registries);
        this.modId = modId;
        this.fileHelper = fileHelper;
    }

    public class BlockLoot extends BlockLootSubProvider {
        protected final HolderLookup.Provider registries;
        protected BlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlagSet.of(), registries);
            this.registries = registries;
        }

        @Override
        protected void generate() {
            Species.REGISTRY.dataGenerationStream(modId).forEach(this::addVoluntaryTable);

            BuiltInRegistries.BLOCK.stream()
                    .filter(block -> block instanceof BranchBlock)
                    .map(block -> (BranchBlock) block)
                    .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(modId))
                    .forEach(this::addBranchTable);

            LeavesProperties.REGISTRY.dataGenerationStream(modId).forEach(leavesProperties -> {
                addLeavesBlockTable(leavesProperties);
                addLeavesTable(leavesProperties);
            });

            Fruit.REGISTRY.dataGenerationStream(modId).forEach(this::addFruitBlockTable);
            Pod.REGISTRY.dataGenerationStream(modId).forEach(this::addPodBlockTable);

            ModLoader.postEvent(new DataGenerationStreamEvent(this, modId, fileHelper, map));
        }

        @Override
        public void generate(@NotNull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            this.generate();

            this.map.forEach(output);
        }

        private void addVoluntaryTable(Species species) {
            if (species.shouldGenerateVoluntaryDrops()) {
                final ResourceLocation leavesTablePath = species.getVoluntaryDropsPath();
                if (!fileHelper.exists(leavesTablePath, PackType.SERVER_DATA)) {
                    this.map.put(ResourceKey.create(Registries.LOOT_TABLE, leavesTablePath), species.createVoluntaryDrops(registries));
                }
            }
        }

        private void addBranchTable(BranchBlock branchBlock) {
            if (branchBlock.shouldGenerateBranchDrops()) {
                final ResourceLocation branchTablePath = branchBlock.getLootTableName();
                if (!fileHelper.exists(branchTablePath, PackType.SERVER_DATA)) {
                    this.map.put(ResourceKey.create(Registries.LOOT_TABLE, branchTablePath), branchBlock.createBranchDrops(registries));
                }
            }
        }

        private void addLeavesBlockTable(LeavesProperties leavesProperties) {
            if (leavesProperties.shouldGenerateBlockDrops()) {
                final ResourceLocation leavesBlockTablePath = leavesProperties.getBlockLootTableName();
                if (!fileHelper.exists(leavesBlockTablePath, PackType.SERVER_DATA)) {
                    this.map.put(ResourceKey.create(Registries.LOOT_TABLE, leavesBlockTablePath), leavesProperties.createBlockDrops(registries));
                }
            }
        }

        private void addLeavesTable(LeavesProperties leavesProperties) {
            if (leavesProperties.shouldGenerateDrops()) {
                final ResourceLocation leavesTablePath = leavesProperties.getLootTableName();
                if (!fileHelper.exists(leavesTablePath, PackType.SERVER_DATA)) {
                    this.map.put(ResourceKey.create(Registries.LOOT_TABLE, leavesTablePath), leavesProperties.createDrops(registries));
                }
            }
        }

        private void addFruitBlockTable(Fruit fruit) {
            if (fruit.shouldGenerateBlockDrops()) {
                final ResourceLocation fruitBlockTablePath = fruit.getBlockDropsPath();
                if (!fileHelper.exists(fruitBlockTablePath, PackType.SERVER_DATA)) {
                    this.map.put(ResourceKey.create(Registries.LOOT_TABLE, fruitBlockTablePath), fruit.createBlockDrops(registries));
                }
            }
        }

        private void addPodBlockTable(Pod pod) {
            if (pod.shouldGenerateBlockDrops()) {
                final ResourceLocation fruitBlockTablePath = pod.getBlockDropsPath();
                if (!fileHelper.exists(fruitBlockTablePath, PackType.SERVER_DATA)) {
                    this.map.put(ResourceKey.create(Registries.LOOT_TABLE, fruitBlockTablePath), pod.createBlockDrops(registries));
                }
            }
        }

	}
}
