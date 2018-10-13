package com.ferreusveritas.dynamictrees.proxy;

import java.util.Random;

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
import com.ferreusveritas.dynamictrees.blocks.BlockBranchThick;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.blocks.BlockTrunkShell;
import com.ferreusveritas.dynamictrees.entities.EntityFallingTree;
import com.ferreusveritas.dynamictrees.event.ModelBakeEventListener;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.models.ModelLoaderBranch;
import com.ferreusveritas.dynamictrees.models.ModelLoaderCactus;
import com.ferreusveritas.dynamictrees.models.ModelLoaderThick;
import com.ferreusveritas.dynamictrees.render.RenderFallingTree;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();
		registerClientEventHandlers();
		registerEntityRenderers();
	}
	
	@Override
	public void init() {
		super.init();
		registerColorHandlers();
	}
	
	@Override
	public void registerModels() {
		
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
		
		TreeFamily thickFamily = ModTrees.thickTestFamily;
		BlockBranchThick thickBranch = (BlockBranchThick) thickFamily.getDynamicBranch();
		ModelHelper.regModel(thickBranch.getPairSide(false));
		ModelHelper.regModel(thickBranch.getPairSide(true));
		ModelHelper.regModel(thickFamily.getCommonSpecies().getSeed());
		ModelHelper.regModel(thickFamily);
		ModelLoader.setCustomStateMapper(((BlockBranchThick) thickFamily.getDynamicBranch()).otherBlock, new StateMap.Builder().ignore(thickFamily.getDynamicBranch().getIgnorableProperties()).build());
		ModelLoader.setCustomStateMapper(ModBlocks.blockTrunkShell, new StateMap.Builder().ignore(BlockTrunkShell.COREDIR).build());
		
		//Register models for cactus
		ModelLoader.setCustomStateMapper(ModTrees.dynamicCactus.getDynamicBranch(), new StateMap.Builder().ignore(BlockBranchCactus.TRUNK, BlockBranchCactus.ORIGIN).build());
		ModelHelper.regModel(ModTrees.dynamicCactus.getDynamicBranch());
		ModelHelper.regModel(ModTrees.dynamicCactus.getCommonSpecies().getSeed());
		
		//Special seed for apple
		ModelHelper.regModel(Species.REGISTRY.getValue(new ResourceLocation(ModConstants.MODID, "apple")).getSeed());
		
		//Register GrowingLeavesBlocks Meshers and Colorizers
		TreeHelper.getLeavesMapForModId(ModConstants.MODID).forEach((key,leaves) -> ModelHelper.regModel(leaves));
		TreeHelper.getLeavesMapForModId(ModConstants.MODID).forEach((key,leaves) -> ModelLoader.setCustomStateMapper(leaves, new StateMap.Builder().ignore(BlockLeaves.DECAYABLE).build()));
		
		//Register the file loader for Branch models
		ModelLoaderRegistry.registerLoader(new ModelLoaderBranch());
		ModelLoaderRegistry.registerLoader(new ModelLoaderCactus());
		ModelLoaderRegistry.registerLoader(new ModelLoaderThick());
	}
	
	public void registerColorHandlers() {
		
		final int white = 0xFFFFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.
		
		//BLOCKS
		
		final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
		
		//Register Rooty Colorizers
		blockColors.registerBlockColorHandler(
		new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				switch(tintIndex) {
					case 0: return blockColors.colorMultiplier(ModBlocks.blockStates.grass, world, pos, tintIndex);
					case 1: return 0xFFF1AE;//Root Color
					default: return white;
				}
			}
		},
		new Block[] {ModBlocks.blockRootyDirt, ModBlocks.blockRootyDirtSpecies, ModBlocks.blockRootySand, ModBlocks.blockRootyDirtFake});
		
		//Register Sapling Colorizers
		ModelHelper.regDynamicSaplingColorHandler(ModBlocks.blockDynamicSapling);
		ModelHelper.regDynamicSaplingColorHandler(ModBlocks.blockDynamicSaplingSpecies);
		
		//Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockBonsaiPot, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess access, BlockPos pos, int tintIndex) {
				return (access == null || pos == null || !(state.getBlock() instanceof BlockBonsaiPot))
					? white
					: ModBlocks.blockBonsaiPot.getSpecies(access, pos).getLeavesProperties().foliageColorMultiplier(state, access, pos);
			}
		});
		
		
		//ITEMS
		
		//Register Potion Colorizer
		ModelHelper.regColorHandler(ModItems.dendroPotion, new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				return tintIndex == 0 ? ModItems.dendroPotion.getColor(stack) : white;
			}
		});
		
		//Register Woodland Staff Mesher and Colorizer
		ModelHelper.regColorHandler(ModItems.treeStaff, new IItemColor() {
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) {
				return tintIndex == 1 ? ModItems.treeStaff.getColor(stack) : white;
			}
		});
		
		
		//TREE PARTS
		
		//Register GrowingLeavesBlocks Colorizers
		for(BlockDynamicLeaves leaves: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			
			ModelHelper.regColorHandler(leaves, new IBlockColor() {
				@Override
				public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
					Block block = state.getBlock();
					if(TreeHelper.isLeaves(block)) {
						return ((BlockDynamicLeaves) block).getProperties(state).foliageColorMultiplier(state, worldIn, pos);
					}
					return magenta;
				}
			});
				
			ModelHelper.regColorHandler(Item.getItemFromBlock(leaves), new IItemColor() {
				@Override
				public int colorMultiplier(ItemStack stack, int tintIndex) {
					return ColorizerFoliage.getFoliageColorBasic();
				}
			});
		}
		
	}
	
	public void registerClientEventHandlers() {
		MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
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
