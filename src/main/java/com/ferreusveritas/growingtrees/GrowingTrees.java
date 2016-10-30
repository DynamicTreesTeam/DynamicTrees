package com.ferreusveritas.growingtrees;

import com.ferreusveritas.growingtrees.blocks.BlockAcacia;
import com.ferreusveritas.growingtrees.blocks.BlockBirchBranch;
import com.ferreusveritas.growingtrees.blocks.BlockBranch;
import com.ferreusveritas.growingtrees.blocks.BlockDarkOak;
import com.ferreusveritas.growingtrees.blocks.BlockJungleBranch;
import com.ferreusveritas.growingtrees.blocks.BlockOakBranch;
import com.ferreusveritas.growingtrees.blocks.BlockRootyDirt;
import com.ferreusveritas.growingtrees.blocks.BlockSpruceBranch;
import com.ferreusveritas.growingtrees.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

@Mod(modid = GrowingTrees.MODID, version=GrowingTrees.VERSION)
public class GrowingTrees {

	public static final String MODID = "growingtrees";
	public static final String VERSION = "0.4.3";

	public static final GrowingTreesTab growingTreesTab = new GrowingTreesTab(MODID);
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockBranch blockOakBranch;
	public static BlockBranch blockSpruceBranch;
	public static BlockBranch blockBirchBranch;
	public static BlockBranch blockJungleBranch;
	public static BlockBranch blockAcaciaBranch;
	public static BlockBranch blockDarkOak;
	public static ForgeDirection cardinalDirs[] = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
	
	@Instance(MODID)
	public static GrowingTrees instance;

	@SidedProxy(clientSide = "com.ferreusveritas.growingtrees.proxy.ClientProxy", serverSide = "com.ferreusveritas.growingtrees.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry.
		
		//Dirt
		blockRootyDirt = (BlockRootyDirt) new BlockRootyDirt().setBlockName(GrowingTrees.MODID + "_rootydirt");
		GameRegistry.registerBlock(blockRootyDirt, "rootydirt");

		//Trees
		blockOakBranch = TreeHelper.createTree("oak", new BlockOakBranch(), 0);
		blockSpruceBranch = TreeHelper.createTree("spruce", new BlockSpruceBranch(), 1);
		blockBirchBranch = TreeHelper.createTree("birch", new BlockBirchBranch(), 2);
		blockJungleBranch = TreeHelper.createTree("jungle", new BlockJungleBranch(), 3);
		blockAcaciaBranch = TreeHelper.createTree("acacia", new BlockAcacia(), 4);
		blockDarkOak = TreeHelper.createTree("darkoak", new BlockDarkOak(), 5);
		
		growingTreesTab.setTabIconItemStack(new ItemStack(blockOakBranch.getSeed()));
		
		proxy.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about. Register recipes.
		
		GameRegistry.addShapelessRecipe(new ItemStack(blockOakBranch.getSeed()), new Object[]{new ItemStack(Blocks.sapling, 1, 0), Items.bowl});
		GameRegistry.addShapelessRecipe(new ItemStack(blockSpruceBranch.getSeed()), new Object[]{new ItemStack(Blocks.sapling, 1, 1), Items.bowl});
		GameRegistry.addShapelessRecipe(new ItemStack(blockBirchBranch.getSeed()), new Object[]{new ItemStack(Blocks.sapling, 1, 2), Items.bowl});
		GameRegistry.addShapelessRecipe(new ItemStack(blockJungleBranch.getSeed()), new Object[]{new ItemStack(Blocks.sapling, 1, 3), Items.bowl});
		GameRegistry.addShapelessRecipe(new ItemStack(blockAcaciaBranch.getSeed()), new Object[]{new ItemStack(Blocks.sapling, 1, 4), Items.bowl});
		GameRegistry.addShapelessRecipe(new ItemStack(blockDarkOak.getSeed()), new Object[]{new ItemStack(Blocks.sapling, 1, 5), Items.bowl});
		
		GameRegistry.addShapelessRecipe(new ItemStack(Blocks.sapling, 1, 0), new Object[]{blockOakBranch.getSeed(), Blocks.dirt });
		GameRegistry.addShapelessRecipe(new ItemStack(Blocks.sapling, 1, 1), new Object[]{blockSpruceBranch.getSeed(), Blocks.dirt });
		GameRegistry.addShapelessRecipe(new ItemStack(Blocks.sapling, 1, 2), new Object[]{blockBirchBranch.getSeed(), Blocks.dirt });
		GameRegistry.addShapelessRecipe(new ItemStack(Blocks.sapling, 1, 3), new Object[]{blockJungleBranch.getSeed(), Blocks.dirt });
		GameRegistry.addShapelessRecipe(new ItemStack(Blocks.sapling, 1, 4), new Object[]{blockAcaciaBranch.getSeed(), Blocks.dirt });
		GameRegistry.addShapelessRecipe(new ItemStack(Blocks.sapling, 1, 5), new Object[]{blockDarkOak.getSeed(), Blocks.dirt });
		
		proxy.init();
	}

	@EventHandler
	public void PostInit(FMLPostInitializationEvent e){
		//Handle interaction with other mods, complete your setup based on this.
	}

}
