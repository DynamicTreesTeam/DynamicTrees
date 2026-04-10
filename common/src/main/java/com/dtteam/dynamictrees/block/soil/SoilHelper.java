package com.dtteam.dynamictrees.block.soil;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.deserialization.applier.PropertyApplierResult;
import com.dtteam.dynamictrees.deserialization.applier.VoidApplier;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.google.common.collect.BiMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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

    public static void registerSoil(Identifier name, Block soilBlock, String... adjNames) {
        if (soilBlock == Blocks.AIR) {
            return;
        }

        int flag = 0;
        for (String adjName : adjNames) {
            if (adjectiveMap.containsKey(adjName)) {
                flag |= adjectiveMap.get(adjName);
            } else {
                DynamicTrees.LOG.error("Adjective \"{}\" not found while registering soil block: {}", adjName, soilBlock);
                return;
            }
        }

        registerSoil(name, soilBlock, flag);
    }

    public static void registerSoil(Identifier name, Block soilBlock, int adjFlag) {
        dirtMap.compute(soilBlock, (bl, prop) -> (prop == null) ? new SoilProperties(soilBlock, name, adjFlag, true) : prop.addSoilFlags(adjFlag));
    }

    public static boolean isSoilAcceptable(BlockState soilState, int soilFlags) {
        Block soilBlock = soilState.getBlock();
        if (soilBlock instanceof SoilBlock) {
            soilBlock = ((SoilBlock) soilBlock).getPrimitiveSoilBlock();
        }
        //underwater foliage is taken as just water
        if ((soilState.getFluidState().is(Fluids.WATER) && soilState.is(DTBlockTags.FOLIAGE))){
            soilBlock = Blocks.WATER;
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

    public static int getSoilFlags(String... types) {
        int flags = 0;

        for (String t : types) {
            flags |= getFlags(t);
        }

        return flags;
    }

    public static <T> PropertyApplierResult applyIfSoilIsAcceptable(T value, String acceptableSoil, VoidApplier<T, String> soilApplier) {
        if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
            return PropertyApplierResult.failure("Could not find acceptable soil '" + acceptableSoil + "'.");
        }
        soilApplier.apply(value, acceptableSoil);
        return PropertyApplierResult.success();
    }
}