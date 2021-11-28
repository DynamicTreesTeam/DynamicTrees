package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.StackDrops;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * @author Harley O'Connor
 * @deprecated once systems are feature complete this can be defined in Json
 */
@Deprecated
public final class WartBlockDropCreator extends DropCreator {

    public static final ConfigurationProperty<Block> BLOCK = ConfigurationProperty.block("block");
    public static final ConfigurationProperty<Integer> HARVEST_CHANCE = ConfigurationProperty.integer("harvest_chance");

    public WartBlockDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(BLOCK, HARVEST_CHANCE);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(BLOCK, Blocks.AIR)
                .with(HARVEST_CHANCE, 10);
    }

    private Block getWartBlock(final DropCreatorConfiguration configuration, final Species species) {
        final Block wartBlock = configuration.get(BLOCK);
        return wartBlock == Blocks.AIR ? species.getLeavesBlock()
                .map(leaves -> (Block) leaves)
                .orElse(Blocks.AIR) : Blocks.AIR;
    }

    @Override
    public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
        StackDrops.create(1F, configuration.get(HARVEST_CHANCE), new ItemStack(this.getWartBlock(configuration, context.species())));
    }

    @Override
    public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
        StackDrops.create(1F, 1, new ItemStack(this.getWartBlock(configuration, context.species())));
    }

}
