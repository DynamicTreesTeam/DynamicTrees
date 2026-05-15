
package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.builder.BasicLoaderBuilder;
import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Harley O'Connor
 */
public class AltBranchStateGenerator extends BranchStateGenerator {

    @Override
    protected BiFunction<Map<String, Identifier>, Family, BasicLoaderBuilder> getBranchLoader(Family input) {
        if (input instanceof AltBranchFamily altFamily){
            return BasicLoaderBuilder.loaderBuilders.get(altFamily.getAltBranchLoader());
        }
        return super.getBranchLoader(input);
    }

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
