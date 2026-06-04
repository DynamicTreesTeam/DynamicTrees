package com.dtteam.dynamictrees.deserialization;

import com.dtteam.dynamictrees.deserialization.math.*;
import com.dtteam.dynamictrees.deserialization.math.noise.NoiseType;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map.Entry;

@Deprecated
public class JsonMath implements MathOperator {

    public Noise noise = null; // Lazy initialized
    public MathOperator rootOp = NullOperator.NULL;

    @Deprecated
    public JsonMath(JsonElement mathElement) {
        if (mathElement.isJsonPrimitive()) {
            if (mathElement.getAsJsonPrimitive().isString()) {
                rootOp = ExpressionParser.parse(mathElement.getAsString());
            } else if (mathElement.getAsJsonPrimitive().isNumber()) {
                rootOp = new Const(mathElement.getAsDouble());
            }
        } else if (mathElement.isJsonObject()) {
            JsonObject mathObject = mathElement.getAsJsonObject();

            for (Entry<String, JsonElement> entry : mathObject.entrySet()) {
                rootOp = processElement(entry.getKey(), entry.getValue());
                if (rootOp != NullOperator.NULL) {
                    return;
                }
            }
        }
    }
    
    @Deprecated
    private MathOperator getVariable(String name) {
        return switch (name) {
            case "x" -> mc -> mc.pos().getX();
            case "z" -> mc -> mc.pos().getZ();
            case "noise" -> lazyInitNoise();
            case "rand" -> mc -> mc.rand().nextDouble();
            case "radius" -> MathContext::radius;
            default -> NullOperator.NULL;
        };
    }

    private Noise lazyInitNoise() {
        if(noise == null) {
            noise = Noise.build(NoiseType.LEGACY);
        }
        return noise;
    }
    
    @Deprecated
    private MathOperator processElement(String key, JsonElement value) {

        MathFunction op = MathFunction.getFunction(key);

        if (op == null) {
            return NullOperator.NULL;
        }

        ArrayList<MathOperator> paramList = new ArrayList<>();
        Species speciesArg = Species.NULL_SPECIES;

        //If the value is an array then these are the parameters for this operation
        if (value.isJsonArray()) {
            for (JsonElement parameter : value.getAsJsonArray()) {
                MathOperator m = NullOperator.NULL;
                if (parameter.isJsonObject()) {
                    Entry<String, JsonElement> entry = parameter.getAsJsonObject().entrySet().iterator().next();
                    m = processElement(entry.getKey(), entry.getValue());
                } else if (parameter.isJsonPrimitive()) {
                    if (parameter.getAsJsonPrimitive().isNumber()) {
                        m = new Const(parameter.getAsDouble());
                    } else if (parameter.getAsJsonPrimitive().isString()) {
                        String name = parameter.getAsString();
                        MathOperator var = getVariable(name);
                        if (var != NullOperator.NULL) {
                            m = var;
                        } else if (Species.findSpeciesSloppy(name) != Species.NULL_SPECIES) {
                            speciesArg = Species.findSpeciesSloppy(name);
                        }
                    }
                }

                if (m != NullOperator.NULL) {
                    paramList.add(m);
                }

            }
        }

        MathOperator[] paramArray = paramList.toArray(new MathOperator[0]);
        
        return switch (op) {
            case NOISE -> lazyInitNoise();
            case RAND -> mc -> mc.rand().nextDouble();
            case RADIUS -> MathContext::radius;
            case ADD -> new Add(paramArray);
            case SUB -> new Sub(paramArray);
            case MUL -> new Mul(paramArray);
            case DIV -> new Div(paramArray);
            case MAX -> new MaxOperator(paramArray);
            case MIN -> new MinOperator(paramArray);
            case IFGT -> new IfGt(paramArray);
            case SPECIES -> speciesArg != Species.NULL_SPECIES ? new IfSpecies(speciesArg, paramArray) : NullOperator.NULL;
            case DEBUG -> new Debug(paramArray);
            default -> NullOperator.NULL;
        };

    }
    
    @Deprecated
    public double apply(MathContext mc) {
        return rootOp.apply(mc);
    }
    
    @Deprecated
    public static class Add implements MathOperator {

        private final MathOperator[] functions;

        public Add(MathOperator[] functionArray) {
            this.functions = functionArray;
        }

        @Override
        public double apply(MathContext mc) {
            double r = 0;
            for (MathOperator f : functions) {
                r += f.apply(mc);
            }

            return r;
        }
    }
    
    @Deprecated   
    public static class Sub implements MathOperator {
        
        private final MathOperator[] functions;
        
        public Sub(MathOperator[] functionArray) {
            this.functions = functionArray;
        }
        
        @Override
        public double apply(MathContext mc) {

            Double r = null;
            for (MathOperator f : functions) {
                double v = f.apply(mc);
                r = r == null ? v : r - v;
            }
            
            return r == null ? 0.0 : r;
        }
    }
    
    @Deprecated
    public static class Mul implements MathOperator {

        private final MathOperator[] functions;
        
        public Mul(MathOperator[] functionArray) {
            this.functions = functionArray;
        }
        
        @Override
        public double apply(MathContext mc) {
           double r = 1.0f;
            for (MathOperator f : functions) {
                r *= f.apply(mc);
            }
            return r;
        }
    }
    
    @Deprecated
    public static class Div implements MathOperator {

        private final MathOperator[] functions;
        
        public Div(MathOperator[] functionArray) {
            this.functions = functionArray;
        }

        @Override
        public double apply(MathContext mc) {
            Double r = null;
            for (MathOperator f : functions) {
                double v = f.apply(mc);
                r = r == null ? v : r / v;
            }
            return r == null ? 0.0 : r;
        }
    }
    
    @Deprecated
    public static class IfGt implements MathOperator {

        private final MathOperator[] functions;

        public IfGt(MathOperator[] functionArray) {
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
    
}
