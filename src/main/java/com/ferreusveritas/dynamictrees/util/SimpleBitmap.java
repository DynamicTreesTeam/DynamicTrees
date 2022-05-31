package com.ferreusveritas.dynamictrees.util;

import javax.annotation.Nullable;

/**
 * A simple bitmap that favors speed over safety.  Consider yourself disclaimed.
 *
 * @author ferreusveritas
 */
public class SimpleBitmap {

    private final int height;
    private final int width;
    private int[] bits;

    public boolean touched; // Useful for ruling out entire layers for the VoxelMap.

    /**
     * @param width  Width not to exceed 32
     * @param height Height
     */
    public SimpleBitmap(int width, int height) {
        this.width = net.minecraft.util.Mth.clamp(width, 1, 32);
        this.height = Math.max(1, height);
        this.bits = new int[this.height];
        this.touched = false;
    }

    /**
     * @param width  Width not to exceed 32
     * @param height Height
     * @param bits   The pixels
     */
    public SimpleBitmap(final int width, final int height, @Nullable final int[] bits) {
        this(width, height);
        if (bits != null) {
            int size = Math.min(bits.length, this.height);
            if (size >= 0) {
                System.arraycopy(bits, 0, this.bits, 0, size);
            }
        }
        this.touched = true;
    }

    public SimpleBitmap(SimpleBitmap bmp) {
        this(bmp.width, bmp.height);
        System.arraycopy(bmp.bits, 0, this.bits, 0, this.bits.length);
        this.touched = bmp.touched;
    }

    public int[] getBits() {
        return bits;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public SimpleBitmap clear() {
        this.bits = new int[height];
        this.touched = false;
        return this;
    }

    public boolean isColliding(int relX, int relY, SimpleBitmap src) {
        final BlitPreparationResult result = this.prepBlit(relX, relY, src);
        if (!result.success) {
            return false;
        }

        if (relX < 0) {
            relX = -relX;
            while (result.runH-- > 0) {
                if (((this.bits[result.dstOffsetY++] << relX) & src.getBits()[result.srcOffsetY++]) != 0) {
                    return true;
                }
            }
        } else {
            while (result.runH-- > 0) {
                if ((this.bits[result.dstOffsetY++] & (src.getBits()[result.srcOffsetY++] << relX)) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void blitOr(int relX, int relY, SimpleBitmap src) {
        final BlitPreparationResult result = this.prepBlit(relX, relY, src);
        if (!result.success) {
            return;
        }

        if (relX < 0) {
            relX = -relX;
            while (result.runH-- > 0) {
                this.bits[result.dstOffsetY++] |= (src.getBits()[result.srcOffsetY++] >>> relX);
            }
        } else {
            while (result.runH-- > 0) {
                this.bits[result.dstOffsetY++] |= (src.getBits()[result.srcOffsetY++] << relX);
            }
        }
        touched = true;
    }

    public void blitSub(int relX, int relY, SimpleBitmap src) {
        final BlitPreparationResult result = this.prepBlit(relX, relY, src);
        if (!result.success) {
            return;
        }

        if (relX < 0) {
            relX = -relX;
            while (result.runH-- > 0) {
                this.bits[result.dstOffsetY++] &= ~(src.getBits()[result.srcOffsetY++] >>> relX);
            }
        } else {
            while (result.runH-- > 0) {
                this.bits[result.dstOffsetY++] &= ~(src.getBits()[result.srcOffsetY++] << relX);
            }
        }
        touched = true;
    }

    public void blitAnd(int relX, int relY, SimpleBitmap src) {
        final BlitPreparationResult result = this.prepBlit(relX, relY, src);
        if (!result.success) {
            return;
        }

        if (relX < 0) {
            relX = -relX;
            while (result.runH-- > 0) {
                this.bits[result.dstOffsetY++] &= (src.getBits()[result.srcOffsetY++] >>> relX);
            }
        } else {
            while (result.runH-- > 0) {
                this.bits[result.dstOffsetY++] &= ~(src.getBits()[result.srcOffsetY++] << relX);
            }
        }
        touched = true;
    }

    private BlitPreparationResult prepBlit(int relX, int relY, SimpleBitmap src) {

        if (relX <= -src.width || relX >= this.width || relY <= -src.height || relY >= this.height ||
                (!touched && !src.touched)) {
            return BlitPreparationResult.failure();
        }

        final int dstOffsetY;
        final int srcOffsetY;

        if (relY >= 0) {
            dstOffsetY = relY;
            srcOffsetY = 0;
        } else {
            dstOffsetY = 0;
            srcOffsetY = -relY;
        }

        final int runH = Math.min(this.height - dstOffsetY, Math.min(src.height, this.height - relY) - srcOffsetY);

        return BlitPreparationResult.success(dstOffsetY, srcOffsetY, runH);
    }

    private static final class BlitPreparationResult {
        private static final BlitPreparationResult FAILURE = new BlitPreparationResult(false, 0, 0, 0);

        private final boolean success;
        private int dstOffsetY;
        private int srcOffsetY;
        private int runH;

        public BlitPreparationResult(boolean success, int dstOffsetY, int srcOffsetY, int runH) {
            this.success = success;
            this.dstOffsetY = dstOffsetY;
            this.srcOffsetY = srcOffsetY;
            this.runH = runH;
        }

        private static BlitPreparationResult failure() {
            return FAILURE;
        }

        private static BlitPreparationResult success(int dstOffsetY, int srcOffsetY, int runH) {
            return new BlitPreparationResult(true, dstOffsetY, srcOffsetY, runH);
        }

    }

    /**
     * Set a pixel
     *
     * @param x
     * @param y
     * @param mode The value to set 0 or 1
     */
    public void setPixel(int x, int y, int mode) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            mode &= 1;
            bits[y] = (bits[y] & ~(mode << x)) | mode << x;
            touched = true;
        }
    }

    public boolean isPixelOn(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && ((bits[y] >> x) & 1) == 1;
    }

    public boolean isRowBlank(int y) {
        return y >= 0 && y < height && bits[y] == 0;
    }

    public void print() {
        StringBuilder buff;
        for (int y = 0; y < height; y++) {
            buff = new StringBuilder();
            for (int x = 0; x < width; x++) {
                buff.append(isPixelOn(x, y) ? "█" : "░");
            }
            System.out.println(buff);
        }
    }

}