package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.season.*;
import com.dtteam.dynamictrees.api.worldgen.*;
import com.dtteam.dynamictrees.block.branch.*;
import com.dtteam.dynamictrees.block.leaves.*;
import com.dtteam.dynamictrees.block.sapling.*;
import com.dtteam.dynamictrees.block.soil.*;
import com.dtteam.dynamictrees.client.*;
import com.dtteam.dynamictrees.config.*;
import com.dtteam.dynamictrees.entity.render.*;
import com.dtteam.dynamictrees.item.*;
import com.dtteam.dynamictrees.model.*;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.systems.season.*;
import com.dtteam.dynamictrees.tree.*;
import com.dtteam.dynamictrees.tree.family.*;
import com.dtteam.dynamictrees.tree.species.*;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.*;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.item.v1.*;
import net.fabricmc.fabric.api.client.model.loading.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.impl.client.rendering.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.neoforged.fml.config.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class DynamicTreesFabricClient implements ClientModInitializer {

    private static boolean initialized = false;
    private static boolean woodColorsDiscovered = false;

    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID, ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);
        AtlasSourceTypeRegistryImpl.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.setType(ThickBranchRingsSource.CODEC));
        registerModelLoaders();
        registerEntityRenderers();
        registerColorHandlers();
        registerTooltipCallback();
        registerClientTick();
        registerClientWorldLoad();
    }

    private void registerClientWorldLoad() {

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!initialized && client.level != null) {
                discoverWoodColors();
                LeavesProperties.postInitClient();
                BlockColorMultipliers.cleanUp();
                registerBlockColors();
                initialized = true;
            }
        });
    }

    private void registerModelLoaders() {
        ModelLoadingPlugin.register(new DTModelLoadingPlugin());
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.register(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
        EntityRendererRegistry.register(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
    }

    private void registerColorHandlers() {
        BlockColorMultipliers.register("birch", (state, level, pos, tintIndex) -> FoliageColor.getBirchColor());
        BlockColorMultipliers.register("spruce", (state, level, pos, tintIndex) -> FoliageColor.getEvergreenColor());

        ColorProviderRegistry.ITEM.register(DTRegistries.DENDRO_POTION.get()::getColor, DTRegistries.DENDRO_POTION.get());
        ColorProviderRegistry.ITEM.register(DTRegistries.STAFF.get()::getColor, DTRegistries.STAFF.get());
    }

    public static void registerBlockColors() {
        final int white = 0xFFFFFFFF;
        final int magenta = 0x00FF00FF;
        final var blockColors = Minecraft.getInstance().getBlockColors();

        for (SoilProperties soil : SoilProperties.REGISTRY) {
            if (soil.getBlock().isEmpty()) continue;
            SoilBlock roots = soil.getBlock().get();
            ColorProviderRegistry.BLOCK.register(
                    (state, level, pos, tintIndex) -> roots.colorMultiplier(blockColors, state, level, pos, tintIndex),
                    roots
            );
            BlockRenderLayerMap.INSTANCE.putBlock(roots, RenderType.cutoutMipped());
        }

        for (Family family : Family.REGISTRY.getAll()) {
            if (family instanceof UndergroundRootsFamily rootsFamily) {
                rootsFamily.getRoots().ifPresent(roots ->
                    BlockRenderLayerMap.INSTANCE.putBlock(roots, RenderType.cutoutMipped())
                );
            }
        }


        ColorProviderRegistry.BLOCK.register(
                (state, level, pos, tintIndex) -> isValidPos(level, pos) && (state.getBlock() instanceof PottedSaplingBlock)
                        ? DTRegistries.POTTED_SAPLING.get().getSpecies(level, pos).saplingColorMultiplier(state, level, pos, tintIndex) : white,
                DTRegistries.POTTED_SAPLING.get()
        );

        for (Species species : Species.REGISTRY) {
            if (species.getSapling().isPresent()) {
                ColorProviderRegistry.BLOCK.register(
                        (state, level, pos, tintIndex) -> isValidPos(level, pos)
                                ? species.saplingColorMultiplier(state, level, pos, tintIndex) : white,
                        species.getSapling().get()
                );
            }
            species.getSapling().ifPresent(sapling -> BlockRenderLayerMap.INSTANCE.putBlock(sapling, RenderType.cutoutMipped()));
            if(species.hasFruits()){
                species.getFruits().forEach(fruit ->
                        BlockRenderLayerMap.INSTANCE.putBlock(fruit.getBlock(), RenderType.cutoutMipped())
                );
            }
            if(species.hasPods()){
                species.getPods().forEach(pod ->
                        BlockRenderLayerMap.INSTANCE.putBlock(pod.getBlock(), RenderType.cutoutMipped())
                );
            }
        }

        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream()
                .filter(lp -> lp.getDynamicLeavesBlock().isPresent())
                .map(lp -> lp.getDynamicLeavesBlock().get())
                .collect(Collectors.toSet())) {
            ColorProviderRegistry.BLOCK.register(
                    (state, level, pos, tintIndex) -> isValidPos(level, pos) && TreeHelper.isLeaves(state.getBlock())
                            ? ((DynamicLeavesBlock) state.getBlock()).getLeavesProperties().foliageColorMultiplier(state, level, pos) : magenta,
                    leaves
            );
        }
    }

    private static boolean isValidPos(BlockGetter level, BlockPos pos) {
        return level != null && pos != null;
    }

    private void registerTooltipCallback() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Item item = stack.getItem();
            if (!(item instanceof Seed seed)) {
                return;
            }

            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            LevelContext levelContext = LevelContext.create(player.level());
            Species species = seed.getSpecies();
            if(!species.isValid()) {
                return;
            }
            if (SeasonHelper.getSeasonValue(levelContext, BlockPos.ZERO) == null ) {
                return;
            }

            BlockPos playerPos = BlockPos.containing(player.position());
            ClimateZoneType climate = ClimateHelper.getClimate(player.level(), playerPos);
            int flags = seed.getSpecies().getSeasonalTooltipFlags(levelContext, player);
            Tooltips.applySeasonalTooltips(lines, flags, climate);
        });
    }

    private void registerClientTick() {
        ClientTickEvents.START_WORLD_TICK.register(level -> {
            SeasonHelper.updateTick(level, level.getDayTime());
        });
    }

    public static void discoverWoodColors() {
        final Function<Identifier, TextureAtlasSprite> bakedTextureGetter = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

        for (Family family : Family.REGISTRY.getAll()) {
            family.woodRingColor = 0xFFF1AE;
            family.woodBarkColor = 0xB3A979;
            if (family != Family.NULL_FAMILY) {
                family.getPrimitiveLog().ifPresent(branch -> {
                    BlockState state = branch.defaultBlockState();
                    family.woodRingColor = getFaceColor(state, Direction.DOWN, bakedTextureGetter);
                    family.woodBarkColor = getFaceColor(state, Direction.NORTH, bakedTextureGetter);
                });
            }
        }
    }

    private static int getFaceColor(BlockState state, Direction face, Function<Identifier, TextureAtlasSprite> textureGetter) {
        final BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        if (model == null) {
            DynamicTrees.LOG.warn("Could not get model for {}! Branch needs to be handled manually!", state.getBlock());
            return 0;
        }
        List<BakedQuad> quads = model.getQuads(state, face, RandomSource.create());
        if (quads.isEmpty()) {
            quads = model.getQuads(state, null, RandomSource.create());
        }
        if (quads.isEmpty()) {
            DynamicTrees.LOG.warn("Could not get color of {} side for {}! Branch needs to be handled manually!", face, state.getBlock());
            return 0;
        }
        TextureAtlasSprite sprite = quads.getFirst().getSprite();
        final TextureHelper.PixelBuffer pixelBuffer = new TextureHelper.PixelBuffer(sprite);
        final int u = pixelBuffer.w / 16;
        final TextureHelper.PixelBuffer center = new TextureHelper.PixelBuffer(u * 8, u * 8);
        pixelBuffer.blit(center, u * -8, u * -8);

        return center.averageColor();
    }
}
