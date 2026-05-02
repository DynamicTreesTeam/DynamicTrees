package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * @author Max Hyper
 */
public class RootsItemModelGenerator extends BranchItemModelGenerator {

    @Override
    protected void addTextures(Family input, Map<String, Identifier> textures, Identifier primitiveLogPath, Block primitiveLog) {
        input.addRootTextures(textures::put, primitiveLogPath);
    }

    @Override
    protected String itemParentLocation(Family family){
        return family.getRootItemParentLocation().toString();
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        UndergroundRootsFamily mangroveInput = (UndergroundRootsFamily) input;
        return new Dependencies()
                .append(BRANCH_ITEM, mangroveInput.getRootsItem())
                .append(PRIMITIVE_BLOCK, mangroveInput.getPrimitiveRoots());
    }

}