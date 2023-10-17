package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class MapColorDeserialiser implements JsonDeserialiser<MapColor> {

    private static final Map<ResourceLocation, MapColor> MATERIAL_COLORS =
            Util.make(new HashMap<>(), MapColors -> {
                MapColors.put(new ResourceLocation("none"), MapColor.NONE);
                MapColors.put(new ResourceLocation("grass"), MapColor.GRASS);
                MapColors.put(new ResourceLocation("sand"), MapColor.SAND);
                MapColors.put(new ResourceLocation("wool"), MapColor.WOOL);
                MapColors.put(new ResourceLocation("fire"), MapColor.FIRE);
                MapColors.put(new ResourceLocation("ice"), MapColor.ICE);
                MapColors.put(new ResourceLocation("metal"), MapColor.METAL);
                MapColors.put(new ResourceLocation("plant"), MapColor.PLANT);
                MapColors.put(new ResourceLocation("snow"), MapColor.SNOW);
                MapColors.put(new ResourceLocation("clay"), MapColor.CLAY);
                MapColors.put(new ResourceLocation("dirt"), MapColor.DIRT);
                MapColors.put(new ResourceLocation("stone"), MapColor.STONE);
                MapColors.put(new ResourceLocation("water"), MapColor.WATER);
                MapColors.put(new ResourceLocation("wood"), MapColor.WOOD);
                MapColors.put(new ResourceLocation("quartz"), MapColor.QUARTZ);
                MapColors.put(new ResourceLocation("color_orange"), MapColor.COLOR_ORANGE);
                MapColors.put(new ResourceLocation("color_magenta"), MapColor.COLOR_MAGENTA);
                MapColors.put(new ResourceLocation("color_light_blue"), MapColor.COLOR_LIGHT_BLUE);
                MapColors.put(new ResourceLocation("color_yellow"), MapColor.COLOR_YELLOW);
                MapColors.put(new ResourceLocation("color_light_green"), MapColor.COLOR_LIGHT_GREEN);
                MapColors.put(new ResourceLocation("color_pink"), MapColor.COLOR_PINK);
                MapColors.put(new ResourceLocation("color_gray"), MapColor.COLOR_GRAY);
                MapColors.put(new ResourceLocation("color_light_gray"), MapColor.COLOR_LIGHT_GRAY);
                MapColors.put(new ResourceLocation("color_cyan"), MapColor.COLOR_CYAN);
                MapColors.put(new ResourceLocation("color_purple"), MapColor.COLOR_PURPLE);
                MapColors.put(new ResourceLocation("color_blue"), MapColor.COLOR_BLUE);
                MapColors.put(new ResourceLocation("color_brown"), MapColor.COLOR_BROWN);
                MapColors.put(new ResourceLocation("color_green"), MapColor.COLOR_GREEN);
                MapColors.put(new ResourceLocation("color_red"), MapColor.COLOR_RED);
                MapColors.put(new ResourceLocation("color_black"), MapColor.COLOR_BLACK);
                MapColors.put(new ResourceLocation("gold"), MapColor.GOLD);
                MapColors.put(new ResourceLocation("diamond"), MapColor.DIAMOND);
                MapColors.put(new ResourceLocation("lapis"), MapColor.LAPIS);
                MapColors.put(new ResourceLocation("emerald"), MapColor.EMERALD);
                MapColors.put(new ResourceLocation("podzol"), MapColor.PODZOL);
                MapColors.put(new ResourceLocation("nether"), MapColor.NETHER);
                MapColors.put(new ResourceLocation("terracotta_white"), MapColor.TERRACOTTA_WHITE);
                MapColors.put(new ResourceLocation("terracotta_orange"), MapColor.TERRACOTTA_ORANGE);
                MapColors.put(new ResourceLocation("terracotta_magenta"), MapColor.TERRACOTTA_MAGENTA);
                MapColors.put(new ResourceLocation("terracotta_light_blue"), MapColor.TERRACOTTA_LIGHT_BLUE);
                MapColors.put(new ResourceLocation("terracotta_yellow"), MapColor.TERRACOTTA_YELLOW);
                MapColors.put(new ResourceLocation("terracotta_light_green"), MapColor.TERRACOTTA_LIGHT_GREEN);
                MapColors.put(new ResourceLocation("terracotta_pink"), MapColor.TERRACOTTA_PINK);
                MapColors.put(new ResourceLocation("terracotta_gray"), MapColor.TERRACOTTA_GRAY);
                MapColors.put(new ResourceLocation("terracotta_light_gray"), MapColor.TERRACOTTA_LIGHT_GRAY);
                MapColors.put(new ResourceLocation("terracotta_cyan"), MapColor.TERRACOTTA_CYAN);
                MapColors.put(new ResourceLocation("terracotta_purple"), MapColor.TERRACOTTA_PURPLE);
                MapColors.put(new ResourceLocation("terracotta_blue"), MapColor.TERRACOTTA_BLUE);
                MapColors.put(new ResourceLocation("terracotta_brown"), MapColor.TERRACOTTA_BROWN);
                MapColors.put(new ResourceLocation("terracotta_green"), MapColor.TERRACOTTA_GREEN);
                MapColors.put(new ResourceLocation("terracotta_red"), MapColor.TERRACOTTA_RED);
                MapColors.put(new ResourceLocation("terracotta_black"), MapColor.TERRACOTTA_BLACK);
                MapColors.put(new ResourceLocation("crimson_nylium"), MapColor.CRIMSON_NYLIUM);
                MapColors.put(new ResourceLocation("crimson_stem"), MapColor.CRIMSON_STEM);
                MapColors.put(new ResourceLocation("crimson_hyphae"), MapColor.CRIMSON_HYPHAE);
                MapColors.put(new ResourceLocation("warped_nylium"), MapColor.WARPED_NYLIUM);
                MapColors.put(new ResourceLocation("warped_stem"), MapColor.WARPED_STEM);
                MapColors.put(new ResourceLocation("warped_hyphae"), MapColor.WARPED_HYPHAE);
                MapColors.put(new ResourceLocation("warped_wart_block"), MapColor.WARPED_WART_BLOCK);
                MapColors.put(new ResourceLocation("deepslate"), MapColor.DEEPSLATE);
                MapColors.put(new ResourceLocation("raw_iron"), MapColor.RAW_IRON);
                MapColors.put(new ResourceLocation("glow_lichen"), MapColor.GLOW_LICHEN);
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
        return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
                .map(MATERIAL_COLORS::get, "Could not get material color from \"{}\".");
    }
}
