package com.ferreusveritas.dynamictrees.tree.family;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.data.Generator;
import com.ferreusveritas.dynamictrees.api.data.RootsItemModelGenerator;
import com.ferreusveritas.dynamictrees.api.data.RootsStateGenerator;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.roots.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.data.provider.DTBlockStateProvider;
import com.ferreusveritas.dynamictrees.data.provider.DTItemModelProvider;
import com.ferreusveritas.dynamictrees.util.MutableLazyValue;
import com.ferreusveritas.dynamictrees.util.Optionals;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.ferreusveritas.dynamictrees.util.ResourceLocationUtils.suffix;

public class MangroveFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(MangroveFamily::new);
    private AerialRootsSoilProperties defaultSoil;
    private Supplier<BranchBlock> root;
    private Supplier<Item> rootItem;
    private Block primitiveRoots, primitiveRootsFilled, primitiveRootsCovered;

    private Block[] replaceableByRoots = new Block[]
            {Blocks.MUD, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.SAND};

    protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> rootsStateGenerator =
            MutableLazyValue.supplied(RootsStateGenerator::new);
    protected final MutableLazyValue<Generator<DTItemModelProvider, Family>> rootsItemModelGenerator =
            MutableLazyValue.supplied(RootsItemModelGenerator::new);

    public MangroveFamily(ResourceLocation name) {
        super(name);
    }

    ///////////////////////////////////////////
    // DEFAULT SOIL
    ///////////////////////////////////////////

    public void setDefaultSoil(SoilProperties defaultSoil) {
        if (!(defaultSoil instanceof AerialRootsSoilProperties))
            throw new RuntimeException("Soil "+ defaultSoil.toString() +" for mangrove family "+ this +" is not of type "+ AerialRootsSoilProperties.class);
        this.defaultSoil = (AerialRootsSoilProperties) defaultSoil;
        this.defaultSoil.setFamily(this);
    }

    public AerialRootsSoilProperties getDefaultSoil() {
        return defaultSoil;
    }

    ///////////////////////////////////////////
    // DYNAMIC ROOTS
    ///////////////////////////////////////////

    @Override
    public void setupBlocks() {
        super.setupBlocks();
        this.setRoot(this.createRoots(this.getBranchName()));
        this.setRootItem(this.createRootsItem(this.getBranchName(), this.root));
    }

    protected Supplier<BranchBlock> createRoots(final ResourceLocation name) {
        return RegistryHandler.addBlock(suffix(name, getRootNameSuffix()), () -> createRootsBlock(name));
    }
    protected BranchBlock createRootsBlock(ResourceLocation name) {
        final BasicRootsBlock branch = new BasicRootsBlock(name, this.getProperties());
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }
    public Supplier<BlockItem> createRootsItem(final ResourceLocation registryName, final Supplier<BranchBlock> rootsSup) {
        return RegistryHandler.addItem(suffix(registryName, getRootNameSuffix()), () -> new BlockItem(rootsSup.get(), new Item.Properties()));
    }

    protected String getRootNameSuffix() {
        return BasicRootsBlock.NAME_SUFFIX;
    }

    public Family setRoot(final Supplier<BranchBlock> branchSup) {
        this.root = setupBranch(branchSup, false);
        return this;
    }
    @SuppressWarnings("unchecked")
    protected <T extends Item> Family setRootItem(Supplier<T> branchItemSup) {
        this.rootItem = (Supplier<Item>) branchItemSup;
        return this;
    }
    public Optional<BranchBlock> getRoot() {
        return Optionals.ofBlock(root);
    }
    public Optional<Item> getRootItem() {
        return Optionals.ofItem(rootItem);
    }
    @Override
    public void generateStateData(DTBlockStateProvider provider) {
        super.generateStateData(provider);
        this.rootsStateGenerator.get().generate(provider, this);
    }

    @Override
    public void generateItemModelData(DTItemModelProvider provider) {
        super.generateItemModelData(provider);
        this.rootsItemModelGenerator.get().generate(provider, this);
    }

    public ResourceLocation getBranchItemParentLocation() {
        return DynamicTrees.location("item/branch");
    }

    public Boolean isReplaceableByRoots(Block block){
        return Arrays.stream(replaceableByRoots).anyMatch(b->b == block);
    }

    ///////////////////////////////////////////
    // PRIMITIVE ROOTS
    ///////////////////////////////////////////

    public void setPrimitiveRoots(Block primitiveRoots) {
        this.primitiveRoots = primitiveRoots;
    }
    public void setPrimitiveRootsFilled(Block primitiveRootsFilled) {
        this.primitiveRootsFilled = primitiveRootsFilled;
    }
    public void setPrimitiveRootsCovered(Block primitiveRootsCovered) {
        this.primitiveRootsCovered = primitiveRootsCovered;
    }

    public Optional<Block> getPrimitiveRoots() {
        return Optionals.ofBlock(primitiveRoots);
    }
    public Optional<Block> getPrimitiveFilledRoots() {
        return Optionals.ofBlock(primitiveRootsFilled);
    }
    public Optional<Block> getPrimitiveCoveredRoots() {
        return Optionals.ofBlock(primitiveRootsCovered);
    }
}
