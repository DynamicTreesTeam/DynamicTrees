package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * @author Max Hyper
 */
public class RootsItemModelGenerator extends BranchItemModelGenerator {

    @Override
    protected void addTextures(Family input, Map<String, Identifier> textures, Identifier primitiveLogPath, Block primitiveLog) {
        if (input instanceof AerialRootsFamily rootsFamily){
            rootsFamily.addRootTextures(textures::put, primitiveLogPath);
        }
    }

    @Override
    protected String itemParentLocation(Family family){
        return family.getRootItemParentLocation().toString();
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        AerialRootsFamily mangroveInput = (AerialRootsFamily) input;
        return new Dependencies()
                .append(BRANCH_ITEM, mangroveInput.getRootsItem())
                .append(PRIMITIVE_BLOCK, mangroveInput.getPrimitiveRoots());
    }

}