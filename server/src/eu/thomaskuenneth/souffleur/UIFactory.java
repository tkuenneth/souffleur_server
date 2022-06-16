package eu.thomaskuenneth.souffleur;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;

public class UIFactory {

    public static JFormattedTextField createIntegerField(int min, int max) {
        DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(min);
        formatter.setMaximum(max);
        formatter.setAllowsInvalid(true);
        formatter.setCommitsOnValidEdit(true);
        return new JFormattedTextField(formatter);
    }

    public static JPanel createFlowPanel() {
        FlowLayout layout = new FlowLayout(FlowLayout.LEADING, 16, 8);
        layout.setAlignOnBaseline(true);
        return new JPanel(layout);
    }
}
