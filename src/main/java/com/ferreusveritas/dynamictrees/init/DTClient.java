package com.ferreusveritas.dynamictrees.init;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.RootyBlockHelper;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.client.ModelHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.*;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.ferreusveritas.dynamictrees.client.TextureUtils;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.event.BlockBreakAnimationClientHandler;
import com.ferreusveritas.dynamictrees.render.RenderFallingTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

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
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.FoliageColors;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.ForgeRegistries;

public class DTClient {
	
	public static void setup() {
		registerRenderLayers();
		registerJsonColorMultipliers();
		registerEntityRenderers();
		
		registerColorHandlers();
		MinecraftForge.EVENT_BUS.register(BlockBreakAnimationClientHandler.instance);
		
		LeavesPropertiesJson.postInitClient();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void discoverWoodColors() {
		
		Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter = location -> Minecraft.getInstance().getTextureMap().getAtlasSprite(location.toString());
		Random rand = new Random();
		
		for(TreeFamily family : Species.REGISTRY.getValues().stream().map(Species::getFamily).distinct().collect(Collectors.toList())) {
			family.woodColor = 0xFFF1AE;//For roots
			if(family != TreeFamily.NULLFAMILY) {
				BlockState state = family.getPrimitiveLog().getDefaultState();
				if(state.getBlock() != Blocks.AIR) {
					IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state);
					List<BakedQuad> quads = model.getQuads(state, Direction.DOWN, rand, EmptyModelData.INSTANCE); //We get the BOTTOM face of the log model
					ResourceLocation resloc = quads.get(0).getSprite().getName(); //Now we get the texture location of that top face
					if(!resloc.toString().isEmpty()) {
						TextureUtils.PixelBuffer pixbuf = new TextureUtils.PixelBuffer(bakedTextureGetter.apply(resloc));
						int u = pixbuf.w / 16;
						TextureUtils.PixelBuffer center = new TextureUtils.PixelBuffer(u * 8, u * 8);
						pixbuf.blit(center, u * -8, u * -8);
						
						family.woodColor = center.averageColor();
					}
				}
			}
		}
		
	}
	
	public void cleanUp() {
		BlockColorMultipliers.cleanUp();
	}
	
	private static boolean isValid(IBlockReader access, BlockPos pos) {
		return access != null && pos != null;
	}

	private static void registerRenderLayers () {
		ForgeRegistries.BLOCKS.forEach(block -> {
			if (block instanceof BlockDynamicSapling || block instanceof BlockRooty || block instanceof BlockBranchCactus) {
				RenderTypeLookup.setRenderLayer(block, RenderType.getCutoutMipped());
			}
		});
	}
	
	private static void registerColorHandlers() {
		
		final int white = 0xFFFFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.
		
		//BLOCKS
		
		final BlockColors blockColors = Minecraft.getInstance().getBlockColors();
		
		//Register Rooty Colorizers
		for (BlockRooty roots : RootyBlockHelper.generateListForRegistry(false)){
			blockColors.register((state, world, pos, tintIndex) -> {
				switch(tintIndex) {
					case 0: return blockColors.getColor(roots.getPrimitiveDirt().getDefaultState(), world, pos, tintIndex);
					case 1: return state.getBlock() instanceof BlockRooty ? roots.rootColor(state, world, pos) : white;
					default: return white;
				}
			}, roots
					);
		}
		
		//Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(DTRegistries.blockBonsaiPot, (state, access, pos, tintIndex) -> isValid(access, pos) && (state.getBlock() instanceof BlockBonsaiPot)
				? DTRegistries.blockBonsaiPot.getSpecies((World) access, pos).saplingColorMultiplier(state, access, pos, tintIndex) : white);
		
		//ITEMS
		
		// Register Potion Colorizer
		ModelHelper.regColorHandler(DTRegistries.dendroPotion, (stack, tint) -> DTRegistries.dendroPotion.getColor(stack, tint));
		
		//Register Woodland Staff Colorizer
		ModelHelper.regColorHandler(DTRegistries.treeStaff, (stack, tint) -> DTRegistries.treeStaff.getColor(stack, tint));
		
		//TREE PARTS
		
		//Register Sapling Colorizer
		for (Species species : Species.REGISTRY){
			if (species.getSapling().isPresent()){
				ModelHelper.regColorHandler(species.getSapling().get(), (state, access, pos, tintIndex) ->
				isValid(access, pos) ? species.saplingColorMultiplier(state, access, pos, tintIndex) : white);
			}
		}
		
		//Register GrowingLeavesBlocks Colorizers
		for(BlockDynamicLeaves leaves: LeavesPaging.getLeavesListForModId(DynamicTrees.MODID)) {
			ModelHelper.regColorHandler(leaves, (state, worldIn, pos, tintIndex) ->
			TreeHelper.isLeaves(state.getBlock()) ? ((BlockDynamicLeaves) state.getBlock()).getProperties(state).foliageColorMultiplier(state, worldIn, pos) : magenta
			);
		}
		
	}
	
	private static void registerJsonColorMultipliers() {
		//Register programmable custom block color providers for LeavesPropertiesJson
		BlockColorMultipliers.register("birch", (state, worldIn,  pos, tintIndex) -> FoliageColors.getBirch());
		BlockColorMultipliers.register("spruce", (state, worldIn,  pos, tintIndex) -> FoliageColors.getSpruce());
	}
	
	public static void registerClientEventHandlers() {
		//        MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
		//        MinecraftForge.EVENT_BUS.register(TextureGenerationHandler.class);
	}
	
	private static void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingTree.class, new RenderFallingTree.Factory());
	}
	
	private static int getFoliageColor(ILeavesProperties leavesProperties, World world, BlockState blockState, BlockPos pos) {
		return leavesProperties.foliageColorMultiplier(blockState, world, pos);
	}
	
	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////
	
	private static void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
		if(world.isRemote) {
			Particle particle = Minecraft.getInstance().particles.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
			assert particle != null;
			particle.setColor(r, g, b);
		}
	}
	
	/** Not strictly necessary. But adds a little more isolation to the server for particle effects */
	public static void spawnParticle(World world, BasicParticleType particleType, double x, double y, double z, double mx, double my, double mz) {
		if(world.isRemote) {
			world.addParticle(particleType, x, y, z, mx, my, mz);
		}
	}
	
	public static void crushLeavesBlock(World world, BlockPos pos, BlockState blockState, Entity entity) {
		if(world.isRemote) {
			Random random = world.rand;
			ITreePart treePart = TreeHelper.getTreePart(blockState);
			if(treePart instanceof BlockDynamicLeaves) {
				BlockDynamicLeaves leaves = (BlockDynamicLeaves) treePart;
				ILeavesProperties leavesProperties = leaves.getProperties(blockState);
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
								addDustParticle(world, fx, fy, fz, 0, random.nextFloat() * entity.getMotion().y, 0, blockState, r, g, b);
							}
						}
					}
				}
			}
		}
	}
	
}
