package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.Species.LogsAndSticks;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class LogDropCreator extends DropCreator {

    /**
     * This works in addition to {@link DTConfigs#TREE_HARVEST_MULTIPLIER}, meant for trees that are too small to drop
     * any wood.
     */
    public static final ConfigurationProperty<Float> MULTIPLIER = ConfigurationProperty.floatProperty("multiplier");

    public LogDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MULTIPLIER);
    }

    @Override
    protected ConfiguredDropCreator<DropCreator> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MULTIPLIER, 1.0f);
    }

    @Override
    public void appendLogDrops(ConfiguredDropCreator<DropCreator> configuration, LogDropContext context) {
        final Species species = context.species();
        final NetVolumeNode.Volume volume = context.volume();
        volume.multiplyVolume(configuration.get(MULTIPLIER));

        final LogsAndSticks las = species.getLogsAndSticks(volume);

        int numLogs = las.logs.size();
        if (numLogs > 0) {
            context.drops().addAll(las.logs);
        }
        int numSticks = las.sticks;
        if (numSticks > 0) {
            final ItemStack stack = species.getFamily().getStick(numSticks);
            while (numSticks > 0) {
                ItemStack drop = stack.copy();
                drop.setCount(Math.min(numSticks, stack.getMaxStackSize()));
                context.drops().add(drop);
                numSticks -= stack.getMaxStackSize();
            }
        }
    }

}
