package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeatures.HugeMushroomGenFeature;
import net.minecraft.world.level.block.Blocks;

public class Mushroom extends Species {

    protected final boolean redcap;

    /**
     * @param redcap True to select redcap mushroom.  Otherwise brown cap is selected
     */
    public Mushroom(boolean redcap) {
        this.redcap = redcap;

        this.setRegistryName(DynamicTrees.resLoc((redcap ? "red" : "brown") + "_mushroom"));
        this.setUnlocalizedName(this.getRegistryName().toString());
        this.setStandardSoils();

        this.addGenFeature(GenFeatures.HUGE_MUSHROOM.with(HugeMushroomGenFeature.MUSHROOM_BLOCK,
                redcap ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK));
    }

    @Override
    public boolean isTransformable() {
        return false;
    }

}
