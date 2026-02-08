package edu.spp.ml;

import java.io.File;
import java.util.Locale;
import java.util.Random;

import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public final class TrainModel {

    public static final File DEFAULT_MODEL_PATH = new File("model/student_j48_passfail.model");

    private TrainModel() {}

    public static void main(String[] args) throws Exception {
        TrainReport report = trainAndSaveDefaultModel();
        System.out.println(report.toHumanString());
    }

    public static TrainReport trainAndSaveDefaultModel() throws Exception {
        Instances raw = DataPreprocessor.loadRawCsvFromResources();
        Instances data = DataPreprocessor.buildPassFailTrainingData(raw);

        data.randomize(new Random(42));
        int trainSize = (int) Math.round(data.numInstances() * 0.80);
        int testSize = data.numInstances() - trainSize;
        Instances train = new Instances(data, 0, trainSize);
        Instances test = new Instances(data, trainSize, testSize);

        J48 j48 = new J48();
        j48.setUnpruned(false);
        j48.setConfidenceFactor(0.15f); 
        j48.setMinNumObj(10);           
        j48.setUseLaplace(true);        
        j48.buildClassifier(train);

        CostSensitiveClassifier model = new CostSensitiveClassifier();
        model.setClassifier(j48);
        model.setMinimizeExpectedCost(true);
        model.setCostMatrix(defaultPassFailCostMatrix());
        model.buildClassifier(train);

        Evaluation evalTrain = new Evaluation(train, model.getCostMatrix());
        evalTrain.evaluateModel(model, train);

        Evaluation evalTest = new Evaluation(train, model.getCostMatrix());
        evalTest.evaluateModel(model, test);

        ModelIO.save(model, DEFAULT_MODEL_PATH);

        return new TrainReport(
                data.numInstances(),
                train.numInstances(),
                test.numInstances(),
                evalTrain.pctCorrect(),
                evalTest.pctCorrect(),
                DEFAULT_MODEL_PATH.getPath(),
                model.toString()
        );
    }

    private static CostMatrix defaultPassFailCostMatrix() {
        CostMatrix cm = new CostMatrix(2);
        cm.setElement(0, 0, 0.0);
        cm.setElement(1, 1, 0.0);
        cm.setElement(0, 1, 5.0); 
        cm.setElement(1, 0, 1.0); 
        return cm;
    }

    public record TrainReport(
            int totalInstances,
            int trainInstances,
            int testInstances,
            double trainAccuracyPct,
            double testAccuracyPct,
            String modelPath,
            String treeText
    ) {
        public String toHumanString() {
            return String.format(
                    Locale.US,
                    """
                    Training completed.
                    - Instances: total=%d, train=%d, test=%d
                    - Accuracy: train=%.2f%%, test=%.2f%%
                    - Model saved to: %s

                    J48 tree:
                    %s
                    """,
                    totalInstances, trainInstances, testInstances,
                    trainAccuracyPct, testAccuracyPct,
                    modelPath,
                    treeText
            );
        }
    }
}

