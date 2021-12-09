package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Max Hyper
 */
public class SoilHelper {

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

    public static void addSoilPropertiesToMap(SoilProperties properties) {
        if (!dirtMap.containsKey(properties.getPrimitiveSoilBlock()) && properties.getPrimitiveSoilBlock() != Blocks.AIR) {
            dirtMap.put(properties.getPrimitiveSoilBlock(), properties);
        }
    }

    public static void registerSoil(SoilProperties properties, String... adjNames) {
        addSoilPropertiesToMap(properties);
        registerSoil(properties.getRegistryName(), properties.getPrimitiveSoilBlock(), adjNames);
    }

    public static SoilProperties registerSoil(ResourceLocation name, Block soilBlock, String... adjNames) {
        if (soilBlock == Blocks.AIR) {
            return SoilProperties.NULL_SOIL_PROPERTIES;
        }

        int flag = 0;
        for (String adjName : adjNames) {
            if (adjectiveMap.containsKey(adjName)) {
                flag |= adjectiveMap.get(adjName);
            } else {
                LOGGER.error("Adjective \"" + adjName + "\" not found while registering soil block: " + soilBlock);
                return SoilProperties.NULL_SOIL_PROPERTIES;
            }
        }

        return registerSoil(name, soilBlock, flag);
    }

    public static SoilProperties registerSoil(ResourceLocation name, Block soilBlock, int adjFlag) {
        return dirtMap.compute(soilBlock, (bl, prop) -> (prop == null) ? new SoilProperties(soilBlock, name, adjFlag, true) : prop.addSoilFlags(adjFlag));
    }

    public static boolean isSoilAcceptable(BlockState soilState, int soilFlags) {
        Block soilBlock = soilState.getBlock();
        if (soilBlock instanceof RootyBlock) {
            soilBlock = ((RootyBlock) soilBlock).getPrimitiveSoilBlock();
        }
        SoilProperties properties = dirtMap.getOrDefault(soilBlock, SoilProperties.NULL_SOIL_PROPERTIES);
        return (properties.getSoilFlags() & soilFlags) != 0 && properties.isValidState(soilState);
    }

    public static boolean isSoilRegistered(Block block) {
        return dirtMap.containsKey(block);
    }

    public static SoilProperties getProperties(Block block) {
        return dirtMap.getOrDefault(block, SoilProperties.NULL_SOIL_PROPERTIES);
    }

    public static Set<RootyBlock> getRootyBlocksList() {
        return dirtMap.values().stream().map(SoilProperties::getDynamicSoilBlock).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static int getSoilFlags(String... types) {
        int flags = 0;

        for (String t : types) {
            flags |= getFlags(t);
        }

        return flags;
    }

}