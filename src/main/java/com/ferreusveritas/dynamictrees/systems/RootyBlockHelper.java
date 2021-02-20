package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Dynamically creates rooty blocks
 *
 * @author Max Hyper
 */
public class RootyBlockHelper {

    private static final Map<Block, RootyBlock> rootyBlocksMap = new HashMap<>();
    private static LinkedList<RootyBlock> rootyBlocksList;

    /**
     * @param primitiveSoil normal block that trees can be planted on
     * @param rootyBlock rooty dirt that should represent the primitive dirt
     * @return True if block was added, false if it already had a rooty block.
     */
    public static boolean addToRootyBlocksMap (Block primitiveSoil, RootyBlock rootyBlock){
        if (rootyBlocksMap.containsKey(primitiveSoil)){
            LogManager.getLogger().warn("Attempted to register " + rootyBlock + " as the rooty block of " + primitiveSoil + " but it already had " + rootyBlocksMap.get(primitiveSoil));
            return false;
        }
        rootyBlocksMap.put(primitiveSoil, rootyBlock);
        DirtHelper.registerSoil(rootyBlock, primitiveSoil);
        return true;
    }

    public static boolean isBlockRegistered (Block block){
        return rootyBlocksMap.containsKey(block);
    }
    public static RootyBlock getRootyBlock (Block block){
        return rootyBlocksMap.get(block);
    }

    public static LinkedList<RootyBlock> generateListForRegistry(boolean forceRemap){
        return generateListForRegistry(forceRemap, null);
    }

    public static LinkedList<RootyBlock> generateListForRegistry(boolean forceRemap, String namespace){
        if (rootyBlocksList == null){
            rootyBlocksList = new LinkedList<>();
            forceRemap = true;
        }
        if (forceRemap){
            for (RootyBlock rooty : rootyBlocksMap.values()){
                if (!rootyBlocksList.contains(rooty)){
                    rootyBlocksList.add(rooty);
                }
            }
        }
        LinkedList<RootyBlock> thisModRootyBlocks = new LinkedList<>();
        for (RootyBlock block : rootyBlocksList){
            if (namespace == null || block.getRegistryName().getNamespace().equals(namespace)){
                thisModRootyBlocks.add(block);
            }
        }
        return thisModRootyBlocks;
    }

}
