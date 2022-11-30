package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A simple implementation of a voxel map
 *
 * @author ferreusveritas
 */
public class SimpleVoxmap {

    private final byte[] data;
    private final boolean[] touched;

    private final int lenX;
    private final int lenY;
    private final int lenZ;
    private final int layerSize;

    BlockPos center = new BlockPos(0, 0, 0);

    public SimpleVoxmap(int lenX, int lenY, int lenZ) {
        data = new byte[lenX * lenY * lenZ];
        touched = new boolean[lenY];
        this.lenX = lenX;
        this.lenY = lenY;
        this.lenZ = lenZ;
        this.layerSize = lenX * lenZ;
    }

    public SimpleVoxmap(int lenX, int lenY, int lenZ, byte[] extData) {
        data = Arrays.copyOf(extData, lenX * lenY * lenZ);
        touched = new boolean[lenY];
        for (int y = 0; y < lenY; y++) {
            touched[y] = true;
        }
        this.lenX = lenX;
        this.lenY = lenY;
        this.lenZ = lenZ;
        this.layerSize = lenX * lenZ;
    }

    public SimpleVoxmap(SimpleVoxmap vmp) {
        this(vmp.getLenX(), vmp.getLenY(), vmp.getLenZ(), vmp.data);
        this.center = vmp.center;
    }

    public SimpleVoxmap(BlockBounds bounds) {
        this(bounds.getXSize(), bounds.getYSize(), bounds.getZSize());
        setMapAndCenter(bounds.getMin(), new BlockPos(0, 0, 0));
    }

    /**
     * Convenience function to take the guessing and remembering out of how to convert local to world coordinates.
     *
     * @param mapPos
     * @param centerPos
     * @return
     */
    public SimpleVoxmap setMapAndCenter(BlockPos mapPos, BlockPos centerPos) {
        setCenter(centerPos);
        center = center.subtract(mapPos);
        return this;
    }

    public SimpleVoxmap setMap(BlockPos mapPos) {
        center = center.subtract(mapPos);
        return this;
    }

    public SimpleVoxmap setCenter(BlockPos centerPos) {
        center = centerPos;
        return this;
    }

    public BlockPos getCenter() {
        return center;
    }

    public byte[] getData() {
        return data;
    }

    /**
     * @return Size along X-Axis
     */
    public int getLenX() {
        return lenX;
    }

    /**
     * @return Size along Y-Axis
     */
    public int getLenY() {
        return lenY;
    }

    /**
     * @return Size along Z-Axis
     */
    public int getLenZ() {
        return lenZ;
    }

    public BlockBounds getBounds() {
        int minX = center.getX() - lenX + 1;
        int minY = center.getY() - lenY + 1;
        int minZ = center.getZ() - lenZ + 1;
        int maxX = minX + lenX - 1;
        int maxY = minY + lenY - 1;
        int maxZ = minZ + lenZ - 1;
        return new BlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @FunctionalInterface
    public interface BlitOp {
        byte getOp(byte srcValue, byte dstValue);
    }

    public SimpleVoxmap blitOp(BlockPos pos, SimpleVoxmap src, BlitOp op) {
        for (int iy = 0; iy < src.getLenY(); iy++) {
            int srcY = iy - src.center.getY();
            int dstY = pos.getY() + srcY;
            setYTouched(dstY);
            for (int iz = 0; iz < src.getLenZ(); iz++) {
                int srcZ = iz - src.center.getZ();
                int dstZ = pos.getZ() + srcZ;
                for (int ix = 0; ix < src.getLenX(); ix++) {
                    int srcX = ix - src.center.getX();
                    int dstX = pos.getX() + srcX;
                    byte srcValue = src.getVoxel(srcX, srcY, srcZ);
                    byte dstValue = getVoxel(dstX, dstY, dstZ);
                    setVoxel(dstX, dstY, dstZ, op.getOp(srcValue, dstValue));
                }
            }
        }
        return this;
    }

    public SimpleVoxmap blitReplace(BlockPos pos, SimpleVoxmap src) {
        return blitOp(pos, src, (s, d) -> {
            return s;
        });
    }

    public SimpleVoxmap blitMax(BlockPos pos, SimpleVoxmap src) {
        return blitOp(pos, src, (s, d) -> {
            return (s >= d) ? s : d;
        });
    }

    public SimpleVoxmap blitClear(BlockPos pos, SimpleVoxmap src) {
        return blitOp(pos, src, (s, d) -> {
            return (s >= 0) ? 0 : d;
        });
    }

    @FunctionalInterface
    public interface FilterOp {
        byte getOp(byte data);
    }

    public SimpleVoxmap filter(FilterOp op) {
        Arrays.fill(touched, true);
        for (int i = 0; i < data.length; i++) {
            data[i] = op.getOp(data[i]);
        }
        return this;
    }

    public SimpleVoxmap crop(BlockPos from, BlockPos to) {
        for (BlockPos.MutableBlockPos pos : getAllNonZero()) {
            if (pos.getX() < from.getX() ||
                    pos.getY() < from.getY() ||
                    pos.getZ() < from.getZ() ||
                    pos.getX() > to.getX() ||
                    pos.getY() > to.getY() ||
                    pos.getZ() > to.getZ()) {
                setVoxel(pos, (byte) 0);
            }
        }
        return this;
    }

    public SimpleVoxmap filter(BlockPos from, BlockPos to, FilterOp op) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            setVoxel(pos, op.getOp(getVoxel(pos)));
        }
        return this;
    }

    public SimpleVoxmap fill(byte value) {
        return filter((v) -> {
            return value;
        });
    }

    public SimpleVoxmap fill(BlockPos from, BlockPos to, byte value) {
        return filter(from, to, (v) -> {
            return value;
        });
    }

    private int calcPos(int x, int y, int z) {
        return y * lenX * lenZ + z * lenX + x;
    }

    public void setVoxel(BlockPos pos, byte value) {
        setVoxel(pos.getX(), pos.getY(), pos.getZ(), value);
    }

    public void setVoxelOr(BlockPos pos, byte value) {
        setVoxelOr(pos.getX(), pos.getY(), pos.getZ(), value);
    }

    public void setVoxel(int x, int y, int z, byte value) {
        x += center.getX();
        y += center.getY();
        z += center.getZ();
        if (testBounds(x, y, z)) {
            if (value != 0) {
                setYTouched(y - center.getY());
            }
            data[calcPos(x, y, z)] = value;
        }
    }

    public void setVoxelOr(int x, int y, int z, byte value) {
        x += center.getX();
        y += center.getY();
        z += center.getZ();
        if (testBounds(x, y, z)) {
            if (value != 0) {
                setYTouched(y - center.getY());
            }
            data[calcPos(x, y, z)] |= value;
        }
    }

    /**
     * Get voxel data relative to world coords
     *
     * @param relPos The position of the center in the world
     * @param pos    The world position of the data request
     * @return voxel data at coordinates
     */
    public byte getVoxel(BlockPos relPos, BlockPos pos) {
        return getVoxel(
                pos.getX() - relPos.getX(),
                pos.getY() - relPos.getY(),
                pos.getZ() - relPos.getZ());
    }

    public byte getVoxel(BlockPos pos) {
        return getVoxel(pos.getX(), pos.getY(), pos.getZ());
    }

    public byte getVoxel(int x, int y, int z) {
        if (isYTouched(y)) {
            x += center.getX();
            y += center.getY();
            z += center.getZ();
            return testBounds(x, y, z) ? data[calcPos(x, y, z)] : 0;
        }

        return 0;
    }

    private boolean testBounds(int x, int y, int z) {
        return x >= 0 && x < lenX && y >= 0 && y < lenY && z >= 0 && z < lenZ;
    }

    public boolean isYTouched(int y) {
        y += center.getY();
        return y >= 0 && y < lenY && touched[y];
    }

    public void setYTouched(int y) {
        y += center.getY();
        if (y >= 0 && y < lenY) {
            touched[y] = true;
        }
    }


    public static class Cell {
        private byte value;
        private final BlockPos.MutableBlockPos pos;

        public Cell() {
            pos = new BlockPos.MutableBlockPos();
        }

        public Cell setValue(byte value) {
            this.value = value;
            return this;
        }

        public byte getValue() {
            return value;
        }

        public BlockPos.MutableBlockPos getPos() {
            return pos;
        }

    }


    public Iterable<Cell> getAllNonZeroCells() {
        return getAllNonZeroCells((byte) 0xFF);
    }

    /**
     * Create an Iterable that returns all cells(value and position) in the map whose value is non-zero
     */
    public Iterable<Cell> getAllNonZeroCells(final byte mask) {

        return new Iterable<Cell>() {
            @Override
            public Iterator<Cell> iterator() {
                return new AbstractIterator<Cell>() {
                    private int x = -1;
                    private int y = 0;
                    private int z = 0;
                    private int dataPos = -1;
                    private final Cell workingCell = new Cell();
                    private final BlockPos.MutableBlockPos dPos = workingCell.getPos();

                    @Override
                    protected Cell computeNext() {

                        main:
                        while (true) {

                            if (x < lenX - 1) {
                                x++;
                            } else if (z < lenZ - 1) {
                                x = 0;
                                z++;
                            } else {
                                x = -1;
                                z = 0;
                                y++;

                                while (y < lenY) {
                                    if (touched[y]) {
                                        continue main;
                                    }
                                    dataPos += layerSize;
                                    y++;
                                }

                                return this.endOfData();
                            }

                            byte value = (byte) (data[++dataPos] & mask);
                            if (value > 0) {
                                dPos.set(x - center.getX(), y - center.getY(), z - center.getZ());
                                return workingCell.setValue(value);
                            }
                        }
                    }
                };
            }
        };
    }


    /**
     * Create an Iterable that returns all positions in the map whose value is non-zero
     */
    public Iterable<BlockPos.MutableBlockPos> getAllNonZero() {
        return getAllNonZero((byte) 0xFF);
    }

    /**
     * Create an Iterable that returns all positions in the map whose value is non-zero
     */
    public Iterable<BlockPos.MutableBlockPos> getAllNonZero(final byte mask) {

        return new Iterable<BlockPos.MutableBlockPos>() {
            @Override
            public Iterator<BlockPos.MutableBlockPos> iterator() {
                return new AbstractIterator<BlockPos.MutableBlockPos>() {
                    private int x = -1;
                    private int y = 0;
                    private int z = 0;
                    private int dataPos = -1;
                    private boolean yclean;
                    private final BlockPos.MutableBlockPos dPos = new BlockPos.MutableBlockPos();

                    @Override
                    protected BlockPos.MutableBlockPos computeNext() {

                        main:
                        while (true) {

                            if (x < lenX - 1) {
                                x++;//Move to read next cell on x axis
                            } else if (z < lenZ - 1) {
                                //We have completed an x scan of a y layer but we still have some more z
                                x = 0;//Reset x for another z scan
                                z++;//Jump to the next z bar
                            } else {
                                //We have completed an x and z scan of a y layer.
                                x = -1;//Reset the x to just before the first cell for another y layer
                                z = 0;//Reset z for another y layer

                                //Once we get here we have completed an entire y layer scan
                                //if the layer is clean then we mark it as such to self optimize
                                touched[y] = !yclean;

                                y++;//Bump up a layer
                                yclean = true; //Let's pretend this new layer is clean

                                while (y < lenY) {
                                    if (touched[y]) {
                                        continue main;//We suspect there's data on this layer so let's hit it
                                    }
                                    dataPos += layerSize;//Jump the indexer to the next layer data
                                    y++;//Bump up a layer
                                    yclean = true; //Let's pretend this new layer is clean
                                }

                                return this.endOfData();//There's no more data
                            }

                            if ((data[++dataPos] & mask) > 0) {
                                yclean = false; //We found non-zero data.  Therefore this y layer is dirty
                                return dPos.set(x - center.getX(), y - center.getY(), z - center.getZ());
                            }
                        }

                    }

                };
            }
        };
    }

    /**
     * Create an Iterable that returns all top(Y-axis) positions in the map whose value is non-zero
     */
    public Iterable<BlockPos.MutableBlockPos> getTops() {

        return new Iterable<BlockPos.MutableBlockPos>() {
            @Override
            public Iterator<BlockPos.MutableBlockPos> iterator() {
                return new AbstractIterator<BlockPos.MutableBlockPos>() {
                    private int x = -1;
                    private int y = 0;
                    private int z = 0;
                    private final int yStart = getStartY();
                    private final BlockPos.MutableBlockPos dPos = new BlockPos.MutableBlockPos();

                    protected int getStartY() {
                        int yi;
                        for (yi = lenY - 1; yi >= 0 && !touched[yi]; yi--) {
                        }
                        return yi;
                    }

                    @Override
                    protected BlockPos.MutableBlockPos computeNext() {

                        while (true) {
                            if (x < lenX - 1) {
                                x++;
                            } else if (z < lenZ - 1) {
                                x = 0;
                                z++;
                            } else {
                                return this.endOfData();
                            }

                            y = yStart;
                            int dataPos = calcPos(x, y, z);

                            while (y >= 0) {
                                if (data[dataPos] != 0) {
                                    return dPos.set(x - center.getX(), y - center.getY(), z - center.getZ());
                                }
                                dataPos -= layerSize;
                                y--;
                            }
                        }

                    }

                };
            }
        };
    }

    public void print() {

        StringBuilder buffer;
        for (int y = 0; y < lenY; y++) {
            System.out.println("Touched: " + touched[y]);
            for (int z = 0; z < lenZ; z++) {
                buffer = new StringBuilder();
                for (int x = 0; x < lenX; x++) {
                    byte b = getVoxel(x - center.getX(), y - center.getY(), z - center.getZ());
                    if ((b & 32) != 0) {
                        buffer.append("B");
                    } else if ((b & 16) != 0) {
                        buffer.append("T");
                    } else {
                        buffer.append(Integer.toHexString(b & 0xF));
                    }
                }
                System.out.println(buffer);
            }
            buffer = new StringBuilder();
            for (int k = 0; k < lenX; k++) {
                buffer.append("-");
            }
            System.out.println(buffer);
        }
    }

}
