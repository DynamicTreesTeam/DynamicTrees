package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.trees.IResettable;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * This exists solely to aid in the creation of a cleaner anonymous class. All of the members in this class act as
 * pass-thrus by default.
 *
 * @author ferreusveritas
 */
public abstract class DropCreator extends ConfigurableRegistryEntry<DropCreator, ConfiguredDropCreator> implements IResettable<DropCreator> {

    public static final ConfigurationProperty<Float> RARITY = ConfigurationProperty.floatProperty("rarity");

    public static final class DropType<C extends DropContext> extends RegistryEntry<DropType<C>> {
        public static final DropType<DropContext> NULL = new DropType<>(DTTrees.NULL, (dropCreator, configured, context) -> {
        });

        @SuppressWarnings("unchecked")
        public static final Class<DropType<DropContext>> TYPE = (Class<DropType<DropContext>>) NULL.getClass();
        public static final Registry<DropType<DropContext>> REGISTRY = new Registry<>(TYPE, NULL);

        public static final DropType<DropContext> HARVEST = register(new DropType<>(DynamicTrees.resLoc("harvest"), DropCreator::appendHarvestDrops));
        public static final DropType<DropContext> VOLUNTARY = register(new DropType<>(DynamicTrees.resLoc("voluntary"), DropCreator::appendVoluntaryDrops));
        public static final DropType<DropContext> LEAVES = register(new DropType<>(DynamicTrees.resLoc("leaves"), DropCreator::appendLeavesDrops));
        public static final DropType<LogDropContext> LOGS = register(new DropType<>(DynamicTrees.resLoc("logs"), DropCreator::appendLogDrops));

        @SuppressWarnings("unchecked")
        private static <C extends DropContext> DropType<C> register(DropType<C> dropType) {
            REGISTRY.register((DropType<DropContext>) dropType);
            return dropType;
        }

        private final TriConsumer<DropCreator, ConfiguredDropCreator, C> appendDropConsumer;

        public DropType(ResourceLocation registryName, TriConsumer<DropCreator, ConfiguredDropCreator, C> appendDropConsumer) {
            super(registryName);
            this.appendDropConsumer = appendDropConsumer;
        }

        public void appendDrops(ConfiguredDropCreator configuration, C context) {
            this.appendDropConsumer.accept(configuration.getConfigurable(), configuration, context);
        }

        @SuppressWarnings("unchecked")
        public static Class<DropType<DropContext>> getGenericClass() {
            return (Class<DropType<DropContext>>) NULL.getClass();
        }
    }

    public static final DropCreator NULL_DROP_CREATOR = new DropCreator(DTTrees.NULL) {
        @Override
        protected void registerProperties() {
        }
    };

    public static final Registry<DropCreator> REGISTRY = new Registry<>(DropCreator.class, NULL_DROP_CREATOR);

    public DropCreator(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected ConfiguredDropCreator createDefaultConfiguration() {
        return new ConfiguredDropCreator(this);
    }

    public <C extends DropContext> void appendDrops(final ConfiguredDropCreator configuration, final DropType<C> dropType, final C context) {
        dropType.appendDrops(configuration, context);
    }

    public void appendHarvestDrops(final ConfiguredDropCreator configuration, DropContext context) {
    }

    public void appendVoluntaryDrops(final ConfiguredDropCreator configuration, DropContext context) {
    }

    public void appendLeavesDrops(final ConfiguredDropCreator configuration, DropContext context) {
    }

    public void appendLogDrops(final ConfiguredDropCreator configuration, LogDropContext context) {
    }

}
