package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import net.minecraft.world.level.block.Block;

/**
 * @author Harley O'Connor
 */
public class LeavesStateGenerator implements Generator<DTBlockStateProvider, LeavesProperties> {

    public static final DependencyKey<DynamicLeavesBlock> LEAVES = new DependencyKey<>("leaves");
    public static final DependencyKey<Block> PRIMITIVE_LEAVES = new DependencyKey<>("primitive_leaves");

    @Override
    public void generate(DTBlockStateProvider provider, LeavesProperties input, Dependencies dependencies) {
        provider.simpleBlock(dependencies.get(LEAVES), provider.models().getExistingFile(
                provider.block(dependencies.get(PRIMITIVE_LEAVES).getRegistryName())
        ));
    }

    @Override
    public Dependencies gatherDependencies(LeavesProperties input) {
        return new Dependencies()
                .append(LEAVES, input.getDynamicLeavesBlock())
                .append(PRIMITIVE_LEAVES, input.getPrimitiveLeavesBlock());
    }

}
