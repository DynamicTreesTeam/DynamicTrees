package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.client.TintSources.DendroPotionItemTintSource;
import com.dtteam.dynamictrees.client.TintSources.StaffCrystalItemTintSource;
import com.dtteam.dynamictrees.client.TintSources.StaffHandleItemTintSource;
import com.dtteam.dynamictrees.data.BiGenerator;
import com.dtteam.dynamictrees.data.builder.PottedSaplingLoaderBuilder;
import com.dtteam.dynamictrees.registry.DTRegistries;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class DTExtraModelGenerator implements BiGenerator<BlockModelGenerators, ItemModelGenerators, String> {

    @Override
    public void generate(BlockModelGenerators blockModels, ItemModelGenerators itemModels, String input, Dependencies deps) {
        //items
        generateDirtBucket(itemModels);
        generateDendroPotion(itemModels);
        generateStaff(itemModels);

        //blocks
        generateTrunkShell(blockModels);
        generatePottedSapling(blockModels);
    }

    private static void generatePottedSapling(BlockModelGenerators blockModels) {
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        DTRegistries.POTTED_SAPLING.get(),
                        MultiVariant.of(new PottedSaplingLoaderBuilder(Identifier.withDefaultNamespace("block/flower_pot")))
                )
        );
    }

    private static void generateTrunkShell(BlockModelGenerators blockModels) {
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(
                        DTRegistries.TRUNK_SHELL.get(),
                        BlockModelGenerators.variant(new Variant(DynamicTrees.location("block/empty")))
                )
        );
    }

    private static void generateStaff(ItemModelGenerators itemModels) {
        ModelTemplate threeLayeredHandheldItem = ModelTemplates.createItem("handheld", TextureSlot.LAYER0, TextureSlot.LAYER1, TextureSlot.LAYER2);
        Item staff = DTRegistries.STAFF.get();
        Identifier staffModel = threeLayeredHandheldItem.create(staff,
                TextureMapping.layered(
                        TextureMapping.getItemTexture(staff, "_handle"),
                        TextureMapping.getItemTexture(staff, "_overlay"),
                        TextureMapping.getItemTexture(staff, "_glimmer")),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(staff,
                ItemModelUtils.tintedModel(staffModel,
                        new StaffHandleItemTintSource(),
                        new StaffCrystalItemTintSource())
        );
    }

    private static void generateDendroPotion(ItemModelGenerators itemModels) {
        Item dendroPotion = DTRegistries.DENDRO_POTION.get();
        Identifier potionModel = itemModels.generateLayeredItem(dendroPotion,
                TextureMapping.getItemTexture(dendroPotion, "_overlay"),
                TextureMapping.getItemTexture(dendroPotion));
        itemModels.itemModelOutput.accept(dendroPotion,
                ItemModelUtils.tintedModel(potionModel, new DendroPotionItemTintSource())
        );
    }

    private static void generateDirtBucket(ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(DTRegistries.DIRT_BUCKET.get(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    public Dependencies gatherDependencies(String input) {
        return new Dependencies();
    }

}
