package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.*;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.ferreusveritas.dynamictrees.items.DirtBucket;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.tileentity.TileEntityBonsai;
import com.ferreusveritas.dynamictrees.tileentity.TileEntitySpecies;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamilyVanilla;
import com.ferreusveritas.dynamictrees.trees.TreeOak;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.*;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {

    ///////////////////////////////////////////
	//BLOCKS
	///////////////////////////////////////////
    public static BlockRooty blockRootyDirt;
    public static BlockRooty blockRootySand;
    public static BlockRooty blockRootyDirtSpecies;
    public static Block blockRootyDirtFake;
    public static BlockDynamicSapling blockDynamicSapling;
    public static BlockFruit blockApple;
    public static BlockFruitCocoa blockFruitCocoa;
    public static BlockBonsaiPot blockBonsaiPot;
    public static BlockTrunkShell blockTrunkShell;

    public static Map<String, ILeavesProperties> leaves = new HashMap<>();

    public static final CommonBlockStates blockStates = new CommonBlockStates();

    public static void setupBlocks() {
        blockRootyDirt = new BlockRootyDirt(false);//Dirt
        blockRootySand = new BlockRootySand(false);//Sand
        blockRootyDirtSpecies = new BlockRootyDirt(true);//Special dirt for rarer species
        blockRootyDirtFake = new BlockRootyDirtFake("rootydirtfake");
        blockDynamicSapling = new BlockDynamicSapling("sapling");//Dynamic version of a Vanilla sapling
        blockBonsaiPot = new BlockBonsaiPot();//Bonsai Pot
        blockFruitCocoa = new BlockFruitCocoa();//Modified Cocoa pods
        blockApple = new BlockFruit().setDroppedItem(new ItemStack(Items.APPLE));//Apple
        blockTrunkShell = new BlockTrunkShell();

        setupLeavesProperties();
    }

    public static void setupLeavesProperties() {
        leaves = LeavesPaging.build(new ResourceLocation(DynamicTrees.MODID, "leaves/common.json"));
        leaves.put("cactus", new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare")));//Explicitly unbuilt since there's no leaves
    }

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();

        ArrayList<Block> treeBlocks = new ArrayList<Block>();
        DTTrees.baseFamilies.forEach(tree -> tree.getRegisterableBlocks(treeBlocks));
        DTTrees.dynamicCactus.getRegisterableBlocks(treeBlocks);
        treeBlocks.addAll(LeavesPaging.getLeavesMapForModId(DynamicTrees.MODID).values());

        registry.registerAll(
                blockRootyDirt,
                blockRootySand,
                blockRootyDirtSpecies,
                blockRootyDirtFake,
                blockDynamicSapling,
                blockBonsaiPot,
                blockFruitCocoa,
                blockApple,
                blockTrunkShell
        );

        registry.registerAll(treeBlocks.toArray(new Block[0]));
    }

    ///////////////////////////////////////////
    //ITEMS
    ///////////////////////////////////////////

    public static DendroPotion dendroPotion;
    public static DirtBucket dirtBucket;
    public static Staff treeStaff;

    public static void setupItems() {
        dendroPotion = new DendroPotion();//Potions
        dirtBucket = new DirtBucket();//Dirt Bucket
        treeStaff = new Staff();//Creative Mode Staff
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
        IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();

        ArrayList<Item> treeItems = new ArrayList<Item>();
        DTTrees.baseFamilies.forEach(tree -> tree.getRegisterableItems(treeItems));
        DTTrees.dynamicCactus.getRegisterableItems(treeItems);

        registry.registerAll(dendroPotion, dirtBucket, treeStaff);
        registry.registerAll(treeItems.toArray(new Item[0]));

//        Item.Properties properties = new Item.Properties().group(DTRegistries.dynamicTreesTab);
    }

    ///////////////////////////////////////////
    //ENTITIES
    ///////////////////////////////////////////

    public static void setupEntities() {

    }

    @SubscribeEvent
    public static void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> entityRegistryEvent) {
    }

    ///////////////////////////////////////////
    //TILE ENTITIES
    ///////////////////////////////////////////

    public static void setupTileEntities() {
        //In 1.13 these will need to change to the proper Dynamic Trees domain but unfortunately for now it'll have
        //to stay in the minecraft domain for backwards compatibility with existing worldsaves.
//        GameRegistry.registerTileEntity(TileEntitySpecies.class, new ResourceLocation("minecraft", "species_tile_entity"));
//        GameRegistry.registerTileEntity(TileEntityBonsai.class, new ResourceLocation("minecraft", "bonsai_tile_entity"));
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
        tileEntityRegistryEvent.getRegistry().register(TileEntityType.Builder.create(TileEntitySpecies::new, blockRootyDirtSpecies).build(null).setRegistryName(blockRootyDirtSpecies.getRegistryName()));
        tileEntityRegistryEvent.getRegistry().register(TileEntityType.Builder.create(TileEntityBonsai::new, blockBonsaiPot).build(null).setRegistryName(blockBonsaiPot.getRegistryName()));
    }

    ///////////////////////////////////////////
    //MISC
    ///////////////////////////////////////////
    /**This is the creative tab that holds all DT items*/
    public static final ItemGroup dynamicTreesTab = new ItemGroup("dynamictrees") {
        @Override
        public ItemStack createIcon() {
            return TreeRegistry.findSpeciesSloppy(DynamicTrees.VanillaWoodTypes.oak.toString()).getSeedStack(1);
        }
    };

    public static final class CommonBlockStates {
        public final BlockState air = Blocks.AIR.getDefaultState();
        public final BlockState dirt = Blocks.DIRT.getDefaultState();
        public final BlockState coarsedirt = Blocks.COARSE_DIRT.getDefaultState();
        public final BlockState sand = Blocks.SAND.getDefaultState();
        public final BlockState grass = Blocks.GRASS.getDefaultState();
        public final BlockState podzol = Blocks.PODZOL.getDefaultState();
        public final BlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
        public final BlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
    }

}
