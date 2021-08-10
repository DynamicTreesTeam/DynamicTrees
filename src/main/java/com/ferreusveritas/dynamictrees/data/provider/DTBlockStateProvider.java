package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.prefix;

/**
 * @author Harley O'Connor
 */
public class DTBlockStateProvider extends BlockStateProvider {

    private final String modId;

    public DTBlockStateProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator, modId, existingFileHelper);
        this.modId = modId;
    }

    @Override
    protected void registerStatesAndModels() {
        // Generate rooty soil block states and models.
        SoilProperties.REGISTRY.dataGenerationStream(this.modId).forEach(soilProperties ->
                soilProperties.getSoilStateGenerator().generate(this, soilProperties.getSoilBlock(),
                        soilProperties.getPrimitiveSoilBlockOptional())
        );

        Family.REGISTRY.dataGenerationStream(this.modId).forEach(family -> {
            // Generate branch block state and model.
            family.getBranchStateGenerator().generate(this, family.getBranchOptional(),
                    family.getPrimitiveLogOptional());

            // Generate stripped branch block state and model.
            family.getBranchStateGenerator().generate(this, family.getStrippedBranchOptional(),
                    family.getPrimitiveStrippedLogOptional());

            // Generate surface root block state and model.
            family.getSurfaceRootStateGenerator().generate(this, family.getSurfaceRootOptional(),
                    family.getPrimitiveLogOptional());
        });

        // Generate sapling block state and model.
        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species ->
                species.getSaplingStateGenerator().generate(this, species.getSapling(),
                        species.getFamily().getPrimitiveLogOptional(), species.getPrimitiveLeaves())
        );

        // Generate leaves block state and model.
        LeavesProperties.REGISTRY.dataGenerationStream(this.modId).forEach(leavesProperties ->
                leavesProperties.getStateGenerator().generate(this, leavesProperties.getDynamicLeavesBlock(),
                        leavesProperties.getPrimitiveLeavesBlock())
        );
    }

    public ResourceLocation block(ResourceLocation blockLocation) {
        return prefix(blockLocation, "block/");
    }

}
