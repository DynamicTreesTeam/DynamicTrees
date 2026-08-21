package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;

public class MossyRootsStateGenerator extends RootsStateGenerator {

    @Override
    public Dependencies gatherDependencies(Family input) {
        MossyAerialRootsFamily mossyInput = (MossyAerialRootsFamily) input;
        return new Dependencies()
                .append(ROOT, mossyInput.getMossyRoots())
                .append(PRIMITIVE_ROOT, mossyInput.getPrimitiveRoots())
                .append(PRIMITIVE_FILLED_ROOT, mossyInput.getPrimitiveFilledRoots())
                .append(PRIMITIVE_COVERED_ROOT, mossyInput.getPrimitiveCoveredRoots());
    }

}
