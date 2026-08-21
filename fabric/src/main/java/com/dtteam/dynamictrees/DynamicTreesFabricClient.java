package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.season.*;
import com.dtteam.dynamictrees.api.worldgen.*;
import com.dtteam.dynamictrees.block.leaves.*;
import com.dtteam.dynamictrees.block.sapling.*;
import com.dtteam.dynamictrees.block.soil.*;
import com.dtteam.dynamictrees.client.*;
import com.dtteam.dynamictrees.client.TintSources.*;
import com.dtteam.dynamictrees.config.*;
import com.dtteam.dynamictrees.item.*;
import com.dtteam.dynamictrees.model.*;
import com.dtteam.dynamictrees.model.entity.render.FallingTreeRenderer;
import com.dtteam.dynamictrees.model.entity.render.LingeringEffectorRenderer;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.systems.season.*;
import com.dtteam.dynamictrees.tree.*;
import com.dtteam.dynamictrees.tree.family.*;
import com.dtteam.dynamictrees.tree.species.*;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.client.item.v1.*;
import net.fabricmc.fabric.api.client.model.loading.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.*;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
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
        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID, ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);
        SpriteSourceRegistry.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.CODEC);
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
                registerBlockColors();
                BlockColorMultipliers.cleanUp();
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
        BlockColorMultipliers.register("birch", BlockTintSources.constant(FoliageColor.FOLIAGE_BIRCH));
        BlockColorMultipliers.register("spruce", BlockTintSources.constant(FoliageColor.FOLIAGE_EVERGREEN));

        // Item tint sources are now looked up by id from the item model JSON, mirroring
        // NeoForge's RegisterColorHandlersEvent.ItemTintSources handling in ClientModEventHandler.
        ItemTintSources.ID_MAPPER.put(DynamicTrees.location("dendro_potion"), DendroPotionItemTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(DynamicTrees.location("staff_handle"), StaffHandleItemTintSource.MAP_CODEC);
        ItemTintSources.ID_MAPPER.put(DynamicTrees.location("staff_crystal"), StaffCrystalItemTintSource.MAP_CODEC);
    }

    public static void registerBlockColors() {
        final BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        // Register Rooty Soils Tint Sources
        SoilProperties.REGISTRY.getAll().stream().map(SoilProperties::getBlock).flatMap(Optional::stream)
                .forEach(soilBlock -> {
                    SoilProperties properties = soilBlock.getSoilProperties();
                    List<BlockTintSource> sources = CloneTintSource.cloneAllSources(
                            blockColors,
                            () -> properties.getPrimitiveSoilState(soilBlock.defaultBlockState()),
                            properties.getFoliageTintLayerCount());
                    sources.add(new SoilRootsTintSource(soilBlock));
                    BlockColorRegistry.register(sources, soilBlock);
                });

        // Register Leaves Tint Sources
        LeavesProperties.REGISTRY.getAll().stream().map(LeavesProperties::getDynamicLeavesBlock).flatMap(Optional::stream)
                .forEach(leaves -> {
                    LeavesProperties properties = leaves.getLeavesProperties();
                    if (properties.hasCustomColor()) {
                        Integer customColor = properties.getCustomColor();
                        if (customColor == null) //we use null as a way to default back to "biome" index source.
                            BlockColorRegistry.register(List.of(BlockTintSources.foliage()), leaves);
                        else
                            BlockColorRegistry.register(List.of(BlockTintSources.constant(customColor)), leaves);
                    } else {
                        BlockColorRegistry.register(CloneTintSource.cloneAllSources(blockColors, properties::getPrimitiveLeaves, properties.getFoliageTintLayerCount()), leaves);
                    }
                });

        // Register Potted Sapling Tint Sources
        BlockColorRegistry.register(List.of(new PottedSaplingTintSource(blockColors)), DTRegistries.POTTED_SAPLING.get());

        // Register Sapling Tint Sources
        Species.REGISTRY.getAll().stream().map(Species::getSapling).flatMap(Optional::stream)
                .forEach(sapling -> {
                    Species species = sapling.getSpecies();
                    BlockColorRegistry.register(List.of(
                            //Leaves for the leaves
                            new SaplingTintSource(blockColors, species),
                            //Mangrove saplings have roots so we provide a branch tint
                            new SuppliedConstantTintSource(() -> species.getFamily().woodBarkColor)
                    ), sapling);
                });
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
        ClientTickEvents.START_LEVEL_TICK.register(level -> {
            SeasonHelper.updateTick(level, level.getDefaultClockTime());
        });
    }

    public static void discoverWoodColors() {
        for (Family family : Family.REGISTRY.getAll()) {
            family.woodRingColor = 0xFFF1AE;
            family.woodBarkColor = 0xB3A979;
            if (family != Family.NULL_FAMILY) {
                family.getPrimitiveLog().ifPresent(branch -> {
                    BlockState state = branch.defaultBlockState();
                    family.woodRingColor = getFaceColor(state, Direction.DOWN);
                    family.woodBarkColor = getFaceColor(state, Direction.NORTH);
                });
            }
        }
    }

    private static int getFaceColor(BlockState state, Direction face) {
        final BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        List<BakedQuad> quads = getQuads(state, face, model);
        if (quads == null) return 0;
        TextureAtlasSprite sprite = quads.getFirst().materialInfo().sprite();
        final TextureHelper.PixelBuffer pixelBuffer = new TextureHelper.PixelBuffer(sprite);
        final int u = pixelBuffer.w / 16;
        final TextureHelper.PixelBuffer center = new TextureHelper.PixelBuffer(u * 8, u * 8);
        pixelBuffer.blit(center, u * -8, u * -8);

        return center.averageColor();
    }

    private static List<BakedQuad> getQuads(BlockState state, Direction face, BlockStateModel model) {
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);

        if (parts.isEmpty()) { // No parts? empty model
            DynamicTrees.LOG.warn("Could not get any color from {}, model is empty! Branch color needs to be handled manually.", state.getBlock());
            return null;
        }
        //We only care about the first, we assume these are all regular blocks
        List<BakedQuad> quads = parts.getFirst().getQuads(face);
        if (quads.isEmpty()) // If the quad list is empty, means there is no face on that side, so we try with null.
        {
            quads = parts.getFirst().getQuads(null);
        }
        if (quads.isEmpty()) { // If null still returns empty, there is nothing we can do so we just warn and exit.
            DynamicTrees.LOG.warn("Could not get color of {} side for {}! Branch color needs to be handled manually.", face, state.getBlock());
            return null;
        }
        return quads;
    }
}
