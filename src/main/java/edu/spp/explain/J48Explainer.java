package edu.spp.explain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.spp.ml.DataPreprocessor;
import edu.spp.predict.StudentInput;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;

public final class J48Explainer {

    private final DecisionNode root;

    public J48Explainer(String j48ToString) {
        this.root = J48TreeTextParser.parse(j48ToString);
    }

    public static J48Explainer fromModel(Classifier model) {
        if (model instanceof J48 j48) {
            return new J48Explainer(j48.toString());
        }
        return new J48Explainer(model.toString());
    }

    public Explanation explain(StudentInput input, Instance instance, String predictedLabel) {
        List<Condition> path = new ArrayList<>();
        DecisionNode node = root;

        while (node != null && node.label == null) {
            DecisionNode next = null;
            for (DecisionNode child : node.children) {
                if (child.condition != null && child.condition.matches(instance)) {
                    path.add(child.condition);
                    next = child;
                    break;
                }
            }
            node = next;
        }

        StringBuilder english = new StringBuilder();
        english.append(String.format(
                Locale.US,
                "The student is predicted to %s based on the decision tree rules.\n\n",
                predictedLabel));

        if (path.isEmpty()) {
            english.append("No rule path could be extracted from the tree text, so a simplified summary is shown.\n");
        } else {
            english.append("Decision path (tree conditions that matched this student):\n");
            for (int i = 0; i < path.size(); i++) {
                english.append(String.format(Locale.US, "%d) %s\n", i + 1, toEnglish(path.get(i), input)));
            }
        }

        english.append("\nInput values:\n");
        english.append(String.format(Locale.US, "- Weekly self-study hours: %.1f\n", input.weeklySelfStudyHours()));
        english.append(String.format(Locale.US, "- Attendance percentage: %.1f%%\n", input.attendancePercentage()));
        english.append(String.format(Locale.US, "- Class participation: %.1f/10\n", input.classParticipation()));

        english.append("\nSimple recommendations:\n");
        english.append(recommendations(input));

        return new Explanation(english.toString(), path);
    }

    private static String toEnglish(Condition c, StudentInput input) {
        String name = switch (c.attributeName) {
            case DataPreprocessor.ATTR_STUDY_HOURS -> "weekly self-study hours";
            case DataPreprocessor.ATTR_ATTENDANCE -> "attendance percentage";
            case DataPreprocessor.ATTR_PARTICIPATION -> "class participation";
            default -> c.attributeName;
        };

        String units = switch (c.attributeName) {
            case DataPreprocessor.ATTR_STUDY_HOURS -> " hours";
            case DataPreprocessor.ATTR_ATTENDANCE -> "%";
            case DataPreprocessor.ATTR_PARTICIPATION -> " (0–10)";
            default -> "";
        };

        double actual = switch (c.attributeName) {
            case DataPreprocessor.ATTR_STUDY_HOURS -> input.weeklySelfStudyHours();
            case DataPreprocessor.ATTR_ATTENDANCE -> input.attendancePercentage();
            case DataPreprocessor.ATTR_PARTICIPATION -> input.classParticipation();
            default -> Double.NaN;
        };

        return String.format(
                Locale.US,
                "%s is %.2f and it satisfies: %s %s %.2f%s",
                name,
                actual,
                name,
                c.operator,
                c.threshold,
                units);
    }

    private static String recommendations(StudentInput input) {
        StringBuilder sb = new StringBuilder();
        if (input.weeklySelfStudyHours() < 10)
            sb.append("- Increase study hours (target: 15–20 hours/week).\n");
        else if (input.weeklySelfStudyHours() < 15)
            sb.append("- Consider adding 2–5 more study hours per week.\n");
        else
            sb.append("- Keep the current study routine.\n");

        if (input.attendancePercentage() < 70)
            sb.append("- Attendance is low; try to attend at least 80% of classes.\n");
        else if (input.attendancePercentage() < 80)
            sb.append("- Improve attendance to strengthen understanding.\n");
        else
            sb.append("- Maintain good attendance.\n");

        if (input.classParticipation() < 3)
            sb.append("- Participation is low; ask/answer at least 1 question per class.\n");
        else if (input.classParticipation() < 5)
            sb.append("- Participate more in discussions to reinforce learning.\n");
        else
            sb.append("- Keep engaging in class.\n");

        return sb.toString();
    }

    public record Explanation(String englishText, List<Condition> path) {
    }

    static final class DecisionNode {
        final Condition condition; // condition to reach this node from its parent
        final String label; // PASS/FAIL if leaf
        final List<DecisionNode> children = new ArrayList<>();

        DecisionNode(Condition condition, String label) {
            this.condition = condition;
            this.label = label;
        }
    }

    public static final class Condition {
        public final String attributeName;
        public final String operator; // "<=" or ">"
        public final double threshold;

        Condition(String attributeName, String operator, double threshold) {
            this.attributeName = attributeName;
            this.operator = operator;
            this.threshold = threshold;
        }

        boolean matches(Instance instance) {
            int idx = instance.dataset().attribute(attributeName).index();
            double v = instance.value(idx);
            return switch (operator) {
                case "<=" -> v <= threshold;
                case ">" -> v > threshold;
                default -> false;
            };
        }
    }
}
