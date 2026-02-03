package edu.spp.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.spp.ml.TrainModel;
import edu.spp.predict.PredictionResult;
import edu.spp.predict.Predictor;
import edu.spp.predict.StudentInput;

public final class SwingApp {

    private final JSpinner studentIdSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10_000_000, 1));
    private final JSlider studyHoursSlider = new JSlider(0, 40, 15);
    private final JSlider attendanceSlider = new JSlider(50, 100, 85);
    private final JSlider participationSlider = new JSlider(0, 10, 6);

    private final JLabel badgeLabel = new JLabel("—");
    private final JTextArea explanationArea = new JTextArea(14, 70);
    private final JTextArea logArea = new JTextArea(6, 70);

    private final JToggleButton themeToggle = new JToggleButton("Dark mode");
    private Theme currentTheme = Theme.LIGHT;

    private JPanel rootPanel;
    private JFrame frameRef;
    private JLabel footerLabel;
    private JLabel subtitleLabel;

    private JButton trainButton;
    private JButton predictButton;
    private JButton clearButton;

    private Predictor predictor;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            new SwingApp().start();
        });
    }

    private void start() {
        frameRef = new JFrame("Student Performance Prediction System (Explainable ML)");
        frameRef.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frameRef.setLayout(new BorderLayout());

        rootPanel = new JPanel(new BorderLayout(16, 16));
        rootPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        rootPanel.add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.42;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        center.add(buildInputCard(frameRef), c);

        c.gridx = 1;
        c.weightx = 0.58;
        c.insets = new Insets(0, 16, 0, 0);
        center.add(buildOutputCard(), c);

        rootPanel.add(center, BorderLayout.CENTER);
        rootPanel.add(buildFooter(), BorderLayout.SOUTH);

        frameRef.setContentPane(rootPanel);

        frameRef.pack();
        frameRef.setLocationRelativeTo(null);
        frameRef.setVisible(true);

        applyTheme(Theme.LIGHT);
        log("Ready. Tip: click \"Train Model\" once, then \"Predict\".");
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(12, 6));
        panel.setOpaque(false);

        JLabel title = new JLabel("Student Performance Prediction System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        subtitleLabel = new JLabel("Explainable ML with a J48 decision tree (Pass/Fail + rule-path explanation)");
        subtitleLabel.setForeground(new Color(85, 85, 85));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitleLabel);

        badgeLabel.setOpaque(true);
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setBorder(new EmptyBorder(8, 14, 8, 14));
        badgeLabel.setFont(badgeLabel.getFont().deriveFont(Font.BOLD, 14f));
        setBadgeNeutral();

        themeToggle.setFocusPainted(false);
        themeToggle.addActionListener(e -> applyTheme(themeToggle.isSelected() ? Theme.DARK : Theme.LIGHT));
        themeToggle.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(themeToggle);
        right.add(badgeLabel);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JComponent buildInputCard(JFrame frame) {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(12, 12));

        JLabel h = sectionTitle("Student inputs");
        card.add(h, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        styleSlider(studyHoursSlider, 0, 40, 5);
        styleSlider(attendanceSlider, 50, 100, 10);
        styleSlider(participationSlider, 0, 10, 1);

        form.add(labeled("Student ID", studentIdSpinner), c); c.gridy++;
        form.add(labeled("Weekly self-study hours", sliderRow(studyHoursSlider, "hours/week")), c); c.gridy++;
        form.add(labeled("Attendance", sliderRow(attendanceSlider, "%")), c); c.gridy++;
        form.add(labeled("Class participation", sliderRow(participationSlider, "/10")), c); c.gridy++;

        card.add(form, BorderLayout.CENTER);
        card.add(buildButtonsPanel(frame), BorderLayout.SOUTH);
        return card;
    }

    private static JComponent labeled(String title, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setForeground(new Color(70, 70, 70));
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildButtonsPanel(JFrame frame) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.setPreferredSize(new Dimension(10, 52));

        trainButton = primaryButton("Train Model");
        predictButton = primaryButton("Predict");
        clearButton = subtleButton("Clear");

        trainButton.addActionListener(e -> runInBackground(frame, "Training model...", () -> {
            TrainModel.TrainReport report = TrainModel.trainAndSaveDefaultModel();
            log(report.toHumanString().trim());
            predictor = null; // reload model on next prediction
        }));

        predictButton.addActionListener(e -> runInBackground(frame, "Predicting...", () -> {
            StudentInput input = parseInput();
            PredictionResult result = getPredictor().predict(input);
            showResult(result);
            log(String.format(Locale.US, "Predicted %s for student %d (%.0f%% confidence).",
                    result.predictedLabel(), input.studentId(), result.confidence() * 100.0));
        }));

        clearButton.addActionListener(e -> {
            explanationArea.setText("");
            logArea.setText("");
            setBadgeNeutral();
        });

        panel.add(trainButton);
        panel.add(predictButton);
        panel.add(clearButton);

        return panel;
    }

    private JComponent buildOutputCard() {
        JPanel card = cardPanel();
        card.setLayout(new BorderLayout(12, 12));

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        JLabel h = sectionTitle("Prediction result");
        top.add(h, BorderLayout.WEST);

        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logArea.setForeground(new Color(85, 85, 85));

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        body.add(new JScrollPane(explanationArea), BorderLayout.CENTER);

        JPanel logs = new JPanel(new BorderLayout(6, 6));
        logs.setOpaque(false);
        JLabel logTitle = new JLabel("Activity");
        logTitle.setForeground(new Color(70, 70, 70));
        logs.add(logTitle, BorderLayout.NORTH);
        logs.add(new JScrollPane(logArea), BorderLayout.CENTER);

        body.add(logs, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JComponent buildFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        footerLabel = new JLabel("Dataset: src/main/resources/org/student_performance.csv  |  Model: model/student_j48_passfail.model");
        footerLabel.setForeground(new Color(90, 90, 90));
        panel.add(footerLabel, BorderLayout.WEST);
        return panel;
    }

    private StudentInput parseInput() {
        int id = ((Number) studentIdSpinner.getValue()).intValue();
        double study = studyHoursSlider.getValue();
        double att = attendanceSlider.getValue();
        double part = participationSlider.getValue();
        return new StudentInput(id, study, att, part);
    }

    private Predictor getPredictor() throws Exception {
        if (predictor == null) predictor = Predictor.loadDefault();
        return predictor;
    }

    private void showResult(PredictionResult result) {
        SwingUtilities.invokeLater(() -> {
            explanationArea.setText(result.explanation());
            explanationArea.setCaretPosition(0);

            if ("PASS".equalsIgnoreCase(result.predictedLabel())) setBadgePass();
            else if ("FAIL".equalsIgnoreCase(result.predictedLabel())) setBadgeFail();
            else setBadgeNeutral();
        });
    }

    private void log(String text) {
        SwingUtilities.invokeLater(() -> {
            if (!logArea.getText().isEmpty()) logArea.append("\n");
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private static void runInBackground(JFrame frame, String title, ThrowingRunnable work) {
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        JLabel label = new JLabel("Please wait...");
        label.setBorder(new EmptyBorder(12, 12, 12, 12));
        dialog.add(label, BorderLayout.CENTER);
        dialog.setSize(220, 100);
        dialog.setLocationRelativeTo(frame);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception { work.run(); return null; }
            @Override protected void done() {
                dialog.dispose();
                try {
                    get();
                } catch (Exception ex) {
                    Throwable root = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(frame, root.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        dialog.setVisible(true);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    // --- Styling helpers ---

    private static void installLookAndFeel() {
        try {
            boolean applied = false;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    applied = true;
                    break;
                }
            }
            if (!applied) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception ignored) {
        }

        UIManager.put("control", new Color(250, 251, 252));
        UIManager.put("info", new Color(250, 251, 252));
        UIManager.put("nimbusBase", new Color(99, 102, 241));
        UIManager.put("nimbusBlueGrey", new Color(203, 213, 225));
        UIManager.put("text", new Color(15, 23, 42));
        UIManager.put("nimbusFocus", new Color(165, 180, 252));
        UIManager.put("Button.background", new Color(99, 102, 241));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.opaque", Boolean.TRUE);
        UIManager.put("Button.contentAreaFilled", Boolean.TRUE);
    }

    private static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        p.setBackground(Color.WHITE);
        return p;
    }

    private static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        return l;
    }

    // (button creation moved to instance methods so colors adapt to the current theme)

    private static void styleSlider(JSlider s, int min, int max, int majorTick) {
        s.setMinimum(min);
        s.setMaximum(max);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setMajorTickSpacing(majorTick);
        s.setMinorTickSpacing(Math.max(1, majorTick / 2));
        s.setOpaque(false);
    }

    private static JComponent sliderRow(JSlider slider, String suffix) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel v = new JLabel(slider.getValue() + " " + suffix);
        v.setForeground(new Color(70, 70, 70));
        slider.addChangeListener(e -> v.setText(slider.getValue() + " " + suffix));
        p.add(slider, BorderLayout.CENTER);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private void setBadgeNeutral() {
        badgeLabel.setText("No prediction");
        badgeLabel.setBackground(currentTheme.badgeNeutralBg);
        badgeLabel.setForeground(currentTheme.badgeNeutralFg);
    }

    private void setBadgePass() {
        badgeLabel.setText("PASS");
        badgeLabel.setBackground(currentTheme.badgePassBg);
        badgeLabel.setForeground(currentTheme.badgePassFg);
    }

    private void setBadgeFail() {
        badgeLabel.setText("FAIL");
        badgeLabel.setBackground(currentTheme.badgeFailBg);
        badgeLabel.setForeground(currentTheme.badgeFailFg);
    }

    private void applyTheme(Theme theme) {
        this.currentTheme = theme;

        // Progress bar text colors: selectionForeground is text over the filled part,
        // selectionBackground is text over the unfilled part.
        UIManager.put("ProgressBar.selectionForeground", theme.progressTextOnFill);
        UIManager.put("ProgressBar.selectionBackground", theme.progressTextOnTrack);

        if (rootPanel != null) {
            rootPanel.setBackground(theme.appBg);
            applyThemeRecursive(rootPanel, theme);
            if (frameRef != null) SwingUtilities.updateComponentTreeUI(frameRef);
            rootPanel.revalidate();
            rootPanel.repaint();
        }

        // Theme-aware buttons
        if (trainButton != null) stylePrimaryButton(trainButton, theme);
        if (predictButton != null) stylePrimaryButton(predictButton, theme);
        if (clearButton != null) styleSubtleButton(clearButton, theme);
        if (themeToggle != null) {
            themeToggle.setBackground(theme.toggleBg);
            themeToggle.setForeground(theme.textPrimary);
        }
        if (subtitleLabel != null) subtitleLabel.setForeground(theme.textSecondary);
        if (footerLabel != null) footerLabel.setForeground(theme.textSecondary);

        // Update badge colors for current state
        String badge = badgeLabel.getText();
        if ("PASS".equalsIgnoreCase(badge)) setBadgePass();
        else if ("FAIL".equalsIgnoreCase(badge)) setBadgeFail();
        else setBadgeNeutral();
    }

    private static void applyThemeRecursive(Component comp, Theme t) {
        if (comp instanceof JPanel p) {
            if (p.getBorder() instanceof javax.swing.border.CompoundBorder) {
                // Our "card"
                p.setBackground(t.cardBg);
                p.setForeground(t.textPrimary);
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(t.border, 1, true),
                        new EmptyBorder(14, 14, 14, 14)
                ));
            } else if (p.isOpaque()) {
                p.setBackground(t.appBg);
                p.setForeground(t.textPrimary);
            }
        } else if (comp instanceof JLabel l) {
            l.setForeground(t.textPrimary);
        } else if (comp instanceof JTextArea ta) {
            ta.setBackground(t.inputBg);
            ta.setForeground(t.textPrimary);
            ta.setCaretColor(t.textPrimary);
            ta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(t.border, 1, true),
                    new EmptyBorder(10, 10, 10, 10)
            ));
        } else if (comp instanceof JScrollPane sp) {
            sp.getViewport().setBackground(t.inputBg);
            sp.setBorder(BorderFactory.createLineBorder(t.border, 1, true));
        } else if (comp instanceof JSlider s) {
            s.setForeground(t.textSecondary);
            s.setBackground(t.cardBg);
        } else if (comp instanceof JSpinner sp) {
            sp.setBackground(t.inputBg);
        } else if (comp instanceof JButton) {
            // Keep our custom styled buttons
        } else if (comp instanceof JToggleButton tb) {
            tb.setOpaque(true);
            tb.setBackground(t.toggleBg);
            tb.setForeground(t.textPrimary);
            tb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(t.border, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
        } else if (comp instanceof JProgressBar pb) {
            pb.setBackground(t.progressTrack);
            pb.setForeground(UIManager.getColor("ProgressBar.foreground"));
            pb.setBorder(BorderFactory.createLineBorder(t.border, 1, true));
        }

        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyThemeRecursive(child, t);
            }
        }
    }

    private enum Theme {
        LIGHT(
                new Color(248, 250, 252),  // app bg - soft slate blue-gray
                Color.WHITE,               // card bg
                new Color(15, 23, 42),     // primary text - slate 900
                new Color(100, 116, 139),  // secondary text - slate 500
                new Color(226, 232, 240),  // border - slate 200
                new Color(255, 255, 255),  // input bg
                new Color(241, 245, 249),  // badge neutral bg - slate 100
                new Color(51, 65, 85),     // badge neutral fg - slate 700
                new Color(220, 252, 231),  // badge pass bg - green 100
                new Color(21, 128, 61),    // badge pass fg - green 700
                new Color(254, 226, 226),  // badge fail bg - red 100
                new Color(185, 28, 28),    // badge fail fg - red 700
                new Color(241, 245, 249),  // progress track - slate 100
                Color.WHITE,               // progress text on fill
                new Color(15, 23, 42),     // progress text on track - slate 900
                new Color(226, 232, 240),  // button border - slate 200
                new Color(99, 102, 241),   // primary button bg - indigo 500
                new Color(241, 245, 249)   // toggle bg - slate 100
        ),
        DARK(
                new Color(15, 23, 42),     // app bg - slate 900
                new Color(30, 41, 59),     // card bg - slate 800
                new Color(241, 245, 249),  // primary text - slate 100
                new Color(148, 163, 184),  // secondary text - slate 400
                new Color(51, 65, 85),     // border - slate 700
                new Color(30, 41, 59),     // input bg - slate 800
                new Color(51, 65, 85),     // badge neutral bg - slate 700
                new Color(226, 232, 240),  // badge neutral fg - slate 200
                new Color(20, 83, 45),     // badge pass bg - green 900
                new Color(134, 239, 172),  // badge pass fg - green 300
                new Color(127, 29, 29),    // badge fail bg - red 900
                new Color(252, 165, 165),  // badge fail fg - red 300
                new Color(51, 65, 85),     // progress track - slate 700
                new Color(15, 23, 42),     // progress text on fill - slate 900
                Color.WHITE,               // progress text on track
                new Color(51, 65, 85),     // button border - slate 700
                new Color(129, 140, 248),  // primary button bg - indigo 400
                new Color(51, 65, 85)      // toggle bg - slate 700
        );

        final Color appBg;
        final Color cardBg;
        final Color textPrimary;
        final Color textSecondary;
        final Color border;
        final Color inputBg;

        final Color badgeNeutralBg;
        final Color badgeNeutralFg;
        final Color badgePassBg;
        final Color badgePassFg;
        final Color badgeFailBg;
        final Color badgeFailFg;

        final Color progressTrack;
        final Color progressTextOnFill;
        final Color progressTextOnTrack;

        final Color buttonBorder;
        final Color buttonPrimaryBg;
        final Color toggleBg;

        Theme(Color appBg,
              Color cardBg,
              Color textPrimary,
              Color textSecondary,
              Color border,
              Color inputBg,
              Color badgeNeutralBg,
              Color badgeNeutralFg,
              Color badgePassBg,
              Color badgePassFg,
              Color badgeFailBg,
              Color badgeFailFg,
              Color progressTrack,
              Color progressTextOnFill,
              Color progressTextOnTrack,
              Color buttonBorder,
              Color buttonPrimaryBg,
              Color toggleBg
        ) {
            this.appBg = appBg;
            this.cardBg = cardBg;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.border = border;
            this.inputBg = inputBg;
            this.badgeNeutralBg = badgeNeutralBg;
            this.badgeNeutralFg = badgeNeutralFg;
            this.badgePassBg = badgePassBg;
            this.badgePassFg = badgePassFg;
            this.badgeFailBg = badgeFailBg;
            this.badgeFailFg = badgeFailFg;
            this.progressTrack = progressTrack;
            this.progressTextOnFill = progressTextOnFill;
            this.progressTextOnTrack = progressTextOnTrack;
            this.buttonBorder = buttonBorder;
            this.buttonPrimaryBg = buttonPrimaryBg;
            this.toggleBg = toggleBg;
        }
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        stylePrimaryButton(b, currentTheme);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentTheme.buttonPrimaryBg, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return b;
    }

    private JButton subtleButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        styleSubtleButton(b, currentTheme);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(currentTheme.buttonBorder, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        return b;
    }

    private static void stylePrimaryButton(JButton b, Theme t) {
        b.setBackground(t.buttonPrimaryBg);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.buttonPrimaryBg, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private static void styleSubtleButton(JButton b, Theme t) {
        b.setBackground(t.toggleBg);
        b.setForeground(t.textPrimary);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.buttonBorder, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }
}