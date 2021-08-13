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
// A toolbox for Grubss-C table
//
public class GrubbsCTable extends JDialog {
    // Controls
    private JTextField _txtStatConfidence = new JTextField();
    private JTextField _txtNumOfSamples   = new JTextField();
    private JLabel     _lblCriticalValue  = new JLabel();

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Update result
    private void _updateResult()
    {
        double pp = GUtil.str2d(_txtStatConfidence.getText());
        int    n  = GUtil.str2i(_txtNumOfSamples  .getText());

        double gc = DistTable.grubbs_gc(pp, n);

        _lblCriticalValue.setText(String.format("%.5f", gc));
    }

    // Private constructor
    private GrubbsCTable(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(3, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JLabel ipLabelSC  = new JLabel(_S("tb_gtc_stat_conf"));
                JLabel ipLabelDoF = new JLabel(_S("tb_gtc_num_samples"));
                JLabel ipLabelOT  = new JLabel(_S("tb_gtc_crit_val"));
                ipMainPanel.add(ipLabelSC ); ipMainPanel.add(_txtStatConfidence);
                ipMainPanel.add(ipLabelDoF); ipMainPanel.add(_txtNumOfSamples);
                ipMainPanel.add(ipLabelOT ); ipMainPanel.add(_lblCriticalValue);
                ipLabelSC         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelDoF        .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                ipLabelOT         .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
                _txtStatConfidence.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtNumOfSamples  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _lblCriticalValue .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                _txtStatConfidence.setDocument(new NumericDocument(5, false));
                _txtNumOfSamples  .setDocument(new NumericDocument(0, false));
                _txtStatConfidence.setText("95.0");
                _txtNumOfSamples  .setText("10.0");
                GUtil.disableCutAndPasteOnTextField(_txtStatConfidence);
                GUtil.disableCutAndPasteOnTextField(_txtNumOfSamples);
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
        _txtNumOfSamples.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtNumOfSamples.addFocusListener(new FocusListener() {
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
        GrubbsCTable rb = new GrubbsCTable(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_gtc_title"), JRootPane.PLAIN_DIALOG);
    }
}
