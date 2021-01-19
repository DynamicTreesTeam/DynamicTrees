package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.systems.DirtHelper;
import net.minecraft.block.Block;

import java.util.*;

public class RootyBlockHelper {

    private static Map<Block, RootyBlock> rootyBlocksMap = new HashMap<>();
    private static LinkedList<RootyBlock> rootyBlocksList;

    /**
     * @param primitiveSoil normal block that trees can be planted on
     * @param rootyBlock rooty dirt that should represent the primitive dirt
     * @return
     */
    public static boolean addToRootyBlocksMap (Block primitiveSoil, RootyBlock rootyBlock){
        if (rootyBlocksMap.containsKey(primitiveSoil)){
            System.err.println("Attempted to register " + rootyBlock + " as the rooty block of " + primitiveSoil + " but it already had one.");
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
        return rootyBlocksList;
    }

}
