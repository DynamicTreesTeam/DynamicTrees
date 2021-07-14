package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final Map<Block, SoilProperties> dirtMap;

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

    public static SoilProperties registerSoil(SoilProperties properties, String adjName) {
        if (!dirtMap.containsKey(properties.getPrimitiveSoilBlock()))
            dirtMap.put(properties.getPrimitiveSoilBlock(), properties);
        return registerSoil(properties.getRegistryName(), properties.getPrimitiveSoilBlock(), adjName);
    }

    public static SoilProperties registerSoil(ResourceLocation name, Block block, String adjName, RootyBlock generatedRootyBlock) {
        if (dirtMap.containsKey(block)){
            LogManager.getLogger().warn("Attempted to register " + generatedRootyBlock + " as the rooty block of " + block + " but it already had " + dirtMap.get(block));
            return SoilProperties.NULL_PROPERTIES;
        } else {
            SoilProperties properties = new SoilProperties(block, name,0,false);
            properties.setDynamicSoilBlock(generatedRootyBlock);
            dirtMap.put(block, properties);
            return registerSoil(name, block, adjName);
        }
    }

    //Crude way of creating a substitute where two blocks will point to the same soil properties.
    //Avoid if possible.
    public static void addSoilSubstitute(Block block, Block fakeSubstitute) {
        if (!dirtMap.containsKey(fakeSubstitute)){
            LOGGER.error("Attempted to use " + fakeSubstitute + " as a rooty block substitute for " + block + " but it had not been registered.");
        }
        dirtMap.put(block, dirtMap.get(fakeSubstitute));
    }

    public static void addSoilTag(Block block, String adjName) {
        registerSoil(null, block, adjName);
    }
    public static SoilProperties registerSoil(ResourceLocation name, Block block, String adjName) {
        if(adjectiveMap.containsKey(adjName)) {
            int flag = adjectiveMap.get(adjName);
            return registerSoil(name, block, flag);
        } else {
            LOGGER.error("Adjective \"" + adjName + "\" not found while registering soil block: " + block);
            return SoilProperties.NULL_PROPERTIES;
        }
    }

    public static SoilProperties registerSoil(ResourceLocation name, Block block, int adjFlag) {
        return dirtMap.compute(block, (bl, prop) -> (prop == null) ? new SoilProperties(block, name, adjFlag, true) : prop.addSoilFlags(adjFlag));
    }

    public static boolean isSoilAcceptable(Block block, int soilFlags) {
        if (block instanceof RootyBlock)
            block = ((RootyBlock) block).getPrimitiveSoilBlock();
        return (dirtMap.getOrDefault(block, SoilProperties.NULL_PROPERTIES).getSoilFlags() & soilFlags) != 0;
    }

    public static boolean isSoilRegistered(Block block){
        return dirtMap.containsKey(block);
    }

    public static SoilProperties getProperties (Block block){
        return dirtMap.getOrDefault(block, SoilProperties.NULL_PROPERTIES);
    }

    public static Set<RootyBlock> getRootyBlocksList (){
        return dirtMap.values().stream().map(SoilProperties::getDynamicSoilBlock).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static int getSoilFlags(String ... types) {
        int flags = 0;

        for(String t : types) {
            flags |= getFlags(t);
        }

        return flags;
    }

}