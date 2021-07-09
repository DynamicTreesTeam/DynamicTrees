package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.data.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.data.DTLootParameters;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

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
    protected void appendHarvestDrops(ConfiguredDropCreator<DropCreator> configuration,
                                      DropContext context) {
        context.drops().addAll(configuration.get(HARVEST_TABLE).getRandomItems(new LootContext.Builder((ServerWorld) context.world())
                .withParameter(LootParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                .withParameter(DTLootParameters.SPECIES, context.species())
                .withParameter(DTLootParameters.FERTILITY, context.fertility())
                .withParameter(DTLootParameters.FORTUNE, context.fortune())
                .create(DTLootParameterSets.HARVEST)));
    }

    @Override
    protected void appendVoluntaryDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
        context.drops().addAll(configuration.get(VOLUNTARY_TABLE).getRandomItems(new LootContext.Builder((ServerWorld) context.world())
                .withParameter(LootParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                .withParameter(DTLootParameters.SPECIES, context.species())
                .withParameter(DTLootParameters.FERTILITY, context.fertility())
                .create(DTLootParameterSets.VOLUNTARY)));
    }

    @Override
    protected void appendLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
        context.drops().addAll(configuration.get(LEAVES_TABLE).getRandomItems(new LootContext.Builder(((ServerWorld) context.world()))
                .withParameter(LootParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                .withParameter(DTLootParameters.SPECIES, context.species())
                .withParameter(DTLootParameters.FORTUNE, context.fortune())
                .create(DTLootParameterSets.LEAVES)));
    }

    @Override
    protected void appendLogDrops(ConfiguredDropCreator<DropCreator> configuration, LogDropContext context) {
        context.drops().addAll(configuration.get(LOGS_TABLE).getRandomItems(new LootContext.Builder((ServerWorld) context.world())
                .withParameter(LootParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                .withParameter(DTLootParameters.SPECIES, context.species())
                .withParameter(DTLootParameters.LOGS_AND_STICKS, context.species().getLogsAndSticks(context.volume()))
                .create(DTLootParameterSets.LOGS)));
    }

}
