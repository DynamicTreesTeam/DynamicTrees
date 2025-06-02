package com.dtteam.dynamictrees.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThickBranchRingsSprite extends SpriteContents {
    private static final int RESOLUTION = 16;
    private static final int LAYERS = 3;

    public ThickBranchRingsSprite(ResourceLocation name, SpriteContents originalSprite){
        super(name, getFrameSize(originalSprite), processImage(originalSprite.originalImage), originalSprite.metadata);
    }

    private static FrameSize getFrameSize(SpriteContents sprite){
        return new FrameSize(sprite.width() * LAYERS, sprite.height() * LAYERS);
    }
    private static int centerCorner(){
        return (RESOLUTION/2) * (LAYERS-1);
    }

    private static NativeImage processImage(NativeImage image){

        TextureHelper.PixelBuffer baseBuffer = new TextureHelper.PixelBuffer(image);
        TextureHelper.PixelBuffer majorBuffer = createMajorTexture(baseBuffer);

        return majorBuffer.toNativeImage();
    }

    private static TextureHelper.PixelBuffer createMajorTexture(TextureHelper.PixelBuffer baseBuffer) {

        int w = baseBuffer.w * LAYERS;
        int h = baseBuffer.h * LAYERS;
        int scale = baseBuffer.w / RESOLUTION;

        TextureHelper.PixelBuffer antecedentBuffer = createBarklessAntecedent(baseBuffer);
        TextureHelper.PixelBuffer majorBuffer = new TextureHelper.PixelBuffer(w, h);

        TextureHelper.PixelBuffer[] corners = new TextureHelper.PixelBuffer[4];
        TextureHelper.PixelBuffer[] edges = new TextureHelper.PixelBuffer[4];

        compileRingCornersAndEdges(corners, edges, antecedentBuffer, scale);
        fillRings(majorBuffer, edges, scale);
        fillCorners(majorBuffer, corners, scale);
        fillCenter(majorBuffer, antecedentBuffer, scale);

        compileBarkCornersAndEdges(corners, edges, baseBuffer, scale);
        fillBarkBorder(majorBuffer, corners, edges, scale);

        return majorBuffer;
    }

    private static void compileRingCornersAndEdges(TextureHelper.PixelBuffer[] corners, TextureHelper.PixelBuffer[] edges, TextureHelper.PixelBuffer antecedentBuffer, int scale){
        for (int i = 0; i < 4; i++) {
            corners[i] = new TextureHelper.PixelBuffer(6 * scale, 6 * scale);
            edges[i] = new TextureHelper.PixelBuffer(4 * scale, 6 * scale);
            antecedentBuffer.blit(corners[i], 0, 0, i);
            antecedentBuffer.blit(edges[i], -6 * scale, 0, i);
        }
    }
    private static void fillRings (TextureHelper.PixelBuffer majorBuffer, TextureHelper.PixelBuffer[] edges, int scale){
        int centerX = RESOLUTION * LAYERS / 2;
        int centerY = RESOLUTION * LAYERS / 2;
        for (int nesting = 0; nesting < LAYERS; nesting++) {
            int edge = 2;
            int pixbufSel = 0;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                Direction ovr = dir.getClockWise();
                int offX = -dir.getStepX();
                int offZ = -dir.getStepZ();
                int compX = (offX == 1 ? -6 : 0) + (dir.getAxis() == Direction.Axis.Z ? -2 : 0);
                int compZ = (offZ == 1 ? -6 : 0) + (dir.getAxis() == Direction.Axis.X ? -2 : 0);
                int startX = offX * (14 + nesting * 6);
                int startY = offZ * (14 + nesting * 6);
                for (int way = -1; way <= 1; way += 2) {
                    for (int i = 0; i < 4 + nesting; i++) {
                        int rowX = ovr.getStepX() * i * way * 4;
                        int rowZ = ovr.getStepZ() * i * way * 4;
                        int realX = centerX + startX + compX + rowX;
                        int realZ = centerY + startY + compZ + rowZ;
                        edges[((pixbufSel++ * 13402141) >> 1) & 3].blit(majorBuffer, realX * scale, realZ * scale, edge);
                    }
                }
                edge++;
            }
        }
    }
    private static void fillCorners(TextureHelper.PixelBuffer majorBuffer, TextureHelper.PixelBuffer[] corners, int scale){
        int[][] cornerXY = new int[][]{
                {-1, 1, 1, -1},
                {-1, -1, 1, 1}
        };
        for (int nesting = 1; nesting <= LAYERS; nesting++) {
            for (int corner = 0; corner < 4; corner++) {
                TextureHelper.PixelBuffer cornerPixels = corners[(corner + nesting) & 0x3];
                int cX = cornerXY[0][corner];
                int cY = cornerXY[1][corner];
                int offX = cX * 6 * nesting + cX * 5;
                int offY = cY * 6 * nesting + cY * 5;
                int realX = centerCorner() + 5 + offX;
                int realY = centerCorner() + 5 + offY;
                cornerPixels.blit(majorBuffer, realX * scale, realY * scale, corner);
            }
        }
    }
    private static void fillCenter(TextureHelper.PixelBuffer majorBuffer, TextureHelper.PixelBuffer antecedentBuffer, int scale){
        antecedentBuffer.blit(majorBuffer, centerCorner() * scale, centerCorner() * scale);
    }
    private static void compileBarkCornersAndEdges(TextureHelper.PixelBuffer[] corners, TextureHelper.PixelBuffer[] edges, TextureHelper.PixelBuffer baseBuffer, int scale){
        for (int i = 0; i < 4; i++) {
            corners[i] = new TextureHelper.PixelBuffer(scale, scale);
            edges[i] = new TextureHelper.PixelBuffer(14 * scale, scale);
            baseBuffer.blit(corners[i], 0, 0, i);
            baseBuffer.blit(edges[i], -1 * scale, 0, i);
        }
    }
    private static void fillBarkBorder(TextureHelper.PixelBuffer majorBuffer, TextureHelper.PixelBuffer[] corners, TextureHelper.PixelBuffer[] edges, int scale){
        int[][] cornerXY = new int[][]{
                {-1, 1, 1, -1},
                {-1, -1, 1, 1}
        };
        int pixbufSel = 0;
        for (int row = 0; row <= LAYERS; row++) {
            TextureHelper.PixelBuffer edge = edges[((pixbufSel++ * 13402141) >> 1) & 3];
            int span = edge.w;
            edge.blit(majorBuffer, (1 + row * span) * scale, 0, 0);
            edge.blit(majorBuffer, (majorBuffer.w - edge.h) * scale, (1 + row * span) * scale, 1);
            edge.blit(majorBuffer, (majorBuffer.w - 1 - span - row * span) * scale, (majorBuffer.h - edge.h) * scale, 2);
            edge.blit(majorBuffer, 0, (majorBuffer.h - 1 - edge.w - row * span) * scale, 3);
        }
        for (int corner = 0; corner < 4; corner++) {
            int cX = cornerXY[0][corner] >> 1;
            int cY = cornerXY[1][corner] >> 1;
            TextureHelper.PixelBuffer cornerPixels = corners[corner];
            cornerPixels.blit(majorBuffer, cX * (majorBuffer.w - cornerPixels.w) * scale, cY * (majorBuffer.h - cornerPixels.h) * scale, corner);
        }
    }

    private static TextureHelper.PixelBuffer createBarklessAntecedent(TextureHelper.PixelBuffer baseBuffer) {
        TextureHelper.PixelBuffer antecedent = new TextureHelper.PixelBuffer(baseBuffer);

        int scale = baseBuffer.w / RESOLUTION;

        //Place the 4th pixel ring against the corners of the image.
        //Rotate 90deg to break up the pattern
        baseBuffer.blit(antecedent, 3 * scale, 3 * scale, 1);
        baseBuffer.blit(antecedent, -3 * scale, 3 * scale, 1);
        baseBuffer.blit(antecedent, 3 * scale, -3 * scale, 1);
        baseBuffer.blit(antecedent, -3 * scale, -3 * scale, 1);

        //Copy a 6 wide strip of pixels from the 4th pixel ring and place
        //it over the bark texture for all 4 edges.  Alternate the placement
        //to break up the pattern
        TextureHelper.PixelBuffer ringStrip = new TextureHelper.PixelBuffer(6 * scale, 1 * scale);
        baseBuffer.blit(ringStrip, -5 * scale, -3 * scale);
        ringStrip.blit(antecedent, 0 * scale, 2 * scale, -1);
        ringStrip.blit(antecedent, 15 * scale, 8 * scale, 1);

        baseBuffer.blit(ringStrip, -5 * scale, -12 * scale);
        ringStrip.blit(antecedent, 0 * scale, 8 * scale, 1);
        ringStrip.blit(antecedent, 15 * scale, 2 * scale, -1);

        ringStrip = new TextureHelper.PixelBuffer(1 * scale, 6 * scale);
        baseBuffer.blit(ringStrip, -3 * scale, -5 * scale);
        ringStrip.blit(antecedent, 2 * scale, 0 * scale, -1);
        ringStrip.blit(antecedent, 8 * scale, 15 * scale, 1);

        baseBuffer.blit(ringStrip, -12 * scale, -5 * scale);
        ringStrip.blit(antecedent, 8 * scale, 0 * scale, 1);
        ringStrip.blit(antecedent, 2 * scale, 15 * scale, -1);

        //Copy the center 14x14 pixels of the original back over the result
        TextureHelper.PixelBuffer center = new TextureHelper.PixelBuffer(14 * scale, 14 * scale);
        baseBuffer.blit(center, -1 * scale, -1 * scale);
        center.blit(antecedent, 1 * scale, 1 * scale);

        return antecedent;
    }

}
