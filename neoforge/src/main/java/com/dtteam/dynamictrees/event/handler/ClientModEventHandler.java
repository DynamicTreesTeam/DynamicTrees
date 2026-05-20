package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.TextureHelper;
import com.dtteam.dynamictrees.client.ThickBranchRingsSource;
import com.dtteam.dynamictrees.client.TintSources.*;
import com.dtteam.dynamictrees.model.blockstate.*;
import com.dtteam.dynamictrees.model.entity.render.FallingTreeRenderer;
import com.dtteam.dynamictrees.model.entity.render.LingeringEffectorRenderer;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, value = Dist.CLIENT)
public class ClientModEventHandler {

    ///////////////////////////////////////////
    // COLOR HANDLING
    ///////////////////////////////////////////

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
        model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RandomSource.create(), parts);

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

    @SubscribeEvent
    public static void registerItemColorHandlersEvent(RegisterColorHandlersEvent.ItemTintSources event){
        // Register Potion Tint Source
        event.register(DynamicTrees.location("dendro_potion"), DendroPotionItemTintSource.MAP_CODEC);
        // Register Woodland Staff Tint Sources
        event.register(DynamicTrees.location("staff_handle"), StaffHandleItemTintSource.MAP_CODEC);
        event.register(DynamicTrees.location("staff_crystal"), StaffCrystalItemTintSource.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlersEvent(RegisterColorHandlersEvent.BlockTintSources event){
        BlockColors blockColors = event.getBlockColors();

        // Register Rooty Soils Tint Sources
        SoilProperties.REGISTRY.getAll().stream().map(SoilProperties::getBlock).flatMap(Optional::stream)
                .forEach(soilBlock -> {
                    SoilProperties properties = soilBlock.getSoilProperties();
                    List<BlockTintSource> sources = CloneTintSource.cloneAllSources(
                            blockColors,
                            ()->properties.getPrimitiveSoilState(soilBlock.defaultBlockState()),
                            properties.getFoliageTintLayerCount());
                    sources.add(new SoilRootsTintSource(soilBlock));
                    event.register(sources, soilBlock);
                });

        // Register Leaves Tint Sources
        LeavesProperties.REGISTRY.getAll().stream().map(LeavesProperties::getDynamicLeavesBlock).flatMap(Optional::stream)
                .forEach(leaves -> {
                    LeavesProperties properties = leaves.getLeavesProperties();
                    if (properties.hasCustomColor()) {
                        Integer customColor = properties.getCustomColor();
                        if (customColor == null) //we use null as a way to default back to "biome" index source.
                            event.register(List.of(BlockTintSources.foliage()), leaves);
                        else
                            event.register(List.of(BlockTintSources.constant(customColor)), leaves);
                    } else {
                        event.register(
                                CloneTintSource.cloneAllSources(blockColors, properties::getPrimitiveLeaves, properties.getFoliageTintLayerCount()),
                                leaves);
                    }
                });

        // Register Potted Sapling Tint Sources
        event.register(List.of(new PottedSaplingTintSource(blockColors)), DTRegistries.POTTED_SAPLING.get());

        // Register Sapling TintSources
        Species.REGISTRY.getAll().stream().map(Species::getSapling).flatMap(Optional::stream)
                .forEach(sapling ->
                        event.register(List.of(new SaplingTintSource(blockColors, sapling.getSpecies())), sapling)
                );

    }

    ///////////////////////////////////////////
    // ENTITY
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
        event.registerEntityRenderer(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
    }

    ///////////////////////////////////////////
    // BAKED MODEL
    ///////////////////////////////////////////

    //These locs are accessed by the model data generators
    public static final Identifier BRANCH = DynamicTrees.location("branch");
    public static final Identifier SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final Identifier ROOTS = DynamicTrees.location("roots");
    public static final Identifier CREAKING_HEART = DynamicTrees.location("creaking_heart");
    public static final Identifier POTTED_DYNAMIC_SAPLING = DynamicTrees.location("potted_dynamic_sapling");
    public static final Identifier AERIAL_ROOTS_SOIL = DynamicTrees.location("aerial_roots_soil");
    public static final Identifier ROOTS_MOSS = DynamicTrees.location("roots_moss");
    public static final Identifier LARGE_PALM_FRONDS = DynamicTrees.location("large_palm_fronds");
    public static final Identifier MEDIUM_PALM_FRONDS = DynamicTrees.location("medium_palm_fronds");
    public static final Identifier SMALL_PALM_FRONDS = DynamicTrees.location("small_palm_fronds");

    @SubscribeEvent
    public static void onModelRegistryEvent(RegisterBlockStateModels event) {
        // Register model loaders for baked models.
        event.registerModel(BRANCH, UnbakedBranchModel.CODEC);
        event.registerModel(ROOTS, UnbakedRootsModel.CODEC);
        event.registerModel(CREAKING_HEART, UnbakedCreakingHeartModel.CODEC);
        event.registerModel(SURFACE_ROOT, SurfaceRootBlockStateModel.Unbaked.CODEC);
        event.registerModel(POTTED_DYNAMIC_SAPLING, PottedSaplingBlockStateModel.Unbaked.CODEC);
        event.registerModel(AERIAL_ROOTS_SOIL, AerialRootsSoilBlockStateModel.Unbaked.CODEC);
        event.registerModel(ROOTS_MOSS, UnbakedRootsMossModel.CODEC);
//        event.register(LARGE_PALM_FRONDS, new PalmLeavesModelLoader(0));
//        event.register(MEDIUM_PALM_FRONDS, new PalmLeavesModelLoader(1));
//        event.register(SMALL_PALM_FRONDS, new PalmLeavesModelLoader(2));
    }

    @SubscribeEvent
    public static void stitchTextureAtlas(RegisterSpriteSourcesEvent event) {
        event.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.CODEC);
    }

}
