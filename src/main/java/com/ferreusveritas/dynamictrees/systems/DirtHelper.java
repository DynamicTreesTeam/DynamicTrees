package com.ferreusveritas.dynamictrees.systems;

import java.util.HashMap;
import java.util.Map;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import net.minecraft.block.Block;

public class DirtHelper {

    public static final String DIRTLIKE = "dirtlike";
    public static final String SANDLIKE = "sandlike";
    public static final String GRAVELLIKE = "gravellike";
    public static final String WATERLIKE = "waterlike";
    public static final String NETHERLIKE = "netherlike";
    public static final String ENDLIKE = "endlike";
    public static final String MUDLIKE = "mudlike";
    public static final String HARDCLAYLIKE = "hardclaylike";
    public static final String SLIMELIKE = "slimelike";
    public static final String FUNGUSLIKE = "funguslike";

    private static final Map<String, Integer> adjectiveMap;
    private static final Map<Block, Integer> dirtMap;

    static {
        adjectiveMap = new HashMap<>();
        dirtMap = new HashMap<>();

        createNewAdjective(DIRTLIKE);
        createNewAdjective(SANDLIKE);
        createNewAdjective(GRAVELLIKE);
        createNewAdjective(WATERLIKE);
        createNewAdjective(NETHERLIKE);
        createNewAdjective(ENDLIKE);
        createNewAdjective(MUDLIKE);
        createNewAdjective(HARDCLAYLIKE);
        createNewAdjective(SLIMELIKE);
        createNewAdjective(FUNGUSLIKE);
    }

    public static void createNewAdjective(String adjName) {
        adjectiveMap.put(adjName, 1 << adjectiveMap.size());
    }

    private static int getFlags(String adjName) {
        return adjectiveMap.getOrDefault(adjName, 0);
    }

    public static void registerSoil(Block block, String adjName) {
        registerSoil(block, adjName, RootyBlockHelper.isBlockRegistered(block)?null:new RootyBlock(block));
    }
    public static void registerSoil(Block block, String adjName, Block rootyDirtSubstitute) {
        if (!RootyBlockHelper.isBlockRegistered(rootyDirtSubstitute)){
            System.err.println("Attempted to use "+rootyDirtSubstitute+" as a rooty dirt substitute for "+ block + " but it had not been registered.");
            registerSoil(rootyDirtSubstitute, adjName);
        }
        registerSoil(block, adjName, RootyBlockHelper.getRootyBlock(rootyDirtSubstitute));
    }
    public static void registerSoil(Block block, String adjName, RootyBlock rootyDirt) {
        if(adjectiveMap.containsKey(adjName)) {
            int flag = adjectiveMap.get(adjName);
            registerSoil(block, flag);
            if (rootyDirt != null)
                RootyBlockHelper.addToRootyBlocksMap(block, rootyDirt);
        } else {
            System.err.println("Adjective \"" + adjName + "\" not found while registering soil block: " + block);
        }
    }
    public static void registerSoil(Block block, int adjFlag) {
        dirtMap.compute(block, (k, v) -> (v == null) ? adjFlag : v | adjFlag);
    }
    public static void registerSoil(Block block, Block copyFlagsFrom) {
        if (dirtMap.containsKey(copyFlagsFrom))
            dirtMap.put(block, dirtMap.get(copyFlagsFrom));
        else
            System.err.println("Flags from " + copyFlagsFrom + " not found while registering soil block: " + block);
    }

    public static boolean isSoilAcceptable(Block block, int soilFlags) {
        return (dirtMap.getOrDefault(block, 0) & soilFlags) != 0;
    }

    public static int getSoilFlags(String ... types) {
        int flags = 0;

        for(String t : types) {
            flags |= getFlags(t);
        }

        return flags;
    }

}