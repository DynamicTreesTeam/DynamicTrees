package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.data.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.data.DTLootParameters;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class LootTableDropCreator extends DropCreator {

    private static final ConfigurationProperty<LootTable> HARVEST_TABLE = ConfigurationProperty.property("harvest_table", LootTable.class);
    private static final ConfigurationProperty<LootTable> VOLUNTARY_TABLE = ConfigurationProperty.property("voluntary_table", LootTable.class);
    private static final ConfigurationProperty<LootTable> LEAVES_TABLE = ConfigurationProperty.property("leaves_table", LootTable.class);
    private static final ConfigurationProperty<LootTable> LOGS_TABLE = ConfigurationProperty.property("logs_table", LootTable.class);

    public LootTableDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(HARVEST_TABLE, VOLUNTARY_TABLE, LEAVES_TABLE, LOGS_TABLE);
    }

    @Override
    protected ConfiguredDropCreator<DropCreator> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HARVEST_TABLE, LootTable.EMPTY)
                .with(VOLUNTARY_TABLE, LootTable.EMPTY)
                .with(LEAVES_TABLE, LootTable.EMPTY)
                .with(LOGS_TABLE, LootTable.EMPTY);
    }

    @Override
    protected List<ItemStack> getHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, World world, Species species,
                                              BlockPos leafPos, Random random, List<ItemStack> drops, int fertility, int fortune) {
        drops.addAll(configuration.get(HARVEST_TABLE).getRandomItems(new LootContext.Builder((ServerWorld) world)
                .withParameter(LootParameters.BLOCK_STATE, world.getBlockState(leafPos))
                .withParameter(DTLootParameters.SPECIES, species)
                .withParameter(DTLootParameters.FERTILITY, fertility)
                .withParameter(DTLootParameters.FORTUNE, fortune)
                .create(DTLootParameterSets.HARVEST)));
        return drops;
    }

    @Override
    protected List<ItemStack> getVoluntaryDrops(ConfiguredDropCreator<DropCreator> configuration, World world, Species species,
                                                BlockPos rootPos, Random random, List<ItemStack> drops, int fertility) {
        drops.addAll(configuration.get(VOLUNTARY_TABLE).getRandomItems(new LootContext.Builder((ServerWorld) world)
                .withParameter(LootParameters.BLOCK_STATE, world.getBlockState(rootPos))
                .withParameter(DTLootParameters.SPECIES, species)
                .withParameter(DTLootParameters.FERTILITY, fertility)
                .create(DTLootParameterSets.VOLUNTARY)));
        return drops;
    }

    @Override
    protected List<ItemStack> getLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, World world, Species species,
                                             BlockPos breakPos, Random random, List<ItemStack> drops, int fortune) {
        drops.addAll(configuration.get(LEAVES_TABLE).getRandomItems(new LootContext.Builder(((ServerWorld) world))
                .withParameter(LootParameters.BLOCK_STATE, world.getBlockState(breakPos))
                .withParameter(DTLootParameters.SPECIES, species)
                .withParameter(DTLootParameters.FORTUNE, fortune)
                .create(DTLootParameterSets.LEAVES)));
        return drops;
    }

    @Override
    protected List<ItemStack> getLogsDrops(ConfiguredDropCreator<DropCreator> configuration, World world, Species species,
                                           BlockPos breakPos, Random random, List<ItemStack> drops, NetVolumeNode.Volume volume) {
        drops.addAll(configuration.get(LOGS_TABLE).getRandomItems(new LootContext.Builder((ServerWorld) world)
                .withParameter(LootParameters.BLOCK_STATE, world.getBlockState(breakPos))
                .withParameter(DTLootParameters.SPECIES, species)
                .withParameter(DTLootParameters.LOGS_AND_STICKS, species.getLogsAndSticks(volume))
                .create(DTLootParameterSets.LOGS)));
        return drops;
    }

}
