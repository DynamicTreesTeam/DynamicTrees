package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BasicRootsBlock;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.BasicRootsBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.SurfaceRootBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DTModelLoadingPlugin implements ModelLoadingPlugin {

    public static final Identifier POTTED_SAPLING_MODEL = DynamicTrees.location("potted_sapling");
    private static final Map<Identifier, BlockStateModel> BRANCH_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> ROOT_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BlockStateModel> UNDERGROUND_ROOTS_MODEL_CACHE = new HashMap<>();
    private static boolean modelsInitialized = false;

    @Override
    public void initialize(Context pluginContext) {
        modelsInitialized = false;
        BRANCH_MODEL_CACHE.clear();
        ROOT_MODEL_CACHE.clear();
        UNDERGROUND_ROOTS_MODEL_CACHE.clear();
        pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, this::modifyModelAfterBake);
    }

    private void initBranchModels(net.minecraft.client.resources.model.ModelBaker baker) {
        if (modelsInitialized) return;
        modelsInitialized = true;
        ModelDebugName debugName = () -> "dynamictrees:branch";

        for (Family family : Family.REGISTRY.getAll()) {
            if (!family.isValid()) continue;

            family.getPrimitiveLog().ifPresent(primitiveLog -> {
                Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                Identifier ringsTexture = barkTexture.withSuffix("_top");

                AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                AtomicReference<Identifier> ringsRef = new AtomicReference<>(ringsTexture);

                family.getTexturePath(Family.BRANCH).ifPresent(barkRef::set);
                family.getTexturePath(Family.BRANCH_TOP).ifPresent(ringsRef::set);

                boolean isThick = family.isThick();

                family.getBranch().ifPresent(branch -> {
                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(branch);
                    BRANCH_MODEL_CACHE.put(blockId, createBranchModel(baker, debugName, barkRef.get(), ringsRef.get(), isThick));
                });
            });

            family.getPrimitiveStrippedLog().ifPresent(strippedLog -> {
                Identifier strippedLogId = BuiltInRegistries.BLOCK.getKey(strippedLog);
                Identifier strippedBarkTexture = Identifier.fromNamespaceAndPath(strippedLogId.getNamespace(), "block/" + strippedLogId.getPath());
                Identifier strippedRingsTexture = strippedBarkTexture.withSuffix("_top");

                AtomicReference<Identifier> barkRef = new AtomicReference<>(strippedBarkTexture);
                AtomicReference<Identifier> ringsRef = new AtomicReference<>(strippedRingsTexture);

                family.getTexturePath(Family.STRIPPED_BRANCH).ifPresent(barkRef::set);
                family.getTexturePath(Family.STRIPPED_BRANCH_TOP).ifPresent(ringsRef::set);

                boolean isThick = family.isThick();

                family.getStrippedBranch().ifPresent(strippedBranch -> {
                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(strippedBranch);
                    BRANCH_MODEL_CACHE.put(blockId, createBranchModel(baker, debugName, barkRef.get(), ringsRef.get(), isThick));
                });
            });

            family.getSurfaceRoot().ifPresent(surfaceRoot -> {
                family.getPrimitiveLog().ifPresent(primitiveLog -> {
                    Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                    Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                    AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                    family.getTexturePath(Family.BRANCH).ifPresent(barkRef::set);

                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(surfaceRoot);
                    Material.Baked bark = baker.materials().get(new Material(barkRef.get()), debugName);
                    ROOT_MODEL_CACHE.put(blockId, new SurfaceRootBlockBakedModel(baker, bark));
                });
            });

            if (family instanceof UndergroundRootsFamily undergroundFamily) {
                undergroundFamily.getRoots().ifPresent(roots -> {
                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(roots);

                    undergroundFamily.getPrimitiveRoots().ifPresent(primitiveRoots -> {
                        Identifier primitiveRootsId = BuiltInRegistries.BLOCK.getKey(primitiveRoots);
                        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_side");
                        Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_top");

                        AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                        AtomicReference<Identifier> ringsRef = new AtomicReference<>(ringsTexture);

                        family.getTexturePath(Family.ROOTS_SIDE).ifPresent(barkRef::set);
                        family.getTexturePath(Family.ROOTS_TOP).ifPresent(ringsRef::set);

                        UNDERGROUND_ROOTS_MODEL_CACHE.put(blockId.withSuffix("_exposed"),
                                createRootsBlockModel(baker, debugName, barkRef.get(), ringsRef.get()));
                    });

                    undergroundFamily.getPrimitiveFilledRoots().ifPresent(primitiveFilledRoots -> {
                        Identifier primitiveFilledRootsId = BuiltInRegistries.BLOCK.getKey(primitiveFilledRoots);
                        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_side");
                        Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_top");
                        UNDERGROUND_ROOTS_MODEL_CACHE.put(blockId.withSuffix("_filled"),
                                createRootsBlockModel(baker, debugName, barkTexture, ringsTexture));
                    });
                });
            }
        }
    }

    private BlockStateModel createBranchModel(net.minecraft.client.resources.model.ModelBaker baker, ModelDebugName debugName,
                                              Identifier barkTexture, Identifier ringsTexture, boolean isThick) {
        Material.Baked bark = baker.materials().get(new Material(barkTexture), debugName);
        Material.Baked rings = baker.materials().get(new Material(ringsTexture), debugName);
        if (isThick) {
            Material.Baked thickRings = baker.materials().get(new Material(ringsTexture.withSuffix("_thick")), debugName);
            return new ThickBranchBlockBakedModel(baker, bark, rings, thickRings);
        }
        return new BasicBranchBlockBakedModel(baker, bark, rings);
    }

    private BlockStateModel createRootsBlockModel(net.minecraft.client.resources.model.ModelBaker baker, ModelDebugName debugName,
                                                  Identifier barkTexture, Identifier ringsTexture) {
        Material.Baked bark = baker.materials().get(new Material(barkTexture), debugName);
        Material.Baked rings = baker.materials().get(new Material(ringsTexture), debugName);
        return new BasicRootsBlockBakedModel(baker, bark, rings);
    }

    private BlockStateModel modifyModelAfterBake(BlockStateModel model, ModelModifier.AfterBakeBlock.Context context) {
        BlockState state = context.state();
        Block block = state.getBlock();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);

        if (blockId.equals(POTTED_SAPLING_MODEL)) {
            return new BakedModelBlockPottedSapling(model);
        }

        if (block instanceof BasicRootsBlock) {
            initBranchModels(context.baker());
            if (state.hasProperty(BasicRootsBlock.LAYER)) {
                BasicRootsBlock.Layer layer = state.getValue(BasicRootsBlock.LAYER);
                if (layer == BasicRootsBlock.Layer.COVERED) {
                    return model;
                }
                Identifier cacheKey = blockId.withSuffix(layer == BasicRootsBlock.Layer.FILLED ? "_filled" : "_exposed");
                BlockStateModel rootsModel = UNDERGROUND_ROOTS_MODEL_CACHE.get(cacheKey);
                if (rootsModel != null) {
                    return rootsModel;
                }
            }
            return model;
        }

        if (block instanceof SurfaceRootBlock) {
            initBranchModels(context.baker());
            BlockStateModel rootModel = ROOT_MODEL_CACHE.get(blockId);
            return rootModel != null ? rootModel : model;
        }

        if (block instanceof BranchBlock) {
            initBranchModels(context.baker());
            BlockStateModel branchModel = BRANCH_MODEL_CACHE.get(blockId);
            if (branchModel != null) {
                return branchModel;
            }
        }

        return model;
    }
}
