package com.ferreusveritas.growingtrees;

import com.ferreusveritas.growingtrees.blocks.BlockRootyDirt;
import com.ferreusveritas.growingtrees.proxy.CommonProxy;
import com.ferreusveritas.growingtrees.trees.GrowingTree;
import com.ferreusveritas.growingtrees.trees.TreeAcacia;
import com.ferreusveritas.growingtrees.trees.TreeBirch;
import com.ferreusveritas.growingtrees.trees.TreeDarkOak;
import com.ferreusveritas.growingtrees.trees.TreeJungle;
import com.ferreusveritas.growingtrees.trees.TreeOak;
import com.ferreusveritas.growingtrees.trees.TreeSpruce;
import com.ferreusveritas.growingtrees.worldgen.CircleDebug;
import com.ferreusveritas.growingtrees.worldgen.CircleHelper;
import com.ferreusveritas.growingtrees.worldgen.DecorateEventHandler;
import com.ferreusveritas.growingtrees.worldgen.TreeGenerator;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * <p><pre><tt><b>
 *  ╭─────────────────╮
 *  │                 │
 *  │       ▓▓        │
 *  │      ▓▓▓▓▓      │
 *  │     ▓▓▓┼▓▓▓     │
 *  │    ▓▓▓▓│▓▓▓     │
 *  │    ▓▓└─┧ ▓▓▓    │
 *  │     ▓  ┠─┘▓     │
 *  │        ┃        │
 *  │  █████████████  │
 *  │  ▒▒▒▒▒▒▒▒▒▒▒▒▒  │
 *  │  ░░░░░░░░░░░░░  │
 *  ╞═════════════════╡
 *  │  GROWING TREES  │
 *  ╰─────────────────╯
 * </b></tt></pre></p>
 * 
 * <p>
 * 2016-2017 Ferreusveritas
 * </p>
 * 
 * @author ferreusveritas
 * @version 0.4.5
 *
 */
@Mod(modid = GrowingTrees.MODID, version=GrowingTrees.VERSION)
public class GrowingTrees {

	public static final String MODID = "growingtrees";
	public static final String VERSION = "0.4.5";

	public static final GrowingTreesTab growingTreesTab = new GrowingTreesTab(MODID);
	
	public static BlockRootyDirt blockRootyDirt;
	public static GrowingTree treeOak;
	public static GrowingTree treeSpruce;
	public static GrowingTree treeBirch;
	public static GrowingTree treeJungle;
	public static GrowingTree treeAcacia;
	public static GrowingTree treeDarkOak;

	public static ForgeDirection cardinalDirs[] = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
	
	@Instance(MODID)
	public static GrowingTrees instance;

	@SidedProxy(clientSide = "com.ferreusveritas.growingtrees.proxy.ClientProxy", serverSide = "com.ferreusveritas.growingtrees.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry.
		
		//CircleDebug.initCircleTests();
		
		ConfigHandler.preInit(event);
		
		//Dirt
		blockRootyDirt = (BlockRootyDirt) new BlockRootyDirt().setBlockName(GrowingTrees.MODID + "_rootydirt");
		GameRegistry.registerBlock(blockRootyDirt, "rootydirt");

		//Trees
		treeOak = new TreeOak(0).register();
		treeSpruce = new TreeSpruce(1).register();
		treeBirch = new TreeBirch(2).register();
		treeJungle = new TreeJungle(3).register();
		treeAcacia = new TreeAcacia(4).register();
		treeDarkOak = new TreeDarkOak(5).register();
		
		growingTreesTab.setTabIconItemStack(new ItemStack(treeOak.getSeed()));

		//Conveniently accessible disaster
		if(ConfigHandler.worldGen){
			GameRegistry.registerWorldGenerator(new TreeGenerator(), 20);
			MinecraftForge.TERRAIN_GEN_BUS.register(new DecorateEventHandler());
		}
		
		proxy.preInit();
		
		proxy.registerEventHandlers();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about. Register recipes.
		
		treeOak.registerRecipes(new ItemStack(Blocks.sapling, 1, 0));
		treeSpruce.registerRecipes(new ItemStack(Blocks.sapling, 1, 1));
		treeBirch.registerRecipes(new ItemStack(Blocks.sapling, 1, 2));
		treeJungle.registerRecipes(new ItemStack(Blocks.sapling, 1, 3));
		treeAcacia.registerRecipes(new ItemStack(Blocks.sapling, 1, 4));
		treeDarkOak.registerRecipes(new ItemStack(Blocks.sapling, 1, 5));
		
		proxy.init();
	}

	@EventHandler
	public void PostInit(FMLPostInitializationEvent e){
		//Handle interaction with other mods, complete your setup based on this.	
	}

	
	
}
