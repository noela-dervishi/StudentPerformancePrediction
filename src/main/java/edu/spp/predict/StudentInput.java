package edu.spp.predict;

public record StudentInput(
        int studentId,
        double weeklySelfStudyHours,
        double attendancePercentage,
        double classParticipation
) {}

