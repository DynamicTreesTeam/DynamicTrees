
package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.tree.family.Family;

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
