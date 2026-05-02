package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.GeneratorHelper;
import com.dtteam.dynamictrees.tree.family.Family;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class BranchItemModelGenerator implements Generator<ItemModelGenerators, Family> {

    public static final DependencyKey<Block> PRIMITIVE_BLOCK = new DependencyKey<>("primitive_log_block");
    public static final DependencyKey<Item> BRANCH_ITEM = new DependencyKey<>("primitive_log_item");

    @Override
    public void generate(ItemModelGenerators generators, Family input, Dependencies dependencies) {
        final Item branchItem = dependencies.get(BRANCH_ITEM);
        final Block primitiveLog = dependencies.get(PRIMITIVE_BLOCK);
        Identifier primitiveLogPath = ModelLocationUtils.getModelLocation(primitiveLog);

        final Map<String, Identifier> textures = new HashMap<>();
        addTextures(input, textures, primitiveLogPath, primitiveLog);

        TextureSlot[] slots = GeneratorHelper.createSlots(textures);
        ModelTemplate branchItemTemplate = ModelTemplates.createItem(itemParentLocation(input), slots);
        Identifier model = branchItemTemplate.create(branchItem, GeneratorHelper.createMapping(textures, slots), generators.modelOutput);

        generators.itemModelOutput.accept(
                branchItem,
                ItemModelUtils.plainModel(model)
        );
    }

    /**
     * Extracted to be overriden by the root generator
     */
    protected void addTextures(Family input, Map<String, Identifier> textures, Identifier primitiveLogPath, Block primitiveLog) {
        input.addBranchTextures(textures::put, primitiveLogPath, primitiveLog);
    }

    /**
     * Extracted to be overriden by the root generator
     */
    protected String itemParentLocation(Family family){
        return family.getBranchItemParentLocation().toString();
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(PRIMITIVE_BLOCK, input.getPrimitiveLog())
                .append(BRANCH_ITEM, input.getBranchItem());
    }

}