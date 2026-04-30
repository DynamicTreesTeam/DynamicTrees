
package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.tree.family.Family;

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
