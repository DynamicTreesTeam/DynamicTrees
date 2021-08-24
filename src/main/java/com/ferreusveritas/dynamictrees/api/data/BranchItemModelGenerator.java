package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;

/**
 * @author Harley O'Connor
 */
public class BranchItemModelGenerator implements Generator<DTItemModelProvider, Family> {

    public static final DependencyKey<Block> PRIMITIVE_LOG_BLOCK = new DependencyKey<>("primitive_log_block");
    public static final DependencyKey<Item> PRIMITIVE_LOG_ITEM = new DependencyKey<>("primitive_log_item");

    @Override
    public void generate(DTItemModelProvider provider, Family input, Dependencies dependencies) {
        final ItemModelBuilder builder = provider.withExistingParent(
                String.valueOf(dependencies.get(PRIMITIVE_LOG_ITEM).getRegistryName()),
                input.getBranchItemParentLocation()
        );
        input.addBranchTextures(
                builder::texture,
                provider.block(dependencies.get(PRIMITIVE_LOG_BLOCK).getRegistryName())
        );
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(PRIMITIVE_LOG_BLOCK, input.getPrimitiveLog())
                .append(PRIMITIVE_LOG_ITEM, input.getBranchItem());
    }

}
