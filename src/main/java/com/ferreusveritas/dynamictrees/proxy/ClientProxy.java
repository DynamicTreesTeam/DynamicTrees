package com.ferreusveritas.dynamictrees.proxy;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.client.ModelHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchCactus;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.blocks.LeavesPropertiesJson;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.ferreusveritas.dynamictrees.client.QuadManipulator;
import com.ferreusveritas.dynamictrees.client.TextureUtils.PixelBuffer;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.event.BlockBreakAnimationClientHandler;
import com.ferreusveritas.dynamictrees.event.ModelBakeEventListener;
import com.ferreusveritas.dynamictrees.event.TextureGenerationHandler;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.models.loaders.ModelLoaderBlockBranchBasic;
import com.ferreusveritas.dynamictrees.models.loaders.ModelLoaderBlockBranchCactus;
import com.ferreusveritas.dynamictrees.models.loaders.ModelLoaderBlockBranchThick;
import com.ferreusveritas.dynamictrees.models.loaders.ModelLoaderBlockSurfaceRoot;
import com.ferreusveritas.dynamictrees.models.loaders.ModelLoaderSapling;
import com.ferreusveritas.dynamictrees.render.RenderFallingTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		registerJsonColorMultipliers();
		registerClientEventHandlers();
		registerEntityRenderers();
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		registerColorHandlers();
		MinecraftForge.EVENT_BUS.register(BlockBreakAnimationClientHandler.instance);
	}
	
	@Override
	public void postInit() {
		super.postInit();
		discoverWoodColors();
		LeavesPropertiesJson.postInitClient();
	}
	
	private void discoverWoodColors() {
		
		Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
		
		for(TreeFamily family : Species.REGISTRY.getValues().stream().map(s -> s.getFamily()).distinct().collect(Collectors.toList())) {
			family.woodColor = 0xFFF1AE;//For roots
			if(family != TreeFamily.NULLFAMILY) {
				IBlockState state = family.getPrimitiveLog();
				if(state.getBlock() != Blocks.AIR) {
					IModel model = QuadManipulator.getModelForState(state);
					ResourceLocation resloc = QuadManipulator.getModelTexture(model, bakedTextureGetter, state, EnumFacing.UP);
					if(resloc != null) {
						PixelBuffer pixbuf = new PixelBuffer(bakedTextureGetter.apply(resloc));
						int u = pixbuf.w / 16;
						PixelBuffer center = new PixelBuffer(u * 8, u * 8);
						pixbuf.blit(center, u * -8, u * -8);
						
						family.woodColor = center.averageColor();
					}
				}
			}
		}
		
	}
	
	@Override
	public void cleanUp() {
		super.cleanUp();
		BlockColorMultipliers.cleanUp();
	}
	
	@Override
	public void registerModels() {
		
		//Resolve all leaves properties so the LeavesStateMapper can function properly
		LeavesPropertiesJson.resolveAll();
		
		//BLOCKS
		ModelLoader.setCustomStateMapper(ModBlocks.blockRootyDirt, new StateMap.Builder().ignore(BlockRooty.LIFE).build());
		ModelLoader.setCustomStateMapper(ModBlocks.blockRootyDirtSpecies, new StateMap.Builder().ignore(BlockRooty.LIFE).build());
		ModelLoader.setCustomStateMapper(ModBlocks.blockRootySand, new StateMap.Builder().ignore(BlockRooty.LIFE).build());
		
		//Register Potion Mesher
		for(DendroPotion.DendroPotionType type: DendroPotion.DendroPotionType.values()) {
			ModelHelper.regModel(ModItems.dendroPotion, type.getIndex());
		}
		
		//Register DirtBucket Mesher
		ModelHelper.regModel(ModItems.dirtBucket);
		
		//Register Woodland Staff Mesher
		ModelHelper.regModel(ModItems.treeStaff);
		
		
		//TREE PARTS
		
		//Register Meshers for Branches and Seeds
		for(TreeFamily tree: ModTrees.baseFamilies) {
			ModelHelper.regModel(tree.getDynamicBranch());//Register Branch itemBlock
			ModelHelper.regModel(tree.getCommonSpecies().getSeed());//Register Seed Item Models
			ModelHelper.regModel(tree);//Register custom state mapper for branch
		}
		
		//Sapling
		ModelHelper.setGenericStateMapper(ModBlocks.blockDynamicSapling, new ModelResourceLocation(new ResourceLocation(ModConstants.MODID, "sapling"), ""));
		
		//Setup the state mapper for the trunk shell
		ModelLoader.setCustomStateMapper(ModBlocks.blockTrunkShell, new StateMap.Builder().ignore(BlockTrunkShell.COREDIR).build());
		
		//Register models for cactus
		ModelLoader.setCustomStateMapper(ModTrees.dynamicCactus.getDynamicBranch(), new StateMap.Builder().ignore(BlockBranchCactus.TRUNK, BlockBranchCactus.ORIGIN).build());
		ModelHelper.regModel(ModTrees.dynamicCactus.getDynamicBranch());
		ModelHelper.regModel(ModTrees.dynamicCactus.getCommonSpecies().getSeed());
		
		//Special seed for apple
		ModelHelper.regModel(Species.REGISTRY.getValue(new ResourceLocation(ModConstants.MODID, "apple")).getSeed());
		
		//Set state mappers for all blocks created with the LeavesPaging object
		LeavesPaging.setStateMappers();
		
		//Register the file loader for Branch models
		ModelLoaderRegistry.registerLoader(new ModelLoaderBlockBranchBasic());
		ModelLoaderRegistry.registerLoader(new ModelLoaderBlockBranchCactus());
		ModelLoaderRegistry.registerLoader(new ModelLoaderBlockBranchThick());
		ModelLoaderRegistry.registerLoader(new ModelLoaderBlockSurfaceRoot());
		
		ModelLoaderRegistry.registerLoader(new ModelLoaderSapling());
	}
	
	private boolean isValid(IBlockAccess access, BlockPos pos) {
		return access != null && pos != null;
	}
	
	public void registerColorHandlers() {
		
		final int white = 0xFFFFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.
		
		//BLOCKS
		
		final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
		
		//Register Rooty Colorizers
		blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
				BlockRooty blockRooty;
				if (state.getBlock() instanceof BlockRooty){
					blockRooty = (BlockRooty) state.getBlock();
				} else return white;
				switch(tintIndex) {
					case 0: return blockColors.colorMultiplier(blockRooty.getMimic(world, pos), world, pos, tintIndex);
					case 1: return blockRooty.rootColor(state, world, pos);
					default: return white;
				}
			},
			ModBlocks.blockRootyDirt, ModBlocks.blockRootyDirtSpecies, ModBlocks.blockRootySand, ModBlocks.blockRootyDirtFake);
		
		//Register Sapling Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockDynamicSapling, (state, access, pos, tintIndex) -> {
			return isValid(access, pos) ? ModBlocks.blockDynamicSapling.getSpecies(access, pos, state).saplingColorMultiplier(state, access, pos, tintIndex) : white;
		});
		
		//Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockBonsaiPot, (state, access, pos, tintIndex) -> {
			return isValid(access, pos) && (state.getBlock() instanceof BlockBonsaiPot)
				? ModBlocks.blockBonsaiPot.getSpecies(access, pos).saplingColorMultiplier(state, access, pos, tintIndex) : white;
		});
		
		//ITEMS
		
		//Register Potion Colorizer
		ModelHelper.regColorHandler(ModItems.dendroPotion, (stack, tint) -> ModItems.dendroPotion.getColor(stack, tint));
		
		//Register Woodland Staff Mesher and Colorizer
		ModelHelper.regColorHandler(ModItems.treeStaff, (stack, tint) -> ModItems.treeStaff.getColor(stack, tint));
		
		//TREE PARTS
		
		//Register GrowingLeavesBlocks Colorizers
		for(BlockDynamicLeaves leaves: LeavesPaging.getLeavesMapForModId(ModConstants.MODID).values()) {
			ModelHelper.regColorHandler(leaves, (state, worldIn, pos, tintIndex) -> 
				TreeHelper.isLeaves(state.getBlock()) ? ((BlockDynamicLeaves) state.getBlock()).getProperties(state).foliageColorMultiplier(state, worldIn, pos) : magenta
			);
		}
		
	}
	
	public void registerJsonColorMultipliers() {
		//Register programmable custom block color providers for LeavesPropertiesJson
		BlockColorMultipliers.register("birch", (state, worldIn,  pos, tintIndex) -> ColorizerFoliage.getFoliageColorBirch() );
		BlockColorMultipliers.register("spruce", (state, worldIn,  pos, tintIndex) -> ColorizerFoliage.getFoliageColorPine() );
	}
	
	public void registerClientEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
		MinecraftForge.EVENT_BUS.register(TextureGenerationHandler.class);
	}
	
	public void registerEntityRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingTree.class, new RenderFallingTree.Factory());
	}
	
	@Override
	public int getFoliageColor(ILeavesProperties leavesProperties, World world, IBlockState blockState, BlockPos pos) {
		return leavesProperties.foliageColorMultiplier(blockState, world, pos);
	}
	
	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////
	
	@Override
	public void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, IBlockState blockState, float r, float g, float b) {
		if(world.isRemote) {
			Particle particle = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), fx, fy, fz, mx, my, mz, new int[]{Block.getStateId(blockState)});
			particle.setRBGColorF(r, g, b);
		}
	}
	
	/** Not strictly necessary. But adds a little more isolation to the server for particle effects */
	@Override
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {
		if(world.isRemote) {
			world.spawnParticle(particleType, x, y, z, mx, my, mz);
		}
	}
	
	@Override
	public void crushLeavesBlock(World world, BlockPos pos, IBlockState blockState, Entity entity) {
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
								addDustParticle(world, fx, fy, fz, 0, random.nextFloat() * entity.motionY, 0, blockState, r, g, b);
							}
						}
					}
				}
			}
		}
	}
	
}
