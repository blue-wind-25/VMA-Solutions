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
// A toolbox for Fisher-F table
//
public class FisherFTable extends JDialog {
    // Controls
    private JTextField _txtStatConfidence = new JTextField();
    private JTextField _txtDegOfFreedom1  = new JTextField();
    private JTextField _txtDegOfFreedom2  = new JTextField();
    private JLabel     _lblOneTailed      = new JLabel();

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Update result
    private void _updateResult()
    {
        double pp   = GUtil.str2d(_txtStatConfidence.getText());
        int    dof1 = GUtil.str2i(_txtDegOfFreedom1 .getText());
        int    dof2 = GUtil.str2i(_txtDegOfFreedom2 .getText());

        double ot = DistTable.F1(pp, dof1, dof2);

        _lblOneTailed.setText(String.format("%.5f", ot));
    }

    // Private constructor
    private FisherFTable(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(4, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JLabel ipLabelSC   = new JLabel(_S("tb_ft_stat_conf"));
                JLabel ipLabelDoF1 = new JLabel(_S("tb_ft_def_of_free1"));
                JLabel ipLabelDoF2 = new JLabel(_S("tb_ft_def_of_free2"));
                JLabel ipLabelOT   = new JLabel(_S("tb_ft_one_tailed"));
                ipMainPanel.add(ipLabelSC  ); ipMainPanel.add(_txtStatConfidence);
                ipMainPanel.add(ipLabelDoF1); ipMainPanel.add(_txtDegOfFreedom1);
                ipMainPanel.add(ipLabelDoF2); ipMainPanel.add(_txtDegOfFreedom2);
                ipMainPanel.add(ipLabelOT  ); ipMainPanel.add(_lblOneTailed);
                ipLabelSC         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelDoF1       .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelDoF2       .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelOT         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                _txtStatConfidence.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtDegOfFreedom1 .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtDegOfFreedom2 .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblOneTailed     .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtStatConfidence.setDocument(new NumericDocument(3, false));
                _txtDegOfFreedom1 .setDocument(new NumericDocument(0, false));
                _txtDegOfFreedom2 .setDocument(new NumericDocument(0, false));
                _txtStatConfidence.setText("95.0");
                _txtDegOfFreedom1 .setText("10");
                _txtDegOfFreedom2 .setText("10");
                GUtil.disableCutAndPasteOnTextField(_txtStatConfidence);
                GUtil.disableCutAndPasteOnTextField(_txtDegOfFreedom1);
                GUtil.disableCutAndPasteOnTextField(_txtDegOfFreedom2);
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
        _txtDegOfFreedom1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtDegOfFreedom1.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtDegOfFreedom2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtDegOfFreedom2.addFocusListener(new FocusListener() {
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
        FisherFTable rb = new FisherFTable(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_ft_title"), JRootPane.PLAIN_DIALOG);
    }
}
