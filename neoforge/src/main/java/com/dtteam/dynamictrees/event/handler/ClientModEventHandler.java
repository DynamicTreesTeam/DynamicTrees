package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.sapling.DynamicSaplingBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.client.TextureHelper;
import com.dtteam.dynamictrees.client.ThickBranchRingsSource;
import com.dtteam.dynamictrees.client.TintSources.CloneTintSource;
import com.dtteam.dynamictrees.client.TintSources.PottedSaplingTintSource;
import com.dtteam.dynamictrees.client.TintSources.SaplingTintSource;
import com.dtteam.dynamictrees.client.TintSources.SoilRootsTintSource;
import com.dtteam.dynamictrees.model.blockstate.UnbakedBranchModel;
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
import java.util.stream.Collectors;

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
//        // Register Potion Colorizer
//        event.register(DynamicTrees.location("dendro_potion"), DTRegistries.DENDRO_POTION.get()::getColor);
//        // Register Woodland Staff Colorizer
//        event.register(DynamicTrees.location("staff"), DTRegistries.STAFF.get()::getColor);
    }

    @SubscribeEvent
    public static void registerBlockColorHandlersEvent(RegisterColorHandlersEvent.BlockTintSources event){
        BlockColors blockColors = event.getBlockColors();

        // Register Rooty Colorizers
        for (SoilBlock soilBlock : SoilProperties.REGISTRY.getAll().stream().filter(sp -> sp.getBlock().isPresent()).map(sp -> sp.getBlock().get()).collect(Collectors.toSet())) {
            SoilProperties properties = soilBlock.getSoilProperties();
            List<BlockTintSource> sources = CloneTintSource.cloneAllSources(
                    blockColors,
                    ()->properties.getPrimitiveSoilState(soilBlock.defaultBlockState()),
                    properties.getFoliageTintLayerCount());
            sources.add(new SoilRootsTintSource(soilBlock));
            event.register(sources, soilBlock);
        }

        // Register Leaves Colorizers
        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream().filter(lp -> lp.getDynamicLeavesBlock().isPresent()).map(lp -> lp.getDynamicLeavesBlock().get()).collect(Collectors.toSet())) {
            LeavesProperties properties = leaves.getLeavesProperties();
            if (properties.hasCustomColor()) {
                Integer customColor = properties.getCustomColor();
                if (customColor == null) //we use null as a way to default back to "biome" tint source.
                    event.register(List.of(BlockTintSources.foliage()), leaves);
                else
                    event.register(List.of(BlockTintSources.constant(customColor)), leaves);
            } else {
                event.register(
                        CloneTintSource.cloneAllSources(blockColors, properties::getPrimitiveLeaves, properties.getFoliageTintLayerCount()),
                        leaves);
            }
        }

        // Register Bonsai Pot Colorizer
        event.register(List.of(new PottedSaplingTintSource(blockColors)), DTRegistries.POTTED_SAPLING.get());

        // Register Sapling Colorizer
        for (DynamicSaplingBlock sapling : Species.REGISTRY.getAll().stream().filter(s -> s.getSapling().isPresent()).map(s -> s.getSapling().get()).collect(Collectors.toSet())) {
            event.register(List.of(new SaplingTintSource(blockColors, sapling.getSpecies())), sapling);
        }

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
    public static final Identifier THICK_BRANCH = DynamicTrees.location("thick_branch");
    public static final Identifier SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final Identifier ROOTS = DynamicTrees.location("roots");
    public static final Identifier LARGE_PALM_FRONDS = DynamicTrees.location("large_palm_fronds");
    public static final Identifier MEDIUM_PALM_FRONDS = DynamicTrees.location("medium_palm_fronds");
    public static final Identifier SMALL_PALM_FRONDS = DynamicTrees.location("small_palm_fronds");

    @SubscribeEvent
    public static void onModelRegistryEvent(RegisterBlockStateModels event) {
        // Register model loaders for baked models.
        event.registerModel(BRANCH, UnbakedBranchModel.CODEC);
//        event.register(SURFACE_ROOT, new SurfaceRootBlockModelLoader());
//        event.register(ROOTS, new RootsBlockModelLoader());
//        event.register(LARGE_PALM_FRONDS, new PalmLeavesModelLoader(0));
//        event.register(MEDIUM_PALM_FRONDS, new PalmLeavesModelLoader(1));
//        event.register(SMALL_PALM_FRONDS, new PalmLeavesModelLoader(2));
    }

//    @SubscribeEvent
//    public static void onModelModifyBakingResultResult(ModelEvent.ModifyBakingResult event) {
//        // Put bonsai pot baked model into its model location.
//        event.getBakingResult().blockStateModels().computeIfPresent(new ModelIdentifier(DynamicTrees.location("potted_sapling"), ""), (k, val) -> new BakedModelBlockPottedSapling(val));
//    }

    @SubscribeEvent
    public static void stitchTextureAtlas(RegisterSpriteSourcesEvent event) {
        event.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.CODEC);
    }

}
