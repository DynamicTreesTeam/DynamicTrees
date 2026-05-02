package com.dtteam.dynamictrees.data;

import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class GeneratorHelper {

    public static TextureSlot[] createSlots(Map<String, Identifier> textures) {
        return textures.keySet().stream().map(TextureSlot::create).toArray(TextureSlot[]::new);
    }

    public static TextureMapping createMapping(Map<String, Identifier> textures, TextureSlot[] slots) {
        TextureMapping mapping = new TextureMapping();
        for (TextureSlot slot : slots) {
            mapping.put(slot, new Material(textures.get(slot.getId())));
        }
        return mapping;
    }

}
