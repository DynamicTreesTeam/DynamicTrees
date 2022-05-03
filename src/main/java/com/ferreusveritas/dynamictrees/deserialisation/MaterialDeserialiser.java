package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class MaterialDeserialiser implements JsonDeserialiser<Material> {

    private static final Map<ResourceLocation, Material> MATERIALS = Util.make(new HashMap<>(), materials -> {
        materials.put(new ResourceLocation("air"), Material.AIR);
        materials.put(new ResourceLocation("structural_air"), Material.STRUCTURAL_AIR);
        materials.put(new ResourceLocation("portal"), Material.PORTAL);
        materials.put(new ResourceLocation("cloth_decoration"), Material.CLOTH_DECORATION);
        materials.put(new ResourceLocation("plant"), Material.PLANT);
        materials.put(new ResourceLocation("water_plant"), Material.WATER_PLANT);
        materials.put(new ResourceLocation("replaceable_plant"), Material.REPLACEABLE_PLANT);
        materials.put(new ResourceLocation("replaceable_fireproof_plant"), Material.REPLACEABLE_FIREPROOF_PLANT);
        materials.put(new ResourceLocation("replaceable_water_plant"), Material.REPLACEABLE_WATER_PLANT);
        materials.put(new ResourceLocation("water"), Material.WATER);
        materials.put(new ResourceLocation("bubble_column"), Material.BUBBLE_COLUMN);
        materials.put(new ResourceLocation("lava"), Material.LAVA);
        materials.put(new ResourceLocation("top_snow"), Material.TOP_SNOW);
        materials.put(new ResourceLocation("fire"), Material.FIRE);
        materials.put(new ResourceLocation("decoration"), Material.DECORATION);
        materials.put(new ResourceLocation("web"), Material.WEB);
        materials.put(new ResourceLocation("buildable_glass"), Material.BUILDABLE_GLASS);
        materials.put(new ResourceLocation("clay"), Material.CLAY);
        materials.put(new ResourceLocation("dirt"), Material.DIRT);
        materials.put(new ResourceLocation("grass"), Material.GRASS);
        materials.put(new ResourceLocation("ice_solid"), Material.ICE_SOLID);
        materials.put(new ResourceLocation("sand"), Material.SAND);
        materials.put(new ResourceLocation("sponge"), Material.SPONGE);
        materials.put(new ResourceLocation("shulker_shell"), Material.SHULKER_SHELL);
        materials.put(new ResourceLocation("wood"), Material.WOOD);
        materials.put(new ResourceLocation("nether_wood"), Material.NETHER_WOOD);
        materials.put(new ResourceLocation("bamboo_sapling"), Material.BAMBOO_SAPLING);
        materials.put(new ResourceLocation("bamboo"), Material.BAMBOO);
        materials.put(new ResourceLocation("wool"), Material.WOOL);
        materials.put(new ResourceLocation("explosive"), Material.EXPLOSIVE);
        materials.put(new ResourceLocation("leaves"), Material.LEAVES);
        materials.put(new ResourceLocation("glass"), Material.GLASS);
        materials.put(new ResourceLocation("ice"), Material.ICE);
        materials.put(new ResourceLocation("cactus"), Material.CACTUS);
        materials.put(new ResourceLocation("stone"), Material.STONE);
        materials.put(new ResourceLocation("metal"), Material.METAL);
        materials.put(new ResourceLocation("snow"), Material.SNOW);
        materials.put(new ResourceLocation("heavy_metal"), Material.HEAVY_METAL);
        materials.put(new ResourceLocation("barrier"), Material.BARRIER);
        materials.put(new ResourceLocation("piston"), Material.PISTON);
        materials.put(new ResourceLocation("coral"), Material.CORAL);
        materials.put(new ResourceLocation("vegetable"), Material.VEGETABLE);
        materials.put(new ResourceLocation("egg"), Material.EGG);
        materials.put(new ResourceLocation("cake"), Material.CAKE);
    });

    /**
     * Registers given material under the given name, if that name is not already taken.
     *
     * @param name     the name to register the material under
     * @param material the material to register
     */
    public static void registerMaterial(ResourceLocation name, Material material) {
        MATERIALS.putIfAbsent(name, material);
    }

    @Override
    public Result<Material, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
                .map(MATERIALS::get, "Could not get material from \"{}\".");
    }
}
