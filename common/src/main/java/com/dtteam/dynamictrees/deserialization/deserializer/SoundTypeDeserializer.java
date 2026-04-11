package com.dtteam.dynamictrees.deserialization.deserializer;

import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.result.Result;
import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.SoundType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class SoundTypeDeserializer implements JsonDeserializer<SoundType> {

    private static final Map<Identifier, SoundType> SOUND_TYPES =
            Util.make(new HashMap<>(), soundTypes -> {
                soundTypes.put(Identifier.parse("wood"), SoundType.WOOD);
                soundTypes.put(Identifier.parse("gravel"), SoundType.GRAVEL);
                soundTypes.put(Identifier.parse("grass"), SoundType.GRASS);
                soundTypes.put(Identifier.parse("lily_pad"), SoundType.LILY_PAD);
                soundTypes.put(Identifier.parse("stone"), SoundType.STONE);
                soundTypes.put(Identifier.parse("metal"), SoundType.METAL);
                soundTypes.put(Identifier.parse("glass"), SoundType.GLASS);
                soundTypes.put(Identifier.parse("wool"), SoundType.WOOL);
                soundTypes.put(Identifier.parse("sand"), SoundType.SAND);
                soundTypes.put(Identifier.parse("snow"), SoundType.SNOW);
                soundTypes.put(Identifier.parse("ladder"), SoundType.LADDER);
                soundTypes.put(Identifier.parse("anvil"), SoundType.ANVIL);
                soundTypes.put(Identifier.parse("slime_block"), SoundType.SLIME_BLOCK);
                soundTypes.put(Identifier.parse("honey_block"), SoundType.HONEY_BLOCK);
                soundTypes.put(Identifier.parse("wet_grass"), SoundType.WET_GRASS);
                soundTypes.put(Identifier.parse("coral_block"), SoundType.CORAL_BLOCK);
                soundTypes.put(Identifier.parse("bamboo"), SoundType.BAMBOO);
                soundTypes.put(Identifier.parse("bamboo_sapling"), SoundType.BAMBOO_SAPLING);
                soundTypes.put(Identifier.parse("scaffolding"), SoundType.SCAFFOLDING);
                soundTypes.put(Identifier.parse("sweet_berry_bush"), SoundType.SWEET_BERRY_BUSH);
                soundTypes.put(Identifier.parse("crop"), SoundType.CROP);
                soundTypes.put(Identifier.parse("hard_crop"), SoundType.HARD_CROP);
                soundTypes.put(Identifier.parse("vine"), SoundType.VINE);
                soundTypes.put(Identifier.parse("nether_wart"), SoundType.NETHER_WART);
                soundTypes.put(Identifier.parse("lantern"), SoundType.LANTERN);
                soundTypes.put(Identifier.parse("stem"), SoundType.STEM);
                soundTypes.put(Identifier.parse("nylium"), SoundType.NYLIUM);
                soundTypes.put(Identifier.parse("fungus"), SoundType.FUNGUS);
                soundTypes.put(Identifier.parse("roots"), SoundType.ROOTS);
                soundTypes.put(Identifier.parse("shroomlight"), SoundType.SHROOMLIGHT);
                soundTypes.put(Identifier.parse("weeping_vines"), SoundType.WEEPING_VINES);
                soundTypes.put(Identifier.parse("twisting_vines"), SoundType.TWISTING_VINES);
                soundTypes.put(Identifier.parse("soul_sand"), SoundType.SOUL_SAND);
                soundTypes.put(Identifier.parse("soul_soil"), SoundType.SOUL_SOIL);
                soundTypes.put(Identifier.parse("basalt"), SoundType.BASALT);
                soundTypes.put(Identifier.parse("wart_block"), SoundType.WART_BLOCK);
                soundTypes.put(Identifier.parse("netherrack"), SoundType.NETHERRACK);
                soundTypes.put(Identifier.parse("nether_bricks"), SoundType.NETHER_BRICKS);
                soundTypes.put(Identifier.parse("nether_sprouts"), SoundType.NETHER_SPROUTS);
                soundTypes.put(Identifier.parse("nether_ore"), SoundType.NETHER_ORE);
                soundTypes.put(Identifier.parse("bone_block"), SoundType.BONE_BLOCK);
                soundTypes.put(Identifier.parse("netherite_block"), SoundType.NETHERITE_BLOCK);
                soundTypes.put(Identifier.parse("ancient_debris"), SoundType.ANCIENT_DEBRIS);
                soundTypes.put(Identifier.parse("lodestone"), SoundType.LODESTONE);
                soundTypes.put(Identifier.parse("chain"), SoundType.CHAIN);
                soundTypes.put(Identifier.parse("nether_gold_ore"), SoundType.NETHER_GOLD_ORE);
                soundTypes.put(Identifier.parse("gilded_blackstone"), SoundType.GILDED_BLACKSTONE);
                soundTypes.put(Identifier.parse("candle"), SoundType.CANDLE);
                soundTypes.put(Identifier.parse("amethyst"), SoundType.AMETHYST);
                soundTypes.put(Identifier.parse("amethyst_cluster"), SoundType.AMETHYST_CLUSTER);
                soundTypes.put(Identifier.parse("small_amethyst_bud"), SoundType.SMALL_AMETHYST_BUD);
                soundTypes.put(Identifier.parse("medium_amethyst_bud"), SoundType.MEDIUM_AMETHYST_BUD);
                soundTypes.put(Identifier.parse("large_amethyst_bud"), SoundType.LARGE_AMETHYST_BUD);
                soundTypes.put(Identifier.parse("tuff"), SoundType.TUFF);
                soundTypes.put(Identifier.parse("calcite"), SoundType.CALCITE);
                soundTypes.put(Identifier.parse("dripstone_block"), SoundType.DRIPSTONE_BLOCK);
                soundTypes.put(Identifier.parse("pointed_dripstone"), SoundType.POINTED_DRIPSTONE);
                soundTypes.put(Identifier.parse("copper"), SoundType.COPPER);
                soundTypes.put(Identifier.parse("cave_vines"), SoundType.CAVE_VINES);
                soundTypes.put(Identifier.parse("spore_blossom"), SoundType.SPORE_BLOSSOM);
                soundTypes.put(Identifier.parse("azalea"), SoundType.AZALEA);
                soundTypes.put(Identifier.parse("flowering_azalea"), SoundType.FLOWERING_AZALEA);
                soundTypes.put(Identifier.parse("moss_carpet"), SoundType.MOSS_CARPET);
                soundTypes.put(Identifier.parse("moss"), SoundType.MOSS);
                soundTypes.put(Identifier.parse("big_dripleaf"), SoundType.BIG_DRIPLEAF);
                soundTypes.put(Identifier.parse("small_dripleaf"), SoundType.SMALL_DRIPLEAF);
                soundTypes.put(Identifier.parse("rooted_dirt"), SoundType.ROOTED_DIRT);
                soundTypes.put(Identifier.parse("hanging_roots"), SoundType.HANGING_ROOTS);
                soundTypes.put(Identifier.parse("azalea_leaves"), SoundType.AZALEA_LEAVES);
                soundTypes.put(Identifier.parse("sculk_sensor"), SoundType.SCULK_SENSOR);
                soundTypes.put(Identifier.parse("glow_lichen"), SoundType.GLOW_LICHEN);
                soundTypes.put(Identifier.parse("deepslate"), SoundType.DEEPSLATE);
                soundTypes.put(Identifier.parse("deepslate_bricks"), SoundType.DEEPSLATE_BRICKS);
                soundTypes.put(Identifier.parse("deepslate_tiles"), SoundType.DEEPSLATE_TILES);
                soundTypes.put(Identifier.parse("polished_deepslate"), SoundType.POLISHED_DEEPSLATE);
            });

    /**
     * Registers given sound type under the given name, if that name is not already taken.
     *
     * @param name      the name to register the sound type under
     * @param soundType the sound type to register
     */
    public static void registerSoundType(Identifier name, SoundType soundType) {
        SOUND_TYPES.putIfAbsent(name, soundType);
    }

    @Override
    public Result<SoundType, JsonElement> deserialize(JsonElement input) {
        return JsonDeserializers.RESOURCE_LOCATION.deserialize(input)
                .map(SOUND_TYPES::get, "Could not get sound type from \"{}\".");
    }
}
