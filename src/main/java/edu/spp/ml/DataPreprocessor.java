package edu.spp.ml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public final class DataPreprocessor {

    public static final String RESOURCE_CSV = "/org/student_performance.csv";

    public static final String ATTR_STUDY_HOURS = "weekly_self_study_hours";
    public static final String ATTR_ATTENDANCE = "attendance_percentage";
    public static final String ATTR_PARTICIPATION = "class_participation";
    public static final String ATTR_TOTAL_SCORE = "total_score";
    public static final String ATTR_GRADE = "grade";

    public static final String ATTR_TARGET = "pass_fail";

    private DataPreprocessor() {}

    public static Instances loadRawCsvFromResources() throws Exception {
        try (InputStream in = DataPreprocessor.class.getResourceAsStream(RESOURCE_CSV)) {
            if (in == null) {
                throw new IllegalStateException("Dataset not found on classpath: " + RESOURCE_CSV);
            }
            CSVLoader loader = new CSVLoader();
            loader.setSource(in);
            Instances data = loader.getDataSet();
            if (data == null || data.numInstances() == 0) {
                throw new IllegalStateException("Loaded dataset is empty.");
            }
            return data;
        }
    }

    public static Instances buildPassFailTrainingData(Instances raw) {
        int idxStudy = raw.attribute(ATTR_STUDY_HOURS).index();
        int idxAttendance = raw.attribute(ATTR_ATTENDANCE).index();
        int idxParticipation = raw.attribute(ATTR_PARTICIPATION).index();
        Attribute totalScoreAttr = raw.attribute(ATTR_TOTAL_SCORE);

        List<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute(ATTR_STUDY_HOURS));
        attrs.add(new Attribute(ATTR_ATTENDANCE));
        attrs.add(new Attribute(ATTR_PARTICIPATION));
        attrs.add(new Attribute(ATTR_TARGET, List.of("FAIL", "PASS")));

        Instances out = new Instances("student_performance_passfail", new ArrayList<>(attrs), raw.numInstances());
        out.setClassIndex(out.numAttributes() - 1);

        for (int i = 0; i < raw.numInstances(); i++) {
            Instance r = raw.instance(i);
            double study = r.value(idxStudy);
            double attendance = r.value(idxAttendance);
            double participation = r.value(idxParticipation);

            boolean pass = computePassLabel(r, totalScoreAttr);
            double classVal = out.classAttribute().indexOfValue(pass ? "PASS" : "FAIL");

            DenseInstance inst = new DenseInstance(out.numAttributes());
            inst.setValue(0, study);
            inst.setValue(1, attendance);
            inst.setValue(2, participation);
            inst.setValue(3, classVal);
            out.add(inst);
        }
        return out;
    }

    private static boolean computePassLabel(Instance rawInstance, Attribute totalScoreAttr) {
        boolean basicPass = false;

        if (totalScoreAttr != null && totalScoreAttr.isNumeric()) {
            double score = rawInstance.value(totalScoreAttr);
            basicPass = score >= 55.0;
        } else {
            Attribute gradeAttr = rawInstance.dataset().attribute(ATTR_GRADE);
            if (gradeAttr != null && gradeAttr.isNominal()) {
                String grade = rawInstance.stringValue(gradeAttr);
                grade = grade == null ? "" : grade.trim().toUpperCase(Locale.ROOT);
                basicPass = grade.equals("A") || grade.equals("B") || grade.equals("C");
            }
        }

        Attribute attendanceAttr = rawInstance.dataset().attribute(ATTR_ATTENDANCE);
        Attribute participationAttr = rawInstance.dataset().attribute(ATTR_PARTICIPATION);

        double attendance = attendanceAttr != null ? rawInstance.value(attendanceAttr) : Double.NaN;
        double participation = participationAttr != null ? rawInstance.value(participationAttr) : Double.NaN;

        boolean attendanceOk = !Double.isNaN(attendance) && attendance >= 70.0;
        boolean participationOk = !Double.isNaN(participation) && participation >= 3.0;

        return basicPass && attendanceOk && participationOk;
    }

    public static Instances buildPredictionHeader() {
        List<Attribute> attrs = new ArrayList<>();
        attrs.add(new Attribute(ATTR_STUDY_HOURS));
        attrs.add(new Attribute(ATTR_ATTENDANCE));
        attrs.add(new Attribute(ATTR_PARTICIPATION));
        attrs.add(new Attribute(ATTR_TARGET, List.of("FAIL", "PASS")));

        Instances header = new Instances("student_performance_passfail_header", new ArrayList<>(attrs), 0);
        header.setClassIndex(header.numAttributes() - 1);
        return header;
    }
}

