package com.ferreusveritas.dynamictrees.cell;

public class MetadataCell {

    //Cell Kit Metadata values
    public static final int NONE = 0;
    public static final int TOP_BRANCH = 1;

    /**
     * @param radius Radius value to be encoded in the lower 8 bits
     * @param meta   Metadata to be encoded in the upper 24 bits
     * @return encoded value
     */
    public static int radiusAndMeta(int radius, int meta) {
        return radius & 0xff | meta << 8;
    }

    public static int getRadius(int radiusMeta) {
        return radiusMeta & 0xFF;
    }

    public static int getMeta(int radiusMeta) {
        return radiusMeta >> 8;
    }
}
