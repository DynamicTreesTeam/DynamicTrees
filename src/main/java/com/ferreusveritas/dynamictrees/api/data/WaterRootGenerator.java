package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;

/**
 * @author Harley O'Connor
 */
public final class WaterRootGenerator extends SoilStateGenerator {

    @Override
    public void generate(DTBlockStateProvider provider, SoilProperties input, Dependencies dependencies) {
        // TODO: Smart model for water roots.
        provider.simpleBlock(
                dependencies.get(SOIL),
                provider.models().getExistingFile(DynamicTrees.resLoc("block/roots_water"))
        );
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        return new Dependencies()
                .append(SOIL, input.getBlock());
    }

}
