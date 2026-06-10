package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class BasicRootsBlockBakedModel extends BasicBranchBlockBakedModel {

    final static float Z_FIGHTING_OFFSET = 0.001f;

    private final List<BakedQuad>[][] sleeveEndFaces = new List[6][8];

    public BasicRootsBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture) {
        super(barkTexture, ringsTexture);
        initRootsModels();
    }

    private void initRootsModels() {
        for (int i = 0; i < 8; i++) {
            int radius = i + 1;
            for (Direction dir : Direction.values()) {
                sleeveEndFaces[dir.get3DDataValue()][i] = bakeSleeveFace(radius, dir, ringsTexture);
            }
        }
    }

    public List<BakedQuad> bakeSleeveFace(int radius, Direction dir, TextureAtlasSprite rings) {
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        float halfSizeX = dir.getStepX() != 0 ? halfSize + Z_FIGHTING_OFFSET : dradius;
        float halfSizeY = dir.getStepY() != 0 ? halfSize + Z_FIGHTING_OFFSET : dradius;
        float halfSizeZ = dir.getStepZ() != 0 ? halfSize + Z_FIGHTING_OFFSET : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerY = 16 + (dir.getStepY() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);
        BlockFaceUV uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
        mapFacesIn.put(dir, new BlockElementFace(dir, -1, "", uvface));

        BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            quads.add(faceBakery.bakeQuad(part.from, part.to, e.getValue(), rings, face, BlockModelRotation.X0_Y0, part.rotation, true));
        }

        return quads;
    }

    @Override
    protected int getRadius(BlockState blockState) {
        if (blockState.getBlock() instanceof BasicRootsBlock) {
            if (blockState.hasProperty(BasicRootsBlock.RADIUS)) {
                return blockState.getValue(BasicRootsBlock.RADIUS);
            }
        }
        return super.getRadius(blockState);
    }

    @Override
    public EnumMap<Direction, List<BakedQuad>> collectQuads(int coreRadius, int[] connections, int twigRadius, Direction forceRingDir) {
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        Direction sourceDir = getSourceDir(coreRadius, connections);
        int coreDir = resolveCoreDir(sourceDir);
        Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

        EnumMap<Direction, List<BakedQuad>> bakedQuads = new EnumMap<>(Direction.class);

        for (Direction face : Direction.values()) {
            List<BakedQuad> quads = bakedQuads.computeIfAbsent(face, dir->new ArrayList<>());
            int connectionOnFace = connections[face.get3DDataValue()];
            if (coreRadius != connectionOnFace) {
                if (coreRingDir == null || coreRingDir != face) {
                    quads.addAll(coresQuads[coreDir][coreRadius - 1]);
                } else {
                    quads.addAll(ringsQuads[coreRadius - 1]);
                }
            }

            if (coreRadius != 8) {
                for (Direction connDir : Direction.values()) {
                    int idx = connDir.get3DDataValue();
                    int connRadius = connections[idx];
                    if (connRadius > 0 && connRadius < 8 && (connRadius <= twigRadius || face != connDir)) {
                        quads.addAll(sleevesQuads[idx][connRadius - 1]);
                    }
                }
            }

            if (connectionOnFace > 0 && connectionOnFace <= coreRadius) {
                quads.addAll(sleeveEndFaces[face.get3DDataValue()][connectionOnFace - 1]);
            }
        }

        return bakedQuads;
    }
}
