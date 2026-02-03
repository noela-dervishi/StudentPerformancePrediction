package edu.spp.ml;

import weka.classifiers.Classifier;

import java.io.*;

public final class ModelIO {
    private ModelIO() {}

    public static void save(Classifier model, File file) throws Exception {
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(model);
        }
    }

    public static Classifier load(File file) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return (Classifier) ois.readObject();
        }
    }
}

