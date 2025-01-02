package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SoundTypeDeserializer implements JsonDeserializer<SoundType> {

    private static final Map<ResourceLocation, SoundType> SOUND_TYPES =
            Util.make(new HashMap<>(), soundTypes -> {
                soundTypes.put(ResourceLocation.parse("wood"), SoundType.WOOD);
                soundTypes.put(ResourceLocation.parse("gravel"), SoundType.GRAVEL);
                soundTypes.put(ResourceLocation.parse("grass"), SoundType.GRASS);
                soundTypes.put(ResourceLocation.parse("lily_pad"), SoundType.LILY_PAD);
                soundTypes.put(ResourceLocation.parse("stone"), SoundType.STONE);
                soundTypes.put(ResourceLocation.parse("metal"), SoundType.METAL);
                soundTypes.put(ResourceLocation.parse("glass"), SoundType.GLASS);
                soundTypes.put(ResourceLocation.parse("wool"), SoundType.WOOL);
                soundTypes.put(ResourceLocation.parse("sand"), SoundType.SAND);
                soundTypes.put(ResourceLocation.parse("snow"), SoundType.SNOW);
                soundTypes.put(ResourceLocation.parse("ladder"), SoundType.LADDER);
                soundTypes.put(ResourceLocation.parse("anvil"), SoundType.ANVIL);
                soundTypes.put(ResourceLocation.parse("slime_block"), SoundType.SLIME_BLOCK);
                soundTypes.put(ResourceLocation.parse("honey_block"), SoundType.HONEY_BLOCK);
                soundTypes.put(ResourceLocation.parse("wet_grass"), SoundType.WET_GRASS);
                soundTypes.put(ResourceLocation.parse("coral_block"), SoundType.CORAL_BLOCK);
                soundTypes.put(ResourceLocation.parse("bamboo"), SoundType.BAMBOO);
                soundTypes.put(ResourceLocation.parse("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
                soundTypes.put(ResourceLocation.parse("scaffolding"), SoundType.SCAFFOLDING);
                soundTypes.put(ResourceLocation.parse("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
                soundTypes.put(ResourceLocation.parse("crop"), SoundType.CROP);
                soundTypes.put(ResourceLocation.parse("hard_crop"), SoundType.HARD_CROP);
                soundTypes.put(ResourceLocation.parse("vine"), SoundType.VINE);
                soundTypes.put(ResourceLocation.parse("nether_wart"), SoundType.NETHER_WART);
                soundTypes.put(ResourceLocation.parse("lantern"), SoundType.LANTERN);
                soundTypes.put(ResourceLocation.parse("stem"), SoundType.STEM);
                soundTypes.put(ResourceLocation.parse("nylium"), SoundType.NYLIUM);
                soundTypes.put(ResourceLocation.parse("fungus"), SoundType.FUNGUS);
                soundTypes.put(ResourceLocation.parse("roots"), SoundType.ROOTS);
                soundTypes.put(ResourceLocation.parse("shroomlight"), SoundType.SHROOMLIGHT);
                soundTypes.put(ResourceLocation.parse("weeping_vines"), SoundType.WEEPING_VINES);
                soundTypes.put(ResourceLocation.parse("twisting_vines"), SoundType.TWISTING_VINES);
                soundTypes.put(ResourceLocation.parse("soul_sand"), SoundType.SOUL_SAND);
                soundTypes.put(ResourceLocation.parse("soul_soil"), SoundType.SOUL_SOIL);
                soundTypes.put(ResourceLocation.parse("basalt"), SoundType.BASALT);
                soundTypes.put(ResourceLocation.parse("wart_block"), SoundType.WART_BLOCK);
                soundTypes.put(ResourceLocation.parse("netherrack"), SoundType.NETHERRACK);
                soundTypes.put(ResourceLocation.parse("nether_bricks"), SoundType.NETHER_BRICKS);
                soundTypes.put(ResourceLocation.parse("nether_sprouts"), SoundType.NETHER_SPROUTS);
                soundTypes.put(ResourceLocation.parse("nether_ore"), SoundType.NETHER_ORE);
                soundTypes.put(ResourceLocation.parse("bone_block"), SoundType.BONE_BLOCK);
                soundTypes.put(ResourceLocation.parse("netherite_block"), SoundType.NETHERITE_BLOCK);
                soundTypes.put(ResourceLocation.parse("ancient_debris"), SoundType.ANCIENT_DEBRIS);
                soundTypes.put(ResourceLocation.parse("lodestone"), SoundType.LODESTONE);
                soundTypes.put(ResourceLocation.parse("chain"), SoundType.CHAIN);
                soundTypes.put(ResourceLocation.parse("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
                soundTypes.put(ResourceLocation.parse("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
                soundTypes.put(ResourceLocation.parse("candle"), SoundType.CANDLE);
                soundTypes.put(ResourceLocation.parse("amethyst"), SoundType.AMETHYST);
                soundTypes.put(ResourceLocation.parse("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
                soundTypes.put(ResourceLocation.parse("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
                soundTypes.put(ResourceLocation.parse("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
                soundTypes.put(ResourceLocation.parse("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
                soundTypes.put(ResourceLocation.parse("tuff"), SoundType.TUFF);
                soundTypes.put(ResourceLocation.parse("calcite"), SoundType.CALCITE);
                soundTypes.put(ResourceLocation.parse("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
                soundTypes.put(ResourceLocation.parse("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
                soundTypes.put(ResourceLocation.parse("copper"), SoundType.COPPER);
                soundTypes.put(ResourceLocation.parse("cave_vines"), SoundType.CAVE_VINES);
                soundTypes.put(ResourceLocation.parse("spore_blossom"), SoundType.SPORE_BLOSSOM);
                soundTypes.put(ResourceLocation.parse("azalea"), SoundType.AZALEA);
                soundTypes.put(ResourceLocation.parse("flowering_azalea"), SoundType.FLOWERING_AZALEA);
                soundTypes.put(ResourceLocation.parse("moss_carpet"), SoundType.MOSS_CARPET);
                soundTypes.put(ResourceLocation.parse("moss"), SoundType.MOSS);
                soundTypes.put(ResourceLocation.parse("big_dripleaf"), SoundType.BIG_DRIPLEAF);
                soundTypes.put(ResourceLocation.parse("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
                soundTypes.put(ResourceLocation.parse("rooted_dirt"), SoundType.ROOTED_DIRT);
                soundTypes.put(ResourceLocation.parse("hanging_roots"), SoundType.HANGING_ROOTS);
                soundTypes.put(ResourceLocation.parse("azalea_leaves"), SoundType.AZALEA_LEAVES);
                soundTypes.put(ResourceLocation.parse("sculk_sensor"), SoundType.SCULK_SENSOR);
                soundTypes.put(ResourceLocation.parse("glow_lichen"), SoundType.GLOW_LICHEN);
                soundTypes.put(ResourceLocation.parse("deepslate"), SoundType.DEEPSLATE);
                soundTypes.put(ResourceLocation.parse("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
                soundTypes.put(ResourceLocation.parse("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
                soundTypes.put(ResourceLocation.parse("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
            });

    /**
     * Registers given sound type under the given name, if that name is not already taken.
     *
     * @param name      the name to register the sound type under
     * @param soundType the sound type to register
     */
    public static void registerSoundType(ResourceLocation name, SoundType soundType) {
        SOUND_TYPES.putIfAbsent(name, soundType);
    }

    @Override
    public Result<SoundType, JsonElement> deserialize(JsonElement input) {
        return JsonDeserializers.RESOURCE_LOCATION.deserialize(input)
                .map(SOUND_TYPES::get, "Could not get sound type from \"{}\".");
    }
}
