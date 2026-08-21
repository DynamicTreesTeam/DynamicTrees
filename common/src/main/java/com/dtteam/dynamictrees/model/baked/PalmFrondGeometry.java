package com.dtteam.dynamictrees.model.baked;

import com.dtteam.dynamictrees.block.leaves.PalmLeavesProperties;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Shared palm-frond mesh bake used by loader-specific runtime wraps.
 */
public final class PalmFrondGeometry {

    public enum Size { LARGE, MEDIUM, SMALL }

    @FunctionalInterface
    public interface Factory {
        BlockStateModel create(Material.Baked material, List<List<BakedQuad>> frondQuads);
    }

    private PalmFrondGeometry() {}

    public static BlockStateModel wrap(BlockState state, BlockStateModel original, ModelBaker baker,
                                       Map<Identifier, BlockStateModel> cache, Factory factory) {
        if (!(state.getBlock() instanceof PalmLeavesProperties.DynamicPalmLeavesBlock palmBlock)) {
            return original;
        }
        int distance = state.getValue(LeavesBlock.DISTANCE);
        int direction = state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);
        if ((distance != 1 && distance != 2) || direction == 0) {
            return original;
        }
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(palmBlock);
        BlockStateModel fronds = cache.computeIfAbsent(blockId, id -> create(baker, palmBlock, factory));
        return fronds != null ? fronds : original;
    }

    private static BlockStateModel create(ModelBaker baker, PalmLeavesProperties.DynamicPalmLeavesBlock palmBlock, Factory factory) {
        if (!(palmBlock.getLeavesProperties() instanceof PalmLeavesProperties palm)) {
            return null;
        }
        Identifier frondTex = palm.getTexturePath(PalmLeavesProperties.FROND).orElse(
                Identifier.fromNamespaceAndPath(palm.getRegistryName().getNamespace(),
                        "block/" + palm.getRegistryName().getPath() + "_frond")
        );
        Material.Baked material = baker.materials().get(new Material(frondTex), () -> "dynamictrees:palm_fronds");
        if (material == null || material.sprite() == null) {
            return null;
        }
        List<List<BakedQuad>> quads = new ArrayList<>(8);
        Size size = sizeFor(palm.getFrondLoader());
        for (CoordUtils.Surround surr : CoordUtils.Surround.values()) {
            quads.add(bakeSurround(baker, material, size, surr));
        }
        return factory.create(material, quads);
    }

    public static Size sizeFor(Identifier loader) {
        String path = loader.getPath();
        if (path.contains("small")) {
            return Size.SMALL;
        }
        if (path.contains("medium")) {
            return Size.MEDIUM;
        }
        return Size.LARGE;
    }

    private static List<BakedQuad> bakeSurround(ModelBaker baker, Material.Baked material, Size size, CoordUtils.Surround surr) {
        List<BakedQuad> quads = new ArrayList<>();
        float[][] template = size == Size.SMALL
                ? new float[][]{{0, 0, 2, 10 / 16f, 4 / 16f}, {0, 1, 2, 10 / 16f, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 0, 4 / 16f},
                {0, 0, 2, 10 / 16f, 4 / 16f}, {0, 0, 0, 0, 4 / 16f}, {0, 1, 0, 0, 0}, {0, 1, 2, 10 / 16f, 0}}
                : new float[][]{{0, 0, 3, 15 / 16f, 4 / 16f}, {0, 1, 3, 15 / 16f, 0}, {0, 1, 0, 0, 0}, {0, 0, 0, 0, 4 / 16f},
                {0, 0, 3, 15 / 16f, 4 / 16f}, {0, 0, 0, 0, 4 / 16f}, {0, 1, 0, 0, 0}, {0, 1, 3, 15 / 16f, 0}};
        int passes = switch (size) {
            case LARGE -> 4;
            case MEDIUM -> 3;
            case SMALL -> 2;
        };
        for (int pass = 0; pass < passes; pass++) {
            for (int half = 0; half < 2; half++) {
                if (size == Size.MEDIUM) {
                    if (pass == 0 && surr.ordinal() % 2 != 0) continue;
                    if (pass == 2 && surr.ordinal() % 2 == 0) continue;
                }
                Vector3f[] out = new Vector3f[8];
                float[][] uv = new float[8][2];
                for (int v = 0; v < 8; v++) {
                    float x = template[v][0];
                    float y = template[v][1];
                    float z = template[v][2];
                    uv[v][0] = template[v][3];
                    uv[v][1] = template[v][4];
                    x *= 40f / 32f;
                    z *= 40f / 32f;

                    double len = 0.75 - y;
                    double angle = Math.atan2(x, y);
                    angle += Math.PI * halfAngle(size, half);
                    x = (float) (Math.sin(angle) * len);
                    y = (float) (Math.cos(angle) * len);

                    len = Math.sqrt(y * y + z * z);
                    angle = Math.atan2(y, z);
                    angle += Math.PI * xAngle(size, pass, surr);
                    y = (float) (Math.sin(angle) * len);
                    z = (float) (Math.cos(angle) * len);

                    len = Math.sqrt(x * x + z * z);
                    angle = Math.atan2(x, z);
                    angle += Math.PI * 0.25 * surr.ordinal() + (Math.PI * yAngle(size, pass));
                    x = (float) (Math.sin(angle) * len);
                    z = (float) (Math.cos(angle) * len);

                    x += 0.5f;
                    z += 0.5f;
                    y += yOffset(size, pass);
                    x += surr.getOffset().getX();
                    z += surr.getOffset().getZ();
                    if (size == Size.SMALL) {
                        y += 1;
                    }
                    out[v] = new Vector3f(x, y, z);
                }
                quads.add(CuboidQuadBaker.bakeUnculled(baker, material,
                        out[0], uv[0][0], uv[0][1], out[1], uv[1][0], uv[1][1],
                        out[2], uv[2][0], uv[2][1], out[3], uv[3][0], uv[3][1]));
                quads.add(CuboidQuadBaker.bakeUnculled(baker, material,
                        out[4], uv[4][0], uv[4][1], out[5], uv[5][0], uv[5][1],
                        out[6], uv[6][0], uv[6][1], out[7], uv[7][0], uv[7][1]));
            }
        }
        return quads;
    }

    private static double halfAngle(Size size, int half) {
        double mag = size == Size.LARGE ? 0.8 : 1.2;
        return half == 1 ? mag : -mag;
    }

    private static double xAngle(Size size, int pass, CoordUtils.Surround surr) {
        return switch (size) {
            case LARGE -> switch (pass) {
                case 0 -> -0.29;
                case 1 -> -0.06;
                case 2 -> 0.16;
                case 3 -> 0.32;
                default -> 0;
            };
            case MEDIUM -> pass == 2 ? 0.28 : pass == 1 ? 0.06 : -0.17;
            case SMALL -> (pass == 1 ? -0.17 : -0.30) + 0.1 * ((surr.ordinal() + pass) % 3) - 0.05;
        };
    }

    private static double yAngle(Size size, int pass) {
        return switch (size) {
            case LARGE -> switch (pass) {
                case 3, 0 -> 0.005;
                case 1 -> 0.185;
                case 2 -> 0.08;
                default -> 0;
            };
            case MEDIUM -> pass == 1 ? (0.185 - 0.25) : pass == 2 ? 0.08 : 0.005;
            case SMALL -> pass == 1 ? 0.185 : 0.005;
        };
    }

    private static float yOffset(Size size, int pass) {
        return switch (size) {
            case LARGE -> pass == 0 ? 0.125f : pass == 2 ? -0.125f : 0f;
            case MEDIUM -> pass == 2 ? -0.125f : pass == 0 ? 0.125f : 0f;
            case SMALL -> pass == 0 ? 0f : 0.15f;
        };
    }
}
