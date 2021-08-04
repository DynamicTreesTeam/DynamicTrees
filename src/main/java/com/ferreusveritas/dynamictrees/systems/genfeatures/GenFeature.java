package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IFullGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.IPreGenFeature;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BiomePredicate;
import com.ferreusveritas.dynamictrees.util.CanGrowPredicate;
import net.minecraft.util.ResourceLocation;

/**
 * Base class for all gen features. These are features that grow on/in/around a tree on generation, or whilst growing,
 * depending on which interface is implemented.
 *
 * <p>Sub-classes should implement at least one of the following: {@link IFullGenFeature},
 * {@link IPostGenFeature}, {@link IPostGrowFeature}, or {@link IPreGenFeature} to do their generation.</p>
 *
 * @author Harley O'Connor
 */
public abstract class GenFeature extends ConfigurableRegistryEntry<GenFeature, ConfiguredGenFeature<GenFeature>> {

    // Common properties.
    public static final ConfigurationProperty<Float> VERTICAL_SPREAD = ConfigurationProperty.floatProperty("vertical_spread");
    public static final ConfigurationProperty<Integer> QUANTITY = ConfigurationProperty.integer("quantity");
    public static final ConfigurationProperty<Float> RAY_DISTANCE = ConfigurationProperty.floatProperty("ray_distance");
    public static final ConfigurationProperty<Integer> MAX_HEIGHT = ConfigurationProperty.integer("max_height");
    public static final ConfigurationProperty<CanGrowPredicate> CAN_GROW_PREDICATE = ConfigurationProperty.property("can_grow_predicate", CanGrowPredicate.class);
    public static final ConfigurationProperty<Integer> MAX_COUNT = ConfigurationProperty.integer("max_count");
    public static final ConfigurationProperty<Integer> FRUITING_RADIUS = ConfigurationProperty.integer("fruiting_radius");
    public static final ConfigurationProperty<Float> PLACE_CHANCE = ConfigurationProperty.floatProperty("place_chance");
    public static final ConfigurationProperty<BiomePredicate> BIOME_PREDICATE = ConfigurationProperty.property("biome_predicate", BiomePredicate.class);

    public static final GenFeature NULL_GEN_FEATURE = new GenFeature(DTTrees.NULL) {
        @Override
        protected void registerProperties() {
        }
    };

    /**
     * Central registry for all {@link GenFeature} objects.
     */
    public static final Registry<GenFeature> REGISTRY = new Registry<>(GenFeature.class, NULL_GEN_FEATURE);

    public GenFeature(final ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return new ConfiguredGenFeature<>(this);
    }

    /**
     * Method called right before a gen feature is added to a Species. Should return true by default, but can return
     * false to cancel the genFeature from being added.
     *
     * @param species       the species the feature is being added to
     * @param configuration the configuration the feature is being added with
     * @return whether the genFeature can be added.
     */
    public boolean onGenFeatureAdded(Species species, ConfiguredGenFeature<GenFeature> configuration) {
        return true;
    }

}
