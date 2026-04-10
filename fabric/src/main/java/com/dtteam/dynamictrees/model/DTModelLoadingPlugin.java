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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class DTModelLoadingPlugin implements ModelLoadingPlugin {

    public static final Identifier POTTED_SAPLING_MODEL = DynamicTrees.location("potted_sapling");
    private static final Map<Identifier, BakedModel> BRANCH_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BakedModel> ROOT_MODEL_CACHE = new HashMap<>();
    private static final Map<Identifier, BakedModel> UNDERGROUND_ROOTS_MODEL_CACHE = new HashMap<>();
    private static boolean modelsInitialized = false;

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        modelsInitialized = false;
        BRANCH_MODEL_CACHE.clear();
        ROOT_MODEL_CACHE.clear();
        UNDERGROUND_ROOTS_MODEL_CACHE.clear();
        pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, this::modifyModelAfterBake);
    }

    private void initBranchModels(Function<Material, TextureAtlasSprite> spriteGetter) {
        if (modelsInitialized) return;
        modelsInitialized = true;

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
                    BakedModel model = createBranchModel(barkRef.get(), ringsRef.get(), isThick, spriteGetter);
                    BRANCH_MODEL_CACHE.put(blockId, model);
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
                    BakedModel model = createBranchModel(barkRef.get(), ringsRef.get(), isThick, spriteGetter);
                    BRANCH_MODEL_CACHE.put(blockId, model);
                });
            });

            family.getSurfaceRoot().ifPresent(surfaceRoot -> {
                family.getPrimitiveLog().ifPresent(primitiveLog -> {
                    Identifier primitiveLogId = BuiltInRegistries.BLOCK.getKey(primitiveLog);
                    Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveLogId.getNamespace(), "block/" + primitiveLogId.getPath());
                    AtomicReference<Identifier> barkRef = new AtomicReference<>(barkTexture);
                    family.getTexturePath(Family.BRANCH).ifPresent(barkRef::set);

                    Identifier blockId = BuiltInRegistries.BLOCK.getKey(surfaceRoot);
                    BakedModel model = createRootModel(barkRef.get(), spriteGetter);
                    ROOT_MODEL_CACHE.put(blockId, model);
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

                        BakedModel model = createRootsBlockModel(barkRef.get(), ringsRef.get(), spriteGetter);
                        UNDERGROUND_ROOTS_MODEL_CACHE.put(blockId.withSuffix("_exposed"), model);
                    });

                    undergroundFamily.getPrimitiveFilledRoots().ifPresent(primitiveFilledRoots -> {
                        Identifier primitiveFilledRootsId = BuiltInRegistries.BLOCK.getKey(primitiveFilledRoots);
                        Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_side");
                        Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_top");

                        BakedModel model = createRootsBlockModel(barkTexture, ringsTexture, spriteGetter);
                        UNDERGROUND_ROOTS_MODEL_CACHE.put(blockId.withSuffix("_filled"), model);
                    });


                });
            }
        }
    }

    private BakedModel createBranchModel(Identifier barkTexture, Identifier ringsTexture, boolean isThick, Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite barkSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, barkTexture));
        TextureAtlasSprite ringsSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ringsTexture));

        if (isThick) {
            Identifier thickRingsTexture = ringsTexture.withSuffix("_thick");
            TextureAtlasSprite thickRingsSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, thickRingsTexture));
            return new ThickBranchBlockBakedModel(barkSprite, ringsSprite, thickRingsSprite);
        }

        return new BasicBranchBlockBakedModel(barkSprite, ringsSprite);
    }

    private BakedModel createRootModel(Identifier barkTexture, Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite barkSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, barkTexture));
        return new SurfaceRootBlockBakedModel(barkSprite);
    }

    private BakedModel createRootsBlockModel(Identifier barkTexture, Identifier ringsTexture, Function<Material, TextureAtlasSprite> spriteGetter) {
        TextureAtlasSprite barkSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, barkTexture));
        TextureAtlasSprite ringsSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, ringsTexture));
        return new BasicRootsBlockBakedModel(barkSprite, ringsSprite);
    }

    private BakedModel createFallbackRootsModel(UndergroundRootsFamily family, String variant, Function<Material, TextureAtlasSprite> spriteGetter) {
        if (variant.contains("layer=exposed")) {
            return family.getPrimitiveRoots().map(primitiveRoots -> {
                Identifier primitiveRootsId = BuiltInRegistries.BLOCK.getKey(primitiveRoots);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_side");
                Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveRootsId.getNamespace(), "block/" + primitiveRootsId.getPath() + "_top");
                return createRootsBlockModel(barkTexture, ringsTexture, spriteGetter);
            }).orElse(null);
        } else if (variant.contains("layer=filled")) {
            return family.getPrimitiveFilledRoots().map(primitiveFilledRoots -> {
                Identifier primitiveFilledRootsId = BuiltInRegistries.BLOCK.getKey(primitiveFilledRoots);
                Identifier barkTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_side");
                Identifier ringsTexture = Identifier.fromNamespaceAndPath(primitiveFilledRootsId.getNamespace(), "block/" + primitiveFilledRootsId.getPath() + "_top");
                return createRootsBlockModel(barkTexture, ringsTexture, spriteGetter);
            }).orElse(null);
        }
        return null;
    }

    private BakedModel modifyModelAfterBake(BakedModel model, ModelModifier.AfterBake.Context context) {
        ModelIdentifier modelId = context.topLevelId();
        if (modelId == null) return model;

        if (modelId.id().equals(POTTED_SAPLING_MODEL)) {
            return new BakedModelBlockPottedSapling(model);
        }

        Identifier blockId = modelId.id();
        Block block = BuiltInRegistries.BLOCK.get(blockId);

        if (block instanceof BasicRootsBlock rootsBlock) {
            initBranchModels(context.textureGetter());

            String variant = modelId.variant();
            Identifier cacheKey;
            if (variant.contains("layer=filled")) {
                cacheKey = blockId.withSuffix("_filled");
            } else if (variant.contains("layer=exposed")) {
                cacheKey = blockId.withSuffix("_exposed");
            } else if (variant.contains("layer=covered")) {
                return model;
            } else {
                return model;
            }

            BakedModel rootsModel = UNDERGROUND_ROOTS_MODEL_CACHE.get(cacheKey);
            if (rootsModel != null) {
                return rootsModel;
            }

            if (rootsBlock.getFamily() instanceof UndergroundRootsFamily undergroundFamily) {
                BakedModel fallbackModel = createFallbackRootsModel(undergroundFamily, variant, context.textureGetter());
                if (fallbackModel != null) {
                    UNDERGROUND_ROOTS_MODEL_CACHE.put(cacheKey, fallbackModel);
                    return fallbackModel;
                }
            }
            return model;
        }

        if (block instanceof SurfaceRootBlock) {
            initBranchModels(context.textureGetter());

            BakedModel rootModel = ROOT_MODEL_CACHE.get(blockId);
            if (rootModel != null) {
                return rootModel;
            }
            return model;
        }

        if (block instanceof BranchBlock) {
            initBranchModels(context.textureGetter());

            BakedModel branchModel = BRANCH_MODEL_CACHE.get(blockId);
            if (branchModel != null) {
                return branchModel;
            }
        }


        return model;
    }
}
