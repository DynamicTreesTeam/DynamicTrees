package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.renderer.v1.mesh.*;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class BasicBranchBlockBakedModel implements BakedModel, FabricBakedModel {

    protected final TextureAtlasSprite barkTexture;
    protected final TextureAtlasSprite ringsTexture;

    public final List<BakedQuad>[][] sleevesQuads = new List[6][8];
    public final List<BakedQuad>[][] coresQuads = new List[3][8];
    public final List<BakedQuad>[] ringsQuads = new List[8];

    public BasicBranchBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture) {
        this.barkTexture = barkTexture;
        this.ringsTexture = ringsTexture;
        initModels();
    }

    private void initModels() {
        for (int i = 0; i < 8; i++) {
            int radius = i + 1;
            if (radius < 8) {
                for (Direction dir : Direction.values()) {
                    sleevesQuads[dir.get3DDataValue()][i] = bakeSleeve(radius, dir, barkTexture);
                }
            }
            coresQuads[0][i] = bakeCore(radius, Axis.Y, barkTexture);
            coresQuads[1][i] = bakeCore(radius, Axis.Z, barkTexture);
            coresQuads[2][i] = bakeCore(radius, Axis.X, barkTexture);

            ringsQuads[i] = bakeCore(radius, Axis.Y, ringsTexture);
        }
    }

    public BlockElement generateSleevePart(int radius, Direction dir) {
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getStepX() != 0 ? halfSize : dradius;
        int halfSizeY = dir.getStepY() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getStepZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getStepX() * move);
        int centerY = 16 + (dir.getStepY() * move);
        int centerZ = 16 + (dir.getStepZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        boolean negative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
        if (dir.getAxis() == Axis.Z) {
            negative = !negative;
        }

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            if (dir.getOpposite() != face) {
                BlockFaceUV uvface = null;
                if (dir == face) {
                    if (radius == 1) {
                        uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
                    }
                } else {
                    uvface = new BlockFaceUV(new float[]{8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize}, getFaceAngle(dir.getAxis(), face));
                }
                if (uvface != null) {
                    mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
                }
            }
        }

        return new BlockElement(posFrom, posTo, mapFacesIn, null, true);
    }

    public List<BakedQuad> bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark) {
        BlockElement part = generateSleevePart(radius, dir);
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            quads.add(faceBakery.bakeQuad(part.from, part.to, e.getValue(), bark, face, BlockModelRotation.X0_Y0, part.rotation, true));
        }

        return quads;
    }

    protected BlockElement generateCorePart(int radius, Axis axis) {
        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

        Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            BlockFaceUV uvface = new BlockFaceUV(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, getFaceAngle(axis, face));
            mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));
        }

        return new BlockElement(posFrom, posTo, mapFacesIn, null, true);
    }

    public List<BakedQuad> bakeCore(int radius, Axis axis, TextureAtlasSprite icon) {
        BlockElement part = generateCorePart(radius, axis);
        List<BakedQuad> quads = new ArrayList<>();
        FaceBakery faceBakery = new FaceBakery();

        for (Map.Entry<Direction, BlockElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            quads.add(faceBakery.bakeQuad(part.from, part.to, e.getValue(), icon, face, BlockModelRotation.X0_Y0, part.rotation, true));
        }

        return quads;
    }

    public int getFaceAngle(Axis axis, Direction face) {
        if (axis == Axis.Y) {
            return 0;
        } else if (axis == Axis.Z) {
            return switch (face) {
                case UP -> 0;
                case WEST -> 270;
                case DOWN -> 180;
                default -> 90;
            };
        } else {
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        EnumMap<Direction, List<BakedQuad>> bakedQuads = collectQuads(blockView, state, pos);
        if (bakedQuads == null) return;

        QuadEmitter emitter = context.getEmitter();
        RenderMaterial material = getRenderMaterial();

        bakedQuads.forEach((dir, quads) ->
                emitQuads(dir, emitter, material, quads));
    }

    protected @Nullable EnumMap<Direction, List<BakedQuad>> collectQuads(BlockAndTintGetter getter, BlockState state, BlockPos pos) {
        if (state == null) return null;

        final int coreRadius = getRadius(state);
        if (coreRadius <= 0 || coreRadius > maxBranchRadius()) return null;

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        int twigRadius = 1;

        if (state.getBlock() instanceof BranchBlock branchBlock) {
            connections = branchBlock.getConnectionData(getter, pos, state).getAllRadii();
            twigRadius = branchBlock.getFamily().getPrimaryThickness();
        }

        return collectQuads(coreRadius, connections, twigRadius, null);
    }

    protected int maxBranchRadius() {
        return ThickBranchBlock.MAX_RADIUS;
    }

    public EnumMap<Direction, List<BakedQuad>> collectQuads(int coreRadius, int[] connections, int twigRadius, Direction forceRingDir) {
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        Direction sourceDir = getSourceDir(coreRadius, connections);
        int coreDir = resolveCoreDir(sourceDir);
        Direction coreRingDir = forceRingDir != null ? forceRingDir :
                ((numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null);

        EnumMap<Direction, List<BakedQuad>> bakedQuads = new EnumMap<>(Direction.class);

        for (Direction face : Direction.values()) {
            List<BakedQuad> quads = bakedQuads.computeIfAbsent(face, dir->new ArrayList<>());
            if (coreRadius != connections[face.get3DDataValue()]) {
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
        }
        return bakedQuads;
    }

    public List<BakedQuad> getRingQuads(int radius){
        return ringsQuads[radius - 1];
    }

    protected static RenderMaterial getRenderMaterial() {
        var renderer = RendererAccess.INSTANCE.getRenderer();
        MaterialFinder finder = renderer.materialFinder();
//        finder.disableAo(0, true);
//        finder.disableDiffuse(0, true);

        return finder.find();
    }

    protected void emitQuads(Direction face, QuadEmitter emitter, RenderMaterial material, List<BakedQuad> quads) {
        if (quads == null) return;
        for (BakedQuad quad : quads) {
            if (quad.getDirection() == face) {
                emitter.fromVanilla(quad, material, face);
                emitter.emit();
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
    }

    @Nullable
    protected Direction getSourceDir(int coreRadius, int[] connections) {
        int largestConnection = 0;
        Direction sourceDir = null;

        for (Direction dir : Direction.values()) {
            int connRadius = connections[dir.get3DDataValue()];
            if (connRadius > largestConnection) {
                largestConnection = connRadius;
                sourceDir = dir;
            }
        }

        if (largestConnection < coreRadius) {
            sourceDir = null;
        }
        return sourceDir;
    }

    protected int resolveCoreDir(@Nullable Direction dir) {
        if (dir == null) {
            return 0;
        }
        return dir.get3DDataValue() >> 1;
    }

    protected int getRadius(BlockState blockState) {
        return ((BranchBlock) blockState.getBlock()).getRadius(blockState);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return barkTexture;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
