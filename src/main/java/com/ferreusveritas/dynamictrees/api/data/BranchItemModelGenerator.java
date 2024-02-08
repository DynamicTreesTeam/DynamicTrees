package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Harley O'Connor
 */
public class BranchItemModelGenerator implements Generator<DTItemModelProvider, Family> {

    public static final DependencyKey<Block> PRIMITIVE_LOG_BLOCK = new DependencyKey<>("primitive_log_block");
    public static final DependencyKey<Item> PRIMITIVE_LOG_ITEM = new DependencyKey<>("primitive_log_item");

    @Override
    public void generate(DTItemModelProvider provider, Family input, Dependencies dependencies) {
        final ItemModelBuilder builder = provider.withExistingParent(
                String.valueOf(ForgeRegistries.ITEMS.getKey(dependencies.get(PRIMITIVE_LOG_ITEM))),
                input.getBranchItemParentLocation()
        );
        Block block = dependencies.get(PRIMITIVE_LOG_BLOCK);
        input.addBranchTextures(
                builder::texture,
                provider.block(ForgeRegistries.BLOCKS.getKey(block)),
                block
        );
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(PRIMITIVE_LOG_BLOCK, input.getPrimitiveLog())
                .append(PRIMITIVE_LOG_ITEM, input.getBranchItem());
    }

}