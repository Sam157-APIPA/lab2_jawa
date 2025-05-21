package org.example;
import java.util.*;

/**
 * Main class for parsing and evaluating mathematical expressions.
 *
 * This class supports numbers, variables, arithmetic operators, parentheses,
 * and a set of built-in functions (unary and binary)
 *
 */
public class Main {

    /**
     * Entry point. Reads an expression from standard input, prompts for variable values,
     * evaluates the expression, and prints the result. Prints an error message if
     * the expression is invalid.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter expression: ");
        String expression = scanner.nextLine();
        try {
            Set<String> variables = getVariables(expression);
            Map<String, Double> varValues = new HashMap<>();
            for (String var : variables) {
                System.out.print("Enter value for " + var + ": ");
                String valStr = scanner.nextLine();
                double val = Double.parseDouble(valStr);
                varValues.put(var, val);
            }

            List<String> tokens = tokenize(expression);
            List<String> rpn = toRPN(tokens);
            double result = evaluateRPN(rpn, varValues);

            System.out.println("Result: " + result);
        } catch (Exception e) {
            System.out.println("Error: Invalid expression");
        }
    }

    /**
     * Extracts variable names from the expression tokens. Identifiers
     * that are not recognized as functions are considered variables.
     *
     * @param expr the input expression string
     * @return a set of variable names
     * @throws RuntimeException if tokenization fails
     */
    private static Set<String> getVariables(String expr) {
        List<String> tokens = tokenize(expr);
        Set<String> vars = new HashSet<>();
        for (String tok : tokens) {
            if (tok.matches("[a-zA-Z_]\\w*") && !isFunction(tok)) {
                vars.add(tok);
            }
        }
        return vars;
    }

    /**
     * Splits the expression string into a list of tokens: numbers, identifiers,
     * operators, parentheses, and commas.
     *
     * @param expr the input expression string
     * @return list of tokens
     * @throws RuntimeException if an unknown character is encountered
     */
    private static List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                tokens.add(sb.toString());
            } else if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isLetterOrDigit(expr.charAt(i)) || expr.charAt(i) == '_')) {
                    sb.append(expr.charAt(i++));
                }
                tokens.add(sb.toString());
            } else if ("+-*/^(),".indexOf(c) >= 0) {
                tokens.add(Character.toString(c));
                i++;
            } else {
                throw new RuntimeException("Unknown token at position " + i);
            }
        }
        return tokens;
    }

    /**
     * Converts a list of infix tokens to Reverse Polish Notation (RPN)
     * using the Shunting-Yard algorithm.
     *
     * @param tokens list of infix tokens
     * @return list of tokens in RPN order
     * @throws RuntimeException on mismatched parentheses or misplaced commas
     */
    private static List<String> toRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();
        for (String tok : tokens) {
            if (tok.matches("\\d+(\\.\\d+)?")) {
                output.add(tok);
            } else if (tok.matches("[a-zA-Z_]\\w*")) {
                if (isFunction(tok)) {
                    ops.push(tok);
                } else {
                    output.add(tok);
                }
            } else if (tok.equals(",")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                if (ops.isEmpty()) throw new RuntimeException("Misplaced comma");
            } else if (isOperator(tok)) {
                while (!ops.isEmpty() && isOperator(ops.peek()) &&
                        ((isLeftAssoc(tok) && precedence(tok) <= precedence(ops.peek())) ||
                                (!isLeftAssoc(tok) && precedence(tok) < precedence(ops.peek())))) {
                    output.add(ops.pop());
                }
                ops.push(tok);
            } else if (tok.equals("(")) {
                ops.push(tok);
            } else if (tok.equals(")")) {
                while (!ops.isEmpty() && !ops.peek().equals("(")) {
                    output.add(ops.pop());
                }
                if (ops.isEmpty()) throw new RuntimeException("Mismatched parentheses");
                ops.pop();
                if (!ops.isEmpty() && isFunction(ops.peek())) {
                    output.add(ops.pop());
                }
            }
        }
        while (!ops.isEmpty()) {
            String op = ops.pop();
            if ("()".contains(op)) throw new RuntimeException("Mismatched parentheses");
            output.add(op);
        }
        return output;
    }

    /**
     * Evaluates an expression in RPN form.
     *
     * @param rpn list of tokens in RPN order
     * @param vars map of variable names to their values
     * @return the computed result
     * @throws RuntimeException on invalid expression or unknown token
     */
    private static double evaluateRPN(List<String> rpn, Map<String, Double> vars) {
        Deque<Double> stack = new ArrayDeque<>();
        for (String tok : rpn) {
            if (tok.matches("\\d+(\\.\\d+)?")) {
                stack.push(Double.parseDouble(tok));
            } else if (vars.containsKey(tok)) {
                stack.push(vars.get(tok));
            } else if (isOperator(tok)) {
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOp(tok, a, b));
            } else if (isFunction(tok)) {
                if (isUnaryFunction(tok)) {
                    double a = stack.pop();
                    stack.push(applyFunc(tok, a));
                } else {
                    double b = stack.pop();
                    double a = stack.pop();
                    stack.push(applyFunc(tok, a, b));
                }
            } else {
                throw new RuntimeException("Unknown token in RPN: " + tok);
            }
        }
        if (stack.size() != 1) throw new RuntimeException("Invalid RPN expression");
        return stack.pop();
    }

    /**
     * Checks if a token is an operator.
     *
     * @param tok operator token
     * @return true if tok is one of +, -, *, /, ^
     */
    private static boolean isOperator(String tok) {
        return "+-*/^".contains(tok);
    }

    /**
     * Returns operator precedence.
     *
     * @param op operator token
     * @return precedence level (higher means evaluated first)
     */
    private static int precedence(String op) {
        switch (op) {
            case "+": case "-": return 2;
            case "*": case "/": return 3;
            case "^": return 4;
            default: return 0;
        }
    }

    /**
     * Determines if an operator is left-associative.
     *
     * @param op operator token
     * @return true for +, -, *, /; false for ^
     */
    private static boolean isLeftAssoc(String op) {
        return !"^".equals(op);
    }

    /**
     * Applies a binary operator to two operands.
     *
     * @param op operator token
     * @param a left operand
     * @param b right operand
     * @return result of operation
     */
    private static double applyOp(String op, double a, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            case "^": return Math.pow(a, b);
            default: throw new RuntimeException("Unknown operator: " + op);
        }
    }

    // Supported functions
    private static final Set<String> UNARY_FUNCS = new HashSet<>(Arrays.asList(
            "sin","cos","tan","asin","acos","atan","sqrt","abs","log","exp"
    ));
    private static final Set<String> BINARY_FUNCS = new HashSet<>(Arrays.asList(
            "pow","max","min"
    ));

    /**
     * Checks if a token is a recognized function name.
     *
     * @param name function name token
     * @return true if name is in the set of supported functions
     */
    private static boolean isFunction(String name) {
        return UNARY_FUNCS.contains(name) || BINARY_FUNCS.contains(name);
    }

    /**
     * Determines if a function is unary (takes one argument).
     *
     * @param name function name token
     * @return true if name is in the set of unary functions
     */
    private static boolean isUnaryFunction(String name) {
        return UNARY_FUNCS.contains(name);
    }

    /**
     * Applies a unary function to an operand.
     *
     * @param name function name
     * @param a argument value
     * @return result of function
     */
    private static double applyFunc(String name, double a) {
        switch (name) {
            case "sin": return Math.sin(a);
            case "cos": return Math.cos(a);
            case "tan": return Math.tan(a);
            case "asin": return Math.asin(a);
            case "acos": return Math.acos(a);
            case "atan": return Math.atan(a);
            case "sqrt": return Math.sqrt(a);
            case "abs": return Math.abs(a);
            case "log": return Math.log(a);
            case "exp": return Math.exp(a);
            default: throw new RuntimeException("Unknown function: " + name);
        }
    }

    /**
     * Applies a binary function to two operands.
     *
     * @param name function name
     * @param a first argument
     * @param b second argument
     * @return result of function
     */
    private static double applyFunc(String name, double a, double b) {
        switch (name) {
            case "pow": return Math.pow(a, b);
            case "max": return Math.max(a, b);
            case "min": return Math.min(a, b);
            default: throw new RuntimeException("Unknown function: " + name);
        }
    }
}

