package com.dtteam.dynamictrees.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.UnbakedModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

/**
 * Parses DT custom-loader JSON as a cube so the file is valid, then
 * {@code ModelEvent.ModifyBakingResult} replaces the baked result with the real DT model.
 */
public final class DummyUnbakedModelLoader implements UnbakedModelLoader<UnbakedModel> {

    public static final DummyUnbakedModelLoader INSTANCE = new DummyUnbakedModelLoader();

    private DummyUnbakedModelLoader() {}

    @Override
    public UnbakedModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject cube = new JsonObject();
        cube.addProperty("parent", "minecraft:block/cube_all");
        JsonObject textures = new JsonObject();
        String particle = "minecraft:block/oak_log";
        if (jsonObject.has("textures") && jsonObject.get("textures").isJsonObject()) {
            JsonObject src = jsonObject.getAsJsonObject("textures");
            if (src.has("bark")) {
                particle = src.get("bark").getAsString();
            } else if (src.has("all")) {
                particle = src.get("all").getAsString();
            } else if (src.has("frond")) {
                particle = src.get("frond").getAsString();
            }
        }
        textures.addProperty("all", particle);
        cube.add("textures", textures);
        return deserializationContext.deserialize(cube, UnbakedModel.class);
    }
}
