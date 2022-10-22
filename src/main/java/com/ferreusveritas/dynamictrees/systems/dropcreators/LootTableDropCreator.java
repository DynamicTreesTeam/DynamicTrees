package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.data.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.data.DTLootParameters;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * @author Harley O'Connor
 */
public final class LootTableDropCreator extends DropCreator {

    private static final ConfigurationProperty<ResourceLocation> HARVEST_TABLE = ConfigurationProperty.property("harvest_table", ResourceLocation.class);
    private static final ConfigurationProperty<ResourceLocation> VOLUNTARY_TABLE = ConfigurationProperty.property("voluntary_table", ResourceLocation.class);
    private static final ConfigurationProperty<ResourceLocation> LEAVES_TABLE = ConfigurationProperty.property("leaves_table", ResourceLocation.class);
    private static final ConfigurationProperty<ResourceLocation> LOGS_TABLE = ConfigurationProperty.property("logs_table", ResourceLocation.class);

    public LootTableDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(HARVEST_TABLE, VOLUNTARY_TABLE, LEAVES_TABLE, LOGS_TABLE);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HARVEST_TABLE, LootTable.EMPTY.getLootTableId())
                .with(VOLUNTARY_TABLE, LootTable.EMPTY.getLootTableId())
                .with(LEAVES_TABLE, LootTable.EMPTY.getLootTableId())
                .with(LOGS_TABLE, LootTable.EMPTY.getLootTableId());
    }

    @Override
    public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
        context.drops().addAll(((ServerLevel) context.world()).getServer().getLootTables().get(configuration.get(HARVEST_TABLE))
                .getRandomItems(new LootContext.Builder((ServerLevel) context.world())
                        .withParameter(LootContextParams.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .withParameter(DTLootParameters.SPECIES, context.species())
                        .withParameter(DTLootParameters.FERTILITY, context.fertility())
                        .withParameter(DTLootParameters.FORTUNE, context.fortune())
                        .create(DTLootParameterSets.HARVEST)
                )
        );
    }

    @Override
    public void appendVoluntaryDrops(DropCreatorConfiguration configuration, DropContext context) {
        context.drops().addAll(((ServerLevel) context.world()).getServer().getLootTables().get(configuration.get(VOLUNTARY_TABLE))
                .getRandomItems(new LootContext.Builder((ServerLevel) context.world())
                        .withParameter(LootContextParams.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .withParameter(DTLootParameters.SPECIES, context.species())
                        .withParameter(DTLootParameters.FERTILITY, context.fertility())
                        .create(DTLootParameterSets.VOLUNTARY)
                )
        );
    }

    @Override
    public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
        context.drops().addAll(((ServerLevel) context.world()).getServer().getLootTables().get(configuration.get(LEAVES_TABLE))
                .getRandomItems(new LootContext.Builder((ServerLevel) context.world())
                        .withParameter(LootContextParams.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .withParameter(LootContextParams.TOOL, context.tool())
                        .withParameter(DTLootParameters.SPECIES, context.species())
                        .withParameter(DTLootParameters.FORTUNE, context.fortune())
                        .create(DTLootParameterSets.LEAVES)
                )
        );
    }

    @Override
    public void appendLogDrops(DropCreatorConfiguration configuration, LogDropContext context) {
        context.drops().addAll(((ServerLevel) context.world()).getServer().getLootTables().get(configuration.get(LOGS_TABLE))
                .getRandomItems(new LootContext.Builder((ServerLevel) context.world())
                        .withParameter(LootContextParams.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .withParameter(LootContextParams.TOOL, context.tool())
                        .withParameter(DTLootParameters.SPECIES, context.species())
                        .withParameter(DTLootParameters.LOGS_AND_STICKS, context.species().getLogsAndSticks(context.volume()))
                        .create(DTLootParameterSets.LOGS)
                )
        );
    }

}
