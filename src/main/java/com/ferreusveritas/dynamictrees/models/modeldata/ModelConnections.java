package com.ferreusveritas.dynamictrees.models.modeldata;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.Connections;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nullable;

/**
 * Extension of {@link Connections} to implement { IModelData}, so connections can be transferred to the baked
 * model.
 */
public class ModelConnections extends Connections /*implements ModelData */{

    private Direction ringOnly = null;
    private Family family = Family.NULL_FAMILY;

    public ModelConnections() {
    }

    public ModelConnections(Connections connections) {
        this.setAllRadii(connections.getAllRadii());
    }

    public ModelConnections(int[] radii) {
        super(radii);
    }

    public ModelConnections(Direction ringDir) {
        ringOnly = ringDir;
    }

    public ModelConnections setAllRadii(int[] radii) {
        return (ModelConnections) super.setAllRadii(radii);
    }

    public ModelConnections setFamily(Family family) {
        this.family = family;
        return this;
    }

    public ModelConnections setFamily(@Nullable BranchBlock branch) {
        if (branch != null) {
            this.family = branch.getFamily();
        }
        return this;
    }

    public Family getFamily() {
        return family;
    }

//    @Override
//    public boolean hasProperty(ModelProperty<?> prop) {
//        return false;
//    }
//
//    @Nullable
//    @Override
//    public <T> T getData(ModelProperty<T> prop) {
//        return null;
//    }
//
//    @Nullable
//    @Override
//    public <T> T setData(ModelProperty<T> prop, T data) {
//        return null;
//    }


    public Direction getRingOnly() {
        return ringOnly;
    }

    public void setForceRing(Direction ringSide) {
        ringOnly = ringSide;
    }

}
