package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AltBranchFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(AltBranchFamily::new);

    protected Supplier<BranchBlock> altBranch;
    protected Block primitiveAltLog;

    public AltBranchFamily(Identifier name) {
        super(name);
    }

    @Override
    public void setupBlocks() {
        super.setupBlocks();

        this.altBranch = setupBranch(createAltBranch(altBranchName()), false);
    }

    protected Identifier altBranchName(){
        return getBranchName("alt_") ;
    }

    protected BranchBlock createAltBranchBlock(Identifier name) {
        BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(name, this.getProperties()){
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof AltBranchFamily altLogFamily)
                    return altLogFamily.getPrimitiveAltLog();
                return super.getPrimitiveLog();
            }
        } : new BasicBranchBlock(name, this.getProperties()){
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof AltBranchFamily altLogFamily)
                    return altLogFamily.getPrimitiveAltLog();
                return super.getPrimitiveLog();
            }
        };
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }

        return branch;
    }

    protected Supplier<BranchBlock> createAltBranch(Identifier name) {
        return RegistryHandler.addBlock(IdentifierUtils.suffix(name, this.getBranchNameSuffix()), () -> this.createAltBranchBlock(name));
    }

    public Family setPrimitiveAltLog(Block primitiveLog) {
        this.primitiveAltLog = primitiveLog;
        altBranch.get().setPrimitiveLogDrops(List.of(()->new ItemStack(primitiveLog)));
        return this;
    }

    public Optional<BranchBlock> getAltBranch() {
        return Optionals.ofBlock(altBranch.get());
    }

    public Optional<Block> getPrimitiveAltLog() {
        return Optionals.ofBlock(primitiveAltLog);
    }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        generators.add(DynamicTrees.location("alt_branch"));
        return generators;
    }

    public Identifier getAltBranchLoader() {
        return getBranchLoader();
    }

    @Override
    public void addBranchTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock) {
        Optional<Block> primAlt = getPrimitiveAltLog();
        if (primAlt.isPresent() && primAlt.get() == sourceBlock){
            Identifier bark = primitiveLogLocation;
            Identifier rings = IdentifierUtils.suffix(primitiveLogLocation, "_top");
            if (this.textureOverrides.containsKey("alt_branch")) {
                bark = this.textureOverrides.get("alt_branch");
            }

            if (this.textureOverrides.containsKey("alt_branch_top")) {
                rings = this.textureOverrides.get("alt_branch_top");
            }
            textureConsumer.accept("bark", bark);
            textureConsumer.accept("rings", rings);
            return;
        }
        super.addBranchTextures(textureConsumer, primitiveLogLocation, sourceBlock);
    }

    public void addGeneratedBlockTags (Function<TagKey<Block>, TagAppender<Block, Block>> tagAppender){
        super.addGeneratedBlockTags(tagAppender);
        getAltBranch().ifPresent(branch -> {
            tierTag(getDefaultBranchHarvestTier(), tagAppender).ifPresent(tagBuilder -> tagBuilder.add(branch));
            defaultBranchTags().forEach(tag -> {
                if (!isOnlyIfLoaded()) {
                    tagAppender.apply(tag).add(branch);
                } else {
                    tagAppender.apply(tag).addOptional(branch);
                }
            });
        });
    }

}
