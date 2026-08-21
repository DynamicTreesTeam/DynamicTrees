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
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class DTBlockLootSubProvider extends BlockLootSubProvider {
    protected final HolderLookup.Provider registries;
    private final String modId;
    private final Map<ResourceKey<LootTable>, LootTable.Builder> tables = new HashMap<>();

    protected DTBlockLootSubProvider(HolderLookup.Provider registries, String modId) {
        super(Set.of(), FeatureFlagSet.of(), registries);
        this.registries = registries;
        this.modId = modId;
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

        ModLoader.postEvent(new DataGenerationStreamEvent(this, modId, tables, registries));
    }

    @Override
    public void generate(@NotNull BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        this.generate();
        this.tables.forEach((key, builder) -> {
            // 26.2 has no public loot ContextKeySet registry; JSON type must be minecraft:block.
            builder.setParamSet(LootContextParamSets.BLOCK);
            output.accept(key, builder);
        });
    }

    private void addVoluntaryTable(Species species) {
        if (species.shouldGenerateVoluntaryDrops()) {
            final Identifier leavesTablePath = species.getVoluntaryDropsPath();
            this.tables.put(ResourceKey.create(Registries.LOOT_TABLE, leavesTablePath), species.createVoluntaryDrops(registries));
        }
    }

    private void addBranchTable(BranchBlock branchBlock) {
        if (branchBlock.shouldGenerateBranchDrops()) {
            final Identifier branchTablePath = branchBlock.getLootTableName();
            this.tables.put(ResourceKey.create(Registries.LOOT_TABLE, branchTablePath), branchBlock.createBranchDrops(registries));
        }
    }

    private void addLeavesBlockTable(LeavesProperties leavesProperties) {
        if (leavesProperties.shouldGenerateBlockDrops()) {
            final Identifier leavesBlockTablePath = leavesProperties.getBlockLootTableName();
            this.tables.put(ResourceKey.create(Registries.LOOT_TABLE, leavesBlockTablePath), leavesProperties.createBlockDrops(registries));
        }
    }

    private void addLeavesTable(LeavesProperties leavesProperties) {
        if (leavesProperties.shouldGenerateDrops()) {
            final Identifier leavesTablePath = leavesProperties.getLootTableName();
            this.tables.put(ResourceKey.create(Registries.LOOT_TABLE, leavesTablePath), leavesProperties.createDrops(registries));
        }
    }

    private void addFruitBlockTable(Fruit fruit) {
        if (fruit.shouldGenerateBlockDrops()) {
            final Identifier fruitBlockTablePath = fruit.getBlockDropsPath();
            this.tables.put(ResourceKey.create(Registries.LOOT_TABLE, fruitBlockTablePath), fruit.createBlockDrops(registries));
        }
    }

    private void addPodBlockTable(Pod pod) {
        if (pod.shouldGenerateBlockDrops()) {
            final Identifier fruitBlockTablePath = pod.getBlockDropsPath();
            this.tables.put(ResourceKey.create(Registries.LOOT_TABLE, fruitBlockTablePath), pod.createBlockDrops(registries));
        }
    }

}
