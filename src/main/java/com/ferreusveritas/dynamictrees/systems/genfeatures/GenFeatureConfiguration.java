package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.Configuration;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.configurations.TemplateRegistry;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.GenerationContext;
import com.ferreusveritas.dynamictrees.trees.Species;

/**
 * A configured version of a {@link GenFeature}. This is used for holding {@link ConfigurationProperty} objects so that
 * {@link GenFeature} objects can be customised by different {@link Species}.
 *
 * @author Harley O'Connor
 */
public class GenFeatureConfiguration extends Configuration<GenFeatureConfiguration, GenFeature> {

    public static final TemplateRegistry<GenFeatureConfiguration> TEMPLATES = new TemplateRegistry<>();

    public GenFeatureConfiguration(GenFeature genFeature) {
        super(genFeature);
    }

    public GenFeature getGenFeature() {
        return this.configurable;
    }

    /**
     * {@inheritDoc}
     *
     * @return The copy of this {@link GenFeatureConfiguration}.
     */
    @Override
    public GenFeatureConfiguration copy() {
        final GenFeatureConfiguration duplicateGenFeature = new GenFeatureConfiguration(this.configurable);
        duplicateGenFeature.properties.putAll(this.properties);
        return duplicateGenFeature;
    }

    /**
     * Invokes {@link GenFeature#generate(GenFeatureConfiguration, GenFeature.Type, GenerationContext)} for this configured
     * feature's gen feature.
     *
     * @param type    the type of generation to perform
     * @param context the context
     * @param <C>     the type of the context
     * @param <R>     the return type of the action
     * @return the return of the executed action
     */
    public <C extends GenerationContext, R> R generate(GenFeature.Type<C, R> type, C context) {
        return this.configurable.generate(this, type, context);
    }

    /**
     * Called before this {@link GenFeature} is applied to a {@link Species}. Returns {@code false} if the application
     * should be aborted.
     *
     * @param species the species the feature is being added to
     * @return {@code true} if it should be applied; otherwise {@code false} if the application should be aborted
     */
    public boolean shouldApply(Species species) {
        return this.configurable.shouldApply(species, this);
    }

    public static GenFeatureConfiguration getNull() {
        return GenFeature.NULL.getDefaultConfiguration();
    }

}
