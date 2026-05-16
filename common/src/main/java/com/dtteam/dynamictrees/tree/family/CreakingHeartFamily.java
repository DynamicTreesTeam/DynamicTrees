package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.BiConsumer;

import static com.dtteam.dynamictrees.utility.IdentifierUtils.suffix;

public class CreakingHeartFamily extends AltBranchFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CreakingHeartFamily::new);

    protected float heartHardnessMultiplier = 10;
    protected Item heartDropItem = Items.RESIN_CLUMP;

    public CreakingHeartFamily(Identifier name) {
        super(name);
    }

    /**
     * @return false if you are using a custom block entity for the heart.
     * Otherwise, DT will register {@link com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlockEntity}
     */
    public boolean registerDefaultBlockEntity(){
        return true;
    }

    public boolean hasHeart(BlockState state, BlockGetter level, BlockPos pos){
        BlockPos heart = CreakingHeartBranchBlock.findFromBranch(state, level, pos, this.getMaxSignalDepth());
        return heart != null;
    }

    public float getHeartHardness(BlockState state, BlockGetter level, BlockPos pos, float baseHardness){
        if (hasHeart(state, level, pos))
            return baseHardness * heartHardnessMultiplier;
        return baseHardness;
    }

    protected Identifier altBranchName(){
        return suffix(this.getRegistryName(), "_creaking_heart");
    }

    @Override
    protected BranchBlock createAltBranchBlock(Identifier name) {
        BasicBranchBlock branch = new CreakingHeartBranchBlock(name, this.getProperties());
        if (this.isFireProof()) branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
    }

    @Override
    protected BranchBlock createBranchBlock(Identifier name) {
        final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(name, this.getProperties()){
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return ((CreakingHeartFamily)getFamily()).getHeartHardness(state, level, pos, super.getHardness(state, level, pos));
            }
        } : new BasicBranchBlock(name, this.getProperties()){
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return ((CreakingHeartFamily)getFamily()).getHeartHardness(state, level, pos, super.getHardness(state, level, pos));
            }
        };
        if (this.isFireProof()) {
            branch.setFireSpreadSpeed(0).setFlammability(0);
        }
        return branch;
    }

    @Override
    protected Identifier altBranchModelGenerator() {
        return DynamicTrees.location("creaking_heart");
    }

    @Override
    public Identifier getAltBranchLoader() {
        return DynamicTrees.location("creaking_heart");
    }

    public void addHeartTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock, String state) {
        Optional<Block> primAlt = getPrimitiveAltLog();
        if (primAlt.isPresent() && primAlt.get() == sourceBlock){
            String u = state.isEmpty() ? "" : "_";
            Identifier barkAwake = primitiveLogLocation.withSuffix(u+state);
            Identifier ringsAwake = primitiveLogLocation.withSuffix("_top"+u+state);
            String textureName = state+u+"heart_branch";
            if (this.textureOverrides.containsKey(textureName))
                barkAwake = this.textureOverrides.get(textureName);
            if (this.textureOverrides.containsKey(textureName+"_top"))
                ringsAwake = this.textureOverrides.get(textureName+"_top");

            textureConsumer.accept("heart_bark", barkAwake);
            textureConsumer.accept("heart_rings", ringsAwake);
            return;
        }
        super.addBranchTextures(textureConsumer, primitiveLogLocation, sourceBlock);
    }

    public void setHeartHardnessMultiplier(float heartHardnessMultiplier) {
        this.heartHardnessMultiplier = heartHardnessMultiplier;
    }

    public void setHeartDropItem(Item heartDropItem) {
        this.heartDropItem = heartDropItem;
    }

    public Item getHeartDropItem() {
        return heartDropItem;
    }
}
