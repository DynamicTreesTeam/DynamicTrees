package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.GeneratesFruit;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.CocoaFruitNode;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

@GeneratesFruit
public class CocoaGenFeature extends GenFeature {

    public CocoaGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (context.fertility() == 0 && context.random().nextInt() % 16 == 0) {
            final World world = context.world();
            if (context.species().seasonalFruitProductionFactor(world, context.treePos()) > context.random().nextFloat()) {
                this.addCocoa(world, context.pos(), false);
            }
        }
        return false;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.random().nextInt() % 8 == 0) {
            this.addCocoa(context.world(), context.pos(), true);
            return true;
        }
        return false;
    }

    private void addCocoa(IWorld world, BlockPos rootPos, boolean worldGen) {
        TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new CocoaFruitNode().setWorldGen(worldGen)));
    }

}
