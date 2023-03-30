package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SoundTypeDeserialiser implements JsonDeserialiser<SoundType> {

    private static final Map<ResourceLocation, SoundType> SOUND_TYPES =
            Util.make(new HashMap<>(), soundTypes -> {
                soundTypes.put(new ResourceLocation("wood"), SoundType.WOOD);
                soundTypes.put(new ResourceLocation("gravel"), SoundType.GRAVEL);
                soundTypes.put(new ResourceLocation("grass"), SoundType.GRASS);
                soundTypes.put(new ResourceLocation("lily_pad"), SoundType.LILY_PAD);
                soundTypes.put(new ResourceLocation("stone"), SoundType.STONE);
                soundTypes.put(new ResourceLocation("metal"), SoundType.METAL);
                soundTypes.put(new ResourceLocation("glass"), SoundType.GLASS);
                soundTypes.put(new ResourceLocation("wool"), SoundType.WOOL);
                soundTypes.put(new ResourceLocation("sand"), SoundType.SAND);
                soundTypes.put(new ResourceLocation("snow"), SoundType.SNOW);
                soundTypes.put(new ResourceLocation("ladder"), SoundType.LADDER);
                soundTypes.put(new ResourceLocation("anvil"), SoundType.ANVIL);
                soundTypes.put(new ResourceLocation("slime_block"), SoundType.SLIME_BLOCK);
                soundTypes.put(new ResourceLocation("honey_block"), SoundType.HONEY_BLOCK);
                soundTypes.put(new ResourceLocation("wet_grass"), SoundType.WET_GRASS);
                soundTypes.put(new ResourceLocation("coral_block"), SoundType.CORAL_BLOCK);
                soundTypes.put(new ResourceLocation("bamboo"), SoundType.BAMBOO);
                soundTypes.put(new ResourceLocation("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
                soundTypes.put(new ResourceLocation("scaffolding"), SoundType.SCAFFOLDING);
                soundTypes.put(new ResourceLocation("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
                soundTypes.put(new ResourceLocation("crop"), SoundType.CROP);
                soundTypes.put(new ResourceLocation("hard_crop"), SoundType.HARD_CROP);
                soundTypes.put(new ResourceLocation("vine"), SoundType.VINE);
                soundTypes.put(new ResourceLocation("nether_wart"), SoundType.NETHER_WART);
                soundTypes.put(new ResourceLocation("lantern"), SoundType.LANTERN);
                soundTypes.put(new ResourceLocation("stem"), SoundType.STEM);
                soundTypes.put(new ResourceLocation("nylium"), SoundType.NYLIUM);
                soundTypes.put(new ResourceLocation("fungus"), SoundType.FUNGUS);
                soundTypes.put(new ResourceLocation("roots"), SoundType.ROOTS);
                soundTypes.put(new ResourceLocation("shroomlight"), SoundType.SHROOMLIGHT);
                soundTypes.put(new ResourceLocation("weeping_vines"), SoundType.WEEPING_VINES);
                soundTypes.put(new ResourceLocation("twisting_vines"), SoundType.TWISTING_VINES);
                soundTypes.put(new ResourceLocation("soul_sand"), SoundType.SOUL_SAND);
                soundTypes.put(new ResourceLocation("soul_soil"), SoundType.SOUL_SOIL);
                soundTypes.put(new ResourceLocation("basalt"), SoundType.BASALT);
                soundTypes.put(new ResourceLocation("wart_block"), SoundType.WART_BLOCK);
                soundTypes.put(new ResourceLocation("netherrack"), SoundType.NETHERRACK);
                soundTypes.put(new ResourceLocation("nether_bricks"), SoundType.NETHER_BRICKS);
                soundTypes.put(new ResourceLocation("nether_sprouts"), SoundType.NETHER_SPROUTS);
                soundTypes.put(new ResourceLocation("nether_ore"), SoundType.NETHER_ORE);
                soundTypes.put(new ResourceLocation("bone_block"), SoundType.BONE_BLOCK);
                soundTypes.put(new ResourceLocation("netherite_block"), SoundType.NETHERITE_BLOCK);
                soundTypes.put(new ResourceLocation("ancient_debris"), SoundType.ANCIENT_DEBRIS);
                soundTypes.put(new ResourceLocation("lodestone"), SoundType.LODESTONE);
                soundTypes.put(new ResourceLocation("chain"), SoundType.CHAIN);
                soundTypes.put(new ResourceLocation("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
                soundTypes.put(new ResourceLocation("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
                soundTypes.put(new ResourceLocation("candle"), SoundType.CANDLE);
                soundTypes.put(new ResourceLocation("amethyst"), SoundType.AMETHYST);
                soundTypes.put(new ResourceLocation("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
                soundTypes.put(new ResourceLocation("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
                soundTypes.put(new ResourceLocation("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
                soundTypes.put(new ResourceLocation("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
                soundTypes.put(new ResourceLocation("tuff"), SoundType.TUFF);
                soundTypes.put(new ResourceLocation("calcite"), SoundType.CALCITE);
                soundTypes.put(new ResourceLocation("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
                soundTypes.put(new ResourceLocation("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
                soundTypes.put(new ResourceLocation("copper"), SoundType.COPPER);
                soundTypes.put(new ResourceLocation("cave_vines"), SoundType.CAVE_VINES);
                soundTypes.put(new ResourceLocation("spore_blossom"), SoundType.SPORE_BLOSSOM);
                soundTypes.put(new ResourceLocation("azalea"), SoundType.AZALEA);
                soundTypes.put(new ResourceLocation("flowering_azalea"), SoundType.FLOWERING_AZALEA);
                soundTypes.put(new ResourceLocation("moss_carpet"), SoundType.MOSS_CARPET);
                soundTypes.put(new ResourceLocation("moss"), SoundType.MOSS);
                soundTypes.put(new ResourceLocation("big_dripleaf"), SoundType.BIG_DRIPLEAF);
                soundTypes.put(new ResourceLocation("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
                soundTypes.put(new ResourceLocation("rooted_dirt"), SoundType.ROOTED_DIRT);
                soundTypes.put(new ResourceLocation("hanging_roots"), SoundType.HANGING_ROOTS);
                soundTypes.put(new ResourceLocation("azalea_leaves"), SoundType.AZALEA_LEAVES);
                soundTypes.put(new ResourceLocation("sculk_sensor"), SoundType.SCULK_SENSOR);
                soundTypes.put(new ResourceLocation("glow_lichen"), SoundType.GLOW_LICHEN);
                soundTypes.put(new ResourceLocation("deepslate"), SoundType.DEEPSLATE);
                soundTypes.put(new ResourceLocation("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
                soundTypes.put(new ResourceLocation("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
                soundTypes.put(new ResourceLocation("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
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
    public Result<SoundType, JsonElement> deserialise(JsonElement input) {
        return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
                .map(SOUND_TYPES::get, "Could not get sound type from \"{}\".");
    }
}
