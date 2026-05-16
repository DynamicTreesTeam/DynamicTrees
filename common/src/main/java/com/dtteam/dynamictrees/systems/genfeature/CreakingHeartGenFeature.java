package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.growthlogic.context.PositionalSpeciesContext;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public class CreakingHeartGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> MAX_RADIUS = ConfigurationProperty.integer("max_radius");
    public static final ConfigurationProperty<Integer> MIN_RADIUS = ConfigurationProperty.integer("min_radius");

    public CreakingHeartGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(MAX_HEIGHT, MAX_RADIUS, MIN_RADIUS, PLACE_CHANCE, FRUITING_RADIUS);
    }

    @Override
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return species.getFamily() instanceof CreakingHeartFamily chf && chf.getAltBranch().isPresent();
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MAX_HEIGHT, 8)
                .with(MAX_RADIUS, 8)
                .with(MIN_RADIUS, 6)
                .with(PLACE_CHANCE, 0.2f)
                .with(FRUITING_RADIUS, 14);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.random().nextFloat() > configuration.get(PLACE_CHANCE)) return false;
        if (TreeHelper.getRadius(context.level(), context.pos().above()) < configuration.get(FRUITING_RADIUS)) return false;

        int height = context.species().getGrowthLogicKit().getLowestBranchHeight(new PositionalSpeciesContext(context.levelContext().level(), context.pos(), context.species()));
        BranchBlock heart = ((CreakingHeartFamily)context.species().getFamily()).getAltBranch().get();

        int limit = configuration.get(MAX_HEIGHT);
        for (int i=1; i<limit; i++){
            BlockPos testPos = context.pos().above(height + i);
            BlockState testState = context.level().getBlockState(testPos);
            if (!TreeHelper.isBranch(testState)) return false;
            if (isRadiusJustRight(testState, configuration)
                    && TreeHelper.isBranch(context.level().getBlockState(testPos.above()))){
                heart.setRadius(context.level(), testPos, TreeHelper.getRadius(testState), Direction.DOWN, 3);
                return true;
            }
        }
        return false;
    }

    private boolean isRadiusJustRight(BlockState branchState, GenFeatureConfiguration configuration){
        int max = configuration.get(MAX_RADIUS);
        int min = configuration.get(MIN_RADIUS);
        int radius = TreeHelper.getRadius(branchState);
        return radius >= min && radius <= max;
    }

}
