package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.ConfigurableRegistryEntry;
import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.*;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BiomePredicate;
import com.ferreusveritas.dynamictrees.util.CanGrowPredicate;
import com.ferreusveritas.dynamictrees.util.TriFunction;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * Base class for all gen features. These are features that grow on/in/around a tree on generation, or whilst growing,
 * depending on which methods are overridden.
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

    public static final class Type<C extends GenerationContext<?>, R> {
        public static final Type<PreGenerationContext, BlockPos> PRE_GENERATION = new Type<>(GenFeature::preGenerate);
        public static final Type<PostGenerationContext, Boolean> POST_GENERATION = new Type<>(GenFeature::postGenerate);
        public static final Type<PostGrowContext, Boolean> POST_GROW = new Type<>(GenFeature::postGrow);
        public static final Type<PostRotContext, Boolean> POST_ROT = new Type<>(GenFeature::postRot);
        public static final Type<FullGenerationContext, Boolean> FULL = new Type<>(GenFeature::generate);

        private final TriFunction<GenFeature, ConfiguredGenFeature<GenFeature>, C, R> generateConsumer;

        public Type(TriFunction<GenFeature, ConfiguredGenFeature<GenFeature>, C, R> generateConsumer) {
            this.generateConsumer = generateConsumer;
        }

        public R generate(ConfiguredGenFeature<GenFeature> configuration, C context) {
            return generateConsumer.apply(configuration.getGenFeature(), configuration, context);
        }
    }

    public <C extends GenerationContext<?>, R> R generate(ConfiguredGenFeature<GenFeature> configuration, Type<C, R> type, C context) {
        return type.generate(configuration, context);
    }

    protected BlockPos preGenerate(ConfiguredGenFeature<GenFeature> configuration, PreGenerationContext context) {
        return context.pos();
    }

    protected boolean postGenerate(ConfiguredGenFeature<GenFeature> configuration, PostGenerationContext context) {
        return true;
    }

    protected boolean postGrow(ConfiguredGenFeature<GenFeature> configuration, PostGrowContext context) {
        return true;
    }

    protected boolean postRot(ConfiguredGenFeature<GenFeature> configuration, PostRotContext context) {
        return true;
    }

    /**
     * Handles full generation of a tree.
     *
     * @param configuration The {@link ConfiguredGenFeature} instance to generate for.
     * @param context       The {@link FullGenerationContext} object.
     * @return {@code true} if this {@link GenFeature} handles full generation and so generation from a {@link JoCode}
     * should <b>not</b> proceed; {@code false} otherwise.
     */
    protected boolean generate(ConfiguredGenFeature<GenFeature> configuration, FullGenerationContext context) {
        return false;
    }

    /**
     * Called before this {@link GenFeature} is applied to a {@link Species}. Returns {@code false} if the application
     * should be aborted.
     *
     * @param species       the species the feature is being added to
     * @param configuration the configuration the feature is being added with
     * @return {@code true} if it should be applied; otherwise {@code false} if the application should be aborted
     */
    public boolean onApplied(Species species, ConfiguredGenFeature<GenFeature> configuration) {
        return true;
    }

}
