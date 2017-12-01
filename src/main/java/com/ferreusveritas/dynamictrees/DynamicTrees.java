package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.WorldGenRegistry;
import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSaplingVanilla;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.compat.CommonProxyCompat;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.proxy.CommonProxy;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.trees.TreeAcacia;
import com.ferreusveritas.dynamictrees.trees.TreeBirch;
import com.ferreusveritas.dynamictrees.trees.TreeDarkOak;
import com.ferreusveritas.dynamictrees.trees.TreeJungle;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import com.ferreusveritas.dynamictrees.trees.TreeSpruce;
import com.ferreusveritas.dynamictrees.worldgen.TreeGenerator;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

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
* @version 0.6.6
*
*/
@Mod(modid = DynamicTrees.MODID, version=DynamicTrees.VERSION,dependencies="after:ComputerCraft;")
public class DynamicTrees {

	public static final String MODID = "dynamictrees";
	public static final String VERSION = "0.6.6";

	public static final DynamicTreesTab dynamicTreesTab = new DynamicTreesTab(MODID);
	
	public static BlockRootyDirt blockRootyDirt;
	public static BlockDynamicSapling blockDynamicSapling;
	public static BlockFruitCocoa blockFruitCocoa;
	public static Staff treeStaff;
	public static DendroPotion dendroPotion;
	public static DirtBucket dirtBucket;
	public static BlockBonsaiPot blockBonsaiPot;
	public static TreeGenerator treeGenerator;

	@Instance(MODID)
	public static DynamicTrees instance;

	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.proxy.ClientProxy", serverSide = "com.ferreusveritas.dynamictrees.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	@SidedProxy(clientSide = "com.ferreusveritas.dynamictrees.compat.ClientProxyCompat", serverSide = "com.ferreusveritas.dynamictrees.compat.CommonProxyCompat")
	public static CommonProxyCompat compatProxy;

	public static ArrayList<DynamicTree> baseTrees = new ArrayList<DynamicTree>();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry.

		//CircleDebug.initCircleTests(); //This is for circle generation testing purposes only

		ConfigHandler.preInit(event);

		if(WorldGenRegistry.isWorldGenEnabled()) {
			treeGenerator = new TreeGenerator();
		}
		
		//Dirt
		blockRootyDirt = new BlockRootyDirt();
		blockDynamicSapling = new BlockDynamicSaplingVanilla("sapling");
		
		//Trees
		baseTrees.add(new TreeOak());
		baseTrees.add(new TreeSpruce());
		baseTrees.add(new TreeBirch());
		baseTrees.add(new TreeJungle());
		baseTrees.add(new TreeAcacia());
		baseTrees.add(new TreeDarkOak());
		
		//Register Trees
		TreeRegistry.registerTrees(baseTrees);
		
		//Potions
		dendroPotion = new DendroPotion();
		
		//Dirt Bucket
		dirtBucket = new DirtBucket();
		
		//Bonsai Pot
		blockBonsaiPot = new BlockBonsaiPot();
		
		//Fruit
		blockFruitCocoa = new BlockFruitCocoa();
		
		//Creative Mode Stuff
		treeStaff = new Staff();
		
		//Set the creative tabs icon
		dynamicTreesTab.setTabIconItemStack(TreeRegistry.findTree("oak").getSeedStack());
		
		proxy.preInit();
		compatProxy.preInit();
		
		RegistrationHandler.registerBlocks();
		RegistrationHandler.registerItems();
		proxy.registerModels();
		
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about. Register recipes.
		
		if(WorldGenRegistry.isWorldGenEnabled()) {
			treeGenerator.biomeTreeHandler.init();
		}
		
		RegistrationHandler.registerRecipes();
		
		proxy.init();
		compatProxy.init();
	}

	@EventHandler
	public void PostInit(FMLPostInitializationEvent e){
		//Handle interaction with other mods, complete your setup based on this.
	}

	///////////////////////////////////////////
	// MISSING REMAPPING
	///////////////////////////////////////////

	/**
	 * Here we'll simply remap the old "growingtrees" modid to the new modid for old blocks and items.
	 * 
	 * @param event
	 */
	//Missing Blocks Resolved Here
	@EventHandler
	public void missingMappings(FMLMissingMappingsEvent event) {
		for(MissingMapping missing: event.getAll()) {
			ResourceLocation resLoc = new ResourceLocation(missing.name);
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(MODID).log(Level.CONFIG, "Remapping Missing Object: " + missing.name);
				if(missing.type == GameRegistry.Type.BLOCK) {
					Block mappedBlock = cpw.mods.fml.common.registry.GameRegistry.findBlock(DynamicTrees.MODID, path);
					if(mappedBlock != null) { //Null is what you get when do don't get what you're looking for.
						missing.remap(mappedBlock);
					}
				}
				else if(missing.type == GameRegistry.Type.ITEM) {
					Item mappedItem = cpw.mods.fml.common.registry.GameRegistry.findItem(DynamicTrees.MODID, path);
					if(mappedItem != null) { //Null is what you get when do don't get what you're looking for.
						missing.remap(mappedItem);
					}
				}
			}
		}
	}

	//Missing Items Resolved Here

	///////////////////////////////////////////
	// REGISTRATION
	///////////////////////////////////////////
	
	public static class RegistrationHandler {
		
		public static void registerBlocks() {

			GameRegistry.register(blockRootyDirt);
			GameRegistry.register(blockDynamicSapling);
			GameRegistry.register(blockBonsaiPot);
			GameRegistry.register(blockFruitCocoa);

			for(DynamicTree tree: baseTrees) {
				tree.registerBlocks();
			}

			for(BlockDynamicLeaves leavesBlock: TreeHelper.getLeavesMapForModId(MODID).values()) {
				GameRegistry.register(leavesBlock);
			}

			compatProxy.registerBlocks();
		}

		public static void registerItems() {
			GameRegistry.register(treeStaff);

			for(DynamicTree tree: baseTrees) {
				tree.registerItems();
			}

			GameRegistry.register(dendroPotion);
			GameRegistry.register(dirtBucket);
			
			//We don't need to register ItemBlocks in 1.7.10
			
			compatProxy.registerItems();
		}

		public static void registerRecipes() {
			for(DynamicTree tree: baseTrees) {
				tree.registerRecipes();
			}

			dirtBucket.registerRecipes();			
			dendroPotion.registerRecipes();
			
			compatProxy.registerRecipes();
		}
		
	}

}