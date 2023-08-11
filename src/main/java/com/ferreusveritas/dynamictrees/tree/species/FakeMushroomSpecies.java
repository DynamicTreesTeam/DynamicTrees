package com.ferreusveritas.dynamictrees.tree.species;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatures;
import com.ferreusveritas.dynamictrees.systems.genfeature.HugeMushroomGenFeature;
import net.minecraft.world.level.block.Blocks;

public class FakeMushroomSpecies extends Species {

    protected final boolean redcap;

    /**
     * @param redcap True to select redcap mushroom.  Otherwise, brown cap is selected
     */
    public FakeMushroomSpecies(boolean redcap) {
        this.redcap = redcap;

        this.setRegistryName(DynamicTrees.location((redcap ? "red" : "brown") + "_mushroom"));
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
