package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.registry.IRegistry;
import net.minecraft.util.ResourceLocation;

/**
 * Stores all {@link GenFeature} objects created by Dynamic Trees.
 *
 * <p>Add-ons can register new {@link GenFeature} objects using <tt>GenFeature.REGISTRY.register</tt>.</p>
 *
 * @author Harley O'Connor
 */
public final class GenFeatures {

    public static final GenFeature FRUIT = new FruitGenFeature(regName("fruit"));
    public static final GenFeature COCOA = new CocoaGenFeature(regName("cocoa"));

    public static final GenFeature VINES = new VinesGenFeature(regName("vines"));

    public static final GenFeature BEE_NEST = new BeeNestGenFeature(regName("bee_nest"));
    public static final GenFeature SHROOMLIGHT = new ShroomlightGenFeature(regName("shroomlight"));

    public static final GenFeature CONIFER_TOPPER = new ConiferTopperGenFeature(regName("conifer_topper"));
    public static final GenFeature MOUND = new MoundGenFeature(regName("mound"));

    public static final GenFeature ROOTS = new RootsGenFeature(regName("roots"));

    public static final GenFeature CLEAR_VOLUME = new ClearVolumeGenFeature(regName("clear_volume"));
    public static final GenFeature BOTTOM_FLARE = new BottomFlareGenFeature(regName("bottom_flare"));
    public static final GenFeature UNDERGROWTH = new UndergrowthGenFeature(regName("undergrowth"));

    public static final GenFeature PODZOL = new PodzolGenFeature(regName("podzol"));

    public static final GenFeature BUSH = new BushGenFeature(regName("bush"));

    public static final GenFeature HUGE_MUSHROOM = new HugeMushroomGenFeature(regName("huge_mushroom"));
    public static final GenFeature HUGE_MUSHROOMS = new HugeMushroomsGenFeature(regName("huge_mushrooms"));

    public static final GenFeature MUSHROOM_ROT = new MushroomRotGenFeature(regName("mushroom_rot"));
    public static final GenFeature ROT_SOIL = new RotSoilGenFeature(regName("rot_soil"));

    public static final GenFeature BIOME_PREDICATE = new BiomePredicateGenFeature(regName("biome_predicate"));
    public static final GenFeature RANDOM_PREDICATE = new RandomPredicateGenFeature(regName("random_predicate"));

    public static final GenFeature ALTERNATIVE_LEAVES = new AlternativeLeavesGenFeature(regName("alt_leaves"));

    private static ResourceLocation regName(String name) {
        return new ResourceLocation(DynamicTrees.MOD_ID, name);
    }

    public static void register(final IRegistry<GenFeature> registry) {
        registry.registerAll(FRUIT, COCOA, VINES, BEE_NEST, SHROOMLIGHT, CONIFER_TOPPER, MOUND,
                ROOTS, CLEAR_VOLUME, BOTTOM_FLARE, UNDERGROWTH, PODZOL, BUSH, HUGE_MUSHROOM,
                HUGE_MUSHROOMS, MUSHROOM_ROT, ROT_SOIL, BIOME_PREDICATE, RANDOM_PREDICATE, ALTERNATIVE_LEAVES);
    }

}
