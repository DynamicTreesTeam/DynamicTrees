package com.ferreusveritas.dynamictrees.systems;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DirtHelper {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String DIRT_LIKE = "dirt_like";
    public static final String SAND_LIKE = "sand_like";
    public static final String GRAVEL_LIKE = "gravel_like";
    public static final String WATER_LIKE = "water_like";
    public static final String NETHER_LIKE = "nether_like";
    public static final String NETHER_SOIL_LIKE = "nether_soil_like";
    public static final String END_LIKE = "end_like";
    public static final String MUD_LIKE = "mud_like";
    public static final String TERRACOTTA_LIKE = "terracotta_like";
    public static final String SLIME_LIKE = "slime_like";
    public static final String FUNGUS_LIKE = "fungus_like";

    private static final Map<String, Integer> adjectiveMap;
    private static final Map<Block, Integer> dirtMap;

    static {
        adjectiveMap = new HashMap<>();
        dirtMap = new HashMap<>();

        createNewAdjective(DIRT_LIKE);
        createNewAdjective(SAND_LIKE);
        createNewAdjective(GRAVEL_LIKE);
        createNewAdjective(WATER_LIKE);
        createNewAdjective(NETHER_LIKE);
        createNewAdjective(NETHER_SOIL_LIKE);
        createNewAdjective(END_LIKE);
        createNewAdjective(MUD_LIKE);
        createNewAdjective(TERRACOTTA_LIKE);
        createNewAdjective(SLIME_LIKE);
        createNewAdjective(FUNGUS_LIKE);
    }

    public static void createNewAdjective(String adjName) {
        adjectiveMap.put(adjName, 1 << adjectiveMap.size());
    }

    private static int getFlags(String adjName) {
        return adjectiveMap.getOrDefault(adjName, 0);
    }

    public static void registerSoil(Block block, String adjName) {
        registerSoil(block, adjName, RootyBlockHelper.isBlockRegistered(block)?RootyBlockHelper.getRootyBlock(block):new RootyBlock(block));
    }

    public static void registerSoil(Block block, String adjName, Block rootyBlockSubstitute) {
        if (!RootyBlockHelper.isBlockRegistered(rootyBlockSubstitute)){
            LOGGER.error("Attempted to use " + rootyBlockSubstitute + " as a rooty block substitute for " + block + " but it had not been registered.");
            registerSoil(rootyBlockSubstitute, adjName);
        }
        registerSoil(block, adjName, RootyBlockHelper.getRootyBlock(rootyBlockSubstitute));
    }

    public static void registerSoil(Block block, String adjName, RootyBlock rootyDirt) {
        if(adjectiveMap.containsKey(adjName)) {
            int flag = adjectiveMap.get(adjName);
            registerSoil(block, flag);
            if (rootyDirt != null){
                //This creates a new rooty block for the newly registered soil block if one does not exist already.
                if (!RootyBlockHelper.isBlockRegistered(rootyDirt))
                    RootyBlockHelper.addToRootyBlocksMap(block, rootyDirt);
                registerSoil(rootyDirt, flag);
            }
        } else {
            LOGGER.error("Adjective \"" + adjName + "\" not found while registering soil block: " + block);
        }
    }

    public static void registerSoil(Block block, int adjFlag) {
        dirtMap.compute(block, (k, v) -> (v == null) ? adjFlag : v | adjFlag);
    }

    public static void registerSoil(Block block, Block copyFlagsFrom) {
        if (dirtMap.containsKey(copyFlagsFrom))
            registerSoil(block, dirtMap.get(copyFlagsFrom));
        else
            LOGGER.error("Flags from " + copyFlagsFrom + " not found while registering soil block: " + block);
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