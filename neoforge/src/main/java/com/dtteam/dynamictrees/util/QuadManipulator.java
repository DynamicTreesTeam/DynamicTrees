package com.dtteam.dynamictrees.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class QuadManipulator {

    public static final Direction[] everyFace = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Vec3 offset, RandomSource rand, ModelData modelData) {
        return getQuads(modelIn, stateIn, offset, everyFace, rand, modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Vec3 offset, Direction[] sides, RandomSource rand, ModelData modelData) {
        ArrayList<BakedQuad> outQuads = new ArrayList<>();

        if (stateIn != null) {
            for (Direction dir : sides) {
                outQuads.addAll(modelIn.getQuads(stateIn, dir, rand, modelData, null));
            }
        }

        return offset.equals(Vec3.ZERO) ? outQuads : moveQuads(outQuads, offset);
    }

    public static List<BakedQuad> moveQuads(List<BakedQuad> inQuads, Vec3 offset) {
        ArrayList<BakedQuad> outQuads = new ArrayList<>();

        for (BakedQuad inQuad : inQuads) {
            BakedQuad quadCopy = new BakedQuad(inQuad.getVertices().clone(), inQuad.getTintIndex(), inQuad.getDirection(), inQuad.getSprite(), inQuad.isShade());
            int[] vertexData = quadCopy.getVertices();
            for (int i = 0; i < vertexData.length; i += (DefaultVertexFormat.BLOCK.getVertexSize()/4)) {
                int pos = 0;
                for (VertexFormatElement vfe : DefaultVertexFormat.BLOCK.getElements()) {
                    if (vfe.usage() == VertexFormatElement.Usage.POSITION) {
                        float x = Float.intBitsToFloat(vertexData[i + pos + 0]);
                        float y = Float.intBitsToFloat(vertexData[i + pos + 1]);
                        float z = Float.intBitsToFloat(vertexData[i + pos + 2]);
                        x += (float) offset.x;
                        y += (float) offset.y;
                        z += (float) offset.z;
                        vertexData[i + pos + 0] = Float.floatToIntBits(x);
                        vertexData[i + pos + 1] = Float.floatToIntBits(y);
                        vertexData[i + pos + 2] = Float.floatToIntBits(z);
                        break;
                    }
                    pos += vfe.byteSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
                }
            }

            outQuads.add(quadCopy);
        }

        outQuads.trimToSize();
        return outQuads;
    }

    @Nullable
    public static BakedModel getModelForState(BlockState state) {
        BakedModel model = null;

        try {
            model = getModel(state);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    public static ModelManager getModelManager() {
        return Minecraft.getInstance().getModelManager();
    }

    public static BakedModel getModel(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);//This gives us earlier access
    }

    @Nullable
    public static ResourceLocation getModelTexture(BakedModel model, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, BlockState state, Direction dir) {

        float[] uvs = getSpriteUVFromBlockState(state, dir);

        if (uvs != null) {
            List<TextureAtlasSprite> sprites = new ArrayList<>();

            float closest = Float.POSITIVE_INFINITY;
            ResourceLocation closestTex = ResourceLocation.parse("missingno");
            if (model != null) {
                ResourceLocation tex = model.getParticleIcon(ModelData.EMPTY).contents().name();
                TextureAtlasSprite tas = bakedTextureGetter.apply(tex);
                float u = tas.getU(8);
                float v = tas.getV(8);
                sprites.add(tas);
                float du = u - uvs[0];
                float dv = v - uvs[1];
                float distSq = du * du + dv * dv;
                if (distSq < closest) {
                    closest = distSq;
                    closestTex = tex;
                }
            }

            return closestTex;
        }

        return null;
    }

    @Nullable
    public static float[] getSpriteUVFromBlockState(BlockState state, Direction side) {
        BakedModel bakedModel = getModelManager().getBlockModelShaper().getBlockModel(state);
        List<BakedQuad> quads = new ArrayList<>();
        RandomSource random = RandomSource.create();
        quads.addAll(bakedModel.getQuads(state, side, random, ModelData.EMPTY, null));
        quads.addAll(bakedModel.getQuads(state, null, random, ModelData.EMPTY, null));

        Optional<BakedQuad> quad = quads.stream().filter(q -> q.getDirection() == side).findFirst();

        if (quad.isPresent()) {

            float u = 0.0f;
            float v = 0.0f;

            int[] vertexData = quad.get().getVertices();
            int numVertices = 0;
            for (int i = 0; i < vertexData.length; i += (DefaultVertexFormat.BLOCK.getVertexSize()/4)) {
                int pos = 0;
                for (VertexFormatElement vfe : DefaultVertexFormat.BLOCK.getElements()) {
                    if (vfe.usage() == VertexFormatElement.Usage.UV) {
                        u += Float.intBitsToFloat(vertexData[i + pos + 0]);
                        v += Float.intBitsToFloat(vertexData[i + pos + 1]);
                    }
                    pos += vfe.byteSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
                }
                numVertices++;
            }

            return new float[]{u / numVertices, v / numVertices};
        }

        System.err.println("Warning: Could not get \"" + side + "\" side quads from blockstate: " + state);

        return null;
    }

}