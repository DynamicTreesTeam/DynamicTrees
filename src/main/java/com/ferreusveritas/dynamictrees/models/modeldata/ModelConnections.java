package com.ferreusveritas.dynamictrees.models.modeldata;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.Connections;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

/**
 * Extension of {@link Connections} for storing and transferring model data to baked models.
 */
public class ModelConnections extends Connections {
    public static final ModelProperty<ModelConnections> CONNECTIONS_PROPERTY = new ModelProperty<>();

    private Direction ringOnly = null;
    private Family family = Family.NULL_FAMILY;

    public ModelConnections() {}

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

    public Direction getRingOnly() {
        return ringOnly;
    }

    public void setForceRing(Direction ringSide) {
        ringOnly = ringSide;
    }

    public ModelData toModelData() {
        return ModelData.builder().with(CONNECTIONS_PROPERTY, this).build();
    }

    public ModelData toModelData(ModelData baseData) {
        return baseData.derive().with(CONNECTIONS_PROPERTY, this).build();
    }
}
