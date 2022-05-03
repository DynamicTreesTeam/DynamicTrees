package com.ferreusveritas.dynamictrees.util;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

/**
 * Extension of regular {@link Connections} data, modified for surface roots. This is needed because roots only have
 * horizontal connections and a connection level.
 *
 * @author Harley O'Connor
 */
public class RootConnections extends Connections {

    public final static ConnectionLevel[] PLACEHOLDER_CONNECTION_LEVELS = new ConnectionLevel[]{ConnectionLevel.MID, ConnectionLevel.MID, ConnectionLevel.MID, ConnectionLevel.MID};

    /**
     * An array of connection levels, with the index being equivalent to their horizontal index of the connection
     * level's {@link Direction}. For example, if the connection level to <tt>EAST</tt> of the root is <tt>HIGH</tt>,
     * <tt>connectionsLevels[3]</tt> will equal <tt>ConnectionLevel.HIGH</tt>.
     */
    protected ConnectionLevel[] connectionLevels;

    public RootConnections() {
        // Surface roots only need horizontal connections, so the radii has 4 items with equivalent index to the horizontal index of the respective Direction.
        this.radii = new int[]{0, 0, 0, 0};
        this.connectionLevels = PLACEHOLDER_CONNECTION_LEVELS.clone();
    }

    public ConnectionLevel[] getConnectionLevels() {
        return connectionLevels;
    }

    /**
     * Sets the radius of the connection in a horizontal direction.
     *
     * @param dir    The horizontal direction.
     * @param radius The connection radius for that direction.
     */
    @Override
    public void setRadius(Direction dir, int radius) {
        // Surface radii uses horizontal index, so use that instead.
        this.radii[dir.get2DDataValue()] = radius;
    }

    public void setConnectionLevel(Direction dir, ConnectionLevel connectionLevel) {
        this.connectionLevels[dir.get2DDataValue()] = connectionLevel;
    }

    public void setConnectionLevels(ConnectionLevel[] connectionLevels) {
        this.connectionLevels = connectionLevels;
    }

    /**
     * This holds the type of connection a surface root has with the block in the given {@link Direction} as described
     * by its index (see {@link RootConnections#connectionLevels}).
     * <ul>
     *     <li>A <tt>MID</tt> connection level is a normal connection with another root at the same y-level in the given {@link Direction}.</li>
     *     <li>A <tt>LOW</tt> connection level describes one where there is a surface root down one block in the y-direction and offset by one block in the given {@link Direction}.</li>
     *     <li>A <tt>HIGH</tt> connection describes one where there is a surface root up one block in the y-direction and offset by one block in the given {@link Direction}.</li>
     * </ul>
     */
    public enum ConnectionLevel implements StringRepresentable {
        MID(0),
        LOW(-1),
        HIGH(1);

        /**
         * This holds the offset in the y-level of the connecting surface root.
         */
        private final int yOffset;

        ConnectionLevel(int y) {
            this.yOffset = y;
        }

        @Override
        public String getSerializedName() {
            return toString().toLowerCase();
        }

        public int getYOffset() {
            return yOffset;
        }
    }

}
