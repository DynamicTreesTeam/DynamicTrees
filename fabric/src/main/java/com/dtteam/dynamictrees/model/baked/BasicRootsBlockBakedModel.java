package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.google.common.collect.Maps;
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
import org.joml.Vector3f;

import java.util.ArrayList;
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
        if (blockState.getBlock() instanceof BasicRootsBlock rootsBlock) {
            return rootsBlock.getRadius(blockState);
        }
        return super.getRadius(blockState);
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (state == null) return;

        final int coreRadius = getRadius(state);
        if (coreRadius <= 0 || coreRadius > 8) return;

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        int twigRadius = 1;

        if (state.getBlock() instanceof BranchBlock branchBlock) {
            connections = branchBlock.getConnectionData(blockView, pos, state).getAllRadii();
            twigRadius = branchBlock.getFamily().getPrimaryThickness();
        }

        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        var emitter = context.getEmitter();

        Direction sourceDir = getSourceDir(coreRadius, connections);
        int coreDir = resolveCoreDir(sourceDir);
        Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

        for (Direction face : Direction.values()) {
            if (coreRadius != connections[face.get3DDataValue()]) {
                List<BakedQuad> quads;
                if (coreRingDir == null || coreRingDir != face) {
                    quads = coresQuads[coreDir][coreRadius - 1];
                } else {
                    quads = ringsQuads[coreRadius - 1];
                }
                for (BakedQuad quad : quads) {
                    if (quad.getDirection() == face) {
                        emitter.fromVanilla(quad, null, face);
                        emitter.emit();
                    }
                }
            }

            if (coreRadius != 8) {
                for (Direction connDir : Direction.values()) {
                    int idx = connDir.get3DDataValue();
                    int connRadius = connections[idx];
                    if (connRadius > 0 && (connRadius <= twigRadius || face != connDir)) {
                        List<BakedQuad> sleeveQuads = sleevesQuads[idx][connRadius - 1];
                        if (sleeveQuads != null) {
                            for (BakedQuad quad : sleeveQuads) {
                                if (quad.getDirection() == face) {
                                    emitter.fromVanilla(quad, null, face);
                                    emitter.emit();
                                }
                            }
                        }
                    }
                }
            }

            int idx = face.get3DDataValue();
            int connRadius = connections[idx];
            if (connRadius > 0) {
                List<BakedQuad> endFaceQuads = sleeveEndFaces[idx][connRadius - 1];
                if (endFaceQuads != null) {
                    for (BakedQuad quad : endFaceQuads) {
                        if (quad.getDirection() == face) {
                            emitter.fromVanilla(quad, null, face);
                            emitter.emit();
                        }
                    }
                }
            }
        }
    }
}
