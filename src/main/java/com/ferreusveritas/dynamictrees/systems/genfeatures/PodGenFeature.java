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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

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
            final World world = context.world();
            if (shouldGrow(configuration, context.species(), context.world(), context.treePos(), context.random())) {
                this.place(configuration.get(POD)::place, world, context.pos(),
                        SeasonHelper.getSeasonValue(world, context.pos()));
            }
        }
        return false;
    }

    private boolean shouldGrow(GenFeatureConfiguration configuration, Species species, World world, BlockPos treePos,
                               Random random) {
        return species.seasonalFruitProductionFactor(world, treePos) >
                random.nextFloat() && random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (shouldGenerate(configuration, context.random())) {
            this.place(configuration.get(POD)::placeDuringWorldGen, context.world(), context.pos(),
                    context.seasonValue());
            return true;
        }
        return false;
    }

    private boolean shouldGenerate(GenFeatureConfiguration configuration, Random random) {
        return random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    private void place(PodGenerationNode.PodPlacer podPlacer, IWorld world, BlockPos rootPos,
                       @Nullable Float seasonValue) {
        TreeHelper.startAnalysisFromRoot(world, rootPos,
                new MapSignal(new PodGenerationNode(podPlacer, seasonValue)));
    }

}
