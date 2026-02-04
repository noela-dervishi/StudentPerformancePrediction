## Student Performance Prediction System (Explainable ML)

This project predicts **student performance (PASS/FAIL)** from:
- **weekly_self_study_hours**
- **attendance_percentage**
- **class_participation**

It uses **Java + Weka** and a **J48 Decision Tree**.  
For every prediction, it shows a **human-readable English explanation** by extracting the **decision path** (the matching tree conditions) from the trained J48 model.  
The desktop UI is built with **Java Swing** and includes a modern layout plus a **light/dark mode** toggle.

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

### Requirements
- **Java JDK 11+** on your `PATH` (for `javac` and `java`)
- No manual Weka setup needed – **`lib/weka.jar` is bundled** and used by the helper scripts
- (Optional) **Maven 3+** if you prefer running via Maven instead of the provided scripts

### How to run after cloning the project (Windows / Linux / macOS)

#### Option A (recommended): Run without Maven (uses the bundled `lib/weka.jar`)

##### 1) Train the model

On **Git Bash / Linux / macOS**:

```bash
bash train.sh
```

On **Windows CMD / PowerShell** (from the project root):

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

On **Windows CMD / PowerShell** (from the project root):

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
- click **Train Model** once (loads data, trains J48, saves the model)
- then enter a student’s values and click **Predict**
- the **PASS/FAIL badge** and the **English rule-path explanation** will update for each prediction

### Notes
- The explanation is **local** (per prediction) and is based on the **actual decision-tree conditions** that matched the input.
- If you replace the dataset file (`src/main/resources/org/student_performance.csv`), re-train the model before predicting.

