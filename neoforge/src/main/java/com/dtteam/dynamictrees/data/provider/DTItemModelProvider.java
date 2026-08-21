package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.treepack.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes item models and 26.2 item definitions through vanilla {@link PackOutput}.
 */
public class DTItemModelProvider implements DTDataProvider.ItemModel {

    private final PackOutput output;
    private final String modId;
    private final List<Registry<?>> registries;
    private final Map<Identifier, JsonObject> itemModels = new LinkedHashMap<>();
    private final Map<Identifier, JsonObject> itemDefinitions = new LinkedHashMap<>();

    public DTItemModelProvider(PackOutput output, String modId, List<Registry<?>> registries) {
        this.output = output;
        this.modId = modId;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Resources.prepareDatagen();
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry ->
                        entry.generateItemModelData(this)
                )
        );
        writeStandaloneItemDefinitions();

        Path packRoot = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        this.itemModels.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json,
                packRoot.resolve(id.getNamespace()).resolve("models").resolve("item").resolve(id.getPath() + ".json"))));
        this.itemDefinitions.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json,
                packRoot.resolve(id.getNamespace()).resolve("items").resolve(id.getPath() + ".json"))));
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "DT Item Models: " + this.modId;
    }

    public void parentedItemModel(Item item, Identifier parent, Map<String, Identifier> textures) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        JsonObject json = new JsonObject();
        json.addProperty("parent", parent.toString());
        if (textures != null && !textures.isEmpty()) {
            JsonObject tex = new JsonObject();
            textures.forEach((key, location) -> tex.addProperty(key, location.toString()));
            json.add("textures", tex);
        }
        this.itemModels.put(itemId, json);
        this.itemDefinitions.put(itemId, simpleItemDefinition(itemId));
    }

    public void itemDefinition(Identifier itemId, Identifier model, JsonArray tints) {
        JsonObject modelObj = new JsonObject();
        modelObj.addProperty("type", "minecraft:model");
        modelObj.addProperty("model", model.toString());
        if (tints != null && tints.size() > 0) {
            modelObj.add("tints", tints);
        }
        JsonObject root = new JsonObject();
        root.add("model", modelObj);
        this.itemDefinitions.put(itemId, root);
    }

    private static JsonObject simpleItemDefinition(Identifier itemId) {
        JsonObject modelObj = new JsonObject();
        modelObj.addProperty("type", "minecraft:model");
        modelObj.addProperty("model", Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath()).toString());
        JsonObject root = new JsonObject();
        root.add("model", modelObj);
        return root;
    }

    private void writeStandaloneItemDefinitions() {
        if (!DynamicTrees.MOD_ID.equals(this.modId)) {
            return;
        }
        itemDefinition(DynamicTrees.location("dirt_bucket"), DynamicTrees.location("item/dirt_bucket"), null);
        itemDefinition(DynamicTrees.location("manual"), DynamicTrees.location("item/manual"), null);

        JsonArray staffTints = new JsonArray();
        staffTints.add(tintObject("dynamictrees:staff", 0));
        staffTints.add(tintObject("dynamictrees:staff", 1));
        staffTints.add(constantTint());
        itemDefinition(DynamicTrees.location("staff"), DynamicTrees.location("item/staff"), staffTints);

        JsonArray potionTints = new JsonArray();
        JsonObject dendro = new JsonObject();
        dendro.addProperty("type", "dynamictrees:dendro_potion");
        potionTints.add(dendro);
        potionTints.add(constantTint());
        itemDefinition(DynamicTrees.location("dendro_potion"), DynamicTrees.location("item/dendro_potion"), potionTints);
    }

    private static JsonObject tintObject(String type, int index) {
        JsonObject tint = new JsonObject();
        tint.addProperty("type", type);
        tint.addProperty("index", index);
        return tint;
    }

    private static JsonObject constantTint() {
        JsonObject tint = new JsonObject();
        tint.addProperty("type", "minecraft:constant");
        tint.addProperty("value", -1);
        return tint;
    }
}
