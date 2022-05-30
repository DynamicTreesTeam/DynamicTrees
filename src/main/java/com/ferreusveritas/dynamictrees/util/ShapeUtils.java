package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Max Hyper
 */
public final class ShapeUtils {

    /**
     * Method to easily create bounding boxes for fruit blocks centered in the block
     *
     * @param radius     the radius of the fruit bounding box in relation to the fraction
     * @param height     the height of the fruit bounding box in relation to the fraction
     * @param stemLength the offset of the bounding box down from the top of the block
     * @param fraction   the fraction that the full block is divided by which determines the size of the other
     *                   parameters
     * @return an Axis Aligned Bounding Box of the fruit
     */
    public static AxisAlignedBB createFruitShape(float radius, float height, float stemLength, float fraction) {
        float topHeight = fraction - stemLength;
        float bottomHeight = topHeight - height;
        return new AxisAlignedBB(
                ((fraction / 2) - radius) / fraction, topHeight / fraction, ((fraction / 2) - radius) / fraction,
                ((fraction / 2) + radius) / fraction, bottomHeight / fraction, ((fraction / 2) + radius) / fraction);
    }

    public static AxisAlignedBB createFruitShape(float radius, float height, float stemLength) {
        return createFruitShape(radius, height, stemLength, 20);
    }
}
