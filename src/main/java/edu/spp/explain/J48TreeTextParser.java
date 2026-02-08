package edu.spp.explain;

import java.util.ArrayList;
import java.util.List;

import edu.spp.explain.J48Explainer.Condition;
import edu.spp.explain.J48Explainer.DecisionNode;

final class J48TreeTextParser {

    private J48TreeTextParser() {}

    static DecisionNode parse(String modelToString) {
        List<String> lines = extractTreeLines(modelToString);
        DecisionNode root = new DecisionNode(null, null);

        List<DecisionNode> stack = new ArrayList<>();
        stack.add(root);

        for (String rawLine : lines) {
            if (rawLine.isBlank()) continue;
            int depth = countDepth(rawLine); 
            String line = stripDepthPrefix(rawLine);

            ParsedLine parsed = parseLine(line);
            if (parsed == null) continue;

            while (stack.size() > depth + 1) stack.remove(stack.size() - 1);
            DecisionNode parent = stack.get(stack.size() - 1);

            DecisionNode node = new DecisionNode(parsed.condition, parsed.leafLabel);
            parent.children.add(node);

            stack.add(node);
        }

        return root;
    }

    private static List<String> extractTreeLines(String txt) {
        String[] all = txt.split("\\R");
        int start = -1;
        int end = all.length;

        for (int i = 0; i < all.length; i++) {
            String s = all[i].trim();
            if (s.equalsIgnoreCase("J48 pruned tree") || s.equalsIgnoreCase("J48 unpruned tree")) {
                start = i + 2;
                break;
            }
        }
        if (start == -1) return List.of();

        for (int i = start; i < all.length; i++) {
            String s = all[i].trim();
            if (s.startsWith("Number of Leaves") || s.startsWith("Size of the tree") || s.startsWith("=== ")) {
                end = i;
                break;
            }
        }

        List<String> out = new ArrayList<>();
        for (int i = start; i < end; i++) out.add(all[i]);
        return out;
    }

    private static int countDepth(String line) {
        int depth = 0;
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (ch == '|') {
                depth++;
                i++;
                while (i < line.length() && line.charAt(i) == ' ') i++;
            } else if (ch == ' ') {
                i++;
            } else {
                break;
            }
        }
        return depth;
    }

    private static String stripDepthPrefix(String line) {
        int i = 0;
        while (i < line.length()) {
            char ch = line.charAt(i);
            if (ch == '|') {
                i++;
                while (i < line.length() && line.charAt(i) == ' ') i++;
            } else if (ch == ' ') {
                i++;
            } else {
                break;
            }
        }
        return line.substring(i).trim();
    }

    private static ParsedLine parseLine(String line) {
        String conditionPart = line;
        String leafPart = null;

        int colon = line.indexOf(':');
        if (colon >= 0) {
            conditionPart = line.substring(0, colon).trim();
            leafPart = line.substring(colon + 1).trim();
        }

        Condition cond = parseCondition(conditionPart);
        if (cond == null) return null;

        String leafLabel = null;
        if (leafPart != null && !leafPart.isBlank()) {
            String[] toks = leafPart.split("\\s+");
            if (toks.length > 0) leafLabel = toks[0].trim();
        }
        return new ParsedLine(cond, leafLabel);
    }

    private static Condition parseCondition(String s) {
        String op;
        int opIdx;
        if ((opIdx = s.indexOf("<=")) >= 0) op = "<=";
        else if ((opIdx = s.indexOf(">")) >= 0) op = ">";
        else return null;

        String attr = s.substring(0, opIdx).trim();
        String rhs = s.substring(opIdx + op.length()).trim();
        if (attr.isEmpty() || rhs.isEmpty()) return null;

        String[] toks = rhs.split("\\s+");
        double threshold;
        try {
            threshold = Double.parseDouble(toks[0]);
        } catch (NumberFormatException e) {
            return null;
        }
        return new Condition(attr, op, threshold);
    }

    private record ParsedLine(Condition condition, String leafLabel) {}
}

