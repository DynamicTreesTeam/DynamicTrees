package com.ferreusveritas.dynamictrees.proxy;

import java.util.Random;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.ModItems;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.client.ModelHelper;
import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.models.ModelLoaderBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		super.preInit();
		registerEventHandlers();
	}
	
	@Override
	public void init() {
		super.init();
		registerColorHandlers();
	}
	
	@Override
	public void registerModels() {
		
		//BLOCKS
		
		//Register Rootydirt Mesher
		ModelHelper.regModel(ModBlocks.blockRootyDirt);

		//Register Bonsai Pot Mesher
		ModelHelper.regModel(ModBlocks.blockBonsaiPot);//Register this just in case something weird happens.
		
		//Register DendroCoil Mesher
		Block dendroCoil = Block.REGISTRY.getObject(new ResourceLocation(ModConstants.MODID, "dendrocoil"));
		if(dendroCoil != Blocks.AIR) {
			ModelHelper.regModel(dendroCoil);
		}

		
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
			ModelHelper.regModel(tree.getSeed());//Register Seed Item Models
			ModelHelper.regModel(tree);//Register custom state mapper for branch
		}
		
		//Register GrowingLeavesBlocks Meshers and Colorizers
		for(BlockDynamicLeaves leaves: TreeHelper.getLeavesMapForModId(ModConstants.MODID).values()) {
			Item item = Item.getItemFromBlock(leaves);
			ModelHelper.regModel(item);
		}

		//Register the file loader for Branch models
		ModelLoaderRegistry.registerLoader(new ModelLoaderBranch());
	}
	
	public void registerColorHandlers() {
		
		final int white = 0x00FFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.
		
		//BLOCKS
		
		//Register Rootydirt Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockRootyDirt, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return BiomeColorHelper.getGrassColorAtPos(world, pos);
			}
		});
		
		//Register Sapling Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockDynamicSapling, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return ModBlocks.blockDynamicSapling.getTree(state).foliageColorMultiplier(state, world, pos);
			}
		});
		
		//Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(ModBlocks.blockBonsaiPot, new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return ModBlocks.blockBonsaiPot.getTree(state).foliageColorMultiplier(state, world, pos);
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
						return ((BlockDynamicLeaves) block).getTree(state).foliageColorMultiplier(state, worldIn, pos);
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

	@Override 
	public void registerEventHandlers() {
		//There are currently no Client Side events to handle
	}
	
	@Override
	public int getTreeFoliageColor(DynamicTree tree, World world, IBlockState blockState, BlockPos pos) {
		return tree.foliageColorMultiplier(blockState, world, pos);
	}
	
	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////
	
	@Override
	public void addDustParticle(double fx, double fy, double fz, double mx, double my, double mz, IBlockState blockState, float r, float g, float b) {
		Particle particle = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), fx, fy, fz, mx, my, mz, new int[]{Block.getStateId(blockState)});
		particle.setRBGColorF(r, g, b);
	}
	
	/** Not strictly necessary. But adds a little more isolation to the server for particle effects */
	@Override
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {
		world.spawnParticle(particleType, x, y, z, mx, my, mz);
	}

	public void crushLeavesBlock(World world, BlockPos pos, IBlockState blockState, Entity entity) {
		Random random = world.rand;
		ITreePart treePart = TreeHelper.getTreePart(blockState);
		if(treePart instanceof BlockDynamicLeaves) {
			BlockDynamicLeaves leaves = (BlockDynamicLeaves) treePart;
			DynamicTree tree = leaves.getTree(blockState);
			if(tree != null) {
				int color = getTreeFoliageColor(tree, world, blockState, pos);
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
								addDustParticle(fx, fy, fz, 0, random.nextFloat() * entity.motionY, 0, blockState, r, g, b);
							}
						}
					}
				}
			}
		}
	}
	
}
