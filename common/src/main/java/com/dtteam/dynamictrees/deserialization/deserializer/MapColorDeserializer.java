package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class MapColorDeserializer implements JsonDeserializer<MapColor> {

    private static final Map<ResourceLocation, MapColor> MATERIAL_COLORS =
            Util.make(new HashMap<>(), MapColors -> {
                MapColors.put(ResourceLocation.parse("none"), MapColor.NONE);
                MapColors.put(ResourceLocation.parse("grass"), MapColor.GRASS);
                MapColors.put(ResourceLocation.parse("sand"), MapColor.SAND);
                MapColors.put(ResourceLocation.parse("wool"), MapColor.WOOL);
                MapColors.put(ResourceLocation.parse("fire"), MapColor.FIRE);
                MapColors.put(ResourceLocation.parse("ice"), MapColor.ICE);
                MapColors.put(ResourceLocation.parse("metal"), MapColor.METAL);
                MapColors.put(ResourceLocation.parse("plant"), MapColor.PLANT);
                MapColors.put(ResourceLocation.parse("snow"), MapColor.SNOW);
                MapColors.put(ResourceLocation.parse("clay"), MapColor.CLAY);
                MapColors.put(ResourceLocation.parse("dirt"), MapColor.DIRT);
                MapColors.put(ResourceLocation.parse("stone"), MapColor.STONE);
                MapColors.put(ResourceLocation.parse("water"), MapColor.WATER);
                MapColors.put(ResourceLocation.parse("wood"), MapColor.WOOD);
                MapColors.put(ResourceLocation.parse("quartz"), MapColor.QUARTZ);
                MapColors.put(ResourceLocation.parse("color_orange"), MapColor.COLOR_ORANGE);
                MapColors.put(ResourceLocation.parse("color_magenta"), MapColor.COLOR_MAGENTA);
                MapColors.put(ResourceLocation.parse("color_light_blue"), MapColor.COLOR_LIGHT_BLUE);
                MapColors.put(ResourceLocation.parse("color_yellow"), MapColor.COLOR_YELLOW);
                MapColors.put(ResourceLocation.parse("color_light_green"), MapColor.COLOR_LIGHT_GREEN);
                MapColors.put(ResourceLocation.parse("color_pink"), MapColor.COLOR_PINK);
                MapColors.put(ResourceLocation.parse("color_gray"), MapColor.COLOR_GRAY);
                MapColors.put(ResourceLocation.parse("color_light_gray"), MapColor.COLOR_LIGHT_GRAY);
                MapColors.put(ResourceLocation.parse("color_cyan"), MapColor.COLOR_CYAN);
                MapColors.put(ResourceLocation.parse("color_purple"), MapColor.COLOR_PURPLE);
                MapColors.put(ResourceLocation.parse("color_blue"), MapColor.COLOR_BLUE);
                MapColors.put(ResourceLocation.parse("color_brown"), MapColor.COLOR_BROWN);
                MapColors.put(ResourceLocation.parse("color_green"), MapColor.COLOR_GREEN);
                MapColors.put(ResourceLocation.parse("color_red"), MapColor.COLOR_RED);
                MapColors.put(ResourceLocation.parse("color_black"), MapColor.COLOR_BLACK);
                MapColors.put(ResourceLocation.parse("gold"), MapColor.GOLD);
                MapColors.put(ResourceLocation.parse("diamond"), MapColor.DIAMOND);
                MapColors.put(ResourceLocation.parse("lapis"), MapColor.LAPIS);
                MapColors.put(ResourceLocation.parse("emerald"), MapColor.EMERALD);
                MapColors.put(ResourceLocation.parse("podzol"), MapColor.PODZOL);
                MapColors.put(ResourceLocation.parse("nether"), MapColor.NETHER);
                MapColors.put(ResourceLocation.parse("terracotta_white"), MapColor.TERRACOTTA_WHITE);
                MapColors.put(ResourceLocation.parse("terracotta_orange"), MapColor.TERRACOTTA_ORANGE);
                MapColors.put(ResourceLocation.parse("terracotta_magenta"), MapColor.TERRACOTTA_MAGENTA);
                MapColors.put(ResourceLocation.parse("terracotta_light_blue"), MapColor.TERRACOTTA_LIGHT_BLUE);
                MapColors.put(ResourceLocation.parse("terracotta_yellow"), MapColor.TERRACOTTA_YELLOW);
                MapColors.put(ResourceLocation.parse("terracotta_light_green"), MapColor.TERRACOTTA_LIGHT_GREEN);
                MapColors.put(ResourceLocation.parse("terracotta_pink"), MapColor.TERRACOTTA_PINK);
                MapColors.put(ResourceLocation.parse("terracotta_gray"), MapColor.TERRACOTTA_GRAY);
                MapColors.put(ResourceLocation.parse("terracotta_light_gray"), MapColor.TERRACOTTA_LIGHT_GRAY);
                MapColors.put(ResourceLocation.parse("terracotta_cyan"), MapColor.TERRACOTTA_CYAN);
                MapColors.put(ResourceLocation.parse("terracotta_purple"), MapColor.TERRACOTTA_PURPLE);
                MapColors.put(ResourceLocation.parse("terracotta_blue"), MapColor.TERRACOTTA_BLUE);
                MapColors.put(ResourceLocation.parse("terracotta_brown"), MapColor.TERRACOTTA_BROWN);
                MapColors.put(ResourceLocation.parse("terracotta_green"), MapColor.TERRACOTTA_GREEN);
                MapColors.put(ResourceLocation.parse("terracotta_red"), MapColor.TERRACOTTA_RED);
                MapColors.put(ResourceLocation.parse("terracotta_black"), MapColor.TERRACOTTA_BLACK);
                MapColors.put(ResourceLocation.parse("crimson_nylium"), MapColor.CRIMSON_NYLIUM);
                MapColors.put(ResourceLocation.parse("crimson_stem"), MapColor.CRIMSON_STEM);
                MapColors.put(ResourceLocation.parse("crimson_hyphae"), MapColor.CRIMSON_HYPHAE);
                MapColors.put(ResourceLocation.parse("warped_nylium"), MapColor.WARPED_NYLIUM);
                MapColors.put(ResourceLocation.parse("warped_stem"), MapColor.WARPED_STEM);
                MapColors.put(ResourceLocation.parse("warped_hyphae"), MapColor.WARPED_HYPHAE);
                MapColors.put(ResourceLocation.parse("warped_wart_block"), MapColor.WARPED_WART_BLOCK);
                MapColors.put(ResourceLocation.parse("deepslate"), MapColor.DEEPSLATE);
                MapColors.put(ResourceLocation.parse("raw_iron"), MapColor.RAW_IRON);
                MapColors.put(ResourceLocation.parse("glow_lichen"), MapColor.GLOW_LICHEN);
            });

    /**
     * Registers given material color under the given name, if that name is not already taken.
     *
     * @param name          the name to register the material color under
     * @param MapColor the material color to register
     */
    public static void registerMapColor(ResourceLocation name, MapColor MapColor) {
        MATERIAL_COLORS.putIfAbsent(name, MapColor);
    }

    @Override
    public Result<MapColor, JsonElement> deserialise(JsonElement input) {
        return JsonDeserializers.RESOURCE_LOCATION.deserialise(input)
                .map(MATERIAL_COLORS::get, "Could not get material color from \"{}\".");
    }
}
