package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.api.network.Connections;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Predicate;

public class ThickBranchBlockBakedModel extends BasicBranchBlockBakedModel {

    private final Material.Baked thickRings;
    private final List<BakedQuad>[] trunksBark = new List[16];
    private final List<BakedQuad>[] trunksTopBark = new List[16];
    private final List<BakedQuad>[] trunksTopRings = new List[16];
    private final List<BakedQuad>[] trunksBotRings = new List[16];

    public ThickBranchBlockBakedModel(TextureAtlasSprite barkTexture, TextureAtlasSprite ringsTexture, TextureAtlasSprite thickRingsTexture) {
        this(null, new Material.Baked(barkTexture, false), new Material.Baked(ringsTexture, false), new Material.Baked(thickRingsTexture, false));
    }

    public ThickBranchBlockBakedModel(ModelBaker baker, Material.Baked bark, Material.Baked rings, Material.Baked thickRings) {
        super(baker, bark, rings);
        this.thickRings = thickRings;
        if (baker != null) {
            initThickModels();
        }
    }

    private void initThickModels() {
        for (int i = 0; i < ThickBranchBlock.MAX_RADIUS_THICK - BranchBlock.MAX_RADIUS; i++) {
            int radius = i + BranchBlock.MAX_RADIUS + 1;
            trunksBark[i] = bakeTrunk(radius, bark, true);
            trunksTopBark[i] = bakeTrunk(radius, bark, false);
            trunksTopRings[i] = bakeTrunkRings(radius, thickRings, Direction.UP);
            trunksBotRings[i] = bakeTrunkRings(radius, thickRings, Direction.DOWN);
        }
    }

    private List<BakedQuad> bakeTrunk(int radius, Material.Baked material, boolean side) {
        Vector3f from = new Vector3f(8 - radius, 0, 8 - radius);
        Vector3f to = new Vector3f(8 + radius, 16, 8 + radius);
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        Direction[] faces = side
                ? new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}
                : new Direction[]{Direction.UP, Direction.DOWN};
        for (Direction face : faces) {
            quads.add(CuboidQuadBaker.bake(baker, from, to, face, 0, 0, 16, 16, CuboidQuadBaker.faceAngle(Direction.Axis.Y, face), material));
        }
        return quads;
    }

    private List<BakedQuad> bakeTrunkRings(int radius, Material.Baked material, Direction face) {
        Vector3f from = new Vector3f(8 - radius, 0, 8 - radius);
        Vector3f to = new Vector3f(8 + radius, 16, 8 + radius);
        List<BakedQuad> quads = CuboidQuadBaker.newList();
        quads.add(CuboidQuadBaker.bake(baker, from, to, face, 0, 0, 16, 16, 0, material));
        return quads;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
                          RandomSource random, Predicate<Direction> cullTest) {
        int coreRadius = getRadius(state);
        if (coreRadius <= BranchBlock.MAX_RADIUS) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }
        if (baker == null) {
            return;
        }
        coreRadius = Mth.clamp(coreRadius, 9, ThickBranchBlock.MAX_RADIUS_THICK);
        int index = coreRadius - 9;

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        int twigRadius = 1;
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            Connections data = branchBlock.getConnectionData(level, pos, state);
            connections = data.getAllRadii();
            twigRadius = Math.max(1, branchBlock.getFamily().getPrimaryThickness());
        }

        boolean branchesAround = connections[2] + connections[3] + connections[4] + connections[5] != 0;
        for (Direction face : Direction.values()) {
            CuboidQuadBaker.emit(emitter, trunksBark[index], face, cullTest);
            if (face == Direction.UP || face == Direction.DOWN) {
                if (connections[face.get3DDataValue()] < twigRadius && !branchesAround) {
                    CuboidQuadBaker.emit(emitter, trunksTopRings[index], face, cullTest);
                } else if (connections[face.get3DDataValue()] < coreRadius) {
                    CuboidQuadBaker.emit(emitter, trunksTopBark[index], face, cullTest);
                }
            }
        }
    }
}
