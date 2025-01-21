package com.dtteam.dynamictrees.api.network;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import net.minecraft.core.Direction;

/**
 * This holds connection data for branches.
 * <p>
 * Mainly used for model data in the form of the sub-class ModelConnections. The data is obtained and written in
 * {@link BranchBlock} and read by the BasicBranchBlockBakedModel and sub-classes to construct the appropriate
 * baked model for each branch.
 */
public class Connections {

    /**
     * An array of connection radii. These radii use the equivalent index of their {@link Direction}, and their value
     * depends on the adjacent branch's radius in that direction - for example, if a branch in <code>Direction.UP</code> has
     * radius <code>5</code> then <code>radii[1]</code> will equal <code>5</code>.
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
