package edu.spp.predict;

/**
 * Input features provided by the user at prediction time.
 */
public record StudentInput(
        int studentId,
        double weeklySelfStudyHours,
        double attendancePercentage,
        double classParticipation
) {}

