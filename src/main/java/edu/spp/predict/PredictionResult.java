package edu.spp.predict;

import java.util.Locale;

public record PredictionResult(
        StudentInput input,
        String predictedLabel,     
        double confidence,         
        String explanation         
) {
    public String toHumanString() {
        return String.format(
                Locale.US,
                """
                Student %d prediction: %s (confidence %.0f%%)

                %s
                ------------------------------------------------------------
                """,
                input.studentId(),
                predictedLabel,
                confidence * 100.0,
                explanation
        );
    }
}

