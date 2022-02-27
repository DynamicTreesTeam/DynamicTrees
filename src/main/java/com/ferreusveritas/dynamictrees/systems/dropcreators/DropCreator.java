package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurableRegistry;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.*;
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
public abstract class DropCreator extends ConfigurableRegistryEntry<DropCreator, DropCreatorConfiguration>
        implements Resettable<DropCreator> {

    public static final ConfigurationProperty<Float> RARITY = ConfigurationProperty.floatProperty("rarity");
    public static final DropCreator NULL = new DropCreator(DTTrees.NULL) {
        @Override
        protected void registerProperties() {
        }
    };

    public static final ConfigurableRegistry<DropCreator, DropCreatorConfiguration> REGISTRY =
            new ConfigurableRegistry<>(DropCreator.class, NULL, DropCreatorConfiguration.TEMPLATES);

    public DropCreator(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return new DropCreatorConfiguration(this);
    }

    public <C extends DropContext> void appendDrops(final DropCreatorConfiguration configuration, final Type<C> type,
                                                    final C context) {
        type.appendDrops(configuration, context);
    }

    protected void appendHarvestDrops(final DropCreatorConfiguration configuration, DropContext context) {
    }

    protected void appendVoluntaryDrops(final DropCreatorConfiguration configuration, DropContext context) {
    }

    protected void appendLeavesDrops(final DropCreatorConfiguration configuration, DropContext context) {
    }

    protected void appendLogDrops(final DropCreatorConfiguration configuration, LogDropContext context) {
    }

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
        private final TriConsumer<DropCreator, DropCreatorConfiguration, C> appendDropConsumer;

        public Type(ResourceLocation registryName,
                    TriConsumer<DropCreator, DropCreatorConfiguration, C> appendDropConsumer) {
            super(registryName);
            this.appendDropConsumer = appendDropConsumer;
        }

        @SuppressWarnings("unchecked")
        private static <C extends DropContext> Type<C> register(Type<C> type) {
            REGISTRY.register((Type<DropContext>) type);
            return type;
        }

        @SuppressWarnings("unchecked")
        public static Class<Type<DropContext>> getGenericClass() {
            return (Class<Type<DropContext>>) NULL.getClass();
        }

        public void appendDrops(DropCreatorConfiguration configuration, C context) {
            this.appendDropConsumer.accept(configuration.getConfigurable(), configuration, context);
        }
    }

}
