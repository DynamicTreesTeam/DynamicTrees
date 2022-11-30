package com.ferreusveritas.dynamictrees.worldgen.structure;

import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

/**
 * @author Harley O'Connor
 */
public interface TemplatePoolModifier {

    TemplatePoolModifier NULL = new TemplatePoolModifier() {
        @Override public TemplatePoolModifier replaceTemplate(int index, StructurePoolElement element) { return this; }
        @Override public TemplatePoolModifier removeTemplate(int index) { return this; }
        @Override public void removeAllTemplates() {}
    };

    TemplatePoolModifier replaceTemplate(int index, StructurePoolElement element);

    TemplatePoolModifier removeTemplate(int index);

    void removeAllTemplates();

}
