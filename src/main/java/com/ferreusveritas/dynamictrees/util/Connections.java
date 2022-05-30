package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BasicBranchBlockBakedModel;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import net.minecraft.util.Direction;

/**
 * This holds connection data for branches.
 * <p>
 * Mainly used for model data in the form of the sub-class {@link ModelConnections}. The data is obtained and written in
 * {@link BranchBlock} and read by the {@link BasicBranchBlockBakedModel} and sub-classes to construct the appropriate
 * baked model for each branch.
 */
public class Connections {

    /**
     * An array of connection radii. These radii use the equivalent index of their {@link Direction}, and their value
     * depends on the adjacent branch's radius in that direction - for example, if a branch in <tt>Direction.UP</tt> has
     * radius <tt>5</tt> then <tt>radii[1]</tt> will equal <tt>5</tt>.
     */
    protected int[] radii;

    public Connections() {
        radii = new int[]{0, 0, 0, 0, 0, 0};
    }

    public Connections(int[] radii) {
        this.radii = radii;
    }

    /**
     * Sets the radius in a given {@link Direction}.
     *
     * @param dir    The direction.
     * @param radius The connection radius for that direction.
     */
    public void setRadius(Direction dir, int radius) {
        radii[dir.get3DDataValue()] = radius;
    }

    public int[] getAllRadii() {
        return radii;
    }

    public Connections setAllRadii(int[] radii) {
        this.radii = radii;
        return this;
    }

}
