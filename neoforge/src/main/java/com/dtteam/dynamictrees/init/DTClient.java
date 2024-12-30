package com.dtteam.dynamictrees.init;

import com.dtteam.dynamictrees.DynamicTreesCommon;
import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.client.BlockColorMultipliers;
import com.dtteam.dynamictrees.util.TreeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.function.Function;

@EventBusSubscriber(modid = DynamicTreesCommon.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class DTClient {

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

    public static void setup() {
        registerRenderLayers();
        registerColorHandlers();
        LeavesProperties.postInitClient();
//        BlockColorMultipliers.cleanUp();
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
//        event.registerEntityRenderer(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
//        event.registerEntityRenderer(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
    }

    ///////////////////////////////////////////
    // COLOR HANDLING
    ///////////////////////////////////////////

    @OnlyIn(Dist.CLIENT)
    public static void discoverWoodColors() {

        final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS);

//        for (Family family : Species.REGISTRY.getAll().stream().map(Species::getFamily).distinct().collect(Collectors.toList())) {
//            family.woodRingColor = 0xFFF1AE;
//            family.woodBarkColor = 0xB3A979;
//            if (family != Family.NULL_FAMILY) {
//                family.getPrimitiveLog().ifPresent(branch -> {
//                    BlockState state = branch.defaultBlockState();
//                    family.woodRingColor = getFaceColor(state, Direction.DOWN, bakedTextureGetter);
//                    family.woodBarkColor = getFaceColor(state, Direction.NORTH, bakedTextureGetter);
//                });
//            }
//        }
    }

    @OnlyIn(Dist.CLIENT)
    private static int getFaceColor(BlockState state, Direction face, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
//        final BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
//        List<BakedQuad> quads = model.getQuads(state, face, RandomSource.create(), ModelData.EMPTY, null);
//        if (quads.isEmpty()) // If the quad list is empty, means there is no face on that side, so we try with null.
//        {
//            quads = model.getQuads(state, null, RandomSource.create(), ModelData.EMPTY, null);
//        }
//        if (quads.isEmpty()) { // If null still returns empty, there is nothing we can do so we just warn and exit.
//            LogManager.getLogger().warn("Could not get color of " + face + " side for " + state.getBlock() + "! Branch needs to be handled manually!");
//            return 0;
//        }
//        TextureAtlasSprite sprite = quads.get(0).getSprite();
//        final TextureUtils.PixelBuffer pixelBuffer = new TextureUtils.PixelBuffer(sprite);
//        final int u = pixelBuffer.w / 16;
//        final TextureUtils.PixelBuffer center = new TextureUtils.PixelBuffer(u * 8, u * 8);
//        pixelBuffer.blit(center, u * -8, u * -8);
//
//        return center.averageColor();
        return 0;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerColorResolversEvent(RegisterColorHandlersEvent.ColorResolvers event){
        BlockColorMultipliers.register("birch", (state, level, pos, tintIndex) -> FoliageColor.getBirchColor());
        BlockColorMultipliers.register("spruce", (state, level, pos, tintIndex) -> FoliageColor.getEvergreenColor());
    }

    private static boolean isValidPos(BlockGetter level, BlockPos pos) {
        return level != null && pos != null;
    }

    //todo: come back to this
    private static void registerRenderLayers() {
//        ItemBlockRenderTypes.setRenderLayer(DTRegistries.POTTED_SAPLING.get(), RenderType.cutoutMipped());
//
//        ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof DynamicSaplingBlock || block instanceof RootyBlock || block instanceof FruitBlock || block instanceof PodBlock)
//                .forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped()));

    }

    private static void registerColorHandlers() {
        final int white = 0xFFFFFFFF;
        final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.

        // BLOCKS

//        final BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//
//        // Register Rooty Colorizers
//        for (RootyBlock roots : SoilHelper.getRootyBlocksList()) {
//            blockColors.register((state, level, pos, tintIndex) -> roots.colorMultiplier(blockColors, state, level, pos, tintIndex), roots);
//        }
//
//        // Register Bonsai Pot Colorizer
//        ModelHelper.regColorHandler(DTRegistries.POTTED_SAPLING.get(), (state, level, pos, tintIndex) -> isValidPos(level, pos) && (state.getBlock() instanceof PottedSaplingBlock)
//                ? DTRegistries.POTTED_SAPLING.get().getSpecies(level, pos).saplingColorMultiplier(state, level, pos, tintIndex) : white);
//
//        // ITEMS
//
//        // Register Potion Colorizer
//        ModelHelper.regColorHandler(DTRegistries.DENDRO_POTION.get(), DTRegistries.DENDRO_POTION.get()::getColor);
//
//        // Register Woodland Staff Colorizer
//        ModelHelper.regColorHandler(DTRegistries.STAFF.get(), DTRegistries.STAFF.get()::getColor);
//
//        // TREE PARTS
//
//        // Register Sapling Colorizer
//        for (Species species : Species.REGISTRY) {
//            if (species.getSapling().isPresent()) {
//                ModelHelper.regColorHandler(species.getSapling().get(), (state, level, pos, tintIndex) ->
//                        isValidPos(level, pos) ? species.saplingColorMultiplier(state, level, pos, tintIndex) : white);
//            }
//        }
//
//        // Register Leaves Colorizers
//        for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream().filter(lp -> lp.getDynamicLeavesBlock().isPresent()).map(lp -> lp.getDynamicLeavesBlock().get()).collect(Collectors.toSet())) {
//            ModelHelper.regColorHandler(leaves, (state, level, pos, tintIndex) -> {
//                        final LeavesProperties properties = ((DynamicLeavesBlock) state.getBlock()).getProperties(state);
//                        return TreeHelper.isLeaves(state.getBlock()) ? properties.foliageColorMultiplier(state, level, pos) : magenta;
//                    }
//            );
//        }

    }

    ///////////////////////////////////////////
    // PARTICLES
    ///////////////////////////////////////////

    private static void addDustParticle(Level level, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
        if (level.isClientSide) {
            Particle particle = Minecraft.getInstance().particleEngine.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
            assert particle != null;
            particle.setColor(r, g, b);
        }
    }

    public static void spawnParticles(Level level, SimpleParticleType particleType, BlockPos pos, int numParticles, RandomSource random) {
        spawnParticles(level, particleType, pos.getX(), pos.getY(), pos.getZ(), numParticles, random);
    }

    public static void spawnParticles(LevelAccessor level, SimpleParticleType particleType, int x, int y, int z, int numParticles, RandomSource random) {
        for (int i1 = 0; i1 < numParticles; ++i1) {
            double mx = random.nextGaussian() * 0.02D;
            double my = random.nextGaussian() * 0.02D;
            double mz = random.nextGaussian() * 0.02D;
            DTClient.spawnParticle(level, particleType, x + random.nextFloat(), (double) y + (double) random.nextFloat(), (double) z + random.nextFloat(), mx, my, mz);
        }
    }

    /**
     * Not strictly necessary. But adds a little more isolation to the server for particle effects
     */
    public static void spawnParticle(LevelAccessor level, SimpleParticleType particleType, double x, double y, double z, double mx, double my, double mz) {
        if (level.isClientSide()) {
            level.addParticle(particleType, x, y, z, mx, my, mz);
        }
    }

    private static int getFoliageColor(LeavesProperties leavesProperties, Level level, BlockState blockState, BlockPos pos) {
        return leavesProperties.foliageColorMultiplier(blockState, level, pos);
    }
    public static void crushLeavesBlock(Level level, BlockPos pos, BlockState blockState, Entity entity) {
        if (level.isClientSide) {
            RandomSource random = level.random;
            TreePart treePart = TreeHelper.getTreePart(blockState);
            if (treePart instanceof DynamicLeavesBlock leaves) {
                LeavesProperties leavesProperties = leaves.getProperties();
                int color = getFoliageColor(leavesProperties, level, blockState, pos);
                float r = (color >> 16 & 255) / 255.0F;
                float g = (color >> 8 & 255) / 255.0F;
                float b = (color & 255) / 255.0F;
                for (int dz = 0; dz < 8; dz++) {
                    for (int dy = 0; dy < 8; dy++) {
                        for (int dx = 0; dx < 8; dx++) {
                            if (random.nextInt(8) == 0) {
                                double fx = pos.getX() + dx / 8.0;
                                double fy = pos.getY() + dy / 8.0;
                                double fz = pos.getZ() + dz / 8.0;
                                addDustParticle(level, fx, fy, fz, 0, random.nextFloat() * entity.getDeltaMovement().y, 0, blockState, r, g, b);
                            }
                        }
                    }
                }
            }
        }
    }

}
