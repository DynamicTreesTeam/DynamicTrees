package com.dtteam.dynamictrees.api.registry;

import com.dtteam.dynamictrees.api.cell.CellKit;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.growthlogic.GrowthLogicKit;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;

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
                    Fruit.REGISTRY,
                    Pod.REGISTRY,
                    Species.REGISTRY,
                    SoilProperties.REGISTRY
            )
    );

}
