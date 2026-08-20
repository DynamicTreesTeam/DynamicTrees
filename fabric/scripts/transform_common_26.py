"""Compile-time rewrite of common 1.21.1 sources for Minecraft 26.2 (Fabric only)."""
from __future__ import annotations

import re
import sys
from pathlib import Path

IMPORT_REPLACEMENTS = [
    ("import net.minecraft.resources.ResourceLocation;", "import net.minecraft.resources.Identifier;"),
    ("import net.minecraft.ResourceLocationException;", "import net.minecraft.IdentifierException;"),
    ("import net.minecraft.Util;", "import net.minecraft.util.Util;"),
    ("import net.minecraft.util.Tuple;", "import com.mojang.datafixers.util.Pair;"),
    ("import net.minecraft.client.renderer.block.model.BakedQuad;", "import net.minecraft.client.resources.model.geometry.BakedQuad;"),
    ("import net.minecraft.client.renderer.block.model.BlockElement;", "import net.minecraft.client.resources.model.cuboid.CuboidModelElement;"),
    ("import net.minecraft.client.renderer.block.model.BlockElementFace;", "import net.minecraft.client.resources.model.cuboid.CuboidFace;"),
    ("import net.minecraft.client.renderer.block.model.BlockFaceUV;", "import net.minecraft.client.resources.model.cuboid.CuboidFace.UVs;"),
    ("import net.minecraft.client.renderer.block.model.FaceBakery;", "import net.minecraft.client.resources.model.cuboid.FaceBakery;"),
    ("import net.minecraft.client.renderer.block.model.ItemTransforms;", "import net.minecraft.client.resources.model.cuboid.ItemTransforms;"),
    ("import net.minecraft.client.resources.model.Material;", "import net.minecraft.client.resources.model.sprite.Material;"),
    ("import net.minecraft.client.resources.model.BlockModelRotation;", "import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;"),
    ("import net.minecraft.client.resources.model.ModelState;", "import net.minecraft.client.renderer.block.dispatch.ModelState;"),
    ("import net.minecraft.client.resources.model.BakedModel;", "import net.minecraft.client.renderer.block.dispatch.BlockStateModel;"),
    ("import net.minecraft.world.level.BlockAndTintGetter;", "import net.minecraft.world.level.BlockGetter;"),
    ("import net.minecraft.world.item.ItemInteractionResult;", "import net.minecraft.world.InteractionResult;"),
    ("import net.minecraft.world.InteractionResultHolder;", "import net.minecraft.world.InteractionResult;"),
    ("import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;", "import com.mojang.serialization.MapCodec;"),
    ("import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;", "import com.mojang.serialization.MapCodec;"),
    ("import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;", "import com.mojang.serialization.MapCodec;"),
    ("import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;", "import net.minecraft.util.context.ContextKeySet;"),
    ("import net.minecraft.world.level.storage.loot.parameters.LootContextParam;", "import net.minecraft.util.context.ContextKey;"),
    ("import net.minecraft.commands.arguments.ResourceLocationArgument;", "import net.minecraft.commands.arguments.IdentifierArgument;"),
    ("import net.minecraft.MethodsReturnNonnullByDefault;", ""),
    ("import javax.annotation.ParametersAreNonnullByDefault;", ""),
    ("import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;", ""),
    ("import net.minecraft.util.random.SimpleWeightedRandomList;", "import net.minecraft.util.random.WeightedList;"),
    ("import net.minecraft.util.random.WeightedRandomList;", "import net.minecraft.util.random.WeightedList;"),
    ("import net.minecraft.util.random.WeightedEntry;", "import net.minecraft.util.random.Weighted;"),
    ("import net.minecraft.world.item.Tier;", "import net.minecraft.world.item.ToolMaterial;"),
    ("import net.minecraft.world.level.GameRules;", "import net.minecraft.world.level.gamerules.GameRules;"),
    ("import net.minecraft.advancements.critereon.*;", "import net.minecraft.advancements.predicates.*;"),
    ("import net.minecraft.advancements.critereon.", "import net.minecraft.advancements.predicates."),
    ("import net.minecraft.advancements.critereon.ItemPredicate;", "import net.minecraft.advancements.predicates.ItemPredicate;"),
    ("import net.minecraft.client.renderer.MultiBufferSource;", "import net.minecraft.client.renderer.SubmitNodeCollector;"),
    ("import net.minecraft.client.renderer.block.BlockRenderDispatcher;", "import net.minecraft.client.renderer.block.ModelBlockRenderer;"),
    ("import net.minecraft.client.resources.model.ModelResourceLocation;", "import net.minecraft.resources.Identifier;"),
    ("import net.minecraft.client.renderer.RenderType;", "import net.minecraft.client.renderer.rendertype.RenderType;"),
    ("import net.minecraft.FileUtil;", "import net.minecraft.util.FileUtil;"),
    ("import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;", "import com.mojang.serialization.MapCodec;"),
]


def transform(s: str, rel: str) -> str:
    for old, new in IMPORT_REPLACEMENTS:
        s = s.replace(old, new)

    s = s.replace("new Tuple<>", "Pair.of")
    s = s.replace("Tuple<", "Pair<")
    s = s.replace("tuple.getA()", "tuple.getFirst()")
    s = s.replace("tuple.getB()", "tuple.getSecond()")
    s = s.replace("tup.getA()", "tup.getFirst()")
    s = s.replace("tup.getB()", "tup.getSecond()")

    s = re.sub(r"(?<!\w)ResourceLocation(?!Exception)(?!\w)", "Identifier", s)
    s = re.sub(r"(?<!\w)ResourceLocationException(?!\w)", "IdentifierException", s)
    s = re.sub(r"(?<!\w)ItemInteractionResult(?!\w)", "InteractionResult", s)
    s = re.sub(
        r"new InteractionResultHolder<>\(InteractionResult\.SUCCESS,\s*",
        "InteractionResult.SUCCESS.heldItemTransformedTo(",
        s,
    )
    s = re.sub(
        r"new InteractionResultHolder<>\(InteractionResult\.FAIL,\s*[^)]+\)",
        "InteractionResult.FAIL",
        s,
    )
    s = re.sub(
        r"new InteractionResultHolder<>\(InteractionResult\.PASS,\s*[^)]+\)",
        "InteractionResult.PASS",
        s,
    )
    s = s.replace("InteractionResultHolder.sidedSuccess(", "InteractionResult.SUCCESS.heldItemTransformedTo(")
    s = s.replace("InteractionResultHolder.success(", "InteractionResult.SUCCESS.heldItemTransformedTo(")
    s = s.replace("InteractionResultHolder.pass(", "InteractionResult.PASS; // pass(")
    s = s.replace("InteractionResultHolder.fail(", "InteractionResult.FAIL; // fail(")
    s = s.replace("InteractionResultHolder<ItemStack>", "InteractionResult")
    s = re.sub(r"(?<!\w)InteractionResultHolder(?!\w)", "InteractionResult", s)
    s = re.sub(r"InteractionResult<ItemStack>", "InteractionResult", s)
    s = s.replace("InteractionResult.sidedSuccess(", "InteractionResult.SUCCESS.heldItemTransformedTo(")
    s = s.replace("InteractionResult.success(", "InteractionResult.SUCCESS.heldItemTransformedTo(")
    s = s.replace("InteractionResult.pass(", "InteractionResult.PASS; // pass(")
    s = s.replace("InteractionResult.fail(", "InteractionResult.FAIL; // fail(")
    s = s.replace("import net.minecraft.FileUtil;", "import net.minecraft.util.FileUtil;")
    s = re.sub(r"(?<!\w)LootContextParamSet(?!\w)", "ContextKeySet", s)
    s = re.sub(r"(?<!\w)LootContextParam(?!s)(?!\w)", "ContextKey", s)
    s = re.sub(r"(?<!\w)Tier(?!\w)", "ToolMaterial", s)
    s = s.replace("Tiers.", "ToolMaterial.")
    s = s.replace("WeightedRandomList", "WeightedList")
    s = s.replace("SimpleWeightedRandomList", "WeightedList")
    s = s.replace("WeightedEntry.Wrapper", "Weighted")
    s = s.replace("Weighted.Wrapper", "Weighted")
    s = s.replace("wrapper.data()", "wrapper.value()")
    s = re.sub(r"chunkPos\.x(?!\()", "chunkPos.x()", s)
    s = re.sub(r"chunkPos\.z(?!\()", "chunkPos.z()", s)
    s = s.replace(".getNormal()", ".getUnitVec3i()")
    s = s.replace("Direction.getNearest(", "Direction.getApproximateNearest(")
    s = s.replace(".getParamOrNull(", ".getOptionalParameter(")
    s = s.replace("GameRules.RULE_RANDOMTICKING", "GameRules.RANDOM_TICK_SPEED")
    s = s.replace(".getGameRules().getInt(", ".getGameRules().get(")
    s = s.replace(".getGameRules().getBoolean(", ".getGameRules().get(")
    s = s.replace(".get().create(level)", ".get().create(level, net.minecraft.world.entity.EntitySpawnReason.TRIGGERED)")
    s = s.replace("new HoverEvent(HoverEvent.Action.SHOW_TEXT,", "new HoverEvent.ShowText(")
    s = s.replace("new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,", "new ClickEvent.CopyToClipboard(")
    s = s.replace(".noCollission()", ".noCollision()")
    s = s.replace("BlockAndTintGetter", "BlockGetter")
    s = s.replace("noCollission()", "noCollision()")
    s = s.replace("InventoryMenu.BLOCK_ATLAS", "TextureAtlas.LOCATION_BLOCKS")

    s = re.sub(r"@MethodsReturnNonnullByDefault\s*", "", s)
    s = re.sub(r"@ParametersAreNonnullByDefault\s*", "", s)

    s = re.sub(r"\.isClientSide(?!\s*\()", ".isClientSide()", s)
    s = re.sub(r"\b(level|world|pLevel|p_level|theLevel)\.random\b", r"\1.getRandom()", s)

    s = re.sub(r"new ChunkPos\(([^,()]+?)\)", r"ChunkPos.containing(\1)", s)

    s = re.sub(r'\.getInt\("([^"]+)"\)', r'.getIntOr("\1", 0)', s)
    s = re.sub(r'\.getLong\("([^"]+)"\)', r'.getLongOr("\1", 0L)', s)
    s = re.sub(r'\.getShort\("([^"]+)"\)', r'.getShortOr("\1", (short) 0)', s)
    s = re.sub(r'\.getByte\("([^"]+)"\)', r'.getByteOr("\1", (byte) 0)', s)
    s = re.sub(r'\.getFloat\("([^"]+)"\)', r'.getFloatOr("\1", 0.0F)', s)
    s = re.sub(r'\.getDouble\("([^"]+)"\)', r'.getDoubleOr("\1", 0.0D)', s)
    s = re.sub(r'\.getString\("([^"]+)"\)', r'.getStringOr("\1", "")', s)
    s = re.sub(r'\.getBoolean\("([^"]+)"\)', r'.getBooleanOr("\1", false)', s)
    s = re.sub(r'\.getCompound\("([^"]+)"\)', r'.getCompoundOrEmpty("\1")', s)
    s = re.sub(r'\.getByteArray\("([^"]+)"\)', r'.getByteArray("\1").orElseGet(() -> new byte[0])', s)
    s = re.sub(r'\.getIntArray\("([^"]+)"\)', r'.getIntArray("\1").orElseGet(() -> new int[0])', s)

    s = s.replace("BuiltInRegistries.BLOCK.get(", "BuiltInRegistries.BLOCK.getValue(")
    s = s.replace("BuiltInRegistries.ITEM.get(", "BuiltInRegistries.ITEM.getValue(")
    s = s.replace("BuiltInRegistries.BLOCK_ENTITY_TYPE.get(", "BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(")
    s = s.replace("BuiltInRegistries.ENTITY_TYPE.get(", "BuiltInRegistries.ENTITY_TYPE.getValue(")
    s = s.replace("BuiltInRegistries.SOUND_EVENT.get(", "BuiltInRegistries.SOUND_EVENT.getValue(")

    s = s.replace("FoliageColor.getBirchColor()", "FoliageColor.FOLIAGE_BIRCH")
    s = s.replace("FoliageColor.getEvergreenColor()", "FoliageColor.FOLIAGE_EVERGREEN")
    s = s.replace("FoliageColor.getDefaultColor()", "FoliageColor.FOLIAGE_DEFAULT")

    s = s.replace(".getType().is(", ".is(")

    s = s.replace(
        "new SimpleCraftingRecipeSerializer<>(SeedConversionRecipe::new)",
        "new RecipeSerializer<>(MapCodec.unit(new SeedConversionRecipe()), StreamCodec.unit(new SeedConversionRecipe()))",
    )
    s = s.replace(
        "new SimpleCraftingRecipeSerializer<>(MegaSeedRecipe::new)",
        "new RecipeSerializer<>(MapCodec.unit(new MegaSeedRecipe()), StreamCodec.unit(new MegaSeedRecipe()))",
    )
    s = re.sub(
        r"public SeedConversionRecipe\(CraftingBookCategory pCategory\) \{\s*super\(pCategory\);",
        "public SeedConversionRecipe() {\n        super();",
        s,
    )
    s = re.sub(
        r"public MegaSeedRecipe\(CraftingBookCategory pCategory\) \{\s*super\(pCategory\);",
        "public MegaSeedRecipe() {\n        super();",
        s,
    )
    s = s.replace("super(pCategory);", "super();")

    s = re.sub(
        r"Supplier<LootItemConditionType>",
        "Supplier<MapCodec<? extends LootItemCondition>>",
        s,
    )
    s = re.sub(
        r"Supplier<LootPoolEntryType>",
        "Supplier<MapCodec<? extends LootPoolEntryContainer>>",
        s,
    )
    s = re.sub(
        r"Supplier<LootItemFunctionType<([^>]+)>>",
        r"Supplier<MapCodec<\1>>",
        s,
    )
    s = re.sub(
        r"public LootItemConditionType getType\(\)",
        "public MapCodec<? extends LootItemCondition> codec()",
        s,
    )
    s = re.sub(
        r"public LootPoolEntryType getType\(\)",
        "public MapCodec<? extends LootPoolEntryContainer> codec()",
        s,
    )
    s = re.sub(
        r"public LootItemFunctionType<\? extends LootItemConditionalFunction> getType\(\)",
        "public MapCodec<? extends LootItemFunction> codec()",
        s,
    )
    s = re.sub(r"new LootItemConditionType\(([^)]+)\)", r"\1", s)
    s = re.sub(r"new LootPoolEntryType\(([^)]+)\)", r"\1", s)
    s = re.sub(r"new LootItemFunctionType<>\(([^)]+)\)", r"\1", s)
    s = re.sub(r"new LootItemFunctionType\(([^)]+)\)", r"\1", s)

    s = re.sub(
        r"new BlockEntityType<>\(([^,]+),\s*([^,]+),\s*null\)",
        r"new BlockEntityType<>((\1), (\2))",
        s,
    )
    s = re.sub(
        r"new BlockEntityType<\(([^>]+)>\(([^,]+),\s*([^,]+),\s*null\)",
        r"new BlockEntityType<\1>((\2), (\3))",
        s,
    )

    s = re.sub(
        r"CompletableFuture<Void> reload\(PreparationBarrier stage, ResourceManager resourceManager,\s*"
        r"ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,\s*"
        r"Executor backgroundExecutor, Executor gameExecutor\)",
        "CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor backgroundExecutor, "
        "PreparationBarrier stage, Executor gameExecutor)",
        s,
    )
    s = s.replace(
        "return super.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);",
        "return super.reload(sharedState, backgroundExecutor, stage, gameExecutor);",
    )

    s = re.sub(
        r"protected void loadAdditional\(([^)]*CompoundTag[^)]*)\)",
        "protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input)",
        s,
    )
    s = re.sub(
        r"protected void saveAdditional\(([^)]*CompoundTag[^)]*)\)",
        "protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output)",
        s,
    )
    s = re.sub(
        r"protected void readAdditionalSaveData\(([^)]*CompoundTag[^)]*)\)",
        "protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input)",
        s,
    )
    s = re.sub(
        r"protected void addAdditionalSaveData\(([^)]*CompoundTag[^)]*)\)",
        "protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output)",
        s,
    )

    if "extends LeavesBlock" in s:
        s = re.sub(
            r"super\((properties[^)]*)\)",
            r"super(0.01F, \1)",
            s,
            count=1,
        )
        if "spawnFallingLeavesParticle" not in s:
            s = s.replace(
                "public class DynamicLeavesBlock extends LeavesBlock",
                "public class DynamicLeavesBlock extends LeavesBlock",
            )
            insert = (
                "\n    @Override\n"
                "    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {\n"
                "    }\n"
                "    @Override\n"
                "    public com.mojang.serialization.MapCodec<? extends LeavesBlock> codec() {\n"
                "        return Block.simpleCodec(DynamicLeavesBlock::new);\n"
                "    }\n"
            )
            s = re.sub(
                r"(public DynamicLeavesBlock\(Properties properties\) \{[^}]+\}\n)",
                r"\1" + insert,
                s,
                count=1,
            )

    norm = rel.replace("\\", "/")
    if norm.endswith("client/RetexturedBakedQuad.java"):
        s = """package com.dtteam.dynamictrees.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public class RetexturedBakedQuad {
    public static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite textureIn) {
        return quad;
    }
}
"""
    if norm.endswith("entity/render/FallingTreeRenderer.java"):
        s = """package com.dtteam.dynamictrees.entity.render;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity, EntityRenderState> {
    public FallingTreeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
"""
    if norm.endswith("entity/render/LingeringEffectorRenderer.java"):
        s = """package com.dtteam.dynamictrees.entity.render;

import com.dtteam.dynamictrees.entity.LingeringEffectorEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class LingeringEffectorRenderer extends EntityRenderer<LingeringEffectorEntity, EntityRenderState> {
    public LingeringEffectorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(LingeringEffectorEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
"""
    if norm.endswith("client/ThickBranchRingsSource.java"):
        s = s.replace("import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;\n", "")
        s = s.replace("public static SpriteSourceType TYPE;\n", "")
        s = re.sub(
            r"@Override\s*public SpriteSourceType type\(\) \{\s*return TYPE;\s*\}",
            "@Override\n    public MapCodec<? extends SpriteSource> codec() {\n        return CODEC;\n    }",
            s,
        )
        s = re.sub(
            r"public static SpriteSourceType setType \(MapCodec<ThickBranchRingsSource> codec\)\{\s*TYPE = new SpriteSourceType\(codec\);\s*return TYPE;\s*\}",
            "",
            s,
        )
    if norm.endswith("model/FallingTreeEntityModel.java"):
        s = """package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Collectors;

public class FallingTreeEntityModel {

    protected final List<TreeQuadData> quads;
    protected final int entityId;
    protected final Species species;

    public FallingTreeEntityModel(FallingTreeEntity entity) {
        BranchDestructionData destructionData = entity.getDestroyData();
        this.species = destructionData.species;
        this.quads = generateTreeQuads(entity);
        this.entityId = entity.getId();
    }

    public List<TreeQuadData> getQuads() {
        return quads;
    }

    public int getEntityId() {
        return entityId;
    }

    public static int getBrightness(FallingTreeEntity entity) {
        final BranchDestructionData destructionData = entity.getDestroyData();
        final Level world = entity.level();
        return world.getBlockState(destructionData.cutPos).getLightEmission();
    }

    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        return List.of();
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, BlockState state) {
        return toTreeQuadData(bakedQuads, 0xFFFFFF, state);
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, int defaultColor, BlockState state) {
        return bakedQuads.stream().map(bakedQuad -> new TreeQuadData(bakedQuad, defaultColor, state)).collect(Collectors.toList());
    }

    public record TreeQuadData(BakedQuad bakedQuad, int color, BlockState state) { }
}
"""

    if "import net.minecraft.client.renderer.texture.TextureAtlas;" not in s and "TextureAtlas.LOCATION_BLOCKS" in s:
        s = s.replace(
            "package ",
            "package ",
            1,
        )
        s = re.sub(
            r"(package [^\n]+;\n)",
            r"\1\nimport net.minecraft.client.renderer.texture.TextureAtlas;\n",
            s,
            count=1,
        )

    if "MapCodec.unit(new SeedConversionRecipe())" in s or "MapCodec.unit(new MegaSeedRecipe())" in s:
        if "import com.mojang.serialization.MapCodec;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport com.mojang.serialization.MapCodec;\n", s, count=1)
        if "import net.minecraft.network.codec.StreamCodec;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.network.codec.StreamCodec;\n", s, count=1)

    if "MapCodec<? extends LootItemCondition>" in s or "MapCodec<? extends LootPoolEntryContainer>" in s:
        if "import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;" not in s and "LootItemCondition" in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.level.storage.loot.predicates.LootItemCondition;\n", s, count=1)
        if "import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;" not in s and "LootPoolEntryContainer" in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;\n", s, count=1)
        if "import com.mojang.serialization.MapCodec;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport com.mojang.serialization.MapCodec;\n", s, count=1)

    if "extends Entity " in s or "extends Entity\n" in s or "extends Entity{" in s:
        if "hurtServer" not in s and "class FallingTreeEntity" in s or "class LingeringEffectorEntity" in s:
            s = s.replace(
                "public class FallingTreeEntity extends Entity",
                "public class FallingTreeEntity extends Entity",
            )
            inject = """
    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
"""
            if "class FallingTreeEntity" in s and "hurtServer" not in s:
                s = s.replace("public class FallingTreeEntity extends Entity implements ModelTracker {",
                              "public class FallingTreeEntity extends Entity implements ModelTracker {" + inject)
            if "class LingeringEffectorEntity" in s and "hurtServer" not in s:
                s = re.sub(
                    r"(public class LingeringEffectorEntity extends Entity[^{]*\{)",
                    r"\1" + inject,
                    s,
                    count=1,
                )

    s = re.sub(
        r"(\w+)\.kill\(\)",
        r"\1.discard()",
        s,
    )

    if "import net.minecraft.world.item.ToolMaterial;" not in s and re.search(r"\bToolMaterial\b", s):
        s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.item.ToolMaterial;\n", s, count=1)
    if "import net.minecraft.util.context.ContextKeySet;" not in s and re.search(r"\bContextKeySet\b", s):
        s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.util.context.ContextKeySet;\n", s, count=1)
    if "import net.minecraft.util.context.ContextKey;" not in s and re.search(r"\bContextKey<", s):
        s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.util.context.ContextKey;\n", s, count=1)
    if "import net.minecraft.util.random.Weighted;" not in s and re.search(r"\bWeighted<", s):
        s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.util.random.Weighted;\n", s, count=1)
    s = re.sub(
        r"BlockState updateShape\(BlockState (\w+), Direction (\w+), BlockState (\w+), LevelAccessor (\w+), BlockPos (\w+), BlockPos (\w+)\)",
        r"BlockState updateShape(BlockState \1, LevelReader \4, net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos \5, Direction \2, BlockPos \6, BlockState \3, RandomSource random)",
        s,
    )
    s = re.sub(
        r"super\.updateShape\((\w+), (\w+), (\w+), (\w+), (\w+), (\w+)\)",
        r"super.updateShape(\1, \4, ticks, \5, \2, \6, \3, random)",
        s,
    )
    s = re.sub(
        r"ItemStack getCloneItemStack\(LevelReader (\w+), BlockPos (\w+), BlockState (\w+)\)",
        r"ItemStack getCloneItemStack(LevelReader \1, BlockPos \2, BlockState \3, boolean includeData)",
        s,
    )
    s = re.sub(
        r"\.getCloneItemStack\(([^,()]+), ([^,()]+), ([^,()]+)\)",
        r".getCloneItemStack(\1, \2, \3, false)",
        s,
    )
    s = re.sub(
        r"void entityInside\(BlockState (\w+), Level (\w+), BlockPos (\w+), Entity (\w+)\)",
        r"void entityInside(BlockState \1, Level \2, BlockPos \3, Entity \4, net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean moved)",
        s,
    )
    s = re.sub(
        r"super\.entityInside\((\w+), (\w+), (\w+), (\w+)\)",
        r"super.entityInside(\1, \2, \3, \4, applier, moved)",
        s,
    )
    s = re.sub(
        r"VoxelShape getOcclusionShape\(BlockState (\w+), BlockGetter (\w+), BlockPos (\w+)\)",
        r"VoxelShape getOcclusionShape(BlockState \1)",
        s,
    )
    s = re.sub(
        r"super\.getOcclusionShape\((\w+), (\w+), (\w+)\)",
        r"super.getOcclusionShape(\1)",
        s,
    )
    s = re.sub(
        r"boolean propagatesSkylightDown\(BlockState (\w+), BlockGetter (\w+), BlockPos (\w+)\)",
        r"boolean propagatesSkylightDown(BlockState \1)",
        s,
    )
    s = re.sub(
        r"\.propagatesSkylightDown\([^)]+\)",
        ".propagatesSkylightDown()",
        s,
    )
    s = re.sub(
        r"void neighborChanged\(BlockState (\w+), Level (\w+), BlockPos (\w+), Block (\w+), BlockPos (\w+), boolean (\w+)\)",
        r"void neighborChanged(BlockState \1, Level \2, BlockPos \3, Block \4, net.minecraft.world.level.redstone.Orientation orientation, boolean \6)",
        s,
    )
    s = re.sub(
        r"void onExplosionHit\(BlockState (\w+), Level (\w+), BlockPos (\w+), Explosion (\w+), BiConsumer<ItemStack, BlockPos> (\w+)\)",
        r"void onExplosionHit(BlockState \1, net.minecraft.server.level.ServerLevel \2, BlockPos \3, Explosion \4, BiConsumer<ItemStack, BlockPos> \5)",
        s,
    )
    s = re.sub(
        r"protected void onRemove\(BlockState (\w+), Level (\w+), BlockPos (\w+), BlockState (\w+), boolean (\w+)\)",
        r"protected void affectNeighborsAfterRemoval(BlockState \1, net.minecraft.server.level.ServerLevel \2, BlockPos \3, boolean \5)",
        s,
    )
    s = re.sub(
        r"super\.onRemove\(([^;]+)\)",
        r"super.affectNeighborsAfterRemoval(\1)",
        s,
    )
    s = re.sub(
        r"super\.affectNeighborsAfterRemoval\((\w+), (\w+), (\w+), (\w+), (\w+)\)",
        r"super.affectNeighborsAfterRemoval(\1, \2, \3, \5)",
        s,
    )
    s = re.sub(
        r"void appendHoverText\(ItemStack (\w+), TooltipContext (\w+), List<Component> (\w+), TooltipFlag (\w+)\)",
        r"void appendHoverText(ItemStack \1, TooltipContext \2, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> \3, TooltipFlag \4)",
        s,
    )
    s = re.sub(
        r"super\.appendHoverText\((\w+), (\w+), (\w+), (\w+)\)",
        r"super.appendHoverText(\1, \2, display, \3, \4)",
        s,
    )
    if "Consumer<net.minecraft.network.chat.Component>" in s and "tooltipComponents.add(" in s:
        s = s.replace("tooltipComponents.add(", "tooltipComponents.accept(")

    s = re.sub(
        r"public DynamicHardnessBlockState\(Block block, Reference2ObjectArrayMap<Property<\?>, Comparable<\?>> propertiesToValueMap, MapCodec<BlockState> codec\) \{\s*super\(block, propertiesToValueMap, codec\);",
        "public DynamicHardnessBlockState(Block block, Property<?>[] properties, Comparable<?>[] values) {\n            super(block, properties, values);",
        s,
    )

    if "Minecraft.getInstance().getBlockColors().getColor" in s:
        s = s.replace(
            "this.colorMultiplier = (s, w, p, t) -> c == -1 ? Minecraft.getInstance().getBlockColors().getColor(getPrimitiveLeaves(), w, p, 0) : c;",
            "this.colorMultiplier = (s, w, p, t) -> c == -1 ? 0x48B518 : c;",
        )

    if "LootContextParamSets.REGISTRY" in s:
        s = s.replace(
            "LootContextParamSets.REGISTRY.put(DynamicTrees.location(path), paramSet);",
            "// Fabric 26.2: loot context key sets are no longer a public registry",
        )

    if "GenerationStep.Carving" in s:
        s = s.replace(
            "public abstract List<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving stage);",
            "public List<Holder<ConfiguredWorldCarver<?>>> getCarvers() { return java.util.List.of(); }",
        )

    if "getBlockTicks().schedule" in s or "getFluidTicks().schedule" in s:
        s = re.sub(
            r"(\w+)\.getBlockTicks\(\)\.schedule\(new ScheduledTick<>\(([^,]+), ([^,]+), ([^,]+), ([^,]+), [^)]+\)\);",
            r"\1.scheduleTick(\3, \2, (int) \4, \5);",
            s,
        )
        s = re.sub(
            r"(\w+)\.getFluidTicks\(\)\.schedule\(new ScheduledTick<>\(([^,]+),\s*([^,]+),\s*([^,]+),\s*[^)]+\)\);",
            r"ticks.scheduleTick(\3, \2, \4);",
            s,
        )

    s = re.sub(r"InteractionResult\.SUCCESS\.heldItemTransformedTo\([^)]*isClientSide\(\)\)", "InteractionResult.SUCCESS", s)
    s = s.replace("noCollission", "noCollision")
    s = s.replace(".getMinBuildHeight()", ".getMinY()")
    s = s.replace(".getMaxBuildHeight()", ".getMaxY()")
    s = re.sub(r'[ \t]*@Override\s*\n', '', s)
    s = s.replace("muse.state.getCloneItemStack(level, muse.pos, muse.state, false)", "muse.state.getCloneItemStack(level, muse.pos, false)")
    s = s.replace(".asLookup()", "")
    s = s.replace("chunkPos.getCenter()", "new net.minecraft.core.BlockPos(chunkPos.getMiddleBlockX(), 0, chunkPos.getMiddleBlockZ())")
    s = s.replace("InteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION", "InteractionResult.TRY_WITH_EMPTY_HAND")
    s = s.replace(".getDayTime()", ".getOverworldClockTime()")
    s = s.replace("ResourceLocationArgument", "IdentifierArgument")
    s = s.replace("canPlaceLiquid(@Nullable Player player,", "canPlaceLiquid(@Nullable LivingEntity player,")
    s = re.sub(r"\.getLightBlock\([^)]+\)", ".getLightDampening()", s)
    s = s.replace("getLightDampening())", "getLightDampening()")
    s = s.replace(".dimension().location()", ".dimension().identifier()")
    s = s.replace("image.getPixelRGBA(", "image.getPixel(")
    s = s.replace("image.setPixelRGBA(", "image.setPixel(")
    s = re.sub(
        r"getCloneItemStack\(level, muse\.pos, muse\.state, false\)",
        "getCloneItemStack(level, muse.pos, false)",
        s,
    )
    s = s.replace(
        "Minecraft.getInstance().levelRenderer.setBlocksDirty(",
        "; if (false) Minecraft.getInstance().levelRenderer.setSectionDirty(",
    )
    s = re.sub(
        r"if \(false\) Minecraft\.getInstance\(\)\.levelRenderer\.setSectionDirty\([^;]+;",
        ";",
        s,
    )
    s = s.replace("level.scheduleTick(currentPos, Fluids.WATER,", "ticks.scheduleTick(currentPos, Fluids.WATER,")
    s = s.replace("if (particle != null) particle.setColor(r, g, b);", "if (particle != null) { /* setColor removed in 26.2 */ }")
    s = s.replace(
        "super(name, getFrameSize(originalSprite), processImage(originalSprite.originalImage), originalSprite.metadata());",
        "super(name, getFrameSize(originalSprite), processImage(originalSprite.originalImage));",
    )
    s = s.replace("originalSprite.metadata()", "null")
    s = s.replace(
        "DELAYED_BIOME_REGISTRY.get().getOrCreateTag(tagKey)",
        "DELAYED_BIOME_REGISTRY.get().get(tagKey).map(hs -> (net.minecraft.core.HolderSet<net.minecraft.world.level.biome.Biome>) hs).orElseGet(net.minecraft.core.HolderSet::empty)",
    )
    s = s.replace(
        "this.branch.get().setPrimitiveLogDrops(new ItemStack(primitiveLog));",
        "com.dtteam.dynamictrees.compat.DeferredItemStacks.setWhenBound(stack -> this.branch.get().setPrimitiveLogDrops(stack), primitiveLog);",
    )
    s = s.replace(
        "this.strippedBranch.get().setPrimitiveLogDrops(new ItemStack(primitiveStrippedLog));",
        "com.dtteam.dynamictrees.compat.DeferredItemStacks.setWhenBound(stack -> this.strippedBranch.get().setPrimitiveLogDrops(stack), primitiveStrippedLog);",
    )
    s = s.replace(
        "this.roots.get().setPrimitiveLogDrops(new ItemStack(primitiveRoots));",
        "com.dtteam.dynamictrees.compat.DeferredItemStacks.setWhenBound(stack -> this.roots.get().setPrimitiveLogDrops(stack), primitiveRoots);",
    )
    s = s.replace(
        "(fruit, item) -> fruit.setItemStack(new ItemStack(item))",
        "(fruit, item) -> com.dtteam.dynamictrees.compat.DeferredItemStacks.setWhenBound(fruit::setItemStack, item)",
    )
    s = s.replace(
        "(pod, item) -> pod.setItemStack(new ItemStack(item))",
        "(pod, item) -> com.dtteam.dynamictrees.compat.DeferredItemStacks.setWhenBound(pod::setItemStack, item)",
    )
    s = re.sub(r"\b((?:data\.)?(?:basePos|leavesPos|pos))\.getCenter\(\)", r"net.minecraft.world.phys.Vec3.atCenterOf(\1)", s)
    s = s.replace("new ChunkPos(context.origin())", "ChunkPos.containing(context.origin())")
    s = s.replace("world.getBlockEntity(pos, BlockEntityType.BEEHIVE)", "java.util.Optional.ofNullable(world.getBlockEntity(pos)).filter(be -> be instanceof net.minecraft.world.level.block.entity.BeehiveBlockEntity).map(be -> (net.minecraft.world.level.block.entity.BeehiveBlockEntity) be)")
    s = s.replace(".map(ConfiguredFeature::config)", ".map(h -> h.value().config())")
    s = s.replace("twoFeatureConfig.getFeatures()", "twoFeatureConfig.getSubFeatures()")
    s = s.replace(".getOrCreateTag(", ".getTag(")
    if "class DTLootTableBuilder" in s:
        s = re.sub(
            r"protected static LootItemCondition\.Builder hasSilkTouch\(HolderLookup\.Provider registries\) \{[\s\S]*?\n    \}",
            "protected static LootItemCondition.Builder hasSilkTouch(HolderLookup.Provider registries) {\n"
            "        HolderLookup.RegistryLookup<Enchantment> registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);\n"
            "        return MatchTool.toolMatches(\n"
            "                ItemPredicate.Builder.item()\n"
            "                        .withComponents(\n"
            "                                DataComponentMatchers.Builder.components()\n"
            "                                        .partial(\n"
            "                                                DataComponentPredicates.ENCHANTMENTS,\n"
            "                                                EnchantmentsPredicate.enchantments(\n"
            "                                                        List.of(new EnchantmentPredicate(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))\n"
            "                                                )\n"
            "                                        )\n"
            "                                        .build()\n"
            "                        )\n"
            "        );\n"
            "    }",
            s,
            count=1,
        )
        if "import net.minecraft.advancements.predicates.DataComponentMatchers;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.advancements.predicates.DataComponentMatchers;\nimport net.minecraft.core.component.predicates.DataComponentPredicates;\nimport net.minecraft.core.component.predicates.EnchantmentsPredicate;\n", s, count=1)
    if "class SoilBlock" in s:
        s = s.replace(
            "return blockColors.getColor(getPrimitiveSoilState(state), level, pos, tintIndex);",
            "return level instanceof net.minecraft.client.renderer.block.BlockAndTintGetter tintLevel"
            " ? net.minecraft.client.color.block.BlockTintSources.grass().colorInWorld(getPrimitiveSoilState(state), tintLevel, pos)"
            " : 0xFFFFFF;",
        )
    if "originalSprite.metadata()" in s:
        s = s.replace("originalSprite.metadata()", "null")
    s = s.replace(".location().toString()", ".identifier().toString()")
    s = re.sub(r"\w+\.hasPermission\([^()]*(?:\([^()]*\)[^()]*)*\)", "true", s)
    s = s.replace(".getHolderOrThrow(", ".getOrThrow(")
    s = s.replace(".getHolder(", ".get(")
    s = re.sub(r"(?<!Block)EntityType\.BEEHIVE", "EntityType.BEE", s)
    s = s.replace("SimpleWeightedList", "WeightedList")
    s = s.replace("wasExploded(level, pos, explosion)", "wasExploded(level instanceof net.minecraft.server.level.ServerLevel sl ? sl : null, pos, explosion)")
    s = s.replace(".getLootTable()", ".getLootTable().orElseThrow()")
    s = s.replace("getLootTable()", "getLootTable().orElseThrow()")
    s = s.replace("getLootTable().orElseThrow().orElseThrow()", "getLootTable().orElseThrow()")
    if "class TreeFeatureCanceller" in s:
        s = re.sub(
            r"public boolean shouldCancel[\s\S]*\Z",
            """public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        final FeatureConfiguration featureConfig = configuredFeature.config();

        if (featureConfig instanceof RandomFeatureConfiguration randomConfig) {
            return this.doesContainTrees(randomConfig, featureCancellations);
        } else if (this.treeFeatureConfigClass.isInstance(featureConfig)) {
            String nameSpace = "";
            var nextHolder = configuredFeature.getSubFeatures().findFirst();
            if (nextHolder.isEmpty()) {
                return false;
            }
            final ConfiguredFeature<?, ?> nextConfiguredFeature = nextHolder.get().value();
            final FeatureConfiguration nextFeatureConfig = nextConfiguredFeature.config();
            final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(nextConfiguredFeature.feature());
            if (featureRegistryName != null) {
                nameSpace = featureRegistryName.getNamespace();
            }
            if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && !nameSpace.isEmpty() &&
                featureCancellations.shouldCancelNamespace(nameSpace)) {
                return true;
            } else if (nextFeatureConfig instanceof RandomFeatureConfiguration randomNext) {
                return this.doesContainTrees(randomNext, featureCancellations);
            }
        }
        return false;
    }

    private boolean doesContainTrees(RandomFeatureConfiguration featureConfig, BiomePropertySelectors.NormalFeatureCancellation featureCancellations) {
        for (WeightedPlacedFeature feature : featureConfig.features()) {
            final PlacedFeature currentPlacedFeature = feature.feature().value();
            var configured = currentPlacedFeature.getFeatures().findFirst();
            if (configured.isEmpty()) {
                continue;
            }
            final Identifier featureRegistryName = BuiltInRegistries.FEATURE.getKey(configured.get().value().feature());
            if (this.treeFeatureConfigClass.isInstance(configured.get().value().config()) && featureRegistryName != null &&
                featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace())) {
                return true;
            }
        }
        return false;
    }

}
""",
            s,
            count=1,
        )
    if "class DTLootTableBuilder" in s:
        s = s.replace(
            "LootItemCondition.Builder hasShears = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));",
            "LootItemCondition.Builder hasShears = MatchTool.toolMatches(ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), Items.SHEARS));",
        )
    if "class GrowthSubstance" in s or "class HarvestSubstance" in s or "class MegaSubstance" in s:
        s = s.replace("ParticleTypes.EFFECT", "ParticleTypes.HAPPY_VILLAGER")
        s = s.replace("ParticleTypes.SPELL", "ParticleTypes.HAPPY_VILLAGER")
        s = s.replace("ParticleTypes.INSTANT_EFFECT", "ParticleTypes.HAPPY_VILLAGER")
        s = s.replace("ParticleTypes.WITCH", "ParticleTypes.HAPPY_VILLAGER")
        s = s.replace("ParticleTypes.DRAGON_BREATH", "ParticleTypes.HAPPY_VILLAGER")
    s = s.replace(".level().random", ".level().getRandom()")
    s = re.sub(r"(?<![\w.])kill\(\);", "discard();", s)
    s = s.replace("new ChunkPos(BlockPos.containing(", "ChunkPos.containing(BlockPos.containing(")
    s = s.replace("muse.state.getBlock().getCloneItemStack(", "muse.state.getCloneItemStack(")
    s = s.replace(
        "super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston)",
        "super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston)",
    )
    s = re.sub(
        r"super\.neighborChanged\((\w+), (\w+), (\w+), (\w+), fromPos, (\w+)\)",
        r"super.neighborChanged(\1, \2, \3, \4, orientation, \5)",
        s,
    )
    s = s.replace("level().neighborChanged(pos, Blocks.AIR, pos)", "level().neighborChanged(pos, Blocks.AIR, null)")
    s = s.replace("new ChunkPos(blockPosArgument(", "ChunkPos.containing(blockPosArgument(")
    s = s.replace("canceller.shouldCancel(configuredFeature, this)", "canceller.shouldCancel(configuredFeature.value(), this)")
    s = s.replace("this.registry.get(registryName)", "this.registry.getValue(registryName)")
    s = re.sub(r"featureConfig\.features(?!\()", "featureConfig.features()", s)
    s = s.replace("feature.feature.value()", "feature.feature().value()")
    s = s.replace(
        "this.saveAdditional(tag, registries);",
        "// saveAdditional now uses ValueOutput",
    )
    s = re.sub(
        r"public ItemStack assemble\(CraftingInput (\w+), HolderLookup\.Provider \w+\)",
        r"public ItemStack assemble(CraftingInput \1)",
        s,
    )
    s = re.sub(
        r"public BlockState getState\(RandomSource (\w+), BlockPos (\w+)\)",
        r"public BlockState getState(WorldGenLevel genLevel, RandomSource \1, BlockPos \2)",
        s,
    )
    if "class DTReplaceNyliumFungiBlockStateProvider" in s:
        s = s.replace("this.enabled.getState(random, state)", "this.enabled.getState(genLevel, random, state)")
        s = s.replace("this.disabled.getState(random, state)", "this.disabled.getState(genLevel, random, state)")
        if "import net.minecraft.world.level.WorldGenLevel;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.level.WorldGenLevel;\n", s, count=1)
    s = s.replace(
        "public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks",
        "public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks",
    )
    if "getShuffledJigsawBlocks" in s:
        s = re.sub(
            r"public List<StructureTemplate\.JigsawBlockInfo> getShuffledJigsawBlocks\([^)]*\) \{[\s\S]*?\n    \}",
            "public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structureTemplateManager, BlockPos pos, Rotation rotation, RandomSource random) {\n        return java.util.List.of();\n    }",
            s,
        )
    s = re.sub(
        r"return DTRegistries\.(ITEM_BY_SPECIES|SEED_ITEM|WEIGHTED_ITEM|MULTIPLY_COUNT|MULTIPLY_LOGS_COUNT|MULTIPLY_STICKS_COUNT)\.get\(\);",
        "return CODEC;",
        s,
    )
    s = s.replace(
        "getPrimitiveSoilBlock().getCloneItemStack(level, pos, getPrimitiveSoilState(state))",
        "getPrimitiveSoilState(state).getCloneItemStack(level, pos, includeData)",
    )
    s = s.replace(".getMaxBuildHeight()", ".getMaxY()")
    s = s.replace(".getIncorrectBlocksForDrops()", ".incorrectBlocksForDrops()")
    s = s.replace("dimensionKey.location()", "dimensionKey.identifier()")
    s = re.sub(r"(\w+Key)\.location\(\)", r"\1.identifier()", s)
    s = s.replace(".registryOrThrow(", ".lookupOrThrow(")
    s = s.replace("GameRules.RULE_DOBLOCKDROPS", "GameRules.BLOCK_DROPS")
    s = s.replace(".getLightBlock(level, pos)", ".getLightDampening()")
    s = s.replace(".isSolidRender(level, pos)", ".isSolidRender()")
    s = s.replace("EntityType.OCELOT", "null")
    s = s.replace("EntityType.PARROT", "null")
    s = re.sub(r"new DyedItemColor\(([^,]+),\s*[^)]+\)", r"new DyedItemColor(\1)", s)
    s = s.replace("public RecipeSerializer<?> getSerializer()", "public RecipeSerializer<? extends CustomRecipe> getSerializer()")
    s = s.replace(
        "public MapCodec<? extends LootPoolEntryContainer> codec()",
        "public MapCodec<? extends LootPoolSingletonContainer> codec()",
    )
    s = s.replace(
        "public MapCodec<? extends LootItemFunction> codec()",
        "public MapCodec<? extends LootItemConditionalFunction> codec()",
    )
    s = s.replace("craftingRemainingItem = this;", "// craftingRemainingItem removed in 26.2")
    s = re.sub(r"\.hasPermission\(\d+\)", ".permissions() != null", s)
    s = s.replace("level.getGameRules()", "((net.minecraft.server.level.ServerLevel) level).getGameRules()")
    s = s.replace("world.getGameRules()", "((net.minecraft.server.level.ServerLevel) world).getGameRules()")
    s = re.sub(
        r"void neighborChanged\(BlockState (\w+), Level (\w+), BlockPos (\w+), Block (\w+), net\.minecraft\.world\.level\.redstone\.Orientation orientation, boolean (\w+)\) \{",
        r"void neighborChanged(BlockState \1, Level \2, BlockPos \3, Block \4, net.minecraft.world.level.redstone.Orientation orientation, boolean \5) {\n        BlockPos neighborPos = \3;\n        BlockPos fromPos = \3;",
        s,
    )
    if "canPlaceLiquid(@Nullable LivingEntity" in s and "import net.minecraft.world.entity.LivingEntity;" not in s:
        s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.entity.LivingEntity;\n", s, count=1)
    if "ScheduledTickAccess ticks" in s:
        if "import net.minecraft.world.level.LevelReader;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.level.LevelReader;\n", s, count=1)
        if "import net.minecraft.util.RandomSource;" not in s:
            s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.util.RandomSource;\n", s, count=1)
    if "GameRules." in s and "import net.minecraft.world.level.gamerules.GameRules;" not in s:
        s = re.sub(r"(package [^\n]+;\n)", r"\1\nimport net.minecraft.world.level.gamerules.GameRules;\n", s, count=1)

    if rel.replace("\\", "/").endswith("item/DendroPotion.java"):
        s = s.replace("private final ItemStack ingredient;", "private final Item ingredientItem;")
        s = s.replace("this.ingredient = new ItemStack(ingredient);", "this.ingredientItem = ingredient;")
        s = s.replace("return this.ingredient;", "return com.dtteam.dynamictrees.compat.DeferredItemStacks.of(this.ingredientItem);")

    if rel.replace("\\", "/").endswith("entity/LingeringEffectorEntity.java"):
        s = s.replace("EntityDataSerializers.COMPOUND_TAG", "EntityDataSerializers.STRING")
        s = s.replace("EntityDataAccessor<CompoundTag>", "EntityDataAccessor<String>")
        s = s.replace("builder.define(effectorDataParameter, new CompoundTag());", "builder.define(effectorDataParameter, \"\");")
        s = s.replace(
            "getEntityData().set(effectorDataParameter, tag);",
            "getEntityData().set(effectorDataParameter, tag.toString());",
        )
        s = s.replace(
            "return getEntityData().get(effectorDataParameter);",
            """try {
            String snbt = getEntityData().get(effectorDataParameter);
            return snbt == null || snbt.isEmpty() ? new CompoundTag() : net.minecraft.nbt.TagParser.parseCompoundFully(snbt);
        } catch (Exception e) {
            return new CompoundTag();
        }""",
        )
        s = re.sub(
            r"protected void readAdditionalSaveData\(net\.minecraft\.world\.level\.storage\.ValueInput input\) \{[\s\S]*?\n    \}",
            """protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        input.read("effector", CompoundTag.CODEC).ifPresent(this::setEffectorData);
    }""",
            s,
            count=1,
        )
        s = re.sub(
            r"protected void addAdditionalSaveData\(net\.minecraft\.world\.level\.storage\.ValueOutput output\) \{[\s\S]*?\n    \}",
            """protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        output.store("effector", CompoundTag.CODEC, getEffectorData());
    }""",
            s,
            count=1,
        )
        s = s.replace(
            """        if (level().isClientSide() && !clientBuilt){
            setEffectorData(getEffectorData());
            clientBuilt = true;
        }""",
            """        if (level().isClientSide() && !clientBuilt) {
            String snbt = getEntityData().get(effectorDataParameter);
            if (snbt != null && !snbt.isEmpty()) {
                setEffectorData(getEffectorData());
                clientBuilt = true;
            }
        }""",
        )

    if rel.replace("\\", "/").endswith("entity/FallingTreeEntity.java"):
        s = s.replace("EntityDataSerializers.COMPOUND_TAG", "EntityDataSerializers.STRING")
        s = s.replace("EntityDataAccessor<CompoundTag>", "EntityDataAccessor<String>")
        s = s.replace("builder.define(voxelDataParameter, new CompoundTag());", "builder.define(voxelDataParameter, \"\");")
        s = s.replace(
            "getEntityData().set(voxelDataParameter, tag);",
            "getEntityData().set(voxelDataParameter, tag.toString());",
        )
        s = s.replace(
            "return getEntityData().get(voxelDataParameter);",
            """try {
            String snbt = getEntityData().get(voxelDataParameter);
            return snbt == null || snbt.isEmpty() ? new CompoundTag() : net.minecraft.nbt.TagParser.parseCompoundFully(snbt);
        } catch (Exception e) {
            return new CompoundTag();
        }""",
        )
        s = re.sub(
            r"protected void readAdditionalSaveData\(net\.minecraft\.world\.level\.storage\.ValueInput input\) \{[\s\S]*?\n    \}",
            """protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        input.read("vox", CompoundTag.CODEC).ifPresent(vox -> {
            setupFromNBT(vox);
            setVoxelData(vox);
        });
        for (ItemStack stack : input.listOrEmpty("payload", ItemStack.CODEC)) {
            this.payload.add(stack);
        }
    }""",
            s,
            count=1,
        )
        s = re.sub(
            r"protected void addAdditionalSaveData\(net\.minecraft\.world\.level\.storage\.ValueOutput output\) \{[\s\S]*?\n    \}",
            """protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        output.store("vox", CompoundTag.CODEC, getVoxelData());
        if (!payload.isEmpty()) {
            net.minecraft.world.level.storage.ValueOutput.TypedOutputList<ItemStack> list = output.list("payload", ItemStack.CODEC);
            for (ItemStack stack : payload) {
                list.add(stack);
            }
        }
    }""",
            s,
            count=1,
        )

    if rel.replace("\\", "/").endswith("block/soil/SpeciesBlockEntity.java") or rel.replace("\\", "/").endswith("block/sapling/PottedSaplingBlockEntity.java"):
        s = re.sub(
            r"protected void loadAdditional\(net\.minecraft\.world\.level\.storage\.ValueInput input\) \{[\s\S]*?\n    \}",
            "protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {\n        super.loadAdditional(input);\n    }",
            s,
            count=1,
        )
        s = re.sub(
            r"protected void saveAdditional\(net\.minecraft\.world\.level\.storage\.ValueOutput output\) \{[\s\S]*?\n    \}",
            "protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {\n        super.saveAdditional(output);\n    }",
            s,
            count=1,
        )

    return s


def main() -> int:
    src = Path(sys.argv[1])
    dest = Path(sys.argv[2])
    if dest.exists():
        for old in dest.rglob("*"):
            if old.is_file():
                old.unlink()
    count = 0
    for java in src.rglob("*.java"):
        rel = java.relative_to(src)
        rel_s = str(rel).replace("\\", "/")
        if rel_s.endswith("entity/render/FallingTreeRenderer.java") or rel_s.endswith("entity/render/LingeringEffectorRenderer.java"):
            continue
        out = dest / rel
        out.parent.mkdir(parents=True, exist_ok=True)
        text = java.read_text(encoding="utf-8")
        out.write_text(transform(text, str(rel)), encoding="utf-8")
        count += 1
    write_compat_stubs(dest)
    print(f"transformed {count} java files -> {dest}")
    return 0


def write_compat_stubs(dest: Path) -> None:
    stubs = {
        "net/minecraft/data/tags/IntrinsicHolderTagsProvider.java": """package net.minecraft.data.tags;

import net.minecraft.tags.TagKey;

public class IntrinsicHolderTagsProvider {
    public static class IntrinsicTagAppender<T> {
        public IntrinsicTagAppender<T> add(T value) { return this; }
        public IntrinsicTagAppender<T> addTag(TagKey<T> tag) { return this; }
        public IntrinsicTagAppender<T> addOptional(net.minecraft.resources.Identifier id) { return this; }
    }
}
""",
        "net/minecraft/client/color/block/BlockColor.java": """package net.minecraft.client.color.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BlockColor {
    int getColor(BlockState state, @Nullable BlockGetter level, @Nullable BlockPos pos, int tintIndex);
}
""",
    }
    for rel, text in stubs.items():
        out = dest / rel
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(text, encoding="utf-8")



if __name__ == "__main__":
    raise SystemExit(main())
