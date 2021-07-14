package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds all registries in an ordered list.
 *
 * @author Harley O'Connor
 */
public final class Registries {

    public static final List<IRegistry<?>> REGISTRIES = new ArrayList<>(Arrays.asList(RegistryHandler.REGISTRY, CellKit.REGISTRY, LeavesProperties.REGISTRY,
            GrowthLogicKit.REGISTRY, GenFeature.REGISTRY, Family.REGISTRY, Species.REGISTRY, SoilProperties.REGISTRY));

}
