package com.ferreusveritas.dynamictrees.worldgen.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * @author Harley O'Connor
 */
public class RegularTemplatePoolModifier implements TemplatePoolModifier {
    private static final HolderLookup.Provider VANILLA_LOOKUP = VanillaRegistries.createLookup();
    private final StructureTemplatePool templatePool;

    private RegularTemplatePoolModifier(StructureTemplatePool templatePool) {
        this.templatePool = templatePool;
    }

    public TemplatePoolModifier replaceTemplate(int index, StructurePoolElement element) {
        Pair<StructurePoolElement, Integer> removedRawTemplate = templatePool.rawTemplates.remove(index);
        templatePool.rawTemplates.add(index, Pair.of(element, removedRawTemplate.getSecond()));
        templatePool.templates.replaceAll(template -> {
            if (template == removedRawTemplate.getFirst()) {
                return element;
            }
            return template;
        });
        return this;
    }

    @Override
    public TemplatePoolModifier removeTemplate(int index) {
        Pair<StructurePoolElement, Integer> removedRawTemplate = templatePool.rawTemplates.remove(index);
        templatePool.templates.removeIf(template -> template == removedRawTemplate.getFirst());
        return this;
    }

    @Override
    public void removeAllTemplates() {
        templatePool.rawTemplates.clear();
        templatePool.templates.clear();
    }

    public static TemplatePoolModifier village(String type, String patternGroup) {
        ResourceLocation patternName = new ResourceLocation("village/" + type + "/" + patternGroup);
        StructureTemplatePool pattern = VANILLA_LOOKUP.lookupOrThrow(Registries.TEMPLATE_POOL).getOrThrow(ResourceKey.create(Registries.TEMPLATE_POOL, patternName)).value();
        // if (pattern == null) {
        //     VillageTreeReplacement.LOGGER.error("Could not find StructureTemplatePool with name {}.", patternName);
        //     return TemplatePoolModifier.NULL;
        // }
        return new RegularTemplatePoolModifier(pattern);
    }

}
