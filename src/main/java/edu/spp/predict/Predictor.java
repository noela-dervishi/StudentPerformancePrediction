package edu.spp.predict;

import edu.spp.explain.J48Explainer;
import edu.spp.ml.DataPreprocessor;
import edu.spp.ml.ModelIO;
import edu.spp.ml.TrainModel;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.File;
import java.util.Locale;

/**
 * Loads the trained model and performs Pass/Fail predictions.
 */
public final class Predictor {

    private final Classifier model;
    private final Instances header;
    private final J48Explainer explainer;

    private Predictor(Classifier model, Instances header) {
        this.model = model;
        this.header = header;
        this.explainer = J48Explainer.fromModel(model);
    }

    public static Predictor loadDefault() throws Exception {
        File modelPath = TrainModel.DEFAULT_MODEL_PATH;
        if (!modelPath.exists()) {
            throw new IllegalStateException("Model not found. Train first: " + modelPath.getPath());
        }
        Classifier model = ModelIO.load(modelPath);
        Instances header = DataPreprocessor.buildPredictionHeader();
        return new Predictor(model, header);
    }

    public PredictionResult predict(StudentInput input) throws Exception {
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setDataset(header);
        inst.setValue(header.attribute(DataPreprocessor.ATTR_STUDY_HOURS), input.weeklySelfStudyHours());
        inst.setValue(header.attribute(DataPreprocessor.ATTR_ATTENDANCE), input.attendancePercentage());
        inst.setValue(header.attribute(DataPreprocessor.ATTR_PARTICIPATION), input.classParticipation());
        inst.setMissing(header.classIndex());

        double[] dist = model.distributionForInstance(inst);
        int predictedIndex = argMax(dist);
        String label = header.classAttribute().value(predictedIndex);
        double confidence = dist[predictedIndex];

        var exp = explainer.explain(input, inst, label);
        String explanation = String.format(
                Locale.US,
                "%s\n\nModel confidence: %.0f%%",
                exp.englishText(),
                confidence * 100.0
        );

        return new PredictionResult(input, label, confidence, explanation);
    }

    private static int argMax(double[] arr) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[best]) best = i;
        }
        return best;
    }
}

