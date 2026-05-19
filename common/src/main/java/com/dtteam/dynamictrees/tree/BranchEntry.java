package com.dtteam.dynamictrees.tree;

import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BranchEntry {

    protected final Family family;
    protected Identifier branchName;
    protected BlockBehaviour.Properties blockProperties;
    protected boolean canBeStripped;
    protected Block primitiveBlock = Blocks.AIR;

    protected Supplier<BranchBlock> branchBlock;
    protected Supplier<Item> branchItem;

    public BranchEntry(Family family){
        this(family, family.getDefaultBranchProperties());
    }
    public BranchEntry(Family family, BlockBehaviour.Properties blockProperties){
        this.family = family;
        this.blockProperties = blockProperties;
    }

    public BranchEntry setBranchName(Identifier branchName) {
        this.branchName = branchName;
        return this;
    }

    public BranchEntry setBlockProperties(BlockBehaviour.Properties blockProperties) {
        this.blockProperties = blockProperties;
        return this;
    }

    public BranchEntry setCanBeStripped(boolean canBeStripped) {
        this.canBeStripped = canBeStripped;
        return this;
    }

    public BranchEntry CreateBlock(BiFunction<Identifier, BlockBehaviour.Properties, BranchBlock> newBranch){
        branchBlock = RegistryHandler.addBlock(branchName, () -> {
            final BranchBlock branch = newBranch.apply(branchName, blockProperties);
            setupBranch(branch);
            return branch;
        });
        return this;
    }

    protected void setupBranch(BranchBlock branch) {
        if (family.isFireProof()) branch.setFireSpreadSpeed(0).setFlammability(0);
        branch.setFamily(family);
        branch.setCanBeStripped(canBeStripped);
        family.addValidBranches(branch);
    }

    public BranchEntry CreateItem(){
        branchItem = RegistryHandler.addItem(branchName, () ->
                new BlockItem(
                        branchBlock.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, branchName))
                ));
        return this;
    }

    public Optional<BranchBlock> getBlock(){
        return Optionals.ofBlock(branchBlock);
    }

    public Optional<Item> getItem(){
        return Optionals.ofItem(branchItem);
    }

    public BranchEntry setPrimitiveBlock(Block primitive) {
        this.primitiveBlock = primitive;
        getBlock().ifPresent(b-> b.setPrimitiveLogDrops(List.of(()->new ItemStack(primitive))));
        return this;
    }

    public Optional<Block> getPrimitiveBlock() {
        return Optional.of(primitiveBlock);
    }
}
