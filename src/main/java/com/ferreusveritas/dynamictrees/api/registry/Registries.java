package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.api.cells.CellKit;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.systems.dropcreators.DropCreator;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.Species;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds all registries in an ordered list.
 *
 * @author Harley O'Connor
 */
public final class Registries {

    public static final List<Registry<?>> REGISTRIES = new ArrayList<>(
            Arrays.asList(
                    RegistryHandler.REGISTRY,
                    CellKit.REGISTRY,
                    LeavesProperties.REGISTRY,
                    GrowthLogicKit.REGISTRY,
                    Family.REGISTRY,
                    GenFeature.REGISTRY,
                    DropCreator.REGISTRY,
                    Fruit.REGISTRY,
                    Pod.REGISTRY,
                    Species.REGISTRY,
                    SoilProperties.REGISTRY
            )
    );

}
