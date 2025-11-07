package com.dtteam.dynamictrees.worldgen.featurecancellation;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.api.worldgen.FeatureCanceller;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class FeatureCancellers {

    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("tree"), TreeConfiguration.class);

    public static final FeatureCanceller ROOTED_TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("rooted_tree"), RootSystemConfiguration.class);

    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.location("fungus"), HugeFungusConfiguration.class);

    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.location("mushroom"), HugeMushroomFeatureConfiguration.class);

    public static void register(final Registry<FeatureCanceller> registry) {
        registry.registerAll(TREE_CANCELLER, ROOTED_TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
    }

}
