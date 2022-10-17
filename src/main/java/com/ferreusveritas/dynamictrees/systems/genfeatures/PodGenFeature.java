package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.PodGenerationNode;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;
import java.util.Random;

public class PodGenFeature extends GenFeature {

    public static final ConfigurationProperty<Pod> POD = ConfigurationProperty.property("pod", Pod.class);

    public PodGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(POD, PLACE_CHANCE);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(POD, Pod.NULL)
                .with(PLACE_CHANCE, 0.125F);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() == 0) {
            final LevelAccessor level = context.level();
            if (shouldGrow(configuration, context.species(), context.levelContext(), context.treePos(), context.random())) {
                this.place(configuration.get(POD)::place, level, context.pos(),
                        SeasonHelper.getSeasonValue(context.levelContext(), context.pos()));
            }
        }
        return false;
    }

    private boolean shouldGrow(GenFeatureConfiguration configuration, Species species, LevelContext levelContext, BlockPos treePos,
                               Random random) {
        return species.seasonalFruitProductionFactor(levelContext, treePos) >
                random.nextFloat() && random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (shouldGenerate(configuration, context.random())) {
            this.place(configuration.get(POD)::placeDuringWorldGen, context.level(), context.pos(),
                    context.seasonValue());
            return true;
        }
        return false;
    }

    private boolean shouldGenerate(GenFeatureConfiguration configuration, Random random) {
        return random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    private void place(PodGenerationNode.PodPlacer podPlacer, LevelAccessor level, BlockPos rootPos,
                       @Nullable Float seasonValue) {
        TreeHelper.startAnalysisFromRoot(level, rootPos,
                new MapSignal(new PodGenerationNode(podPlacer, seasonValue)));
    }

}
