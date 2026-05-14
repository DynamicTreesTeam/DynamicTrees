package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BranchMultiPartHolder {

    public enum NullableDirection {
        DOWN (Direction.DOWN),
        UP (Direction.UP),
        NORTH (Direction.NORTH),
        SOUTH (Direction.SOUTH),
        WEST (Direction.WEST),
        EAST (Direction.EAST),
        NULL (null);
        @Nullable final private Direction direction;
        NullableDirection(@Nullable Direction direction){
            this.direction = direction;
        }

        @Nullable
        public Direction getDirection() {
            return direction;
        }

        public static NullableDirection fromDirection(@Nullable Direction direction){
            if (direction == null) return NULL;
            return valueOf(direction.toString().toUpperCase(Locale.ENGLISH));
        }
    }

    public static class PartMap<T> extends EnumMap<BranchMultiPartHolder.NullableDirection, T>{
        public PartMap() {
            super(BranchMultiPartHolder.NullableDirection.class);
        }

        public T put(@Nullable Direction key, T value) {
            return super.put(NullableDirection.fromDirection(key), value);
        }

        public T get(@Nullable Direction key) {
            return super.get(NullableDirection.fromDirection(key));
        }
    }

    private record Key(Direction.Axis orientation, NullableDirection face, Integer radius) {};

    private final Map<Key, @NotNull BranchModelPart> map = new HashMap<>();

    public void putPart(@Nullable Direction dir, int radius, BranchModelPart part){
        putPart(Direction.Axis.Y, dir, radius, part);
    }

    public void putPart(Direction.Axis orientation, @Nullable Direction dir, int radius, BranchModelPart part){
        if (part == null) return;
        Key key = new Key(orientation, NullableDirection.fromDirection(dir), radius);
        map.put(key, part);
    }

    public void putAllParts(int radius, PartMap<BranchModelPart> parts){
        for (NullableDirection dir : NullableDirection.values()){
            putPart(dir.getDirection(), radius, parts.get(dir));
        }
    }

    public void putAllParts(Direction.Axis orientation, int radius, PartMap<BranchModelPart> parts){
        for (NullableDirection dir : NullableDirection.values()){
            putPart(orientation, dir.getDirection(), radius, parts.get(dir));
        }
    }

    @Nullable
    public BranchModelPart getPart(Direction.Axis orientation, @Nullable Direction dir, int radius){
        Key key = new Key(orientation, NullableDirection.fromDirection(dir), radius);
        if (map.containsKey(key)){
            return map.get(key);
        }
        return null;
    }

    @Nullable
    public BranchModelPart getPart(@Nullable Direction dir, int radius){
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

    public boolean isEmpty(){
        return map.isEmpty();
    }

}
