package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.Registry;

/**
 * Stores all {@link GenFeature} objects created by Dynamic Trees.
 *
 * <p>Add-ons can register new {@link GenFeature} objects using <code>GenFeature.REGISTRY.register</code>.</p>
 *
 * @author Harley O'Connor
 */
public final class GenFeatures {

    public static final GenFeature FRUIT = new FruitGenFeature(DynamicTrees.location("fruit"));
    public static final GenFeature POD = new PodGenFeature(DynamicTrees.location("pod"));

    public static final GenFeature VINES = new VinesGenFeature(DynamicTrees.location("vines"));

    public static final GenFeature BEE_NEST = new BeeNestGenFeature(DynamicTrees.location("bee_nest"));
    public static final GenFeature SHROOMLIGHT = new ShroomlightGenFeature(DynamicTrees.location("shroomlight"));

    public static final GenFeature CONIFER_TOPPER = new ConiferTopperGenFeature(DynamicTrees.location("conifer_topper"));
    public static final GenFeature MOUND = new MoundGenFeature(DynamicTrees.location("mound"));

    public static final GenFeature ROOTS = new RootsGenFeature(DynamicTrees.location("roots"));

    public static final GenFeature CLEAR_VOLUME = new ClearVolumeGenFeature(DynamicTrees.location("clear_volume"));
    public static final GenFeature BOTTOM_FLARE = new BottomFlareGenFeature(DynamicTrees.location("bottom_flare"));
    public static final GenFeature UNDERGROWTH = new UndergrowthGenFeature(DynamicTrees.location("undergrowth"));

    public static final GenFeature PODZOL = new PodzolGenFeature(DynamicTrees.location("podzol"));

    public static final GenFeature BUSH = new BushGenFeature(DynamicTrees.location("bush"));

    public static final GenFeature HUGE_MUSHROOM = new HugeMushroomGenFeature(DynamicTrees.location("huge_mushroom"));
    public static final GenFeature HUGE_MUSHROOMS = new HugeMushroomUndergrowthGenFeature(DynamicTrees.location("huge_mushroom_undergrowth"));

    public static final GenFeature MUSHROOM_ROT = new MushroomRotGenFeature(DynamicTrees.location("mushroom_rot"));
    public static final GenFeature ROT_SOIL = new RotSoilGenFeature(DynamicTrees.location("rot_soil"));

    public static final GenFeature BIOME_PREDICATE = new BiomePredicateGenFeature(DynamicTrees.location("biome_predicate"));
    public static final GenFeature RANDOM_PREDICATE = new RandomPredicateGenFeature(DynamicTrees.location("random_predicate"));

    public static final GenFeature ALTERNATIVE_LEAVES = new AlternativeLeavesGenFeature(DynamicTrees.location("alt_leaves"));

    public static final GenFeature ROOT_SYSTEM = new RootSystemGenFeature(DynamicTrees.location("root_system"));
    public static final GenFeature CREAKING_HEART = new CreakingHeartGenFeature(DynamicTrees.location("creaking_heart"));
    public static final GenFeature LEAF_LITTER = new LeafLitterGenFeature(DynamicTrees.location("leaf_litter"));

    public static void register(final Registry<GenFeature> registry) {
        registry.registerAll(FRUIT, POD,

                BEE_NEST, SHROOMLIGHT, CONIFER_TOPPER, MOUND,
                ROOTS, CLEAR_VOLUME, BOTTOM_FLARE, UNDERGROWTH,
                ROT_SOIL, VINES, PODZOL, BUSH, MUSHROOM_ROT,
                HUGE_MUSHROOM, HUGE_MUSHROOMS,  BIOME_PREDICATE,
                RANDOM_PREDICATE,
                ALTERNATIVE_LEAVES, ROOT_SYSTEM, CREAKING_HEART, LEAF_LITTER);
    }

}
