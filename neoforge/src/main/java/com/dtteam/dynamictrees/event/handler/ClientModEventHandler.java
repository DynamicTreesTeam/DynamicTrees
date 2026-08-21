package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import com.dtteam.dynamictrees.client.ThickBranchRingsSource;
import com.dtteam.dynamictrees.client.tint.DendroPotionTintSource;
import com.dtteam.dynamictrees.client.tint.FunctionalBlockTintSource;
import com.dtteam.dynamictrees.client.tint.StaffTintSource;
import com.dtteam.dynamictrees.entity.render.FallingTreeRenderer;
import com.dtteam.dynamictrees.entity.render.LingeringEffectorRenderer;
import com.dtteam.dynamictrees.model.NeoForgeModelPlugin;
import com.dtteam.dynamictrees.model.loader.DummyUnbakedModelLoader;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, value = Dist.CLIENT)
public class ClientModEventHandler {

    public static final Identifier BRANCH = DynamicTrees.location("branch");
    public static final Identifier THICK_BRANCH = DynamicTrees.location("thick_branch");
    public static final Identifier SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final Identifier ROOTS = DynamicTrees.location("roots");
    public static final Identifier LARGE_PALM_FRONDS = DynamicTrees.location("large_palm_fronds");
    public static final Identifier MEDIUM_PALM_FRONDS = DynamicTrees.location("medium_palm_fronds");
    public static final Identifier SMALL_PALM_FRONDS = DynamicTrees.location("small_palm_fronds");

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
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS).getSprite(spriteId);
        if (sprite == null) {
            return fallback;
        }
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        int count = 0;
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = sprite.getPixelRGBA(0, x, y);
                int a = (pixel >> 24) & 0xFF;
                if (a < 16) {
                    continue;
                }
                sumR += pixel & 0xFF;
                sumG += (pixel >> 8) & 0xFF;
                sumB += (pixel >> 16) & 0xFF;
                count++;
            }
        }
        if (count == 0) {
            return fallback;
        }
        return 0xFF000000 | ((sumB / count) << 16) | ((sumG / count) << 8) | (sumR / count);
    }

    @SubscribeEvent
    public static void registerColorResolversEvent(RegisterColorHandlersEvent.ColorResolvers event) {
        BlockColorMultipliers.register("birch", (state, level, pos, tintIndex) -> FoliageColor.FOLIAGE_BIRCH);
        BlockColorMultipliers.register("spruce", (state, level, pos, tintIndex) -> FoliageColor.FOLIAGE_EVERGREEN);
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(DynamicTrees.location("dendro_potion"), DendroPotionTintSource.MAP_CODEC);
        event.register(DynamicTrees.location("staff"), StaffTintSource.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        for (SoilProperties soil : SoilProperties.REGISTRY) {
            if (soil.getBlock().isEmpty()) {
                continue;
            }
            SoilBlock roots = soil.getBlock().get();
            event.register(List.of(
                    new FunctionalBlockTintSource(0xFFFFFF, (state, level, pos) -> BlockTintSources.grass().colorInWorld(state, level, pos)),
                    new FunctionalBlockTintSource(0xFFFFFF, (state, level, pos) -> roots.rootColor(state, level, pos))
            ), roots);
        }

        event.register(List.of(new FunctionalBlockTintSource(0xFFFFFF, ClientModEventHandler::pottedSaplingColor)),
                DTRegistries.POTTED_SAPLING.get());

        for (Species species : Species.REGISTRY) {
            species.getSapling().ifPresent(sapling ->
                    event.register(List.of(new FunctionalBlockTintSource(0xFFFFFF,
                            (state, level, pos) -> species.saplingColorMultiplier(state, level, pos, 0))), sapling));
        }

        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream()
                .filter(lp -> lp.getDynamicLeavesBlock().isPresent())
                .map(lp -> lp.getDynamicLeavesBlock().get())
                .collect(Collectors.toSet())) {
            event.register(List.of(
                    new FunctionalBlockTintSource(0x48B518, ClientModEventHandler::leavesFoliageColor),
                    BlockTintSources.constant(-1),
                    new FunctionalBlockTintSource(0xB3A979, ClientModEventHandler::leavesBarkColor)
            ), leaves);
        }
    }

    private static int pottedSaplingColor(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        if (state.getBlock() instanceof PottedSaplingBlock) {
            return DTRegistries.POTTED_SAPLING.get().getSpecies(level, pos).saplingColorMultiplier(state, level, pos, 0);
        }
        return 0xFFFFFF;
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

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
        event.registerEntityRenderer(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
    }

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelEvent.RegisterLoaders event) {
        event.register(BRANCH, DummyUnbakedModelLoader.INSTANCE);
        event.register(SURFACE_ROOT, DummyUnbakedModelLoader.INSTANCE);
        event.register(THICK_BRANCH, DummyUnbakedModelLoader.INSTANCE);
        event.register(ROOTS, DummyUnbakedModelLoader.INSTANCE);
        event.register(LARGE_PALM_FRONDS, DummyUnbakedModelLoader.INSTANCE);
        event.register(MEDIUM_PALM_FRONDS, DummyUnbakedModelLoader.INSTANCE);
        event.register(SMALL_PALM_FRONDS, DummyUnbakedModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void onModelModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        NeoForgeModelPlugin.modifyBakingResult(event);
    }

    @SubscribeEvent
    public static void registerSpriteSources(RegisterSpriteSourcesEvent event) {
        event.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.CODEC);
    }
}
