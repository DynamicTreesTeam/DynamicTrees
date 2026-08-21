package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.tint.FunctionalBlockTintSource;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class FabricClientColors {

    private FabricClientColors() {}

    public static void register() {
        BlockColorMultipliers.register("birch", (state, level, pos, tintIndex) -> FoliageColor.FOLIAGE_BIRCH);
        BlockColorMultipliers.register("spruce", (state, level, pos, tintIndex) -> FoliageColor.FOLIAGE_EVERGREEN);

        for (SoilProperties soil : SoilProperties.REGISTRY) {
            if (soil.getBlock().isEmpty()) {
                continue;
            }
            SoilBlock roots = soil.getBlock().get();
            BlockColorRegistry.register((state, level, pos, tintValues) -> {
                int foliage = 0xFFFFFF;
                if (level != null && pos != null) {
                    foliage = BlockTintSources.grass().colorInWorld(state, level, pos);
                }
                int rootsColor = (level != null && pos != null) ? roots.rootColor(state, level, pos) : 0xFFFFFF;
                tintValues.size(2);
                tintValues.set(0, foliage);
                tintValues.set(1, rootsColor);
            }, roots);
        }

        PottedSaplingBlock potted = DTRegistries.POTTED_SAPLING.get();
        BlockColorRegistry.register((state, level, pos, tintValues) -> {
            int color = 0xFFFFFFFF;
            if (level != null && pos != null && state.getBlock() instanceof PottedSaplingBlock) {
                color = potted.getSpecies(level, pos).saplingColorMultiplier(state, level, pos, 0);
            }
            tintValues.add(color);
        }, potted);

        for (Species species : Species.REGISTRY) {
            species.getSapling().ifPresent(sapling -> BlockColorRegistry.register((state, level, pos, tintValues) -> {
                int leaf = 0xFFFFFFFF;
                int wood = 0xFFFFFFFF;
                if (level != null && pos != null) {
                    leaf = species.saplingColorMultiplier(state, level, pos, 0);
                    wood = species.saplingColorMultiplier(state, level, pos, 1);
                }
                tintValues.size(2);
                tintValues.set(0, leaf);
                tintValues.set(1, wood);
            }, sapling));
        }

        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream()
                .filter(lp -> lp.getDynamicLeavesBlock().isPresent())
                .map(lp -> lp.getDynamicLeavesBlock().get())
                .collect(Collectors.toSet())) {
            BlockColorRegistry.register((state, level, pos, tintValues) -> {
                int color = 0x00FF00FF;
                int bark = 0xB3A979;
                if (level != null && pos != null && TreeHelper.isLeaves(state.getBlock())) {
                    LeavesProperties properties = ((DynamicLeavesBlock) state.getBlock()).getLeavesProperties();
                    color = properties.foliageColorMultiplier(state, level, pos);
                    bark = properties.getFamily().woodBarkColor;
                }
                tintValues.size(3);
                tintValues.set(0, color);
                tintValues.set(1, 0xFFFFFFFF);
                tintValues.set(2, bark);
            }, leaves);
        }
    }

    /**
     * Falling-leaf particles and Physics-style fractures read {@link BlockColors#getTintSource}, not Fabric's
     * chunk-mesh color callback.
     */
    public static void registerVanillaTintSources() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        BlockColors colors = minecraft.getBlockColors();

        for (SoilProperties soil : SoilProperties.REGISTRY) {
            if (soil.getBlock().isEmpty()) {
                continue;
            }
            SoilBlock roots = soil.getBlock().get();
            colors.register(List.of(
                    BlockTintSources.grass(),
                    new FunctionalBlockTintSource(0xFFFFFF, (state, level, pos) -> roots.rootColor(state, level, pos))
            ), roots);
        }

        PottedSaplingBlock potted = DTRegistries.POTTED_SAPLING.get();
        colors.register(List.of(
                new FunctionalBlockTintSource(0xFFFFFF, (state, level, pos) ->
                        potted.getSpecies(level, pos).saplingColorMultiplier(state, level, pos, 0))
        ), potted);

        for (Species species : Species.REGISTRY) {
            species.getSapling().ifPresent(sapling -> colors.register(List.of(
                    new FunctionalBlockTintSource(0xFFFFFF, (state, level, pos) ->
                            species.saplingColorMultiplier(state, level, pos, 0)),
                    new FunctionalBlockTintSource(0xFFFFFF, (state, level, pos) ->
                            species.saplingColorMultiplier(state, level, pos, 1))
            ), sapling));
        }

        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream()
                .filter(lp -> lp.getDynamicLeavesBlock().isPresent())
                .map(lp -> lp.getDynamicLeavesBlock().get())
                .collect(Collectors.toSet())) {
            colors.register(List.of(
                    new FunctionalBlockTintSource(0x48B518, FabricClientColors::leavesFoliageColor),
                    BlockTintSources.constant(-1),
                    new FunctionalBlockTintSource(0xB3A979, FabricClientColors::leavesBarkColor)
            ), leaves);
        }
    }

    public static int primitiveLeavesColor(BlockState primitiveLeaves, @Nullable BlockGetter level, @Nullable BlockPos pos) {
        BlockTintSource source = Minecraft.getInstance().getBlockColors().getTintSource(primitiveLeaves, 0);
        if (source == null) {
            return 0x48B518;
        }
        if (level instanceof BlockAndTintGetter tintLevel && pos != null) {
            return source.colorInWorld(primitiveLeaves, tintLevel, pos);
        }
        return source.color(primitiveLeaves);
    }

    private static int leavesFoliageColor(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (state.getBlock() instanceof DynamicLeavesBlock leaves) {
            return leaves.getLeavesProperties().foliageColorMultiplier(state, level, pos);
        }
        return 0x48B518;
    }

    private static int leavesBarkColor(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (state.getBlock() instanceof DynamicLeavesBlock leaves) {
            return leaves.getLeavesProperties().getFamily().woodBarkColor;
        }
        return 0xB3A979;
    }

    public static void discoverWoodColors() {
        for (Family family : Family.REGISTRY.getAll()) {
            family.woodRingColor = 0xFFF1AE;
            family.woodBarkColor = 0xB3A979;
            if (family == Family.NULL_FAMILY) {
                continue;
            }
            family.getPrimitiveLog().ifPresent(branch -> {
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(branch);
                Identifier bark = Identifier.fromNamespaceAndPath(blockId.getNamespace(), "block/" + blockId.getPath());
                Identifier rings = bark.withSuffix("_top");
                family.woodBarkColor = averageSpriteColor(bark, family.woodBarkColor);
                family.woodRingColor = averageSpriteColor(rings, family.woodRingColor);
            });
        }
    }

    private static int averageSpriteColor(Identifier spriteId, int fallback) {
        try {
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager()
                    .getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS)
                    .getSprite(spriteId);
            TextureHelper.PixelBuffer buffer = new TextureHelper.PixelBuffer(sprite);
            int u = Math.max(1, buffer.w / 16);
            TextureHelper.PixelBuffer center = new TextureHelper.PixelBuffer(u * 8, u * 8);
            buffer.blit(center, u * -8, u * -8);
            int color = center.averageColor();
            return color == 0 ? fallback : color;
        } catch (Exception e) {
            return fallback;
        }
    }
}
