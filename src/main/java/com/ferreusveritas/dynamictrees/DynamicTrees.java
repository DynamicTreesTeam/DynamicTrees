package com.ferreusveritas.dynamictrees;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockBonsaiPot;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.blocks.BlockFruitCocoa;
import com.ferreusveritas.dynamictrees.blocks.BlockGrowingLeaves;
import com.ferreusveritas.dynamictrees.blocks.BlockRootyDirt;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.proxy.CCProxyActive;
import com.ferreusveritas.dynamictrees.proxy.CCProxyBase;
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
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
	public static CCProxyBase ccproxy;

	public static ArrayList<DynamicTree> baseTrees = new ArrayList<DynamicTree>();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		//Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry.

		//CircleDebug.initCircleTests(); //This is for circle generation testing purposes only

		ConfigHandler.preInit(event);

		//Dirt
		blockRootyDirt = new BlockRootyDirt();
		blockDynamicSapling = new BlockDynamicSapling("sapling");

		//Trees
		baseTrees.add(new TreeOak());
		baseTrees.add(new TreeSpruce());
		baseTrees.add(new TreeBirch());
		baseTrees.add(new TreeJungle());
		baseTrees.add(new TreeAcacia());
		baseTrees.add(new TreeDarkOak());

		//Register Trees
		for(DynamicTree tree: baseTrees) {
			TreeRegistry.registerTree(tree);
		}

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

		//Computercraft Creative Mode Stuff
		ccproxy = CCProxyBase.hasComputerCraft() ? new CCProxyActive() : new CCProxyBase();
		ccproxy.createBlocks();
		
		//Set the creative tabs icon
		dynamicTreesTab.setTabIconItemStack(TreeRegistry.findTree("oak").getSeedStack());

		RegistrationHandler.registerBlocks();
		RegistrationHandler.registerItems();

		proxy.preInit();
		
		proxy.registerEventHandlers();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Do your mod setup. Build whatever data structures you care about.
		//Register recipes.
		RegistrationHandler.registerRecipes();

		proxy.init();
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
			ResourceLocation resLoc = missing.resourceLocation;
			String domain = resLoc.getResourceDomain();
			String path = resLoc.getResourcePath();
			if(domain.equals("growingtrees")) {
				Logger.getLogger(MODID).log(Level.CONFIG, "Remapping Missing Object: " + missing.name);
				if(missing.type == GameRegistry.Type.BLOCK) {
					Block mappedBlock = Block.REGISTRY.getObject(new ResourceLocation(DynamicTrees.MODID, path));
					if(mappedBlock != Blocks.AIR) { //Air is what you get when do don't get what you're looking for.
						missing.remap(mappedBlock);
					}
				}
				else if(missing.type == GameRegistry.Type.ITEM) {
					Item mappedItem = Item.REGISTRY.getObject(new ResourceLocation(DynamicTrees.MODID, path));
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

			for(BlockGrowingLeaves leavesBlock: TreeHelper.leavesArray.values()) {
				GameRegistry.register(leavesBlock);
			}

			ccproxy.registerBlocks();
		}

		public static void registerItems() {
			GameRegistry.register(treeStaff);

			for(DynamicTree tree: baseTrees) {
				tree.registerItems();
			}

			GameRegistry.register(dendroPotion);
			GameRegistry.register(dirtBucket);

			for(BlockGrowingLeaves leavesBlock: TreeHelper.leavesArray.values()) {
				GameRegistry.register(new ItemBlock(leavesBlock).setRegistryName(leavesBlock.getRegistryName()));
			}

			ItemBlock itemBlock = new ItemBlock(blockRootyDirt);
			itemBlock.setRegistryName(blockRootyDirt.getRegistryName());
			GameRegistry.register(itemBlock);

			ItemBlock itemBonsaiBlock = new ItemBlock(blockBonsaiPot);
			itemBonsaiBlock.setRegistryName(blockBonsaiPot.getRegistryName());
			GameRegistry.register(itemBonsaiBlock);
			
			ccproxy.registerItems();
		}

		public static void registerRecipes() {
			for(DynamicTree tree: baseTrees) {
				tree.registerRecipes();
			}

			dirtBucket.registerRecipes();			
			dendroPotion.registerRecipes();
		}
		
	}

}