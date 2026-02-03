## Student Performance Prediction System (Explainable ML)

This project predicts **student performance (PASS/FAIL)** from:
- **weekly_self_study_hours**
- **attendance_percentage**
- **class_participation**

It uses **Java + Weka** and a **J48 Decision Tree**.  
For every prediction, it shows a **human-readable English explanation** by extracting the **decision path** (the matching tree conditions) from the trained J48 model.

### Project architecture (matches the report)
- **Data Layer**: `src/main/resources/org/student_performance.csv`
- **Machine Learning Layer**: `edu.spp.ml.TrainModel` (J48 training in code)
- **Explanation Layer**: `edu.spp.explain.J48Explainer` (tree-path explanation)
- **Presentation Layer**: `edu.spp.app.SwingApp` (desktop UI)

### Dataset
Expected CSV columns (as in the provided dataset):
`student_id, weekly_self_study_hours, attendance_percentage, class_participation, total_score, grade`

**Target label (PASS/FAIL)** is created inside Java code:
- **PASS** if `total_score >= 55` (grade A/B/C)
- **FAIL** otherwise (grade D/F)

### How to run after cloning the project (Windows / any OS with Java)

#### Option A (recommended here): Run without Maven (uses the bundled `lib/weka.jar`)

##### 1) Train the model

On **Git Bash / Linux / macOS**:

```bash
bash train.sh
```

On **Windows CMD**:

```bat
train.cmd
```

This creates the model file:
- `model/student_j48_passfail.model`

##### 2) Run the UI

On **Git Bash / Linux / macOS**:

```bash
bash run.sh
```

On **Windows CMD**:

```bat
run.cmd
```

#### Option B: Run with Maven (if you have Maven installed)

##### 1) Train the model

```bash
mvn -q -DskipTests exec:java -Dexec.mainClass=edu.spp.ml.TrainModel
```

##### 2) Run the UI

```bash
mvn -q -DskipTests exec:java
```

In the UI:
- click **Train Model** (once)
- then enter a student’s values and click **Predict**

### Notes
- The explanation is **local** (per prediction) and is based on the **actual decision-tree conditions** that matched the input.
- If you replace the dataset file, re-train the model.

