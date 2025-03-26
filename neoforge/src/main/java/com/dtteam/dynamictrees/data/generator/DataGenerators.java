package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;

public class DataGenerators {

    public static void register(){
        Family.blockStateGenerators.put(DynamicTrees.location("branch"), BranchStateGenerator::new);
        Family.blockStateGenerators.put(DynamicTrees.location("stripped_branch"), StrippedBranchStateGenerator::new);
        Family.blockStateGenerators.put(DynamicTrees.location("surface_root"), SurfaceRootStateGenerator::new);
        Family.itemModelGenerators.put(DynamicTrees.location("branch_item"), BranchItemModelGenerator::new);
        Family.languageGenerators.put(DynamicTrees.location("family_lang"), FamilyLangGenerator::new);
        Family.blockStateGenerators.put(DynamicTrees.location("roots"), RootsStateGenerator::new);
        Family.itemModelGenerators.put(DynamicTrees.location("roots_item"), RootsItemModelGenerator::new);

        Species.blockStateGenerators.put(DynamicTrees.location("sapling"), SaplingStateGenerator::new);
        Species.itemModelGenerators.put(DynamicTrees.location("seed_item"), SeedItemModelGenerator::new);
        Species.languageGenerators.put(DynamicTrees.location("species_lang"), SpeciesLangGenerator::new);

        LeavesProperties.blockStateGenerators.put(DynamicTrees.location("leaves"), LeavesStateGenerator::new);
        LeavesProperties.languageGenerators.put(DynamicTrees.location("leaves_lang"), LeavesPropertiesLangGenerator::new);
        LeavesProperties.blockStateGenerators.put(DynamicTrees.location("palm_fronds"), PalmLeavesStateGenerator::new);

        SoilProperties.blockStateGenerators.put(DynamicTrees.location("soil"), SoilStateGenerator::new);
        SoilProperties.blockStateGenerators.put(DynamicTrees.location("water_root_soil"), WaterRootSoilGenerator::new);
        SoilProperties.blockStateGenerators.put(DynamicTrees.location("aerial_root_soil"), AerialRootSoilGenerator::new);

    }

}
