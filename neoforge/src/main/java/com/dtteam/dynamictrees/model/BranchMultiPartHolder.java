package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BranchMultiPartHolder {

    private record Key(Direction.Axis orientation, Direction face, Integer radius) {};

    private final Map<Key, BranchModelPart> map = new HashMap<>();

    public void putPart(Direction dir, int radius, BranchModelPart part){
        putPart(Direction.Axis.Y, dir, radius, part);
    }

    public void putPart(Direction.Axis orientation, Direction dir, int radius, BranchModelPart part){
        Key key = new Key(orientation, dir, radius);
        map.put(key, part);
    }

    public void putAllParts(int radius, EnumMap<Direction, BranchModelPart> parts){
        for (Direction dir : Direction.values()){
            putPart(dir, radius, parts.get(dir));
        }
    }

    public void putAllParts(Direction.Axis orientation, int radius, EnumMap<Direction, BranchModelPart> parts){
        for (Direction dir : Direction.values()){
            putPart(orientation, dir, radius, parts.get(dir));
        }
    }

    @Nullable
    public BranchModelPart getPart(Direction.Axis orientation, Direction dir, int radius){
        Key key = new Key(orientation, dir, radius);
        if (map.containsKey(key)){
            return map.get(key);
        }
        return null;
    }

    @Nullable
    public BranchModelPart getPart(Direction dir, int radius){
        return getPart(Direction.Axis.Y, dir, radius);
    }

    public int materialFlags(){
        AtomicInteger flag = new AtomicInteger();
        map.forEach((_,v)-> flag.updateAndGet(v1 -> v1 | v.materialFlags()));
        return flag.get();
    }

    public Material.Baked getFirstMaterial(){
        for (BranchModelPart part : map.values()){
            return part.particleMaterial();
        }
        return null;
    }

}
