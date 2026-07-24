package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BasicRootsBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.SurfaceRootBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.family.AerialRootsFamily;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swaps the baked block state models of dynamic branch, root and potted sapling blocks
 * for DT's procedurally generated models, using the Fabric Model Loading API.
 */
public class DTModelLoadingPlugin implements ModelLoadingPlugin {

    public static final Identifier POTTED_SAPLING_MODEL = DynamicTrees.location("potted_sapling");

    private static final Map<Identifier, BlockStateModel> MODEL_CACHE = new ConcurrentHashMap<>();

    @Override
    public void initialize(Context pluginContext) {
        MODEL_CACHE.clear();
        pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, DTModelLoadingPlugin::modifyModelAfterBake);
    }

    private static BlockStateModel modifyModelAfterBake(BlockStateModel model, ModelModifier.AfterBakeBlock.Context context) {
        BlockState state = context.state();
        Block block = state.getBlock();

        if (block instanceof PottedSaplingBlock) {
            return new BakedModelBlockPottedSapling(model);
        }

        if (block instanceof BasicRootsBlock rootsBlock) {
            BlockStateModel rootsModel = getOrCreateRootsModel(rootsBlock, state, context.baker());
            return rootsModel != null ? rootsModel : model;
        }

        if (block instanceof SurfaceRootBlock surfaceRootBlock) {
            BlockStateModel rootModel = getOrCreateSurfaceRootModel(surfaceRootBlock, context.baker());
            return rootModel != null ? rootModel : model;
        }

        if (block instanceof BranchBlock branchBlock) {
            BlockStateModel branchModel = getOrCreateBranchModel(branchBlock, context.baker());
            return branchModel != null ? branchModel : model;
        }

        return model;
    }

    ///////////////////////////////////////////
    // BRANCHES
    ///////////////////////////////////////////

    @Nullable
    private static BlockStateModel getOrCreateBranchModel(BranchBlock branchBlock, ModelBaker baker) {
        Family family = branchBlock.getFamily();
        if (family == null || !family.isValid()) return null;

        Identifier blockId = BuiltInRegistries.BLOCK.getKey(branchBlock);

        boolean stripped = family.getStrippedBranch().map(b -> b == branchBlock).orElse(false);
        Optional<Block> primitiveLog = stripped ? family.getPrimitiveStrippedLog() : family.getPrimitiveLog();
        if (primitiveLog.isEmpty()) return null;

        Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog.get());
        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
        Identifier ringsTexture = IdentifierUtils.suffix(barkTexture, "_top");

        Identifier barkOverride = family.getTexturePath(stripped ? Family.STRIPPED_BRANCH : Family.BRANCH).orElse(barkTexture);
        Identifier ringsOverride = family.getTexturePath(stripped ? Family.STRIPPED_BRANCH_TOP : Family.BRANCH_TOP).orElse(ringsTexture);

        boolean isThick = family.isThick();

        return MODEL_CACHE.computeIfAbsent(blockId, id -> createBranchModel(barkOverride, ringsOverride, isThick, baker));
    }

    private static BlockStateModel createBranchModel(Identifier barkTexture, Identifier ringsTexture, boolean isThick, ModelBaker baker) {
        Material.Baked barkMat = bakeMaterial(baker, barkTexture);
        Material.Baked ringsMat = bakeMaterial(baker, ringsTexture);

        BasicBranchBlockBakedModel regular = BasicBranchBlockBakedModel.bakeBasic(baker,
                new BranchModelPart.UnbakedCore(barkMat),
                new BranchModelPart.UnbakedSleeve(barkMat),
                new BranchModelPart.UnbakedCore(ringsMat),
                null);

        if (isThick) {
            Identifier thickRingsTexture = IdentifierUtils.suffix(ringsTexture, "_thick");
            Material.Baked thickRingsMat = bakeMaterial(baker, thickRingsTexture);
            return ThickBranchBlockBakedModel.bakeThick(baker, regular,
                    new BranchModelPart.UnbakedThickTrunk(barkMat, false),
                    new BranchModelPart.UnbakedThickTrunk(thickRingsMat, true));
        }

        return regular;
    }

    ///////////////////////////////////////////
    // SURFACE ROOTS
    ///////////////////////////////////////////

    @Nullable
    private static BlockStateModel getOrCreateSurfaceRootModel(SurfaceRootBlock surfaceRootBlock, ModelBaker baker) {
        Family family = surfaceRootBlock.getFamily();
        if (family == null || !family.isValid()) return null;

        Optional<Block> primitiveLog = family.getPrimitiveLog();
        if (primitiveLog.isEmpty()) return null;

        Identifier blockId = BuiltInRegistries.BLOCK.getKey(surfaceRootBlock);
        Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog.get());
        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
        Identifier barkOverride = family.getTexturePath(Family.BRANCH).orElse(barkTexture);

        return MODEL_CACHE.computeIfAbsent(blockId, id -> SurfaceRootBlockBakedModel.bake(baker, barkOverride));
    }

    ///////////////////////////////////////////
    // UNDERGROUND (AERIAL) ROOTS
    ///////////////////////////////////////////

    @Nullable
    private static BlockStateModel getOrCreateRootsModel(BasicRootsBlock rootsBlock, BlockState state, ModelBaker baker) {
        if (!(rootsBlock.getFamily() instanceof AerialRootsFamily rootsFamily) || !rootsFamily.isValid()) return null;
        if (!state.hasProperty(BasicRootsBlock.LAYER)) return null;

        BasicRootsBlock.Layer layer = state.getValue(BasicRootsBlock.LAYER);
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(rootsBlock);

        return switch (layer) {
            case EXPOSED -> rootsFamily.getPrimitiveRoots().map(primitiveRoots -> {
                Identifier primitiveRootsId = BuiltInRegistries.BLOCK.getKey(primitiveRoots);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_side");
                Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_top");

                Identifier barkOverride = rootsFamily.getTexturePath(Family.ROOTS_SIDE).orElse(barkTexture);
                Identifier ringsOverride = rootsFamily.getTexturePath(Family.ROOTS_TOP).orElse(ringsTexture);

                return MODEL_CACHE.computeIfAbsent(IdentifierUtils.suffix(blockId, "_exposed"), id ->
                        BasicRootsBlockBakedModel.bakeRoots(baker, bakeMaterial(baker, barkOverride), bakeMaterial(baker, ringsOverride), false));
            }).orElse(null);
            case FILLED -> rootsFamily.getPrimitiveFilledRoots().map(primitiveFilledRoots -> {
                Identifier primitiveFilledRootsId = BuiltInRegistries.BLOCK.getKey(primitiveFilledRoots);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_side");
                Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_top");

                return MODEL_CACHE.computeIfAbsent(IdentifierUtils.suffix(blockId, "_filled"), id ->
                        BasicRootsBlockBakedModel.bakeRoots(baker, bakeMaterial(baker, barkTexture), bakeMaterial(baker, ringsTexture), true));
            }).orElse(null);
            case COVERED -> null; // Covered roots keep their regular (soil-like) model.
        };
    }

    private static Material.Baked bakeMaterial(ModelBaker baker, Identifier texture) {
        return baker.materials().get(new Material(texture), texture::toDebugFileName);
    }
}
