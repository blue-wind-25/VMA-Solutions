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
public class CalcPMC extends JDialog {
    // Controls
    private JTextField _txtMean = new JTextField();
    private JTextField _txtSD   = new JTextField();
    private JTextField _txtVar  = new JTextField();
    private JTextField _txtOSG  = new JTextField();
    private JTextField _txtUSG  = new JTextField();
    private JLabel     _lblCp   = new JLabel();
    private JLabel     _lblCpk1 = new JLabel();
    private JLabel     _lblCpk2 = new JLabel();
    private JLabel     _lblRes  = new JLabel();

    private boolean useVariance = false;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Update result
    private void _updateResult()
    {
        double mean = GUtil.str2d(_txtMean.getText());
        double sd   = GUtil.str2d(_txtSD  .getText());
        double var  = GUtil.str2d(_txtVar .getText());
        double osg  = GUtil.str2d(_txtOSG .getText());
        double usg  = GUtil.str2d(_txtUSG .getText());

        if(useVariance) {
            sd = Math.sqrt(var);
            _txtSD.setText(String.format("%.3f", sd));
        }
        else {
            var = sd * sd;
            _txtVar.setText(String.format("%.3f", var));
        }

        double cp   = (osg - usg)  / (6.0 * sd);
        double cpk1 = (mean - usg) / (3.0 * sd);
        double cpk2 = (osg - mean) / (3.0 * sd);

        double cpkm = cpk1;
        if(cpk2 < cpkm) cpkm = cpk2;

        _lblCp  .setText(String.format("%.5f", cp  ));
        _lblCpk1.setText(String.format("%.5f", cpk1));
        _lblCpk2.setText(String.format("%.5f", cpk2));

        if(cp > 1 && cpkm > 1) _lblRes.setText(_S("tb_pmc_result_ok"));
        else                   _lblRes.setText(_S("tb_pmc_result_nok"));
    }

    // Private constructor
    private CalcPMC(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(null, true);
            ipMainPanel.setLayout(new BoxLayout(ipMainPanel, BoxLayout.Y_AXIS));
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JPanel ipProcPanel = new JPanel(new GridLayout(8, 2, 5, 5), true);
                JLabel lblMean   = new JLabel(_S("tb_pmc_mean"));
                JLabel lblSD     = new JLabel(_S("tb_pmc_sd"));
                JLabel lblVar    = new JLabel(_S("tb_pmc_var"));
                JLabel lblOSG    = new JLabel(_S("tb_pmc_osg"));
                JLabel lblUSG    = new JLabel(_S("tb_pmc_usg"));
                JLabel lblCp     = new JLabel(_S("tb_pmc_cp"));
                JLabel lblCpk1   = new JLabel(_S("tb_pmc_cpk1"));
                JLabel lblCpk2   = new JLabel(_S("tb_pmc_cpk2"));
                ipProcPanel.add(lblMean); ipProcPanel.add(_txtMean);
                ipProcPanel.add(lblSD  ); ipProcPanel.add(_txtSD  );
                ipProcPanel.add(lblVar ); ipProcPanel.add(_txtVar );
                ipProcPanel.add(lblOSG ); ipProcPanel.add(_txtOSG );
                ipProcPanel.add(lblUSG ); ipProcPanel.add(_txtUSG );
                ipProcPanel.add(lblCp  ); ipProcPanel.add(_lblCp  );
                ipProcPanel.add(lblCpk1); ipProcPanel.add(_lblCpk1);
                ipProcPanel.add(lblCpk2); ipProcPanel.add(_lblCpk2);
                lblMean .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblSD   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblVar  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblOSG  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblUSG  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblCp   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblCpk1 .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                lblCpk2 .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                _txtMean.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtSD  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtVar .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtOSG .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtUSG .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblCp  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblCpk1.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblCpk2.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtMean.setDocument(new NumericDocument(3, false));
                _txtSD  .setDocument(new NumericDocument(3, false));
                _txtVar .setDocument(new NumericDocument(3, false));
                _txtOSG .setDocument(new NumericDocument(3, false));
                _txtUSG .setDocument(new NumericDocument(3, false));
                _txtMean.setText("0.0");
                _txtSD  .setText("0.0");
                _txtVar .setText("0.0");
                _txtOSG .setText("0.0");
                _txtUSG .setText("0.0");
                GUtil.disableCutAndPasteOnTextField(_txtMean);
                GUtil.disableCutAndPasteOnTextField(_txtSD);
                GUtil.disableCutAndPasteOnTextField(_txtOSG);
                GUtil.disableCutAndPasteOnTextField(_txtUSG);
            ipMainPanel.add(ipProcPanel);
            ipMainPanel.add(new JLabel(" ")); ipMainPanel.add(new JSeparator()); ipMainPanel.add(new JLabel(" "));
            ipMainPanel.add(_lblRes);
             ipProcPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            _lblRes     .setAlignmentX(Component.LEFT_ALIGNMENT);
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
        _txtOSG.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtOSG.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtUSG.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtUSG.addFocusListener(new FocusListener() {
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
        CalcPMC rb = new CalcPMC(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_pmc_title"), JRootPane.PLAIN_DIALOG);
    }
}
