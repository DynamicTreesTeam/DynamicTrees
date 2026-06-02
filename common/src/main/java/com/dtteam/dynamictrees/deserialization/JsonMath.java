package com.dtteam.dynamictrees.deserialization;

import com.dtteam.dynamictrees.tree.species.Species;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;

public class JsonMath {

    private static final long BASE_SEED = 96L;
    public MathOperator rootOp;

    public JsonMath(JsonElement mathElement) {
        if (mathElement.isJsonObject()) {
            JsonObject mathObject = mathElement.getAsJsonObject();

            for (Entry<String, JsonElement> entry : mathObject.entrySet()) {
                rootOp = processElement(entry.getKey(), entry.getValue());
                if (rootOp != NULL_OPERATOR) {
                    return;
                }
            }
        }
    }

    private MathOperator getVariable(String name) {
        if (MathFunction.NOISE.name.equals(name)) {
            return new Noise();
        } else if (MathFunction.RAND.name.equals(name)) {
            return new Rand();
        }
        if (MathFunction.RADIUS.name.equals(name)) {
            return new Radius();
        }

        return NULL_OPERATOR;
    }

    private MathOperator processElement(String key, JsonElement value) {

        MathFunction op = MathFunction.getFunction(key);

		if (op == null) {
			return NULL_OPERATOR;
		}

        ArrayList<MathOperator> paramList = new ArrayList<>();
        Species speciesArg = Species.NULL_SPECIES;

        //If the value is an array then these are the parameters for this operation
        if (value.isJsonArray()) {
            for (JsonElement parameter : value.getAsJsonArray()) {
                MathOperator m = NULL_OPERATOR;
                if (parameter.isJsonObject()) {
                    Entry<String, JsonElement> entry = parameter.getAsJsonObject().entrySet().iterator().next();
                    m = processElement(entry.getKey(), entry.getValue());
                } else if (parameter.isJsonPrimitive()) {
                    if (parameter.getAsJsonPrimitive().isNumber()) {
                        m = new Const(parameter.getAsFloat());
                    } else if (parameter.getAsJsonPrimitive().isString()) {
                        String name = parameter.getAsString();
                        MathOperator var = getVariable(name);
                        if (var != NULL_OPERATOR) {
                            m = var;
                        } else if (Species.findSpeciesSloppy(name) != Species.NULL_SPECIES) {
                            speciesArg = Species.findSpeciesSloppy(name);
                        }
                    }
                }

                if (m != NULL_OPERATOR) {
                    paramList.add(m);
                }

            }
        }

        MathOperator[] paramArray = paramList.toArray(new MathOperator[0]);

        return switch (op) {
            case X -> new ValX();
            case Z -> new ValZ();
            case NOISE -> new Noise();
            case PERLIN -> new Perlin(paramArray);
            case SIMPLEX -> new Simplex(paramArray);
            case RAND -> new Rand();
            case RADIUS -> new Radius();
            case ADD -> new Adder(paramArray);
            case SUB -> new Subtractor(paramArray);
            case MUL -> new Multiplier(paramArray);
            case DIV -> new Divider(paramArray);
            case MAX -> new Maximum(paramArray);
            case MIN -> new Minimum(paramArray);
            case IFGT -> new IfGreaterThan(paramArray);
            case SPECIES -> speciesArg != Species.NULL_SPECIES ? new IfSpecies(speciesArg, paramArray) : null;
            case DEBUG -> new Debug(paramArray);
            default -> NULL_OPERATOR;
        };

    }

    public double apply(RandomSource random, int x, int z, float noise) {
        MathContext mc = new MathContext(noise, x, z, random);
        return rootOp.apply(mc);
    }

    public double apply(RandomSource random, Species species, int x, int z, float radius) {
        MathContext mc = new MathSpeciesContext(random, species, x, z, radius);
        return rootOp.apply(mc);
    }

    public static class MathContext {
        public double noise;
        public double x;
        public double z;
        public RandomSource rand;

        public MathContext(
            double noise,
            double x,
            double z,
            RandomSource random
        ) {
            this.noise = noise;
            this.x = x;
            this.z = z;
            this.rand = random;
        }
    }

    public static class MathSpeciesContext extends MathContext {
        public float radius;
        public Species species;

        public MathSpeciesContext(
            RandomSource random,
            Species species,
            double x,
            double z,
            float radius
        ) {
            super(0.0f, x, z, random);
            this.radius = radius;
            this.species = species;
        }

    }

    public interface MathOperator {
        double apply(MathContext mc);
    }

    public static class Const implements MathOperator {
        private final float value;

        Const(float value) {
            this.value = value;
        }

        @Override
        public double apply(MathContext mc) {
            return value;
        }
    }

    public static class ValX implements MathOperator {
        @Override
        public double apply(MathContext mc) {
            return mc.x;
        }
    }
    
    public static class ValZ implements MathOperator {
        @Override
        public double apply(MathContext mc) {
            return mc.z;
        }
    }
    
    public static class Noise implements MathOperator {

        @Override
        public double apply(MathContext mc) {
            return mc.noise;
        }

    }

    public static class Perlin implements MathOperator {
        
        private final MathOperator[] functions;
        private final PerlinNoise noise;
        
        public Perlin(MathOperator[] functionArray) {
            this.functions = functionArray;
            
            WorldgenRandom random = new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(BASE_SEED));
            MathContext mc = new MathContext(0, 0, 0, random);
            
            if(functionArray.length > 2) {
                long seed = (long) (functionArray[2].apply(mc) * Long.MAX_VALUE);
                random = new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(seed));
            }

            java.util.List<Integer> octaves = new ArrayList<>();
            for (int i = 3; i < functionArray.length; i++) {
                octaves.add((int) functionArray[i].apply(mc));
            }

            this.noise = PerlinNoise.create(random, octaves);
        }
        
        @Override
        public double apply(MathContext mc) {
            double x = functions.length > 0 ? functions[0].apply(mc) : mc.x;
            double z = functions.length > 1 ? functions[1].apply(mc) : mc.z;
            
            return (noise.getValue(x, 0.0D, z) + 1.0D) / 2.0D;
        }
        
    }
    
    public static class Simplex implements MathOperator {
        
        private final MathOperator[] functions;
        private final SimplexNoise noise;
        
        public Simplex(MathOperator[] functionArray) {
            this.functions = functionArray;
            
            WorldgenRandom random = new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(BASE_SEED));
            MathContext mc = new MathContext(0, 0, 0, random);
            
            if(functionArray.length > 2) {
                long seed = (long) (functionArray[2].apply(mc) * Long.MAX_VALUE);
                random = new WorldgenRandom(WorldgenRandom.Algorithm.LEGACY.newInstance(seed));
            }
            
            this.noise = new SimplexNoise(random);
        }
        
        @Override
        public double apply(MathContext mc) {
            double x = functions.length > 0 ? functions[0].apply(mc) : mc.x;
            double z = functions.length > 1 ? functions[1].apply(mc) : mc.z;
            
            return (noise.getValue(x, 0.0D, z) + 1.0D) / 2.0D;
        }
        
    }
    
    public static class Rand implements MathOperator {

        @Override
        public double apply(MathContext mc) {
            return mc.rand.nextFloat();
        }

    }

    public static class Radius implements MathOperator {

        @Override
        public double apply(MathContext mc) {
            if (mc instanceof MathSpeciesContext) {
                return ((MathSpeciesContext) mc).radius;
            }

            return 0;
        }

    }


    public static class Adder implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Adder(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return functions[0].apply(mc) + functions[1].apply(mc);
            }

            double r = 0;
            for (MathOperator f : functions) {
                r += f.apply(mc);
            }

            return r;
        }
    }

    public static class Subtractor implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Subtractor(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return functions[0].apply(mc) - functions[1].apply(mc);
            }

            Double r = null;
            for (MathOperator f : functions) {
                double v = f.apply(mc);
                r = r == null ? v : r - v;
            }

            return r == null ? 0.0 : r;
        }
    }

    public static class Multiplier implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Multiplier(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return functions[0].apply(mc) * functions[1].apply(mc);
            }

            double r = 1.0f;
            for (MathOperator f : functions) {
                r *= f.apply(mc);
            }

            return r;
        }
    }

    public static class Divider implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Divider(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return functions[0].apply(mc) / functions[1].apply(mc);
            }

            Double r = null;
            for (MathOperator f : functions) {
                double v = f.apply(mc);
                r = r == null ? v : r / v;
            }

            return r == null ? 0.0 : r;
        }
    }

    public static class Modulus implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Modulus(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return functions[0].apply(mc) % functions[1].apply(mc);
            }

            return 0.0;
        }
    }


    public static class Maximum implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Maximum(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return Math.max(functions[0].apply(mc), functions[1].apply(mc));
            }

            Double r = null;
            for (MathOperator f : functions) {
                double v = f.apply(mc);
                r = r == null ? v : Math.max(r, v);
            }

            return r == null ? 0.0 : r;
        }
    }

    public static class Minimum implements MathOperator {

        private final MathOperator[] functions;
        private final boolean dual;

        public Minimum(MathOperator[] functionArray) {
            this.functions = functionArray;
            dual = functions.length == 2;
        }

        @Override
        public double apply(MathContext mc) {

            if (dual) {
                return Math.min(functions[0].apply(mc), functions[1].apply(mc));
            }

            Double r = null;
            for (MathOperator f : functions) {
                double v = f.apply(mc);
                r = r == null ? v : Math.min(r, v);
            }

            return r == null ? 0.0 : r;
        }
    }

    public static class IfGreaterThan implements MathOperator {

        private final MathOperator[] functions;

        public IfGreaterThan(MathOperator[] functionArray) {
            this.functions = functionArray;
        }

        @Override
        public double apply(MathContext mc) {

            if (functions.length == 4) {
                return functions[0].apply(mc) > functions[1].apply(mc) ? functions[2].apply(mc) : functions[3].apply(mc);
            }

            return 0.0;
        }

    }

    public static class IfSpecies implements MathOperator {

        private final MathOperator[] functions;
        private final Species species;

        public IfSpecies(Species species, MathOperator[] functionArray) {
            this.species = species;
            this.functions = functionArray;
        }

        @Override
        public double apply(MathContext mc) {

            if (mc instanceof MathSpeciesContext && functions.length == 2) {
                return ((MathSpeciesContext) mc).species == species ? functions[0].apply(mc) : functions[1].apply(mc);
            }

            return 0.0;
        }

    }

    public static class Debug implements MathOperator {

        private final MathOperator[] functions;

        public Debug(MathOperator[] functionArray) {
            this.functions = functionArray;
        }

        @Override
        public double apply(MathContext mc) {
            if (functions.length >= 1) {
                double val = functions[0].apply(mc);
                LogManager.getLogger().debug("Json Debug Value: " + val);
                return val;
            }
            return 0;
        }

    }

    public static final MathOperator NULL_OPERATOR = new Null();

    private static class Null implements MathOperator {
        @Override
        public double apply(MathContext mc) {
            return 0;
        }
    }

    public enum MathFunction {
        CONST,
        X,
        Z,
        NOISE,
        PERLIN,
        SIMPLEX,
        RAND,
        RADIUS,
        ADD,
        SUB,
        MUL,
        DIV,
        MOD,
        MAX,
        MIN,
        /**{@link IfGreaterThan}*/
        IFGT,
        SPECIES,
        DEBUG;

        public final String name;

        MathFunction() {
            this.name = toString().toLowerCase(Locale.ENGLISH);
        }

        @Nullable
        static MathFunction getFunction(String findName) {
            for (MathFunction fun : MathFunction.values()) {
                if (fun.name.equals(findName)) {
                    return fun;
                }
            }
            return null;
        }

    }
}
