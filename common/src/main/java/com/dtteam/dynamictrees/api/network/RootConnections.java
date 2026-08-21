package com.dtteam.dynamictrees.api.network;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Extension of regular {@link Connections} data, modified for surface roots. This is needed because roots only have
 * horizontal connections and a connection level.
 *
 * @author Harley O'Connor
 */
public class RootConnections extends Connections {

    public static final ConnectionLevel[] PLACEHOLDER_CONNECTION_LEVELS = new ConnectionLevel[]{ConnectionLevel.MID, ConnectionLevel.MID, ConnectionLevel.MID, ConnectionLevel.MID};

    /**
     * An array of connection levels, with the index being equivalent to their horizontal index of the connection
     * level's {@link Direction}. For example, if the connection level to <code>EAST</code> of the root is <code>HIGH</code>,
     * <code>connectionsLevels[3]</code> will equal <code>ConnectionLevel.HIGH</code>.
     */
    protected ConnectionLevel[] connectionLevels;

    public RootConnections() {
        // Surface roots only need horizontal connections, so the radii has 4 items with equivalent index to the horizontal index of the respective Direction.
        this.radii = new int[]{0, 0, 0, 0};
        this.connectionLevels = PLACEHOLDER_CONNECTION_LEVELS.clone();
    }

    public RootConnections(RootConnections connections) {
        this.setAllRadii(connections.getAllRadii());
        this.setConnectionLevels(connections.getConnectionLevels());
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
     *     <li>A <code>MID</code> connection level is a normal connection with another root at the same y-level in the given {@link Direction}.</li>
     *     <li>A <code>LOW</code> connection level describes one where there is a surface root down one block in the y-direction and offset by one block in the given {@link Direction}.</li>
     *     <li>A <code>HIGH</code> connection describes one where there is a surface root up one block in the y-direction and offset by one block in the given {@link Direction}.</li>
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

        public String getSerializedName() {
            return toString().toLowerCase(Locale.ENGLISH);
        }

        public int getYOffset() {
            return yOffset;
        }
    }

}