package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.util.ResourceLocation;

/**
 * Stores all {@link GenFeature} objects created by Dynamic Trees.
 *
 * Add-ons can register new {@link GenFeature} objects using <tt>GenFeature.REGISTRY.register</tt>.
 *
 * @author Harley O'Connor
 */
public final class GenFeatures {

    public static final GenFeature FRUIT = register(new FruitGenFeature(regName("fruit")));
    public static final GenFeature COCOA = register(new CocoaGenFeature(regName("cocoa")));

    public static final GenFeature VINES = register(new VinesGenFeature(regName("vines")));

    public static final GenFeature BEE_NEST = register(new BeeNestGenFeature(regName("bee_nest")));
    public static final GenFeature SHROOMLIGHT = register(new ShroomlightGenFeature(regName("shroomlight")));

    public static final GenFeature CONIFER_TOPPER = register(new ConiferTopperGenFeature(regName("conifer_topper")));
    public static final GenFeature MOUND = register(new MoundGenFeature(regName("mound")));

    public static final GenFeature ROOTS = register(new RootsGenFeature(regName("roots")));

    public static final GenFeature CLEAR_VOLUME = register(new ClearVolumeGenFeature(regName("clear_volume")));
    public static final GenFeature BOTTOM_FLARE = register(new BottomFlareGenFeature(regName("bottom_flare")));
    public static final GenFeature UNDERGROWTH = register(new UndergrowthGenFeature(regName("undergrowth")));

    public static final GenFeature PODZOL = register(new PodzolGenFeature(regName("podzol")));

    public static final GenFeature BUSH = register(new BushGenFeature(regName("bush")));

    public static final GenFeature HUGE_MUSHROOM = register(new HugeMushroomGenFeature(regName("huge_mushroom")));
    public static final GenFeature HUGE_MUSHROOMS = register(new HugeMushroomsGenFeature(regName("huge_mushrooms")));

    public static final GenFeature BIOME_PREDICATE = register(new BiomePredicateGenFeature(regName("biome_predicate")));

    private static ResourceLocation regName (String name) {
        return new ResourceLocation(DynamicTrees.MOD_ID, name);
    }

    private static GenFeature register (GenFeature genFeature) {
        GenFeature.REGISTRY.register(genFeature);
        return genFeature;
    }

}
