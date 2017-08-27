package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.blocks.BlockDendroCoil;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.proxy.CommonProxy;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.TreeAcacia;
import com.ferreusveritas.dynamictrees.trees.TreeBirch;
import com.ferreusveritas.dynamictrees.trees.TreeDarkOak;
import com.ferreusveritas.dynamictrees.trees.TreeJungle;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import com.ferreusveritas.dynamictrees.trees.TreeSpruce;
import com.ferreusveritas.dynamictrees.worldgen.DecorateEventHandler;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
*  │  DYNAMIC TREES  │
*  ╰─────────────────╯
* </b></tt></pre></p>
* 
* <p>
* 2016-2017 Ferreusveritas
* </p>
* 
* @author ferreusveritas
* @version 0.6.4
*
*/
@Mod(modid = DynamicTrees.MODID, version=DynamicTrees.VERSION,dependencies="after:ComputerCraft;")
public class DynamicTrees {

	public static final String MODID = "dynamictrees";
	public static final String VERSION = "0.6.4";

	public static final DynamicTreesTab dynamicTreesTab = new DynamicTreesTab(MODID);
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockFruitCocoa blockFruitCocoa;
	public static Staff treeStaff;
	public static TreeGenerator treeGenerator;

	public static ForgeDirection horizontalDirs[] = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};

	@Instance(MODID)
	public static DynamicTrees instance;

	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.proxy.ClientProxy", serverSide = "com.ferreusveritas.dynamictrees.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static ArrayList<DynamicTree> baseTrees = new ArrayList<DynamicTree>();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry.

		//CircleDebug.initCircleTests();

		ConfigHandler.preInit(event);

		//Dirt
		blockRootyDirt = (BlockRootyDirt) new BlockRootyDirt().setBlockName(DynamicTrees.MODID + "_rootydirt");
		GameRegistry.registerBlock(blockRootyDirt, "rootydirt");

		//Trees
		baseTrees.add(new TreeOak(0));
		baseTrees.add(new TreeSpruce(1));
		baseTrees.add(new TreeBirch(2));
		baseTrees.add(new TreeJungle(3));
		baseTrees.add(new TreeAcacia(4));
		baseTrees.add(new TreeDarkOak(5));

		//Register Trees
		for(DynamicTree tree: baseTrees) {
			TreeRegistry.registerTree(tree);
		}
		
		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
		blockFruitCocoa.setBlockName(DynamicTrees.MODID + "_" + "fruitcocoa");
		GameRegistry.registerBlock(blockFruitCocoa, "fruitcocoa");
		
		//Creative Mode Stuff
		treeStaff = new Staff();

		//Computercraft Creative Mode Stuff
		if(Loader.isModLoaded("ComputerCraft")){
			new BlockDendroCoil();
		}

		//Set the creative tabs icon
		dynamicTreesTab.setTabIconItemStack(new ItemStack(TreeRegistry.findTree("oak").getSeed()));

		//Conveniently accessible disaster(Optional World Generation)
		if(ConfigHandler.worldGen) {
			treeGenerator = new TreeGenerator();
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

	
	/**
	 * Here we'll simply remap the old "growingtrees" modid to the new modid for old blocks and items.
	 * 
	 * @param event
	 */
	@EventHandler
	public void missingMappings(FMLMissingMappingsEvent event) {
		for(MissingMapping missing: event.getAll()) {
			ResourceLocation resLoc = new ResourceLocation(missing.name);
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(MODID).log(Level.CONFIG, "Remapping Missing Object: " + missing.name);
				if(missing.type == GameRegistry.Type.BLOCK) {
					Block mappedBlock = GameRegistry.findBlock(DynamicTrees.MODID, path);
					if(mappedBlock != null) { //Null is what you get when do don't get what you're looking for.
						missing.remap(mappedBlock);
					}
				}
				else if(missing.type == GameRegistry.Type.ITEM) {
					Item mappedItem = GameRegistry.findItem(DynamicTrees.MODID, path);
					if(mappedItem != null) { //Null is what you get when do don't get what you're looking for.
						missing.remap(mappedItem);
					}
				}
			}
		}
	}
	
}
