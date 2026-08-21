package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;

public class MossyRootsItemModelGenerator extends RootsItemModelGenerator {

    @Override
    public Dependencies gatherDependencies(Family input) {
        MossyAerialRootsFamily mossyInput = (MossyAerialRootsFamily) input;
        return new Dependencies()
                .append(ROOT_ITEM, mossyInput.getMossyRootsItem())
                .append(PRIMITIVE_ROOT, mossyInput.getPrimitiveRoots());
    }

}
