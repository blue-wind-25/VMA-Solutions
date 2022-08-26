/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
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
// A toolbox for Student-t table
//
public class StudentTTable extends JDialog {
    // Controls
    private JTextField _txtStatConfidence = new JTextField();
    private JTextField _txtDegOfFreedom   = new JTextField();
    private JLabel     _lblOneTailed      = new JLabel();
    private JLabel     _lblTwoTailed      = new JLabel();

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Update result
    private void _updateResult()
    {
        double pp  = GUtil.str2d(_txtStatConfidence.getText());
        double dof = GUtil.str2d(_txtDegOfFreedom  .getText());

        double ot  = DistTable.ft1(pp, dof);
        double tt  = DistTable.ft2(pp, dof);

        _lblOneTailed.setText(String.format("%.5f", ot));
        _lblTwoTailed.setText(String.format("%.5f", tt));
    }

    // Private constructor
    private StudentTTable(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(4, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JLabel ipLabelSC  = new JLabel(_S("tb_stt_stat_conf"));
                JLabel ipLabelDoF = new JLabel(_S("tb_stt_def_of_free"));
                JLabel ipLabelOT  = new JLabel(_S("tb_stt_one_tailed"));
                JLabel ipLabelTT  = new JLabel(_S("tb_stt_two_tailed"));
                ipMainPanel.add(ipLabelSC ); ipMainPanel.add(_txtStatConfidence);
                ipMainPanel.add(ipLabelDoF); ipMainPanel.add(_txtDegOfFreedom);
                ipMainPanel.add(ipLabelOT ); ipMainPanel.add(_lblOneTailed);
                ipMainPanel.add(ipLabelTT ); ipMainPanel.add(_lblTwoTailed);
                ipLabelSC         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelDoF        .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelOT         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelTT         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                _txtStatConfidence.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtDegOfFreedom  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblOneTailed     .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblTwoTailed     .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtStatConfidence.setDocument(new NumericDocument(5, false));
                _txtDegOfFreedom  .setDocument(new NumericDocument(5, false));
                _txtStatConfidence.setText("95.0");
                _txtDegOfFreedom  .setText("10.0");
                GUtil.disableCutAndPasteOnTextField(_txtStatConfidence);
                GUtil.disableCutAndPasteOnTextField(_txtDegOfFreedom);
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
        _txtStatConfidence.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtStatConfidence.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtDegOfFreedom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtDegOfFreedom.addFocusListener(new FocusListener() {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show a the toolbox
    public static void showToolbox(Frame parentFrame)
    {
        StudentTTable rb = new StudentTTable(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_stt_title"), JRootPane.PLAIN_DIALOG);
    }
}
