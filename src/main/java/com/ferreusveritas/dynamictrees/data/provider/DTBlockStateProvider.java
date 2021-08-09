package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.SoilProperties;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
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
        SoilProperties.REGISTRY.getAllFor(this.modId).forEach(soilProperties -> {
            if (soilProperties.shouldGenerateData()) {
                soilProperties.getSoilBlock().ifPresent(soil -> soilProperties.registerStatesAndModels(this));
            }
        });

        Family.REGISTRY.getAllFor(this.modId).forEach(family -> {
            if (family.shouldGenerateData()) {
                family.getBranchOptional().ifPresent(branch -> family.registerBranchStateAndModel(this, branch, family.getPrimitiveLog()));
                family.getStrippedBranchOptional().ifPresent(branch -> family.registerBranchStateAndModel(this, branch, family.getPrimitiveStrippedLog()));
                family.getSurfaceRootOptional().ifPresent(surfaceRoot -> family.registerSurfaceRootStateAndModel(this));
            }
        });

        Species.REGISTRY.getAllFor(this.modId).forEach(species -> {
            if (species.shouldGenerateData()) {
                species.getSapling().ifPresent(sapling -> species.registerStatesAndModels(this));
            }
        });

        LeavesProperties.REGISTRY.getAllFor(this.modId).forEach(leavesProperties -> {
            if (leavesProperties.shouldGenerateData()) {
                leavesProperties.getDynamicLeavesBlock().ifPresent(leaves -> leavesProperties.registerStatesAndModels(this));
            }
        });
    }

    public ResourceLocation block(ResourceLocation blockLocation) {
        return prefix(blockLocation, "block/");
    }

}
