package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.tree.family.Family;

public class DataGenerators {

    public static void register(){
        Family.blockStateGenerators.put(DynamicTrees.location("branch"), BranchStateGenerator::new);
        Family.blockStateGenerators.put(DynamicTrees.location("stripped_branch"), StrippedBranchStateGenerator::new);
        Family.blockStateGenerators.put(DynamicTrees.location("surface_root"), SurfaceRootStateGenerator::new);

        Family.itemModelGenerators.put(DynamicTrees.location("branch_item"), BranchItemModelGenerator::new);

        Family.languageGenerators.put(DynamicTrees.location("family_lang"), FamilyLangGenerator::new);
    }

}
