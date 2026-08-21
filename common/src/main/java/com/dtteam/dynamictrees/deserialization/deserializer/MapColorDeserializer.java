package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class MapColorDeserializer implements JsonDeserializer<MapColor> {

    private static final Map<Identifier, MapColor> MATERIAL_COLORS =
            Util.make(new HashMap<>(), MapColors -> {
                MapColors.put(Identifier.parse("none"), MapColor.NONE);
                MapColors.put(Identifier.parse("grass"), MapColor.GRASS);
                MapColors.put(Identifier.parse("sand"), MapColor.SAND);
                MapColors.put(Identifier.parse("wool"), MapColor.WOOL);
                MapColors.put(Identifier.parse("fire"), MapColor.FIRE);
                MapColors.put(Identifier.parse("ice"), MapColor.ICE);
                MapColors.put(Identifier.parse("metal"), MapColor.METAL);
                MapColors.put(Identifier.parse("plant"), MapColor.PLANT);
                MapColors.put(Identifier.parse("snow"), MapColor.SNOW);
                MapColors.put(Identifier.parse("clay"), MapColor.CLAY);
                MapColors.put(Identifier.parse("dirt"), MapColor.DIRT);
                MapColors.put(Identifier.parse("stone"), MapColor.STONE);
                MapColors.put(Identifier.parse("water"), MapColor.WATER);
                MapColors.put(Identifier.parse("wood"), MapColor.WOOD);
                MapColors.put(Identifier.parse("quartz"), MapColor.QUARTZ);
                MapColors.put(Identifier.parse("color_orange"), MapColor.COLOR_ORANGE);
                MapColors.put(Identifier.parse("color_magenta"), MapColor.COLOR_MAGENTA);
                MapColors.put(Identifier.parse("color_light_blue"), MapColor.COLOR_LIGHT_BLUE);
                MapColors.put(Identifier.parse("color_yellow"), MapColor.COLOR_YELLOW);
                MapColors.put(Identifier.parse("color_light_green"), MapColor.COLOR_LIGHT_GREEN);
                MapColors.put(Identifier.parse("color_pink"), MapColor.COLOR_PINK);
                MapColors.put(Identifier.parse("color_gray"), MapColor.COLOR_GRAY);
                MapColors.put(Identifier.parse("color_light_gray"), MapColor.COLOR_LIGHT_GRAY);
                MapColors.put(Identifier.parse("color_cyan"), MapColor.COLOR_CYAN);
                MapColors.put(Identifier.parse("color_purple"), MapColor.COLOR_PURPLE);
                MapColors.put(Identifier.parse("color_blue"), MapColor.COLOR_BLUE);
                MapColors.put(Identifier.parse("color_brown"), MapColor.COLOR_BROWN);
                MapColors.put(Identifier.parse("color_green"), MapColor.COLOR_GREEN);
                MapColors.put(Identifier.parse("color_red"), MapColor.COLOR_RED);
                MapColors.put(Identifier.parse("color_black"), MapColor.COLOR_BLACK);
                MapColors.put(Identifier.parse("gold"), MapColor.GOLD);
                MapColors.put(Identifier.parse("diamond"), MapColor.DIAMOND);
                MapColors.put(Identifier.parse("lapis"), MapColor.LAPIS);
                MapColors.put(Identifier.parse("emerald"), MapColor.EMERALD);
                MapColors.put(Identifier.parse("podzol"), MapColor.PODZOL);
                MapColors.put(Identifier.parse("nether"), MapColor.NETHER);
                MapColors.put(Identifier.parse("terracotta_white"), MapColor.TERRACOTTA_WHITE);
                MapColors.put(Identifier.parse("terracotta_orange"), MapColor.TERRACOTTA_ORANGE);
                MapColors.put(Identifier.parse("terracotta_magenta"), MapColor.TERRACOTTA_MAGENTA);
                MapColors.put(Identifier.parse("terracotta_light_blue"), MapColor.TERRACOTTA_LIGHT_BLUE);
                MapColors.put(Identifier.parse("terracotta_yellow"), MapColor.TERRACOTTA_YELLOW);
                MapColors.put(Identifier.parse("terracotta_light_green"), MapColor.TERRACOTTA_LIGHT_GREEN);
                MapColors.put(Identifier.parse("terracotta_pink"), MapColor.TERRACOTTA_PINK);
                MapColors.put(Identifier.parse("terracotta_gray"), MapColor.TERRACOTTA_GRAY);
                MapColors.put(Identifier.parse("terracotta_light_gray"), MapColor.TERRACOTTA_LIGHT_GRAY);
                MapColors.put(Identifier.parse("terracotta_cyan"), MapColor.TERRACOTTA_CYAN);
                MapColors.put(Identifier.parse("terracotta_purple"), MapColor.TERRACOTTA_PURPLE);
                MapColors.put(Identifier.parse("terracotta_blue"), MapColor.TERRACOTTA_BLUE);
                MapColors.put(Identifier.parse("terracotta_brown"), MapColor.TERRACOTTA_BROWN);
                MapColors.put(Identifier.parse("terracotta_green"), MapColor.TERRACOTTA_GREEN);
                MapColors.put(Identifier.parse("terracotta_red"), MapColor.TERRACOTTA_RED);
                MapColors.put(Identifier.parse("terracotta_black"), MapColor.TERRACOTTA_BLACK);
                MapColors.put(Identifier.parse("crimson_nylium"), MapColor.CRIMSON_NYLIUM);
                MapColors.put(Identifier.parse("crimson_stem"), MapColor.CRIMSON_STEM);
                MapColors.put(Identifier.parse("crimson_hyphae"), MapColor.CRIMSON_HYPHAE);
                MapColors.put(Identifier.parse("warped_nylium"), MapColor.WARPED_NYLIUM);
                MapColors.put(Identifier.parse("warped_stem"), MapColor.WARPED_STEM);
                MapColors.put(Identifier.parse("warped_hyphae"), MapColor.WARPED_HYPHAE);
                MapColors.put(Identifier.parse("warped_wart_block"), MapColor.WARPED_WART_BLOCK);
                MapColors.put(Identifier.parse("deepslate"), MapColor.DEEPSLATE);
                MapColors.put(Identifier.parse("raw_iron"), MapColor.RAW_IRON);
                MapColors.put(Identifier.parse("glow_lichen"), MapColor.GLOW_LICHEN);
            });

    /**
     * Registers given material color under the given name, if that name is not already taken.
     *
     * @param name          the name to register the material color under
     * @param MapColor the material color to register
     */
    public static void registerMapColor(Identifier name, MapColor MapColor) {
        MATERIAL_COLORS.putIfAbsent(name, MapColor);
    }

    public Result<MapColor, JsonElement> deserialize(JsonElement input) {
        return JsonDeserializers.RESOURCE_LOCATION.deserialize(input)
                .map(MATERIAL_COLORS::get, "Could not get material color from \"{}\".");
    }
}
