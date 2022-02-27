package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class MaterialColorDeserialiser implements JsonDeserialiser<MaterialColor> {

    private static final Map<ResourceLocation, MaterialColor> MATERIAL_COLORS =
            Util.make(new HashMap<>(), materialColors -> {
                materialColors.put(new ResourceLocation("none"), MaterialColor.NONE);
                materialColors.put(new ResourceLocation("grass"), MaterialColor.GRASS);
                materialColors.put(new ResourceLocation("sand"), MaterialColor.SAND);
                materialColors.put(new ResourceLocation("wool"), MaterialColor.WOOL);
                materialColors.put(new ResourceLocation("fire"), MaterialColor.FIRE);
                materialColors.put(new ResourceLocation("ice"), MaterialColor.ICE);
                materialColors.put(new ResourceLocation("metal"), MaterialColor.METAL);
                materialColors.put(new ResourceLocation("plant"), MaterialColor.PLANT);
                materialColors.put(new ResourceLocation("snow"), MaterialColor.SNOW);
                materialColors.put(new ResourceLocation("clay"), MaterialColor.CLAY);
                materialColors.put(new ResourceLocation("dirt"), MaterialColor.DIRT);
                materialColors.put(new ResourceLocation("stone"), MaterialColor.STONE);
                materialColors.put(new ResourceLocation("water"), MaterialColor.WATER);
                materialColors.put(new ResourceLocation("wood"), MaterialColor.WOOD);
                materialColors.put(new ResourceLocation("quartz"), MaterialColor.QUARTZ);
                materialColors.put(new ResourceLocation("color_orange"), MaterialColor.COLOR_ORANGE);
                materialColors.put(new ResourceLocation("color_magenta"), MaterialColor.COLOR_MAGENTA);
                materialColors.put(new ResourceLocation("color_light_blue"), MaterialColor.COLOR_LIGHT_BLUE);
                materialColors.put(new ResourceLocation("color_yellow"), MaterialColor.COLOR_YELLOW);
                materialColors.put(new ResourceLocation("color_light_green"), MaterialColor.COLOR_LIGHT_GREEN);
                materialColors.put(new ResourceLocation("color_pink"), MaterialColor.COLOR_PINK);
                materialColors.put(new ResourceLocation("color_gray"), MaterialColor.COLOR_GRAY);
                materialColors.put(new ResourceLocation("color_light_gray"), MaterialColor.COLOR_LIGHT_GRAY);
                materialColors.put(new ResourceLocation("color_cyan"), MaterialColor.COLOR_CYAN);
                materialColors.put(new ResourceLocation("color_purple"), MaterialColor.COLOR_PURPLE);
                materialColors.put(new ResourceLocation("color_blue"), MaterialColor.COLOR_BLUE);
                materialColors.put(new ResourceLocation("color_brown"), MaterialColor.COLOR_BROWN);
                materialColors.put(new ResourceLocation("color_green"), MaterialColor.COLOR_GREEN);
                materialColors.put(new ResourceLocation("color_red"), MaterialColor.COLOR_RED);
                materialColors.put(new ResourceLocation("color_black"), MaterialColor.COLOR_BLACK);
                materialColors.put(new ResourceLocation("gold"), MaterialColor.GOLD);
                materialColors.put(new ResourceLocation("diamond"), MaterialColor.DIAMOND);
                materialColors.put(new ResourceLocation("lapis"), MaterialColor.LAPIS);
                materialColors.put(new ResourceLocation("emerald"), MaterialColor.EMERALD);
                materialColors.put(new ResourceLocation("podzol"), MaterialColor.PODZOL);
                materialColors.put(new ResourceLocation("nether"), MaterialColor.NETHER);
                materialColors.put(new ResourceLocation("terracotta_white"), MaterialColor.TERRACOTTA_WHITE);
                materialColors.put(new ResourceLocation("terracotta_orange"), MaterialColor.TERRACOTTA_ORANGE);
                materialColors.put(new ResourceLocation("terracotta_magenta"), MaterialColor.TERRACOTTA_MAGENTA);
                materialColors.put(new ResourceLocation("terracotta_light_blue"), MaterialColor.TERRACOTTA_LIGHT_BLUE);
                materialColors.put(new ResourceLocation("terracotta_yellow"), MaterialColor.TERRACOTTA_YELLOW);
                materialColors.put(new ResourceLocation("terracotta_light_green"),
                        MaterialColor.TERRACOTTA_LIGHT_GREEN);
                materialColors.put(new ResourceLocation("terracotta_pink"), MaterialColor.TERRACOTTA_PINK);
                materialColors.put(new ResourceLocation("terracotta_gray"), MaterialColor.TERRACOTTA_GRAY);
                materialColors.put(new ResourceLocation("terracotta_light_gray"), MaterialColor.TERRACOTTA_LIGHT_GRAY);
                materialColors.put(new ResourceLocation("terracotta_cyan"), MaterialColor.TERRACOTTA_CYAN);
                materialColors.put(new ResourceLocation("terracotta_purple"), MaterialColor.TERRACOTTA_PURPLE);
                materialColors.put(new ResourceLocation("terracotta_blue"), MaterialColor.TERRACOTTA_BLUE);
                materialColors.put(new ResourceLocation("terracotta_brown"), MaterialColor.TERRACOTTA_BROWN);
                materialColors.put(new ResourceLocation("terracotta_green"), MaterialColor.TERRACOTTA_GREEN);
                materialColors.put(new ResourceLocation("terracotta_red"), MaterialColor.TERRACOTTA_RED);
                materialColors.put(new ResourceLocation("terracotta_black"), MaterialColor.TERRACOTTA_BLACK);
                materialColors.put(new ResourceLocation("crimson_nylium"), MaterialColor.CRIMSON_NYLIUM);
                materialColors.put(new ResourceLocation("crimson_stem"), MaterialColor.CRIMSON_STEM);
                materialColors.put(new ResourceLocation("crimson_hyphae"), MaterialColor.CRIMSON_HYPHAE);
                materialColors.put(new ResourceLocation("warped_nylium"), MaterialColor.WARPED_NYLIUM);
                materialColors.put(new ResourceLocation("warped_stem"), MaterialColor.WARPED_STEM);
                materialColors.put(new ResourceLocation("warped_hyphae"), MaterialColor.WARPED_HYPHAE);
                materialColors.put(new ResourceLocation("warped_wart_block"), MaterialColor.WARPED_WART_BLOCK);
            });

    /**
     * Registers given material color under the given name, if that name is not already taken.
     *
     * @param name          the name to register the material color under
     * @param materialColor the material color to register
     */
    public static void registerMaterialColor(ResourceLocation name, MaterialColor materialColor) {
        MATERIAL_COLORS.putIfAbsent(name, materialColor);
    }

    @Override
    public Result<MaterialColor, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
                .map(MATERIAL_COLORS::get, "Could not get material color from \"{}\".");
    }
}
