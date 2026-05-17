package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link Connections} for storing and transferring model data to baked models.
 */
public class ModelRadius extends Connections {

    private Direction ringOnly = null;
    private Family family = Family.NULL_FAMILY;

    public ModelRadius() {}

    public ModelRadius(Connections connections) {
        this.setAllRadii(connections.getAllRadii());
    }

    public ModelRadius(int[] radii) {
        super(radii);
    }

    public ModelRadius(Direction ringDir) {
        ringOnly = ringDir;
    }

    public ModelRadius setAllRadii(int[] radii) {
        return (ModelRadius) super.setAllRadii(radii);
    }

    public ModelRadius setFamily(Family family) {
        this.family = family;
        return this;
    }

    public ModelRadius setFamily(@Nullable BranchBlock branch) {
        if (branch != null) {
            this.family = branch.getFamily();
        }
        return this;
    }

    public Family getFamily() {
        return family;
    }

    public Direction getRingOnly() {
        return ringOnly;
    }

    public void setForceRing(Direction ringSide) {
        ringOnly = ringSide;
    }

//    public static final ModelProperty<ModelConnections> CONNECTIONS_PROPERTY = new ModelProperty<>();
//
//    public ModelData toModelData() {
//        return ModelData.builder().with(CONNECTIONS_PROPERTY, this).build();
//    }
//
//    public ModelData toModelData(ModelData baseData) {
//        return baseData.derive().with(CONNECTIONS_PROPERTY, this).build();
//    }

}