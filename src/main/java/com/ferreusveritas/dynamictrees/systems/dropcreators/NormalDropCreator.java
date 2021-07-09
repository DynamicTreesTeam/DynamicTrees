package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.Drops;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

/**
 * @author Harley O'Connor
 */
public final class NormalDropCreator extends DropCreator {

    @SuppressWarnings("all")
    public static final ConfigurationProperty<HashMap<DropType<DropContext>, Drops>> DROP_MAP = ConfigurationProperty.property("drops",
            (Class<HashMap<DropType<DropContext>, Drops>>) new HashMap<DropType<DropContext>, Drops>().getClass());

    public NormalDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(DROP_MAP);
    }

    @Override
    public void appendHarvestDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
        configuration.get(DROP_MAP).get(DropType.HARVEST).appendDrops(context.drops(), context.random(), context.fortune());
    }

    @Override
    public void appendVoluntaryDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
        configuration.get(DROP_MAP).get(DropType.VOLUNTARY).appendDrops(context.drops(), context.random(), 0);
    }

    @Override
    public void appendLeavesDrops(ConfiguredDropCreator<DropCreator> configuration, DropContext context) {
        configuration.get(DROP_MAP).get(DropType.LEAVES).appendDrops(context.drops(), context.random(), context.fortune());
    }

    @Override
    public void appendLogDrops(ConfiguredDropCreator<DropCreator> configuration, LogDropContext context) {
        configuration.get(DROP_MAP).get(DropType.LOGS).appendDrops(context.drops(), context.random(), 0);
    }

}
