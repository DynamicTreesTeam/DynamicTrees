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
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.blocks.BlockBranchCactus;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.event.ModelBakeEventListener;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.models.ModelLoaderBranch;
import com.ferreusveritas.dynamictrees.models.RootyStateMapper;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();
		registerClientEventHandlers();
	}
	
	@Override
	public void init() {
		super.init();
		registerColorHandlers();
	}
	
	@Override
	public void registerModels() {
		
		//BLOCKS
		
		//Register Rooty Dirt Mesher
		ModelLoader.setCustomStateMapper(ModBlocks.blockRootyDirt, new RootyStateMapper());
		ModelLoader.setCustomStateMapper(ModBlocks.blockRootyDirtSpecies, new RootyStateMapper());
		
		ModelHelper.regModel(ModBlocks.blockRootyDirt);

		//Register Rooty Sand Mesher
		ModelLoader.setCustomStateMapper(ModBlocks.blockRootySand, new RootyStateMapper());
		ModelHelper.regModel(ModBlocks.blockRootySand);
		
		//Register Bonsai Pot Mesher
		ModelHelper.regModel(ModBlocks.blockBonsaiPot);//Register this just in case something weird happens.
		
		//Register DendroCoil Mesher
		ModelHelper.regModel(Block.REGISTRY.getObject(new ResourceLocation(ModConstants.MODID, "dendrocoil")));
		
		//ITEMS
		
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
		for(DynamicTree tree: ModTrees.baseTrees) {
			ModelHelper.regModel(tree.getDynamicBranch());//Register Branch itemBlock
			ModelHelper.regModel(tree.getCommonSpecies().getSeed());//Register Seed Item Models
			ModelHelper.regModel(tree);//Register custom state mapper for branch
		}
		
		//Register models for cactus
		ModelLoader.setCustomStateMapper(ModTrees.dynamicCactus.getDynamicBranch(), new StateMap.Builder().ignore(BlockBranchCactus.TRUNK, BlockBranchCactus.ORIGIN).build());
		ModelHelper.regModel(ModTrees.dynamicCactus.getDynamicBranch());
		ModelHelper.regModel(ModTrees.dynamicCactus.getCommonSpecies().getSeed());
		
		//Special seed for apple
		ModelHelper.regModel(Species.REGISTRY.getValue(new ResourceLocation(ModConstants.MODID, "apple")).getSeed());
		
		//Register GrowingLeavesBlocks Meshers and Colorizers
		TreeHelper.getLeavesMapForModId(ModConstants.MODID).forEach((key,leaves) -> ModelHelper.regModel(leaves));
		
		//Register the file loader for Branch models
		ModelLoaderRegistry.registerLoader(new ModelLoaderBranch());
	}
	
	public void registerColorHandlers() {
		
		final int white = 0xFFFFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.
		
		//BLOCKS
		
		//Register Rootydirt Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockRootyDirt, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return world == null || pos == null ? white : BiomeColorHelper.getGrassColorAtPos(world, pos);
			}
		});
		
		//Register Rootydirt Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockRootyDirtSpecies, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return world == null || pos == null ? white : BiomeColorHelper.getGrassColorAtPos(world, pos);
			}
		});
		
		//Register Sapling Colorizers
		ModelHelper.regDynamicSaplingColorHandler(ModBlocks.blockDynamicSapling);
		ModelHelper.regDynamicSaplingColorHandler(ModBlocks.blockDynamicSaplingSpecies);
		
		//Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockBonsaiPot, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return world == null || pos == null ? white : ModBlocks.blockBonsaiPot.getTree(state).getCommonSpecies().getLeavesProperties().foliageColorMultiplier(state, world, pos);
			}
		});
		
		
		//ITEMS
		
		//Register Potion Colorizer
		ModelHelper.regColorHandler(ModItems.dendroPotion, new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				return tintIndex == 0 ? ModItems.dendroPotion.getColor(stack) : white;
			}
		});
		
		//Register Woodland Staff Mesher and Colorizer
		ModelHelper.regColorHandler(ModItems.treeStaff, new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
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
				public int getColorFromItemstack(ItemStack stack, int tintIndex) {
					return ColorizerFoliage.getFoliageColorBasic();
				}
			});
		}

		//makePlantsBlue();
	}
	
	public void makePlantsBlue() {
		//Because blue is fukin' tight!    Toying with the idea of how to create seasonal color changes
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
				return 0x6622FF;
			}
		}, new Block[] {Blocks.GRASS, Blocks.TALLGRASS, Blocks.DOUBLE_PLANT, Blocks.LEAVES, Blocks.LEAVES2});
	}
 
	public void registerClientEventHandlers() {
		//There are currently no Client Side events to handle
		MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
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
