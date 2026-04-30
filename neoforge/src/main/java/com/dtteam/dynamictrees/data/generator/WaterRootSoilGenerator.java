package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.block.soil.SoilProperties;
import net.minecraft.client.data.models.BlockModelGenerators;

/**
 * @author Harley O'Connor
 */
public final class WaterRootSoilGenerator extends SoilStateGenerator {

    @Override
    public void generate(BlockModelGenerators generators, SoilProperties input, Dependencies dependencies) {
        generators.createTrivialCube(dependencies.get(SOIL));
//        if (prov instanceof DTBlockStateProvider provider){
//            // TODO: Smart model for water roots.
//            provider.simpleBlock(
//                    dependencies.get(SOIL),
//                    provider.models().getExistingFile(DynamicTrees.location("block/roots_water"))
//            );
//        }
    }

    @Override
    public Dependencies gatherDependencies(SoilProperties input) {
        return new Dependencies()
                .append(SOIL, input.getBlock());
    }

}
