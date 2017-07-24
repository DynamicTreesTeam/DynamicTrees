package com.ferreusveritas.growingtrees;

import com.ferreusveritas.growingtrees.blocks.BlockDendroCoil;
import com.ferreusveritas.growingtrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.growingtrees.blocks.BlockRootyDirt;
import com.ferreusveritas.growingtrees.items.Staff;
import com.ferreusveritas.growingtrees.proxy.CommonProxy;
import com.ferreusveritas.growingtrees.trees.TreeAcacia;
import com.ferreusveritas.growingtrees.trees.TreeBirch;
import com.ferreusveritas.growingtrees.trees.TreeDarkOak;
import com.ferreusveritas.growingtrees.trees.TreeJungle;
import com.ferreusveritas.growingtrees.trees.TreeOak;
import com.ferreusveritas.growingtrees.trees.TreeSpruce;
import com.ferreusveritas.growingtrees.worldgen.DecorateEventHandler;
import com.ferreusveritas.growingtrees.worldgen.TreeGenerator;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
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
* @version 0.4.7
*
*/
@Mod(modid = GrowingTrees.MODID, version=GrowingTrees.VERSION,dependencies="after:ComputerCraft;")
public class GrowingTrees {

	public static final String MODID = "growingtrees";
	public static final String VERSION = "0.4.7";

	public static final GrowingTreesTab growingTreesTab = new GrowingTreesTab(MODID);
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockFruitCocoa blockFruitCocoa;
	public static Staff treeStaff;

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
		TreeRegistry.registerTree(new TreeOak(0));
		TreeRegistry.registerTree(new TreeSpruce(1));
		TreeRegistry.registerTree(new TreeBirch(2));
		TreeRegistry.registerTree(new TreeJungle(3));
		TreeRegistry.registerTree(new TreeAcacia(4));
		TreeRegistry.registerTree(new TreeDarkOak(5));

		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
		blockFruitCocoa.setBlockName(GrowingTrees.MODID + "_" + "fruitcocoa");
		GameRegistry.registerBlock(blockFruitCocoa, "fruitcocoa");
		
		//Creative Mode Stuff
		treeStaff = new Staff();

		if(Loader.isModLoaded("ComputerCraft")){
			new BlockDendroCoil();//For Testing
		}

		growingTreesTab.setTabIconItemStack(new ItemStack(TreeRegistry.findTree("oak").getSeed()));

		//Conveniently accessible disaster
		if(ConfigHandler.worldGen) {
			GameRegistry.registerWorldGenerator(new TreeGenerator(), 20);
			MinecraftForge.TERRAIN_GEN_BUS.register(new DecorateEventHandler());
		}

		proxy.preInit();

		proxy.registerEventHandlers();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about. Register recipes.

		TreeRegistry.registerAllTreeRecipes();

		proxy.init();
	}

	@EventHandler
	public void PostInit(FMLPostInitializationEvent e){
		//Handle interaction with other mods, complete your setup based on this.
	}

}
