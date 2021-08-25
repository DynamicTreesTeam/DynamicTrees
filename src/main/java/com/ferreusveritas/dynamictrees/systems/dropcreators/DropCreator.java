package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;
import com.ferreusveritas.dynamictrees.api.registry.RegistryEntry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.LogDropContext;
import com.ferreusveritas.dynamictrees.trees.Resettable;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

/**
 * This exists solely to aid in the creation of a cleaner anonymous class. All of the members in this class act as
 * pass-thrus by default.
 *
 * @author ferreusveritas
 */
public abstract class DropCreator extends ConfigurableRegistryEntry<DropCreator, ConfiguredDropCreator> implements Resettable<DropCreator> {

    public static final ConfigurationProperty<Float> RARITY = ConfigurationProperty.floatProperty("rarity");

    public static final class Type<C extends DropContext> extends RegistryEntry<Type<C>> {
        public static final Type<DropContext> NULL = new Type<>(DTTrees.NULL, (dropCreator, configured, context) -> {
        });

        @SuppressWarnings("unchecked")
        public static final Class<Type<DropContext>> TYPE = (Class<Type<DropContext>>) NULL.getClass();
        public static final SimpleRegistry<Type<DropContext>> REGISTRY = new SimpleRegistry<>(TYPE, NULL);

        public static final Type<DropContext> HARVEST = register(new Type<>(DynamicTrees.resLoc("harvest"),
                DropCreator::appendHarvestDrops));
        public static final Type<DropContext> VOLUNTARY = register(new Type<>(DynamicTrees.resLoc("voluntary"),
                DropCreator::appendVoluntaryDrops));
        public static final Type<DropContext> LEAVES = register(new Type<>(DynamicTrees.resLoc("leaves"),
                DropCreator::appendLeavesDrops));
        public static final Type<LogDropContext> LOGS = register(new Type<>(DynamicTrees.resLoc("logs"),
                DropCreator::appendLogDrops));

        @SuppressWarnings("unchecked")
        private static <C extends DropContext> Type<C> register(Type<C> type) {
            REGISTRY.register((Type<DropContext>) type);
            return type;
        }

        private final TriConsumer<DropCreator, ConfiguredDropCreator, C> appendDropConsumer;

        public Type(ResourceLocation registryName, TriConsumer<DropCreator, ConfiguredDropCreator, C> appendDropConsumer) {
            super(registryName);
            this.appendDropConsumer = appendDropConsumer;
        }

        public void appendDrops(ConfiguredDropCreator configuration, C context) {
            this.appendDropConsumer.accept(configuration.getConfigurable(), configuration, context);
        }

        @SuppressWarnings("unchecked")
        public static Class<Type<DropContext>> getGenericClass() {
            return (Class<Type<DropContext>>) NULL.getClass();
        }
    }

    public static final DropCreator NULL_DROP_CREATOR = new DropCreator(DTTrees.NULL) {
        @Override
        protected void registerProperties() {
        }
    };

    public static final SimpleRegistry<DropCreator> REGISTRY = new SimpleRegistry<>(DropCreator.class, NULL_DROP_CREATOR);

    public DropCreator(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected ConfiguredDropCreator createDefaultConfiguration() {
        return new ConfiguredDropCreator(this);
    }

    public <C extends DropContext> void appendDrops(final ConfiguredDropCreator configuration, final Type<C> type, final C context) {
        type.appendDrops(configuration, context);
    }

    protected void appendHarvestDrops(final ConfiguredDropCreator configuration, DropContext context) {
    }

    protected void appendVoluntaryDrops(final ConfiguredDropCreator configuration, DropContext context) {
    }

    protected void appendLeavesDrops(final ConfiguredDropCreator configuration, DropContext context) {
    }

    protected void appendLogDrops(final ConfiguredDropCreator configuration, LogDropContext context) {
    }

}
