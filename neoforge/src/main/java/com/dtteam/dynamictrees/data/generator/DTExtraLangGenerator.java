package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTLangProvider;

public class DTExtraLangGenerator implements Generator<DTDataProvider.Language,String> {
    @Override
    public void generate(DTDataProvider.Language prov, String input, Dependencies dependencies) {
        if (prov instanceof DTLangProvider provider){
            provider.add("item.dynamictrees.dendro_potion.biochar","Biochar Agent Base");
            provider.add("item.dynamictrees.dendro_potion.depletion","Depletion Agent");
            provider.add("item.dynamictrees.dendro_potion.mega","Mega Agent");
            provider.add("item.dynamictrees.dendro_potion.burgeoning","Burgeoning Agent");
            provider.add("item.dynamictrees.dendro_potion.harvest","Harvest Agent");
            provider.add("item.dynamictrees.dirt_bucket","Dirt Bucket");
            provider.add("item.dynamictrees.staff","Woodland Staff");
            provider.add("block.dynamictrees.potted_sapling","Potted Sapling");
            provider.add("chat.registry_name","Registry Name: %s");
            provider.add("tooltip.dynamictrees.species","Species: %s");
            provider.add("tooltip.dynamictrees.fertility","Fertility: %s");
            provider.add("tooltip.dynamictrees.jo_code","JoCode: %s");
            provider.add("tooltip.dynamictrees.roots_jo_code","Roots JoCode: %s");
            provider.add("tooltip.dynamictrees.force_planting","Force Planting: %s");
            provider.add("tooltip.dynamictrees.seed_life_span","Seed Life Span: %s");
            provider.add("tooltip.dynamictrees.enabled","Enabled");
            provider.add("commands.dynamictrees.success.get_tree","Species: %s JoCode: %s");
            provider.add("commands.dynamictrees.success.get_roots","Species: %s Roots JoCode: %s");
            provider.add("commands.dynamictrees.error.get_tree","Could not find tree species at position %s.");
            provider.add("commands.dynamictrees.success.set_tree","Successfully set tree at position %s to species %s with JoCode %s.");
            provider.add("commands.dynamictrees.success.set_tree_roots", "Successfully set tree at position %s to species %s with tree JoCode %s and roots JoCode %s.");
            provider.add("commands.dynamictrees.success.kill_tree","Successfully killed tree at position %s.");
            provider.add("commands.dynamictrees.error.unknown_species","Unknown species %s.");
            provider.add("commands.dynamictrees.error.no_roots", "Cannot set tree of species %s with roots JoCode %s, as it does not have any roots.");
            provider.add("commands.dynamictrees.success.get_fertility","Fertility of tree at position %s is %s.");
            provider.add("commands.dynamictrees.success.set_fertility","Successfully set fertility of tree at position %s to %s.");
            provider.add("commands.dynamictrees.success.grow_pulse","Successfully sent %s growth pulses to tree at position %s.");
            provider.add("commands.dynamictrees.success.set_xor","Successfully set coordinate Xor to %s.");
            provider.add("commands.dynamictrees.success.rotate_jo_code","Rotated JoCode: %s");
            provider.add("commands.dynamictrees.error.species_equal","Targeted species is the same as given species.");
            provider.add("commands.dynamictrees.success.transform","Successfully transformed species %s at position %s to species %s.");
            provider.add("commands.dynamictrees.error.not_transformable","Species %s is not transformable.");
            provider.add("commands.dynamictrees.success.create_staff","Successfully created staff with species %s, code %s, color %s, read only %s, and max uses %s at position %s.");
            provider.add("commands.dynamictrees.success.create_transform_potion","Successfully created transform potion for species %s at position %s.");
            provider.add("commands.dynamictrees.success.clear_orphaned","Successfully cleared %s orphaned branch networks.");
            provider.add("commands.dynamictrees.success.purge_trees","Successfully purged %s trees.");
            provider.add("potion.biochar.description","Base tree potion for brewing");
            provider.add("potion.depletion.description","Destroys tree soil fertility");
            provider.add("potion.mega.description","Allows a tree to become mega size");
            provider.add("potion.burgeoning.description","Quickly grow a tree");
            provider.add("potion.harvest.description","Increase a tree's drops");
            provider.add("desc.dynamictrees.seasonal.fertile_seasons","Fertile Seasons");
            provider.add("desc.dynamictrees.seasonal.infertile","Infertile in %s climates");
            provider.add("desc.dynamictrees.seasonal.year_round","Year-Round");
            provider.add("desc.dynamictrees.seasonal.spring","Spring");
            provider.add("desc.dynamictrees.seasonal.summer","Summer");
            provider.add("desc.dynamictrees.seasonal.autumn","Autumn");
            provider.add("desc.dynamictrees.seasonal.winter","Winter");
            provider.add("desc.dynamictrees.climate.temperate","Temperate");
            provider.add("desc.dynamictrees.climate.tropical","Tropical");
            provider.add("desc.dynamictrees.climate.arid","Arid");
            provider.add("desc.dynamictrees.climate.cold","Cold");
            provider.add("death.attack.falling_tree","%1$s was crushed by a falling tree");
            provider.add("death.attack.falling_tree.player","%1$s was crushed by a falling tree whilst fighting %2$s");
            provider.add("sounds.dynamictrees.falling_tree.start","Timber!");
            provider.add("sounds.dynamictrees.falling_tree.small.start","Branch is chopped");
            provider.add("sounds.dynamictrees.falling_tree.end","Tree hits the ground");
            provider.add("sounds.dynamictrees.falling_tree.small.end","Branch hits the ground");
            provider.add("sounds.dynamictrees.falling_tree.hit_water","Tree goes through water");
            provider.add("sounds.dynamictrees.falling_tree.small.hit_water","Branch falls in water");
            provider.add("sounds.dynamictrees.falling_fungus.start","Fungus is felled");
            provider.add("sounds.dynamictrees.falling_fungus.end","Fungus hits the ground");
            provider.add("sounds.dynamictrees.falling_fungus.small.end","Fungus branch hits the ground");
            provider.add("config.jade.plugin_dynamictrees.rooty_water","Rooty Water");
            provider.add("config.jade.plugin_dynamictrees.fruit","Fruit");
            provider.add("config.jade.plugin_dynamictrees.rooty","Rooty Soil");
            provider.add("config.jade.plugin_dynamictrees.pod","Pod");
            provider.add("config.jade.plugin_dynamictrees.branch","Branch");
            provider.add("itemGroup.dynamictrees","Dynamic Trees");
            provider.add("entity.dynamictrees.falling_tree","Falling Tree");
            provider.add("block.dynamictrees.rooty_water", "Water Roots");
            provider.add("species.dynamictrees.red_mushroom", "Red Mushroom");
            provider.add("species.dynamictrees.brown_mushroom", "Brown Mushroom");
            //These rename vanilla blocks:
            provider.add("block.minecraft.mangrove_propagule","Mangrove Sapling");
            provider.add("block.minecraft.potted_mangrove_propagule","Potted Mangrove Sapling");
        }
    }

    @Override
    public Generator.Dependencies gatherDependencies(String input) {
        return new Dependencies();
    }
}
