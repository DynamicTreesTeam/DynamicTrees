package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {

    ///////////////////////////////////////////
	//BLOCKS
	///////////////////////////////////////////
    public static Block test;

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

    public static CommonBlockStates blockStates;

    public static void setupBlocks() {
        test = new TESTBLOCK();

    }

    public static void setupLeavesProperties() {
 //       leaves = LeavesPaging.build(new ResourceLocation(DynamicTrees.MODID, "leaves/common.json"));
 //       leaves.put("cactus", new LeavesProperties(null, ItemStack.EMPTY, TreeRegistry.findCellKit("bare")));//Explicitly unbuilt since there's no leaves
    }

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        blockRegistryEvent.getRegistry().register(test);
    }

    ///////////////////////////////////////////
    //ITEMS
    ///////////////////////////////////////////

    public static void setupItems() {

    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
        Item.Properties properties = new Item.Properties().group(DTRegistries.dynamicTreesTab);
        itemRegistryEvent.getRegistry().register(new BlockItem(test, properties)
                .setRegistryName(Objects.requireNonNull(test.getRegistryName())));
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


    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityRegistryEvent) {
//        tileEntityRegistryEvent.getRegistry().register();
    }

    public static void setupTileEntities() {
        //In 1.13 these will need to change to the proper Dynamic Trees domain but unfortunately for now it'll have
        //to stay in the minecraft domain for backwards compatibility with existing worldsaves.
//        GameRegistry.registerTileEntity(TileEntitySpecies.class, new ResourceLocation("minecraft", "species_tile_entity"));
//        GameRegistry.registerTileEntity(TileEntityBonsai.class, new ResourceLocation("minecraft", "bonsai_tile_entity"));
    }

    ///////////////////////////////////////////
    //MISC
    ///////////////////////////////////////////
    /**This is the creative tab that holds all DT items*/
    public static final ItemGroup dynamicTreesTab = new ItemGroup("dynamictrees") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(test);
        }
    };

    public static class CommonBlockStates {
        public final BlockState air = Blocks.AIR.getDefaultState();
        public final BlockState dirt = Blocks.DIRT.getDefaultState();
        public final BlockState sand = Blocks.SAND.getDefaultState();
        public final BlockState grass = Blocks.GRASS.getDefaultState();
        public final BlockState podzol = Blocks.PODZOL.getDefaultState();
        public final BlockState redMushroom = Blocks.RED_MUSHROOM.getDefaultState();
        public final BlockState brownMushroom = Blocks.BROWN_MUSHROOM.getDefaultState();
    }

}
