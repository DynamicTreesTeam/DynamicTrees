package com.dtteam.dynamictrees.worldgen.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * @author Harley O'Connor
 */
public class RegularTemplatePoolModifier implements TemplatePoolModifier {
    private final ResourceKey<StructureTemplatePool> key;
    private final StructureTemplatePool templatePool;

    private RegularTemplatePoolModifier(ResourceKey<StructureTemplatePool> key, StructureTemplatePool templatePool) {
        this.key = key;
        this.templatePool = templatePool;
    }

    public TemplatePoolModifier replaceTemplate(int index, StructurePoolElement element) {
        if (templatePool.rawTemplates.size() <= index) return this;
        Pair<StructurePoolElement, Integer> removedRawTemplate = templatePool.rawTemplates.remove(index);
        var elementFinal = new DTCancelVanillaTreePoolElement(element, removedRawTemplate.getFirst());
        templatePool.rawTemplates.add(index, Pair.of(elementFinal, removedRawTemplate.getSecond()));
        templatePool.templates.replaceAll(template -> {
            if (template == removedRawTemplate.getFirst()) {
                return elementFinal;
            }
            return template;
        });
        return this;
    }

    public TemplatePoolModifier removeTemplate(int index) {
        Pair<StructurePoolElement, Integer> removedRawTemplate = templatePool.rawTemplates.remove(index);
        templatePool.templates.removeIf(template -> template == removedRawTemplate.getFirst());
        return this;
    }

    public void removeAllTemplates() {
        templatePool.rawTemplates.clear();
        templatePool.templates.clear();
    }

    public void registerPool(BootstrapContext<StructureTemplatePool> context) {
        context.register(this.key, this.templatePool);
    }

    public static TemplatePoolModifier village(HolderLookup.Provider lookupProvider, String type, String patternGroup) {
        return village(lookupProvider.lookupOrThrow(Registries.TEMPLATE_POOL), type, patternGroup);
    }

    public static TemplatePoolModifier village(HolderGetter<StructureTemplatePool> pools, String type, String patternGroup) {
        Identifier patternName = Identifier.parse("village/" + type + "/" + patternGroup);
        return create(pools, ResourceKey.create(Registries.TEMPLATE_POOL, patternName));
    }

    public static TemplatePoolModifier create(HolderLookup.Provider lookupProvider, ResourceKey<StructureTemplatePool> key) {
        return create(lookupProvider.lookupOrThrow(Registries.TEMPLATE_POOL), key);
    }

    public static TemplatePoolModifier create(HolderGetter<StructureTemplatePool> pools, ResourceKey<StructureTemplatePool> key) {
        StructureTemplatePool pattern = pools.getOrThrow(key).value();
        // if (pattern == null) {
        //     VillageTreeReplacement.LOGGER.error("Could not find StructureTemplatePool with name {}.", patternName);
        //     return TemplatePoolModifier.NULL;
        // }
        return new RegularTemplatePoolModifier(key, new StructureTemplatePool(pattern.getFallback(), pattern.rawTemplates));
    }

}
