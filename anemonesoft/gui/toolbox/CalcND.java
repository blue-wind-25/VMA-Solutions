/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.toolbox;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.gui.dialog.*;
import anemonesoft.i18n.*;
import anemonesoft.stat.*;

//
// A toolbox for calculating Process/Method Capability
//
public class CalcND extends JDialog {
    // Controls
    private JTextField _txtMean  = new JTextField();
    private JTextField _txtSD    = new JTextField();
    private JTextField _txtVar   = new JTextField();
    private JTextField _txtMUc   = new JTextField();
    private JTextField _txtLower = new JTextField();
    private JTextField _txtUpper = new JTextField();
    private JLabel     _lblProb  = new JLabel();

    private boolean useVariance = false;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Calculation helper function
    private double normdist(double X, double mean, double sigma)
    {
        double res = 0.0;

        final double x = (X - mean) / sigma;

        if(x == 0.0)
            res = 0.5;
        else {
            double oor2pi = 1.0 / (Math.sqrt(2.0 * Math.PI));
            double t      = 1.0 / (1.0 + 0.2316419 * Math.abs(x));
            t *= oor2pi * Math.exp(-0.5 * x * x) * (0.31938153 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))));
            if(x >= 0.0) res = 1 - t;
            else         res = t;
        }
        return res;
    }

    // Update result
    private void _updateResult()
    {
        double mean  = GUtil.str2d(_txtMean .getText());
        double sd    = GUtil.str2d(_txtSD   .getText());
        double var   = GUtil.str2d(_txtVar  .getText());
        double muc   = GUtil.str2d(_txtMUc  .getText());
        double lower = GUtil.str2d(_txtLower.getText());
        double upper = GUtil.str2d(_txtUpper.getText());

        if(useVariance) {
            sd = Math.sqrt(var);
            _txtSD.setText(String.format("%.3f", sd));
        }
        else {
            var = sd * sd;
            _txtVar.setText(String.format("%.3f", var));
        }

        double r = normdist(upper - muc, mean, sd) - normdist(lower + muc, mean, sd);

        _lblProb.setText(String.format("%.5f", r));
    }

    // Private constructor
    private CalcND(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(7, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel lblMean   = new JLabel(_S("tb_nd_mean"));
            JLabel lblSD     = new JLabel(_S("tb_nd_sd"));
            JLabel lblVar    = new JLabel(_S("tb_nd_var"));
            JLabel lblMUc    = new JLabel(_S("tb_nd_muc"));
            JLabel lblLower  = new JLabel(_S("tb_nd_lower"));
            JLabel lblUpper  = new JLabel(_S("tb_nd_upper"));
            JLabel lblProb   = new JLabel(_S("tb_nd_prob"));
            ipMainPanel.add(lblMean  ); ipMainPanel.add(_txtMean );
            ipMainPanel.add(lblSD    ); ipMainPanel.add(_txtSD   );
            ipMainPanel.add(lblVar   ); ipMainPanel.add(_txtVar  );
            ipMainPanel.add(lblMUc   ); ipMainPanel.add(_txtMUc  );
            ipMainPanel.add(lblUpper ); ipMainPanel.add(_txtUpper);
            ipMainPanel.add(lblLower ); ipMainPanel.add(_txtLower);
            ipMainPanel.add(lblProb  ); ipMainPanel.add(_lblProb );
            lblMean  .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            lblSD    .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            lblVar   .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            lblMUc   .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            lblLower .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            lblUpper .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            lblProb  .setPreferredSize(new Dimension(350, GUtil.DEFAULT_BOX_HEIGHT));
            _txtMean .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtSD   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtMUc  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtUpper.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtLower.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblProb .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtMean .setDocument(new NumericDocument(3, false));
            _txtSD   .setDocument(new NumericDocument(3, false));
            _txtVar  .setDocument(new NumericDocument(3, false));
            _txtMUc  .setDocument(new NumericDocument(3, false));
            _txtUpper.setDocument(new NumericDocument(3, false));
            _txtLower.setDocument(new NumericDocument(3, false));
            _txtMean .setText("0.0");
            _txtSD   .setText("0.0");
            _txtVar  .setText("0.0");
            _txtMUc  .setText("0.0");
            _txtLower.setText("0.0");
            _txtUpper.setText("0.0");
            GUtil.disableCutAndPasteOnTextField(_txtMean);
            GUtil.disableCutAndPasteOnTextField(_txtSD);
            GUtil.disableCutAndPasteOnTextField(_txtMUc);
            GUtil.disableCutAndPasteOnTextField(_txtUpper);
            GUtil.disableCutAndPasteOnTextField(_txtLower);
        _updateResult();

        // Create the dialog
        JPanel dialogPanel = new JPanel(new BorderLayout(), true);
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            dialogPanel.add(ipMainPanel, BorderLayout.CENTER);
        add(dialogPanel);

        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension parentSize = parentFrame.getSize();
        Point     parentPos  = parentFrame.getLocation();
        setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);

        // Initialize event handlers
        _txtMean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtMean.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtSD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                useVariance = false;
                _updateResult();
            }
        });
        _txtSD.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                useVariance = false;
                _updateResult();
            }
        });
        _txtVar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                useVariance = true;
                _updateResult();
            }
        });
        _txtVar.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                useVariance = true;
                _updateResult();
            }
        });
        _txtMUc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtMUc.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtLower.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtLower.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtUpper.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtUpper.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });

        // Allow the dialog to be closed by pressing escape
        final JDialog _THIS = this;
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            { _THIS.setVisible(false); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // Show a the toolbox
    public static void showToolbox(Frame parentFrame)
    {
        CalcND rb = new CalcND(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_nd_title"), JRootPane.PLAIN_DIALOG);
    }
}
