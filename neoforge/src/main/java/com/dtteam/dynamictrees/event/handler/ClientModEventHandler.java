package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.soil.RootyBlock;
import com.dtteam.dynamictrees.block.soil.SoilHelper;
import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import com.dtteam.dynamictrees.entity.render.FallingTreeRenderer;
import com.dtteam.dynamictrees.entity.render.LingeringEffectorRenderer;
import com.dtteam.dynamictrees.models.baked.BakedModelBlockPottedSapling;
import com.dtteam.dynamictrees.models.loader.*;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.TreeHelper;
import com.dtteam.dynamictrees.util.client.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientModEventHandler {

    //TODO: thick ring stitching
//    public static void clientStart() {
//		FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, setupEvent -> {
//			IResourceManager manager = Minecraft.getInstance().getResourceManager();
//			if (manager instanceof IReloadableResourceManager){
//				ThickRingTextureManager.uploader = new ThickRingSpriteUploader(Minecraft.getInstance().textureManager);
//				((IReloadableResourceManager) manager).addReloadListener(ThickRingTextureManager.uploader);
//			}
//		});
//    }

//    public static void registerClientEventHandlers() {
//        MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
//        MinecraftForge.EVENT_BUS.register(TextureGenerationHandler.class);
//    }

    ///////////////////////////////////////////
    // COLOR HANDLING
    ///////////////////////////////////////////

    @OnlyIn(Dist.CLIENT)
    public static void discoverWoodColors() {

        final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter = Minecraft.getInstance()
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

    @OnlyIn(Dist.CLIENT)
    private static int getFaceColor(BlockState state, Direction face, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        final BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        List<BakedQuad> quads = model.getQuads(state, face, RandomSource.create(), ModelData.EMPTY, null);
        if (quads.isEmpty()) // If the quad list is empty, means there is no face on that side, so we try with null.
        {
            quads = model.getQuads(state, null, RandomSource.create(), ModelData.EMPTY, null);
        }
        if (quads.isEmpty()) { // If null still returns empty, there is nothing we can do so we just warn and exit.
            DynamicTrees.LOG.warn("Could not get color of {} side for {}! Branch needs to be handled manually!", face, state.getBlock());
            return 0;
        }
        TextureAtlasSprite sprite = quads.getFirst().getSprite();
        final TextureUtils.PixelBuffer pixelBuffer = new TextureUtils.PixelBuffer(sprite);
        final int u = pixelBuffer.w / 16;
        final TextureUtils.PixelBuffer center = new TextureUtils.PixelBuffer(u * 8, u * 8);
        pixelBuffer.blit(center, u * -8, u * -8);

        return center.averageColor();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerColorResolversEvent(RegisterColorHandlersEvent.ColorResolvers event){
        BlockColorMultipliers.register("birch", (state, level, pos, tintIndex) -> FoliageColor.getBirchColor());
        BlockColorMultipliers.register("spruce", (state, level, pos, tintIndex) -> FoliageColor.getEvergreenColor());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerItemColorHandlersEvent(RegisterColorHandlersEvent.Item event){
        // Register Potion Colorizer
        event.register(DTRegistries.DENDRO_POTION.get()::getColor, DTRegistries.DENDRO_POTION.get());
        // Register Woodland Staff Colorizer
        event.register(DTRegistries.STAFF.get()::getColor, DTRegistries.STAFF.get());
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerBlockColorHandlersEvent(RegisterColorHandlersEvent.Block event){
        final int white = 0xFFFFFFFF;
        final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.

        // Register Rooty Colorizers
        for (RootyBlock roots : SoilHelper.getRootyBlocksList()) {
            event.register((state, level, pos, tintIndex) -> roots.colorMultiplier(event.getBlockColors(), state, level, pos, tintIndex), roots);
            //Unfortunately there's no way around this. We do not own the models to set the renderType there.
            ItemBlockRenderTypes.setRenderLayer(roots, RenderType.cutoutMipped());
        }

        // Register Bonsai Pot Colorizer
        event.register((state, level, pos, tintIndex) -> isValidPos(level, pos) && (state.getBlock() instanceof PottedSaplingBlock)
                ? DTRegistries.POTTED_SAPLING.get().getSpecies(level, pos).saplingColorMultiplier(state, level, pos, tintIndex) : white,
                DTRegistries.POTTED_SAPLING.get());

        // Register Sapling Colorizer
        for (Species species : Species.REGISTRY) {
            if (species.getSapling().isPresent()) {
                event.register((state, level, pos, tintIndex) ->
                                isValidPos(level, pos) ?
                                        species.saplingColorMultiplier(state, level, pos, tintIndex) : white,
                        species.getSapling().get());
            }
        }
        // Register Leaves Colorizers
        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream().filter(lp -> lp.getDynamicLeavesBlock().isPresent()).map(lp -> lp.getDynamicLeavesBlock().get()).collect(Collectors.toSet())) {
            event.register((state, level, pos, tintIndex) ->
                            isValidPos(level, pos) && TreeHelper.isLeaves(state.getBlock()) ?
                                    ((DynamicLeavesBlock) state.getBlock()).getProperties().foliageColorMultiplier(state, level, pos) : magenta,
                    leaves);
        }
    }

    private static boolean isValidPos(BlockGetter level, BlockPos pos) {
        return level != null && pos != null;
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
    public static final ResourceLocation BRANCH = DynamicTrees.location("branch");
    public static final ResourceLocation THICK_BRANCH = DynamicTrees.location("thick_branch");
    public static final ResourceLocation SURFACE_ROOT = DynamicTrees.location("surface_root");
    public static final ResourceLocation ROOTS = DynamicTrees.location("roots");
    public static final ResourceLocation LARGE_PALM_FRONDS = DynamicTrees.location("large_palm_fronds");
    public static final ResourceLocation MEDIUM_PALM_FRONDS = DynamicTrees.location("medium_palm_fronds");
    public static final ResourceLocation SMALL_PALM_FRONDS = DynamicTrees.location("small_palm_fronds");

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        // Register model loaders for baked models.
        event.register(BRANCH, new BranchBlockModelLoader());
        event.register(SURFACE_ROOT, new SurfaceRootBlockModelLoader());
        event.register(THICK_BRANCH, new ThickBranchBlockModelLoader());
        event.register(ROOTS, new RootsBlockModelLoader());
        event.register(LARGE_PALM_FRONDS, new PalmLeavesModelLoader(0));
        event.register(MEDIUM_PALM_FRONDS, new PalmLeavesModelLoader(1));
        event.register(SMALL_PALM_FRONDS, new PalmLeavesModelLoader(2));
    }

    @SubscribeEvent
    public static void onModelModifyBakingResultResult(ModelEvent.ModifyBakingResult event) {
        // Put bonsai pot baked model into its model location.
        event.getModels().computeIfPresent(new ModelResourceLocation(DynamicTrees.location("potted_sapling"), ""), (k, val) -> new BakedModelBlockPottedSapling(val));
    }

}
