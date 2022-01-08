
package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.data.provider.BranchLoaderBuilder;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.trees.Family;
import net.minecraft.block.Block;

import java.util.Objects;

/**
 * @author Harley O'Connor
 */
public class StrippedBranchStateGenerator extends BranchStateGenerator {

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(BRANCH, input.getStrippedBranch())
                .append(PRIMITIVE_LOG, input.getPrimitiveStrippedLog());
    }

}
