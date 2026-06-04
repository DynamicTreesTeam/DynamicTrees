package com.dtteam.dynamictrees.deserialization.math;

import com.dtteam.dynamictrees.deserialization.math.noise.NoiseType;
import com.dtteam.dynamictrees.deserialization.math.operator.*;
import com.dtteam.dynamictrees.tree.species.Species;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class ExpressionParser {
    
    private static final HashMap<String, MathOperator> CACHE = new HashMap<>();

    public static final HashMap<String, MathOperatorBuilder> FUNCTIONS = new HashMap<>();
    static {
        //Basic Math
        FUNCTIONS.put("min", MinOperator::new);
        FUNCTIONS.put("max", MaxOperator::new);
        FUNCTIONS.put("avg", AverageOperator::new);
        FUNCTIONS.put("sqrt", args -> new FunctionOperator(Math::sqrt, args));
        FUNCTIONS.put("pow", args -> new BiFunctionOperator(Math::pow, args));
        FUNCTIONS.put("exp", args -> new FunctionOperator(Math::exp, args));
        FUNCTIONS.put("lerp", LerpOperator::new);
        FUNCTIONS.put("map", MapOperator::new);
        FUNCTIONS.put("clamp", ClampOperator::new);
        FUNCTIONS.put("abs", args -> new FunctionOperator(Math::abs, args));
        FUNCTIONS.put("log", LogOperator::new);
        //Logic
        FUNCTIONS.put("if", IfOperator::new);
        //Noise
        FUNCTIONS.put("perlin", args->Noise.build(NoiseType.PERLIN, args));
        FUNCTIONS.put("simplex", args->Noise.build(NoiseType.SIMPLEX, args));
        FUNCTIONS.put("noise", args->Noise.build(NoiseType.LEGACY, args));
        FUNCTIONS.put("rand", args->mc -> mc.rand().nextDouble());
        //Trig
        FUNCTIONS.put("sin", args -> new FunctionOperator(Math::sin, args));
        FUNCTIONS.put("arcsin", args -> new FunctionOperator(Math::asin, args));
        FUNCTIONS.put("cos", args -> new FunctionOperator(Math::cos, args));
        FUNCTIONS.put("arccos", args -> new FunctionOperator(Math::acos, args));
        FUNCTIONS.put("tan", args -> new FunctionOperator(Math::tan, args));
        FUNCTIONS.put("arctan", args -> new FunctionOperator(Math::atan, args));
        FUNCTIONS.put("arctan2", args -> new BiFunctionOperator(Math::atan2, args));
        //Misc
        FUNCTIONS.put("debug", Debug::new);
    }

    public static final HashMap<String, MathOperator> VARIABLES = new HashMap<>();
    static {
        VARIABLES.put("x", mc -> mc.pos().getX());
        VARIABLES.put("y", mc -> mc.pos().getY());
        VARIABLES.put("z", mc -> mc.pos().getZ());
        VARIABLES.put("radius", MathContext::radius);
        VARIABLES.put("pi", mc -> Math.PI);
    }

    private final String expression;
    private int cursor;
    
    public static MathOperator parse(String expression) throws JsonParseException {
        expression = expression.replaceAll("\\s", "");
        if (CACHE.containsKey(expression)) {
            return CACHE.get(expression);
        }
        final ExpressionParser parser = new ExpressionParser(expression);
        final MathOperator result = parser.parse();
        CACHE.put(expression, result);
        return result;
    }
    
    private ExpressionParser(String expression) {
        this.expression = expression;
    }
    
    private MathOperator parse() {
        final MathOperator operator = parseBoolean();
        if (!isAtEnd()) {
            throw error("Unexpected token \"" + peek() + "\".");
        }
        return operator;
    }

    private MathOperator parseBoolean() {
        MathOperator left = parseComparison();

        while (true) {
            if (consume("&&")) {
                left = new BooleanLogicOperator(left, parseComparison(), BooleanType.AND);
            } else if (consume("||")) {
                left = new BooleanLogicOperator(left, parseComparison(), BooleanType.OR);
            } else if (consume("^")) {
                left = new BooleanLogicOperator(left, parseComparison(), BooleanType.XOR);
            } else {
                return left;
            }
        }
    }

    private MathOperator parseComparison() {
        MathOperator left = parseAdditive();
        
        while (true) {
            if (consume(">=")) {
                left = new ComparisonOperator(left, parseAdditive(), ComparisonType.GREATER_THAN_OR_EQUAL);
            } else if (consume("<=")) {
                left = new ComparisonOperator(left, parseAdditive(), ComparisonType.LESS_THAN_OR_EQUAL);
            } else if (consume("==")) {
                left = new ComparisonOperator(left, parseAdditive(), ComparisonType.EQUAL);
            } else if (consume("!=")) {
                left = new ComparisonOperator(left, parseAdditive(), ComparisonType.NOT_EQUAL);
            } else if (consume(">")) {
                left = new ComparisonOperator(left, parseAdditive(), ComparisonType.GREATER_THAN);
            } else if (consume("<")) {
                left = new ComparisonOperator(left, parseAdditive(), ComparisonType.LESS_THAN);
            } else {
                return left;
            }
        }
    }
    
    private MathOperator parseAdditive() {
        MathOperator left = parseMultiplicative();
        
        while (true) {
            if (consume("+")) {
                left = new ArithmeticOperator(left, parseMultiplicative(), ArithmeticType.ADD);
            } else if (consume("-")) {
                left = new ArithmeticOperator(left, parseMultiplicative(), ArithmeticType.SUB);
            } else {
                return left;
            }
        }
    }
    
    private MathOperator parseMultiplicative() {
        MathOperator left = parseUnary();
        
        while (true) {
            if (consume("*")) {
                left = new ArithmeticOperator(left, parseUnary(), ArithmeticType.MUL);
            } else if (consume("/")) {
                left = new ArithmeticOperator(left, parseUnary(), ArithmeticType.DIV);
            } else if (consume("%")) {
                left = new ArithmeticOperator(left, parseUnary(), ArithmeticType.MOD);
            } else {
                return left;
            }
        }
    }
    
    private MathOperator parseUnary() {
        if (consume("+")) {
            return parseUnary();
        }
        if (consume("-")) {
            return new ArithmeticOperator(new Const(0), parseUnary(), ArithmeticType.SUB);
        }
        if (consume("!")) {
            return new BooleanLogicOperator(parseUnary(), new Const(0), BooleanType.NOT);
        }
        return parsePrimary();
    }
    
    private MathOperator parsePrimary() {
        if (consume("(")) {
            final MathOperator operator = parseComparison();
            expect(")", "Expected closing parenthesis.");
            return operator;
        }
        if (isNumberStart()) {
            return parseNumber();
        }
        if (isIdentifierStart(peek())) {
            final String identifier = parseIdentifier().toLowerCase(Locale.ENGLISH);
            if (consume("(")) {
                return parseFunction(identifier);
            }
            return parseVariable(identifier);
        }
        throw error("Expected number, variable, function, or parenthesized expression.");
    }
    
    private MathOperator parseNumber() {
        final int start = cursor;
        if (peek() == '.') {
            cursor++;
        }
        while (!isAtEnd() && Character.isDigit(peek())) {
            cursor++;
        }
        if (!isAtEnd() && peek() == '.') {
            cursor++;
            while (!isAtEnd() && Character.isDigit(peek())) {
                cursor++;
            }
        }
        try {
            return new Const(Double.parseDouble(expression.substring(start, cursor)));
        } catch (NumberFormatException exception) {
            throw error("Invalid number.");
        }
    }
    
    private MathOperator parseFunction(String name) {
        if ("is".equals(name)) {
            return parseIsSpeciesFunction();
        }
        
        final ArrayList<MathOperator> arguments = new ArrayList<>();
        if (!consume(")")) {
            do {
                arguments.add(parseComparison());
            } while (consume(","));
            expect(")", "Expected closing parenthesis after function arguments.");
        }
        
        final MathOperator[] argumentArray = arguments.toArray(new MathOperator[0]);

        try {
            if (FUNCTIONS.containsKey(name)){
                return FUNCTIONS.get(name).build(argumentArray);
            } else {
                throw error("Invalid function: \"" + name + "\" does not exist.");
            }
        } catch (IllegalArgumentException e){
            throw error("Invalid function \"" + name + "\": "+e.getMessage()+".");
        }
    }
    
    private MathOperator parseIsSpeciesFunction() {
        final List<Species> speciesList = new ArrayList<>();

        if (!consume(")")) {
            do {
                final String speciesName = parseSpeciesName();
                final Species species = Species.findSpeciesSloppy(speciesName);
                if (species != Species.NULL_SPECIES) {
                    speciesList.add(species);
                }
            } while (consume(","));

            expect(")", "Expected closing parenthesis after species function arguments.");
        }

        return new IsSpecies(speciesList);
    }

    private String parseSpeciesName() {
        if (peek() != '\'') {
            throw error("Expected single-quoted species name.");
        }

        final String speciesName = parseString().trim();
        if (speciesName.isEmpty()) {
            throw error("Expected species name.");
        }
        return speciesName;
    }
    
    private MathOperator parseVariable(String name) {
        if (VARIABLES.containsKey(name)){
            return VARIABLES.get(name);
        } else {
            throw error("Unknown variable \"" + name + "\".");
        }
    }
    
    private String parseIdentifier() {
        if (!isIdentifierStart(peek())) {
            throw error("Expected identifier.");
        }
        final int start = cursor++;
        while (!isAtEnd() && isIdentifierPart(peek())) {
            cursor++;
        }
        return expression.substring(start, cursor);
    }
    
    private String parseString() {
        final char quote = peek();
        cursor++;
        final StringBuilder builder = new StringBuilder();
        while (!isAtEnd()) {
            final char character = expression.charAt(cursor++);
            if (character == quote) {
                return builder.toString();
            }
            if (character == '\\') {
                if (isAtEnd()) {
                    throw error("Unterminated escape sequence.");
                }
                builder.append(expression.charAt(cursor++));
            } else {
                builder.append(character);
            }
        }
        throw error("Unterminated string literal.");
    }
    
    private void expect(String token, String message) {
        if (!consume(token)) {
            throw error(message);
        }
    }
    
    private boolean consume(String token) {
        if (expression.startsWith(token, cursor)) {
            cursor += token.length();
            return true;
        }
        return false;
    }

    private boolean isNumberStart() {
        return Character.isDigit(peek()) || (peek() == '.' && cursor + 1 < expression.length() &&
            Character.isDigit(expression.charAt(cursor + 1)));
    }
    
    private boolean isIdentifierStart(char character) {
        return Character.isLetter(character) || character == '_';
    }
    
    private boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }
    
    private char peek() {
        return isAtEnd() ? '\0' : expression.charAt(cursor);
    }
    
    private boolean isAtEnd() {
        return cursor >= expression.length();
    }
    
    private JsonParseException error(String message) {
        return new JsonParseException("Invalid math expression \"" + expression + "\" at position " + cursor +
            ": " + message);
    }
}
