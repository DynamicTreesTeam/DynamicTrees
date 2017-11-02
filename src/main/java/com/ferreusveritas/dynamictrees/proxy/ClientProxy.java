package com.ferreusveritas.dynamictrees.proxy;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.client.TreeModelHelper;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.event.ClientEventHandler;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.models.ModelLoaderBranch;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		registerModels();
	}

	@Override
	public void init() {

		//Register Rootydirt Mesher and Colorizer
		TreeModelHelper.regMesher(Item.getItemFromBlock(DynamicTrees.blockRootyDirt));
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
				return worldIn != null && pos != null ? BiomeColorHelper.getGrassColorAtPos(worldIn, pos) : ColorizerGrass.getGrassColor(0.5D, 1.0D);
			}
		}, new Block[] {DynamicTrees.blockRootyDirt});

		//Register Potion Mesher and Colorizer
		for(DendroPotion.DendroPotionType type: DendroPotion.DendroPotionType.values()) {
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(DynamicTrees.dendroPotion, type.getIndex(), new ModelResourceLocation(DynamicTrees.dendroPotion.getRegistryName(), "inventory"));
		}
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				return tintIndex == 0 ? DynamicTrees.dendroPotion.getColor(stack) : 0x00FFFFFF;
			}
		}, new Item[] {DynamicTrees.dendroPotion});
		
		//Register DirtBucket Mesher
		TreeModelHelper.regMesher(DynamicTrees.dirtBucket);
		
		//Register Woodland Staff Mesher and Colorizer
		TreeModelHelper.regMesher(DynamicTrees.treeStaff);
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				return tintIndex == 1 ? DynamicTrees.treeStaff.getColor(stack) : 0x00FFFFFF;
			}
		}, new Item[] {DynamicTrees.treeStaff});

		//Register Sapling Colorizer
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return DynamicTrees.blockDynamicSapling.getTree(state).foliageColorMultiplier(state, world, pos);
			}
		}, new Block[] {DynamicTrees.blockDynamicSapling});

		//Register Bonsai Pot Colorizer
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return DynamicTrees.blockBonsaiPot.getTree(state).foliageColorMultiplier(state, world, pos);
			}
		}, new Block[] {DynamicTrees.blockBonsaiPot});
		TreeModelHelper.regMesher(Item.getItemFromBlock(DynamicTrees.blockBonsaiPot));//Register this just in case something weird happens

		//Register DendroCoil Mesher if it exists
		Block dendroCoil = Block.REGISTRY.getObject(new ResourceLocation(DynamicTrees.MODID, "dendrocoil"));
		if(dendroCoil != Blocks.AIR) {
			TreeModelHelper.regMesher(Item.getItemFromBlock(dendroCoil));
		}

		//Register Meshers for Branches and Seeds
		for(DynamicTree tree: DynamicTrees.baseTrees) {
			TreeModelHelper.regMesher(Item.getItemFromBlock(tree.getDynamicBranch()));
			TreeModelHelper.regMesher(tree.getSeed());//Register Seed Item Models
		}
		
		//Register GrowingLeavesBlocks Meshers and Colorizers
		for(BlockDynamicLeaves leaves: TreeHelper.getLeavesMapForModId(DynamicTrees.MODID).values()) {
			Item item = Item.getItemFromBlock(leaves);
			if(item != null) {
				TreeModelHelper.regMesher(item);
				
				Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
					@Override
					public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
						Block block = state.getBlock();
						if(TreeHelper.isLeaves(block)) {
							BlockDynamicLeaves leaves = (BlockDynamicLeaves) block;
							DynamicTree tree = leaves.getTree(state);
							return tree.foliageColorMultiplier(state, worldIn, pos);
						}
						return 0x00ff00ff;//Magenta shading to indicate error
					}
				}, new Block[] {leaves});

				Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
					@Override
					public int getColorFromItemstack(ItemStack stack, int tintIndex) {
						return ColorizerFoliage.getFoliageColorBasic();
					}
				}, new Item[] {Item.getItemFromBlock(leaves)});
			}
		}

		//makePlantsBlue();
	}

	@Override
	public void registerModels() {

		for(DynamicTree tree: DynamicTrees.baseTrees) {
			TreeModelHelper.registerTreeModels(tree);
			//ModelLoader.setCustomStateMapper(tree.getDynamicBranch(), new StateMap.Builder().ignore(BlockBranch.RADIUS).build());
		}
		
		ModelLoaderRegistry.registerLoader(new ModelLoaderBranch());
	}
	
	void makePlantsBlue() {
		//Because blue is fukin' tight!    Toying with the idea of how to create seasonal color changes
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
				return 0x6622FF;
			}
		}, new Block[] {Blocks.GRASS, Blocks.TALLGRASS, Blocks.DOUBLE_PLANT, Blocks.LEAVES, Blocks.LEAVES2});
	}
	
	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}

	@Override 
	public void registerEventHandlers() {
		super.registerEventHandlers();//Registers Common Handlers
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());//Registers Client Handler
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

	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	@Override
	public void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double mx, double my, double mz) {
		world.spawnParticle(particleType, x, y, z, mx, my, mz);
	}
	
}
