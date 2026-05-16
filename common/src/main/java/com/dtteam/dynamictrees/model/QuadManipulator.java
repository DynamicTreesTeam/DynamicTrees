package com.dtteam.dynamictrees.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.multipart.MultiPartModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.*;


public class QuadManipulator {

    public static final Direction[] everyFace = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    public static List<BakedQuad> getQuads(BlockStateModel modelIn, BlockState stateIn, Vec3 offset, RandomSource rand, @Nullable ModelConnections modelData) {
        if (modelIn instanceof MultiPartModel mpModel){
            return getMultiPartQuads(mpModel, stateIn, offset, rand, modelData);
        } else {
            return getQuads(modelIn, stateIn, offset, everyFace, rand, modelData);
        }
    }

    private static @NonNull List<BakedQuad> getMultiPartQuads(MultiPartModel mpModel, BlockState stateIn, Vec3 offset, RandomSource rand, @Nullable ModelConnections modelData) {
        List<BakedQuad> list = new LinkedList<>();
        for (BlockStateModel subModel : Optional.ofNullable(mpModel.models).orElse(Collections.emptyList())){
            rand.setSeed(rand.nextLong());
            list.addAll(getQuads(subModel, stateIn, offset, everyFace, rand, modelData));
        }
        return list;
    }

    public static List<BakedQuad> getQuads(BlockStateModel modelIn, BlockState stateIn, Vec3 offset, Direction[] sides, RandomSource rand, @Nullable ModelConnections modelData) {
        ArrayList<BakedQuad> outQuads = new ArrayList<>();

        if (stateIn == null) return outQuads;

        List<BlockStateModelPart> parts = new ArrayList<>();
        if (modelData == null){
            modelIn.collectParts(rand, parts);
        } else if (modelIn instanceof BlockStateModelWithConnectionData branchModelIn) {
            branchModelIn.collectParts(stateIn, parts, modelData);
        }

        for (BlockStateModelPart part : parts){
            for (Direction dir : sides) {
                outQuads.addAll(part.getQuads(dir));
            }
        }

        return offset.equals(Vec3.ZERO) ? outQuads : moveQuads(outQuads, offset);
    }

    public static List<BakedQuad> moveQuads(List<BakedQuad> inQuads, Vec3 offset) {
        ArrayList<BakedQuad> outQuads = new ArrayList<>();

        for (BakedQuad inQuad : inQuads) {
            BakedQuad quadCopy = new BakedQuad(
                    addVectors(inQuad.position0(), offset),
                    addVectors(inQuad.position1(), offset),
                    addVectors(inQuad.position2(), offset),
                    addVectors(inQuad.position3(), offset),
                    inQuad.packedUV0(), inQuad.packedUV1(), inQuad.packedUV2(), inQuad.packedUV3(),
                    inQuad.direction(), inQuad.materialInfo()); //, inQuad.bakedNormals(), inQuad.bakedColors()
//            for (int i = 0; i < vertexData.length; i += (DefaultVertexFormat.BLOCK.getVertexSize()/4)) {
//                int pos = 0;
//                for (VertexFormatElement vfe : DefaultVertexFormat.BLOCK.getElements()) {
//                    if (vfe.usage() == VertexFormatElement.Usage.POSITION) {
//                        float x = Float.intBitsToFloat(vertexData[i + pos + 0]);
//                        float y = Float.intBitsToFloat(vertexData[i + pos + 1]);
//                        float z = Float.intBitsToFloat(vertexData[i + pos + 2]);
//                        x += (float) offset.x;
//                        y += (float) offset.y;
//                        z += (float) offset.z;
//                        vertexData[i + pos + 0] = Float.floatToIntBits(x);
//                        vertexData[i + pos + 1] = Float.floatToIntBits(y);
//                        vertexData[i + pos + 2] = Float.floatToIntBits(z);
//                        break;
//                    }
//                    pos += vfe.byteSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
//                }
//            }
            outQuads.add(quadCopy);
        }

        outQuads.trimToSize();
        return outQuads;
    }

    private static Vector3fc addVectors(Vector3fc original, Vec3 offset){
        return new Vector3f(original.x()+(float) offset.x, original.y()+(float) offset.y, original.z()+(float) offset.z);
    }

    @Nullable
    public static BlockStateModel getModelForState(BlockState state) {
        BlockStateModel model = null;

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

    public static BlockStateModel getModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);//This gives us earlier access
    }

//    @Nullable
//    public static Identifier getModelTexture(BlockStateModel model, Function<Identifier, TextureAtlasSprite> bakedTextureGetter, BlockState state, Direction dir) {
//
//        float[] uvs = getSpriteUVFromBlockState(state, dir);
//
//        if (uvs != null) {
//            List<TextureAtlasSprite> sprites = new ArrayList<>();
//
//            float closest = Float.POSITIVE_INFINITY;
//            Identifier closestTex = Identifier.parse("missingno");
//            if (model != null) {
//                Identifier tex = model.particleMaterial().sprite().contents().name();
//                TextureAtlasSprite tas = bakedTextureGetter.apply(tex);
//                float u = tas.getU(8);
//                float v = tas.getV(8);
//                sprites.add(tas);
//                float du = u - uvs[0];
//                float dv = v - uvs[1];
//                float distSq = du * du + dv * dv;
//                if (distSq < closest) {
//                    closest = distSq;
//                    closestTex = tex;
//                }
//            }
//
//            return closestTex;
//        }
//
//        return null;
//    }

//    @Nullable
//    public static float[] getSpriteUVFromBlockState(BlockState state, Direction side) {
//        BlockStateModel bakedModel = getModelManager().getBlockModelShaper().getBlockModel(state);
//        List<BakedQuad> quads = new ArrayList<>();
//        RandomSource random = RandomSource.create();
//        quads.addAll(bakedModel.getQuads(state, side, random, null));
//        quads.addAll(bakedModel.getQuads(state, null, random, null));
//
//        Optional<BakedQuad> quad = quads.stream().filter(q -> q.getDirection() == side).findFirst();
//
//        if (quad.isPresent()) {
//
//            float u = 0.0f;
//            float v = 0.0f;
//
//            int[] vertexData = quad.get().getVertices();
//            int numVertices = 0;
//            for (int i = 0; i < vertexData.length; i += (DefaultVertexFormat.BLOCK.getVertexSize()/4)) {
//                int pos = 0;
//                for (VertexFormatElement vfe : DefaultVertexFormat.BLOCK.getElements()) {
//                    if (vfe.usage() == VertexFormatElement.Usage.UV) {
//                        u += Float.intBitsToFloat(vertexData[i + pos + 0]);
//                        v += Float.intBitsToFloat(vertexData[i + pos + 1]);
//                    }
//                    pos += vfe.byteSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
//                }
//                numVertices++;
//            }
//
//            return new float[]{u / numVertices, v / numVertices};
//        }
//
//        System.err.println("Warning: Could not get \"" + side + "\" side quads from blockstate: " + state);
//
//        return null;
//    }

}