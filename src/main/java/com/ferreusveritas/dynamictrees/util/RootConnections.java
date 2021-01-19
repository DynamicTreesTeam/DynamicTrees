package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

/**
 * @author Harley O'Connor
 */
public class RootConnections extends Connections {

    protected ConnectionLevel[] connectionLevels;

    public RootConnections () {
        this.radii = new int[] {0,0,0,0};
        this.connectionLevels = new ConnectionLevel[] {ConnectionLevel.MID,ConnectionLevel.MID,ConnectionLevel.MID,ConnectionLevel.MID};
    }

    public ConnectionLevel[] getConnectionLevels() {
        return connectionLevels;
    }

    @Override
    public void setRadius(Direction dir, int radius) {
        this.radii[dir.getHorizontalIndex()] = radius;
    }

    public void setConnectionLevel (Direction dir, ConnectionLevel connectionLevel) {
        this.connectionLevels[dir.getHorizontalIndex()] = connectionLevel;
    }

    public void setConnectionLevels(ConnectionLevel[] connectionLevels) {
        this.connectionLevels = connectionLevels;
    }

    public enum ConnectionLevel implements IStringSerializable {
        MID(0),
        LOW(-1),
        HIGH(1);

        private final int yOffset;

        ConnectionLevel(int y) {
            this.yOffset = y;
        }

        @Override
        public String getString() {
            return toString().toLowerCase();
        }

        public int getYOffset() {
            return yOffset;
        }
    }

}
