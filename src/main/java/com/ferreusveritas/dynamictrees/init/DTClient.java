package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.client.ModelHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.DynamicSaplingBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.ferreusveritas.dynamictrees.client.TextureUtils;
import com.ferreusveritas.dynamictrees.entities.render.FallingTreeRenderer;
import com.ferreusveritas.dynamictrees.entities.render.LingeringEffectorRenderer;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DTClient {

	//TODO: thick ring stitching
	public static void clientStart() {
//		FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, setupEvent -> {
//			IResourceManager manager = Minecraft.getInstance().getResourceManager();
//			if (manager instanceof IReloadableResourceManager){
//				ThickRingTextureManager.uploader = new ThickRingSpriteUploader(Minecraft.getInstance().textureManager);
//				((IReloadableResourceManager) manager).addReloadListener(ThickRingTextureManager.uploader);
//			}
//		});
	}

	public static void setup() {

		registerRenderLayers();
		registerJsonColorMultipliers();
		registerEntityRenderers();
		
		registerColorHandlers();
//		MinecraftForge.EVENT_BUS.register(BlockBreakAnimationClientHandler.instance);
		
		LeavesProperties.postInitClient();
		cleanup();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void discoverWoodColors() {

		final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter = Minecraft.getInstance()
				.getTextureAtlas(PlayerContainer.BLOCK_ATLAS);
		
		for (Family family : Species.REGISTRY.getAll().stream().map(Species::getFamily).distinct().collect(Collectors.toList())) {
			family.woodRingColor = 0xFFF1AE;
			family.woodBarkColor = 0xB3A979;
			if (family != Family.NULL_FAMILY) {
				BlockState state = family.getPrimitiveLog().defaultBlockState();
				if (state.getBlock() != Blocks.AIR) {
					family.woodRingColor = getFaceColor(state, Direction.DOWN, bakedTextureGetter);
					family.woodBarkColor = getFaceColor(state, Direction.NORTH, bakedTextureGetter);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static int getFaceColor (BlockState state, Direction face, Function<ResourceLocation, TextureAtlasSprite> textureGetter){
		final IBakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
		List<BakedQuad> quads = model.getQuads(state, face, new Random(), EmptyModelData.INSTANCE);
		if (quads.isEmpty()) // If the quad list is empty, means there is no face on that side, so we try with null.
			quads = model.getQuads(state, null, new Random(), EmptyModelData.INSTANCE);
		if (quads.isEmpty()) { // If null still returns empty, there is nothing we can do so we just warn and exit.
			LogManager.getLogger().warn("Could not get color of " + face + " side for " + state.getBlock()+ "! Branch needs to be handled manually!");
			return 0;
		}
		final ResourceLocation resLoc = quads.get(0).getSprite().getName(); // Now we get the texture location of that selected face.
		if (!resLoc.toString().isEmpty()) {
			final TextureUtils.PixelBuffer pixelBuffer = new TextureUtils.PixelBuffer(textureGetter.apply(resLoc));
			final int u = pixelBuffer.w / 16;
			final TextureUtils.PixelBuffer center = new TextureUtils.PixelBuffer(u * 8, u * 8);
			pixelBuffer.blit(center, u * -8, u * -8);

			return center.averageColor();
		}
		return 0;
	}
	
	private static void cleanup() {
		BlockColorMultipliers.cleanUp();
	}
	
	private static boolean isValid(IBlockReader access, BlockPos pos) {
		return access != null && pos != null;
	}

	private static void registerRenderLayers () {
		RenderTypeLookup.setRenderLayer(DTRegistries.BONSAI_POT, RenderType.cutoutMipped());

		ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof DynamicSaplingBlock || block instanceof RootyBlock)
				.forEach(block -> RenderTypeLookup.setRenderLayer(block, RenderType.cutoutMipped()));

//		ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof ThickBranchBlock)
//				.forEach(block -> RenderTypeLookup.setRenderLayer(block , ThickRingTextureManager.BRANCH_SOLID));
	}
	
	private static void registerColorHandlers() {
		final int white = 0xFFFFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.
		
		// BLOCKS
		
		final BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		
		// Register Rooty Colorizers
		for (RootyBlock roots : RootyBlockHelper.generateListForRegistry(false)){
			blockColors.register((state, world, pos, tintIndex) -> {
				switch(tintIndex) {
					case 0: return blockColors.getColor(roots.getPrimitiveDirt().defaultBlockState(), world, pos, tintIndex);
					case 1: return state.getBlock() instanceof RootyBlock ? roots.rootColor(state, world, pos) : white;
					default: return white;
				}
			}, roots
					);
		}
		
		// Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(DTRegistries.BONSAI_POT, (state, access, pos, tintIndex) -> isValid(access, pos) && (state.getBlock() instanceof PottedSaplingBlock)
				? DTRegistries.BONSAI_POT.getSpecies(access, pos).saplingColorMultiplier(state, access, pos, tintIndex) : white);
		
		// ITEMS
		
		// Register Potion Colorizer
		ModelHelper.regColorHandler(DTRegistries.DENDRO_POTION, DTRegistries.DENDRO_POTION::getColor);
		
		// Register Woodland Staff Colorizer
		ModelHelper.regColorHandler(DTRegistries.STAFF, DTRegistries.STAFF::getColor);
		
		// TREE PARTS
		
		// Register Sapling Colorizer
		for (Species species : Species.REGISTRY) {
			if (species.getSapling().isPresent()) {
				ModelHelper.regColorHandler(species.getSapling().get(), (state, access, pos, tintIndex) ->
				isValid(access, pos) ? species.saplingColorMultiplier(state, access, pos, tintIndex) : white);
			}
		}
		
		// Register Leaves Colorizers
		for (DynamicLeavesBlock leaves: LeavesPaging.getLeavesList()) {
			ModelHelper.regColorHandler(leaves, (state, worldIn, pos, tintIndex) -> {
						final LeavesProperties properties = ((DynamicLeavesBlock) state.getBlock()).getProperties(state);
						return TreeHelper.isLeaves(state.getBlock()) ? properties.foliageColorMultiplier(state, worldIn, pos) : magenta;
					}
			);
		}
		
	}
	
	private static void registerJsonColorMultipliers() {
		//Register programmable custom block color providers for LeavesPropertiesJson
		BlockColorMultipliers.register("birch", (state, worldIn,  pos, tintIndex) -> FoliageColors.getBirchColor());
		BlockColorMultipliers.register("spruce", (state, worldIn,  pos, tintIndex) -> FoliageColors.getEvergreenColor());
	}
	
	public static void registerClientEventHandlers() {
		//        MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
		//        MinecraftForge.EVENT_BUS.register(TextureGenerationHandler.class);
	}
	
	private static void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(DTRegistries.FALLING_TREE, new FallingTreeRenderer.Factory());
		RenderingRegistry.registerEntityRenderingHandler(DTRegistries.LINGERING_EFFECTOR, new LingeringEffectorRenderer.Factory());
	}
	
	private static int getFoliageColor(LeavesProperties leavesProperties, World world, BlockState blockState, BlockPos pos) {
		return leavesProperties.foliageColorMultiplier(blockState, world, pos);
	}
	
	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////
	
	private static void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
		if(world.isClientSide) {
			Particle particle = Minecraft.getInstance().particleEngine.createParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
			assert particle != null;
			particle.setColor(r, g, b);
		}
	}

	public static void spawnParticles(World world, BasicParticleType particleType, BlockPos pos, int numParticles, Random random) {
		spawnParticles(world, particleType, pos.getX(), pos.getY(), pos.getZ(), numParticles, random);
	}
	public static void spawnParticles(IWorld world, BasicParticleType particleType, int x, int y, int z, int numParticles, Random random) {
		for (int i1 = 0; i1 < numParticles; ++i1) {
			double mx = random.nextGaussian() * 0.02D;
			double my = random.nextGaussian() * 0.02D;
			double mz = random.nextGaussian() * 0.02D;
			DTClient.spawnParticle(world, particleType, x + random.nextFloat(), (double)y + (double)random.nextFloat(), (double)z + random.nextFloat(), mx, my, mz);
		}
	}
	/** Not strictly necessary. But adds a little more isolation to the server for particle effects */
	public static void spawnParticle(IWorld world, BasicParticleType particleType, double x, double y, double z, double mx, double my, double mz) {
		if(world.isClientSide()) {
			world.addParticle(particleType, x, y, z, mx, my, mz);
		}
	}
	
	public static void crushLeavesBlock(World world, BlockPos pos, BlockState blockState, Entity entity) {
		if(world.isClientSide) {
			Random random = world.random;
			ITreePart treePart = TreeHelper.getTreePart(blockState);
			if(treePart instanceof DynamicLeavesBlock) {
				DynamicLeavesBlock leaves = (DynamicLeavesBlock) treePart;
				LeavesProperties leavesProperties = leaves.getProperties(blockState);
				int color = getFoliageColor(leavesProperties, world, blockState, pos);
				float r = (color >> 16 & 255) / 255.0F;
				float g = (color >> 8 & 255) / 255.0F;
				float b = (color & 255) / 255.0F;
				for(int dz = 0; dz < 8; dz++) {
					for(int dy = 0; dy < 8; dy++) {
						for(int dx = 0; dx < 8; dx++) {
							if(random.nextInt(8) == 0) {
								double fx = pos.getX() + dx / 8.0;
								double fy = pos.getY() + dy / 8.0;
								double fz = pos.getZ() + dz / 8.0;
								addDustParticle(world, fx, fy, fz, 0, random.nextFloat() * entity.getDeltaMovement().y, 0, blockState, r, g, b);
							}
						}
					}
				}
			}
		}
	}
	
}
