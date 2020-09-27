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
public class CalcDLQL extends JDialog {
    // Controls
    private JTextField _txtN   = new JTextField();
    private JTextField _txtP   = new JTextField();
    private JTextField _txtSD  = new JTextField();
    private JLabel     _lblT   = new JLabel();
    private JLabel     _lblLOD = new JLabel();
    private JLabel     _lblLOQ = new JLabel();

    private boolean useVariance = false;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Update result
    private void _updateResult()
    {
        int    n    = GUtil.str2i(_txtN .getText());
        int    p    = GUtil.str2i(_txtP .getText());
        double sd   = GUtil.str2d(_txtSD.getText());

        double t1   = DistTable.t1(p, n - 1);
        double lod  = t1 * sd;
        double loq  = 3.0 * lod;

        _lblT  .setText(String.format("%.5f", t1 ));
        _lblLOD.setText(String.format("%.5f", lod));
        _lblLOQ.setText(String.format("%.5f", loq));
    }

    // Private constructor
    private CalcDLQL(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(6, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel lblN      = new JLabel(_S("tb_dlql_n"));
            JLabel lblP      = new JLabel(_S("tb_dlql_p"));
            JLabel lblSD     = new JLabel(_S("tb_dlql_sd"));
            JLabel lblT      = new JLabel(_S("tb_dlql_t"));
            JLabel lblLOD    = new JLabel(_S("tb_dlql_lod"));
            JLabel lblLOQ    = new JLabel(_S("tb_dlql_loq"));
            ipMainPanel.add(lblN   ); ipMainPanel.add(_txtN  );
            ipMainPanel.add(lblP   ); ipMainPanel.add(_txtP  );
            ipMainPanel.add(lblSD  ); ipMainPanel.add(_txtSD );
            ipMainPanel.add(lblT   ); ipMainPanel.add(_lblT  );
            ipMainPanel.add(lblLOD ); ipMainPanel.add(_lblLOD);
            ipMainPanel.add(lblLOQ ); ipMainPanel.add(_lblLOQ);
            lblN    .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblP    .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblSD   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblT    .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblLOD  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblLOQ  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            _txtN   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtP   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtSD  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblT   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblLOD .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblLOQ .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtN   .setDocument(new NumericDocument(0, false));
            _txtP   .setDocument(new NumericDocument(0, false));
            _txtSD  .setDocument(new NumericDocument(5, false));
            _txtN   .setText("0"  );
            _txtP   .setText("95" );
            _txtSD  .setText("0.0");
            GUtil.disableCutAndPasteOnTextField(_txtN);
            GUtil.disableCutAndPasteOnTextField(_txtP);
            GUtil.disableCutAndPasteOnTextField(_txtSD);
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
        _txtN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtN.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtP.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtP.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtSD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtSD.addFocusListener(new FocusListener() {
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
        CalcDLQL rb = new CalcDLQL(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_dlql_title"), JRootPane.PLAIN_DIALOG);
    }
}
