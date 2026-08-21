package com.dtteam.dynamictrees.data.generator;

import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.Generator;
import com.dtteam.dynamictrees.data.provider.DTLangProvider;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.family.UndergroundRootsFamily;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class FamilyLangGenerator implements Generator<DTDataProvider.Language, Family> {
    DTLangProvider provider;

    @Override
    public void generate(DTDataProvider.Language prov, Family input, Dependencies dependencies) {
        if (prov instanceof DTLangProvider provider1) {
            this.provider = provider1;
            input.getBranch().ifPresent(branch -> treeLang(branch, input, input.getLangOverride("branch")));
//        if(input.hasSurfaceRoot()){
//            blockLang(input.getSurfaceRoot().get(), input.getLangOverride("surface_root"));
//        }
            if(input instanceof UndergroundRootsFamily mgf){
                mgf.getRoots().ifPresent(root -> treeLang(root, input, input.getLangOverride("roots")));
                if (input instanceof com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily mossy) {
                    mossy.getMossyRoots().ifPresent(root -> blockLang(root, input.getLangOverride("mossy_roots")));
                }
            }
        }
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies();
    }

    protected void treeLang(Block entry, Family family, Optional<String> blah) {
        provider.addBlock(() -> entry, blah.orElse(checkReplace(family.getRegistryName().getPath()+"_tree")));
    }

    protected void blockLang(Block entry, Optional<String> blah) {
        provider.addBlock(() -> entry, blah.orElse(checkReplace(BuiltInRegistries.BLOCK.getKey(entry).getPath())));
    }

    protected String checkReplace(String path) {
        return Arrays.stream(path.split("_"))
                .map(StringUtils::capitalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }
}