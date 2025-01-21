package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.configuration.ConfigurableRegistry;
import com.dtteam.dynamictrees.api.configuration.ConfigurableRegistryEntry;
import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.systems.genfeature.context.*;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.api.worldgen.BiomePredicate;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiPredicate;

/**
 * Base class for all gen features. These are features that grow on/in/around a tree on generation, or whilst growing,
 * depending on which methods are overridden. The default actions consist of:
 *
 * <ul>
 *     <li>{@linkplain #preGenerate(GenFeatureConfiguration, PreGenerationContext) Pre-generation}</li>
 *     <li>{@linkplain #postGenerate(GenFeatureConfiguration, PostGenerationContext) Post-generation}</li>
 *     <li>{@linkplain #postGrow(GenFeatureConfiguration, PostGrowContext) Post-growth}</li>
 *     <li>{@linkplain #postRot(GenFeatureConfiguration, PostRotContext) Post-rot}</li>
 *     <li>{@linkplain #generate(GenFeatureConfiguration, FullGenerationContext) Full-generation}</li>
 * </ul>
 * <p>
 * Each of these should be invoked by {@link GenFeature#generate(GenFeatureConfiguration, FullGenerationContext)} with
 * their corresponding type.
 *
 * @author Harley O'Connor
 */
public abstract class GenFeature extends ConfigurableRegistryEntry<GenFeature, GenFeatureConfiguration> {

    ///////////////////////////////////////////
    // COMMON PROPERTIES                     //
    ///////////////////////////////////////////

    public static final ConfigurationProperty<Float> VERTICAL_SPREAD =
            ConfigurationProperty.floatProperty("vertical_spread");
    public static final ConfigurationProperty<Integer> QUANTITY = ConfigurationProperty.integer("quantity");
    public static final ConfigurationProperty<Float> RAY_DISTANCE = ConfigurationProperty.floatProperty("ray_distance");
    public static final ConfigurationProperty<Integer> MAX_HEIGHT = ConfigurationProperty.integer("max_height");
    public static final ConfigurationProperty<CanGrowPredicate> CAN_GROW_PREDICATE =
            ConfigurationProperty.property("can_grow_predicate", CanGrowPredicate.class);
    public static final ConfigurationProperty<Integer> MAX_COUNT = ConfigurationProperty.integer("max_count");
    public static final ConfigurationProperty<Integer> FRUITING_RADIUS =
            ConfigurationProperty.integer("fruiting_radius");
    public static final ConfigurationProperty<Float> PLACE_CHANCE = ConfigurationProperty.floatProperty("place_chance");
    public static final ConfigurationProperty<BiomePredicate> BIOME_PREDICATE =
            ConfigurationProperty.property("biome_predicate", BiomePredicate.class);

    ///////////////////////////////////////////
    // REGISTRY                              //
    ///////////////////////////////////////////

    public static final GenFeature NULL = new GenFeature(DynamicTrees.NULL) {
        @Override
        protected void registerProperties() {
        }

        @Override
        public GenFeatureConfiguration getDefaultConfiguration() {
            return this.defaultConfiguration;
        }
    };

    /**
     * Central registry for all {@link GenFeature} objects.
     */
    public static final ConfigurableRegistry<GenFeature, GenFeatureConfiguration> REGISTRY =
            new ConfigurableRegistry<>(GenFeature.class, NULL, GenFeatureConfiguration.TEMPLATES);

    public GenFeature(final ResourceLocation registryName) {
        super(registryName);
    }

    ///////////////////////////////////////////
    // CONFIGURATION                         //
    ///////////////////////////////////////////

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return new GenFeatureConfiguration(this);
    }

    ///////////////////////////////////////////
    // GENERATION                            //
    ///////////////////////////////////////////

    /**
     * Performs a generation action as defined by the specified {@code type}.
     *
     * @param configuration the configuration
     * @param type          the type of generation to perform
     * @param context       the context
     * @param <C>           the type of the context
     * @param <R>           the return type of the action
     * @return the return of the executed action
     */
    public <C extends GenFeatureContext, R> R generate(GenFeatureConfiguration configuration, Type<C, R> type,
                                                       C context) {
        return type.generate(configuration, context);
    }

    /**
     * Performs a pre-generation action on a tree. This is invoked before any blocks have been placed, and returns the
     * position at which the {@linkplain SoilBlock root block} should be placed.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return the position at which to place the root block
     */
    protected BlockPos preGenerate(GenFeatureConfiguration configuration, PreGenerationContext context) {
        return context.pos();
    }

    /**
     * Performs a post-generation action on a tree. This is invoked after the entire tree has generated and before the
     * SpeciesPostGenerationEvent post generation event is fired.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return {@code true} if the action was successful; {@code false} otherwise
     */
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        return true;
    }

    /**
     * Performs a post-growth action on a tree. This is invoked after a single growth pulse has been sent through the
     * tree.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return {@code true} if the action was successful; {@code false} otherwise
     */
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        return true;
    }

    /**
     * int, int, int, RandomSource, boolean, boolean) rot action} has occurred.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return {@code true} if the action was successful; {@code false} otherwise
     */
    protected boolean postRot(GenFeatureConfiguration configuration, PostRotContext context) {
        return true;
    }

    /**
     * Performs a full generation action of a tree. This is invoked before the
     * {@link JoCode#generate(DynamicTreeGenerationContext)} and acts as a replacement for it.
     * The implementor should therefore note that other methods in this class will not be invoked by default.
     *
     * @param configuration the configuration
     * @param context       the context
     * @return {@code true} if this {@link GenFeature} handles full generation and so generation from a {@link JoCode}
     * should <b>not</b> proceed; {@code false} otherwise
     */
    protected boolean generate(GenFeatureConfiguration configuration, FullGenerationContext context) {
        return false;
    }

    /**
     * Called before this {@link GenFeature} is applied to a {@link Species}. Returns {@code false} if the application
     * should be aborted.
     *
     * @param species       the species the feature is being added to
     * @param configuration the configuration
     * @return {@code true} if it should be applied; otherwise {@code false} if the application should be aborted
     */
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return true;
    }

    ///////////////////////////////////////////
    // GENERATION TYPE                       //
    ///////////////////////////////////////////

    public static final class Type<C extends GenFeatureContext, R> {
        public static final Type<PreGenerationContext, BlockPos> PRE_GENERATION = new Type<>(GenFeature::preGenerate);
        public static final Type<PostGenerationContext, Boolean> POST_GENERATION = new Type<>(GenFeature::postGenerate);
        public static final Type<PostGrowContext, Boolean> POST_GROW = new Type<>(GenFeature::postGrow);
        public static final Type<PostRotContext, Boolean> POST_ROT = new Type<>(GenFeature::postRot);
        public static final Type<FullGenerationContext, Boolean> FULL = new Type<>(GenFeature::generate);

        private final TriFunction<GenFeature, GenFeatureConfiguration, C, R> generateConsumer;

        public Type(TriFunction<GenFeature, GenFeatureConfiguration, C, R> generateConsumer) {
            this.generateConsumer = generateConsumer;
        }

        public R generate(GenFeatureConfiguration configuration, C context) {
            return generateConsumer.apply(configuration.getGenFeature(), configuration, context);
        }
    }

    /**
     * A {@link BiPredicate} that tests if something should grow based on the {@link LevelAccessor} and {@link BlockPos}. Mainly
     * used as a {@link ConfigurationProperty}.
     *
     * @author Harley O'Connor
     */
    @FunctionalInterface
    public interface CanGrowPredicate extends BiPredicate<LevelAccessor, BlockPos> {
    }

}
