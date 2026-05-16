package com.dtteam.dynamictrees.tree.family;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.RegistryHandler;
import com.dtteam.dynamictrees.api.registry.TypedRegistry;
import com.dtteam.dynamictrees.block.branch.BasicBranchBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.CreakingHeartBranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.dtteam.dynamictrees.utility.Optionals;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.dtteam.dynamictrees.utility.IdentifierUtils.suffix;

public class CreakingHeartFamily extends AltBranchFamily {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(CreakingHeartFamily::new);

    protected float heartHardnessMultiplier = 10;
    protected Item heartDropItem = Items.RESIN_CLUMP;
    protected Supplier<BranchBlock> heartBranch;
    protected Block primitiveHeartLog;

    public CreakingHeartFamily(Identifier name) {
        super(name);
    }

    ///////////////////////////////////////////
    // HEART BRANCH
    ///////////////////////////////////////////


    @Override
    public void setupBlocks() {
        super.setupBlocks();

        this.heartBranch = setupBranch(createHeartBranch(heartBranchName()), false);
    }

    protected Identifier heartBranchName(){
        return suffix(this.getRegistryName(), "_creaking_heart");
    }

    protected Supplier<BranchBlock> createHeartBranch(Identifier name) {
        return RegistryHandler.addBlock(IdentifierUtils.suffix(name, this.getBranchNameSuffix()), () -> this.createHeartBranchBlock(name));
    }

    protected BranchBlock createHeartBranchBlock(Identifier name) {
        BasicBranchBlock branch = new CreakingHeartBranchBlock(name, this.getProperties());
        if (this.isFireProof()) branch.setFireSpreadSpeed(0).setFlammability(0);
        return branch;
    }

    public Family setPrimitiveHeartLog(Block primitiveLog) {
        this.primitiveHeartLog = primitiveLog;
        heartBranch.get().setPrimitiveLogDrops(List.of(()->new ItemStack(primitiveLog)));
        return this;
    }

    public Optional<BranchBlock> getHeartBranch() {
        return Optionals.ofBlock(heartBranch.get());
    }

    public Optional<Block> getPrimitiveHeartLog() {
        return Optionals.ofBlock(primitiveHeartLog);
    }

    @Override
    public List<Identifier> getBlockModelGenerators() {
        List<Identifier> generators = new LinkedList<>(super.getBlockModelGenerators());
        generators.add(heartBranchModelGenerator());
        return generators;
    }

    protected Identifier heartBranchModelGenerator() {
        return DynamicTrees.location("creaking_heart");
    }

    public Identifier getHeartBranchLoader() {
        return DynamicTrees.location("creaking_heart");
    }

    ///////////////////////////////////////////
    // OTHER BRANCHES
    ///////////////////////////////////////////

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
    protected BranchBlock createAltBranchBlock(Identifier name) {
        final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(name, this.getProperties()){
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof AltBranchFamily altLogFamily)
                    return altLogFamily.getPrimitiveAltLog();
                return super.getPrimitiveLog();
            }
            @Override
            public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
                return ((CreakingHeartFamily)getFamily()).getHeartHardness(state, level, pos, super.getHardness(state, level, pos));
            }
        } : new BasicBranchBlock(name, this.getProperties()){
            @Override
            public Optional<Block> getPrimitiveLog() {
                if (getFamily() instanceof AltBranchFamily altLogFamily)
                    return altLogFamily.getPrimitiveAltLog();
                return super.getPrimitiveLog();
            }
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
    protected Identifier altBranchName() {
        return getBranchName("resin_");
    }

//    @Override
//    protected Identifier altBranchModelGenerator() {
//        return DynamicTrees.location("resin_branch");
//    }

//    @Override
//    public Identifier getAltBranchLoader() {
//        return DynamicTrees.location("resin_branch");
//    }

    @Override
    public Family setPrimitiveLog(Block primitiveLog) {
        setPrimitiveAltLog(primitiveLog);
        return super.setPrimitiveLog(primitiveLog);
    }

    ///////////////////////////////////////////
    // HEART LOGIC
    ///////////////////////////////////////////

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

    public void addHeartTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation, Block sourceBlock, String state) {
        Optional<Block> primHeart = getPrimitiveHeartLog();
        if (primHeart.isPresent() && primHeart.get() == sourceBlock){
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
        }
        DynamicTrees.LOG.error("Attempted to load heart branch textures for family {} but the provided block {} was not it's heart branch.", getRegistryName(), primHeart);
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
