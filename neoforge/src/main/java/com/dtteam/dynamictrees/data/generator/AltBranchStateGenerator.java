
package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import com.dtteam.dynamictrees.tree.family.Family;

/**
 * @author Harley O'Connor
 */
public class AltBranchStateGenerator extends BranchStateGenerator {

    @Override
    public Dependencies gatherDependencies(Family input) {
        if (input instanceof AltBranchFamily altFamily){
            return new Dependencies()
                    .append(BRANCH, altFamily.getAltBranch())
                    .append(PRIMITIVE_LOG, altFamily.getPrimitiveAltLog());
        }
        return super.gatherDependencies(input);
    }

}
