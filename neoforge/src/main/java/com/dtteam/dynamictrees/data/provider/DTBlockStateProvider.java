package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.treepack.Resources;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Writes blockstate and block model JSON through vanilla {@link PackOutput}. Replaces the
 * removed NeoForge {@code BlockStateProvider} / {@code CustomLoaderBuilder} stack.
 */
public class DTBlockStateProvider implements DTDataProvider.BlockState {

    private final PackOutput output;
    private final String modId;
    private final List<Registry<?>> registries;
    private final Map<Identifier, JsonObject> blockstates = new LinkedHashMap<>();
    private final Map<Identifier, JsonObject> blockModels = new LinkedHashMap<>();

    public DTBlockStateProvider(PackOutput output, String modId, Collection<Registry<?>> registries) {
        this.output = output;
        this.modId = modId;
        this.registries = ImmutableList.copyOf(registries);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Resources.prepareDatagen();
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry ->
                        entry.generateStateData(this)
                )
        );

        Path packRoot = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        this.blockstates.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json,
                packRoot.resolve(id.getNamespace()).resolve("blockstates").resolve(id.getPath() + ".json"))));
        this.blockModels.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json,
                packRoot.resolve(id.getNamespace()).resolve("models").resolve(id.getPath() + ".json"))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "DT Block States: " + this.modId;
    }

    public Identifier blockTexture(Block block) {
        return this.block(BuiltInRegistries.BLOCK.getKey(block));
    }

    public Identifier blockModelLocation(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        return Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath());
    }

    public void simpleBlock(Block block, Identifier model) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model.toString());
        JsonObject variants = new JsonObject();
        variants.add("", variant);
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        this.blockstates.put(BuiltInRegistries.BLOCK.getKey(block), root);
    }

    public void blockModel(Identifier modelLocation, JsonObject json) {
        this.blockModels.put(modelLocation, json);
    }

    public void parentedBlockModel(Identifier modelLocation, Identifier parent,
                                   Map<String, Identifier> textures, String renderType) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent.toString());
        if (renderType != null) {
            json.addProperty("render_type", renderType.contains(":") ? renderType : "minecraft:" + renderType);
        }
        if (textures != null && !textures.isEmpty()) {
            json.add("textures", texturesObject(textures));
        }
        this.blockModels.put(modelLocation, json);
    }

    public void customLoaderModel(Identifier modelLocation, Identifier loader,
                                  Map<String, Identifier> textures) {
        customLoaderModel(modelLocation, loader, textures, null);
    }

    public void customLoaderModel(Identifier modelLocation, Identifier loader,
                                  Map<String, Identifier> textures, String renderType) {
        JsonObject json = new JsonObject();
        json.addProperty("loader", loader.toString());
        if (renderType != null) {
            json.addProperty("render_type", renderType.contains(":") ? renderType : "minecraft:" + renderType);
        }
        json.add("textures", texturesObject(textures));
        this.blockModels.put(modelLocation, json);
    }

    public VariantBuilder variants(Block block) {
        return new VariantBuilder(block);
    }

    public MultipartBuilder multipart(Block block) {
        return new MultipartBuilder(block);
    }

    private static JsonObject texturesObject(Map<String, Identifier> textures) {
        JsonObject tex = new JsonObject();
        textures.forEach((key, location) -> tex.addProperty(key, location.toString()));
        return tex;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static String propertyValueName(Property property, Comparable value) {
        return property.getName(value);
    }

    public final class VariantBuilder {
        private final Identifier blockId;
        private final JsonObject variants = new JsonObject();

        private VariantBuilder(Block block) {
            this.blockId = BuiltInRegistries.BLOCK.getKey(block);
        }

        public VariantBuilder variant(String key, Identifier model) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", model.toString());
            this.variants.add(key, variant);
            return this;
        }

        public VariantBuilder variant(Property<?> property, Comparable<?> value, Identifier model) {
            return variant(property.getName() + "=" + propertyValueName(property, value), model);
        }

        public void finish() {
            JsonObject root = new JsonObject();
            root.add("variants", this.variants);
            DTBlockStateProvider.this.blockstates.put(this.blockId, root);
        }
    }

    public final class MultipartBuilder {
        private final Identifier blockId;
        private final JsonArray parts = new JsonArray();

        private MultipartBuilder(Block block) {
            this.blockId = BuiltInRegistries.BLOCK.getKey(block);
        }

        public Part part(Identifier model) {
            return new Part(model);
        }

        public void finish() {
            JsonObject root = new JsonObject();
            root.add("multipart", this.parts);
            DTBlockStateProvider.this.blockstates.put(this.blockId, root);
        }

        public final class Part {
            private final JsonObject apply = new JsonObject();
            private final Map<String, String> conditions = new LinkedHashMap<>();
            private JsonArray orGroups;

            private Part(Identifier model) {
                this.apply.addProperty("model", model.toString());
            }

            public Part condition(Property<?> property, Comparable<?>... values) {
                this.conditions.put(property.getName(), joinValues(property, values));
                return this;
            }

            public OrClause useOr() {
                this.orGroups = new JsonArray();
                return new OrClause();
            }

            public MultipartBuilder end() {
                JsonObject part = new JsonObject();
                part.add("apply", this.apply);
                if (this.orGroups != null && this.orGroups.size() > 0) {
                    JsonObject when = new JsonObject();
                    when.add("OR", this.orGroups);
                    part.add("when", when);
                } else if (!this.conditions.isEmpty()) {
                    JsonObject when = new JsonObject();
                    this.conditions.forEach(when::addProperty);
                    part.add("when", when);
                }
                MultipartBuilder.this.parts.add(part);
                return MultipartBuilder.this;
            }

            public final class OrClause {
                public NestedGroup nestedGroup() {
                    return new NestedGroup();
                }

                public MultipartBuilder end() {
                    return Part.this.end();
                }
            }

            public final class NestedGroup {
                private final JsonObject group = new JsonObject();

                public NestedGroup condition(Property<?> property, Comparable<?>... values) {
                    this.group.addProperty(property.getName(), joinValues(property, values));
                    return this;
                }

                public OrClause end() {
                    if (this.group.size() > 0) {
                        Part.this.orGroups.add(this.group);
                    }
                    return new OrClause();
                }
            }
        }
    }

    private static String joinValues(Property<?> property, Comparable<?>... values) {
        return Arrays.stream(values)
                .map(value -> propertyValueName(property, value))
                .collect(Collectors.joining("|"));
    }
}
