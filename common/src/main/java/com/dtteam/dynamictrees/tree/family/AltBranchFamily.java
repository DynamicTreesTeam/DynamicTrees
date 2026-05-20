package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.tree.BranchEntry;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class AltBranchFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(AltBranchFamily::new);

    public static final int ALT_BRANCH_INDEX = 2;

    public AltBranchFamily(Identifier name) {
        super(name);
    }

    @Override
    public void setupBlocks() {
        super.setupBlocks();

        addBranch(ALT_BRANCH_INDEX, new BranchEntry(this, getAltBranchName())
                .setCanBeStripped(hasStrippedBranch())
                .CreateBlock(this::createAltBranch));
    }

    protected Identifier getAltBranchName() {
        return getBranchName("alt_");
    }

    protected BranchBlock createAltBranch(Identifier name, BlockBehaviour.Properties properties) {
        return this.isThick() ? new ThickBranchBlock(name, properties){
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof AltBranchFamily altLogFamily)
                    return altLogFamily.getPrimitiveAltLog();
                return super.getPrimitiveLog();
            }
        } : new BasicBranchBlock(name, properties){
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof AltBranchFamily altLogFamily)
                    return altLogFamily.getPrimitiveAltLog();
                return super.getPrimitiveLog();
            }
        };
    }

    public Family setPrimitiveAltLog(Block primitiveLog) {
        branches.get(ALT_BRANCH_INDEX).setPrimitiveBlock(primitiveLog);
        return this;
    }

    public Optional<BranchBlock> getAltBranch() {
        return getBranchBlock(ALT_BRANCH_INDEX);
    }

    public Optional<Block> getPrimitiveAltLog() {
        return getPrimitiveLog(ALT_BRANCH_INDEX);
    }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        generators.add(altBranchModelGenerator());
        return generators;
    }

    protected Identifier altBranchModelGenerator() {
        return DynamicTrees.location("alt_branch");
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
