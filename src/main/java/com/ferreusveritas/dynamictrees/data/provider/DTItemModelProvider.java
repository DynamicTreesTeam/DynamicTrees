package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;

/**
 * @author Harley O'Connor
 */
public class DTItemModelProvider extends ItemModelProvider {

    public DTItemModelProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Generate branch item models.
        Family.REGISTRY.dataGenerationStream(this.modid).forEach(family ->
                family.getBranchItemModelGenerator().generate(this, family.getPrimitiveLogOptional(),
                        Optionals.ofItem(family.getBranchItem()))
        );

        // Generate seed models.
        Species.REGISTRY.dataGenerationStream(this.modid).forEach(species ->
                species.getSeedModelGenerator().generate(this, species.getSeed())
        );
    }

    public ResourceLocation block(ResourceLocation blockLocation) {
        return prefix(blockLocation, "block/");
    }

    public ResourceLocation item(ResourceLocation resourceLocation) {
        return prefix(resourceLocation, "item/");
    }

}
