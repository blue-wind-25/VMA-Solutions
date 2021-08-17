/*
    Copyright (C) 2010-2021 Aloysius Indrayanto
                            VMA Consultant
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
// A toolbox for calculating prediction interval
//
public class CalcPI extends JDialog {
    // Controls
    private JTextField _txtPP   = new JTextField();
    private JTextField _txtN    = new JTextField();
    private JTextField _txtMean = new JTextField();
    private JTextField _txtSD   = new JTextField();
    private JLabel     _lblCP   = new JLabel();
    private JLabel     _lblCPH  = new JLabel();
    private JLabel     _lblCPL  = new JLabel();

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Update result
    private void _updateResult()
    {
        double pp   = GUtil.str2d(_txtPP  .getText());
        int    n    = GUtil.str2i(_txtN   .getText());
        double mean = GUtil.str2d(_txtMean.getText());
        double sd   = GUtil.str2d(_txtSD  .getText());

        double t = DistTable.t2(pp, n - 1);
        double i = t * (sd * Math.sqrt(1.0 + 1.0 / n));

        double lb = mean - i;
        double ub = mean + i;

        _lblCP .setText(String.format("±%.5f", i ));
        _lblCPH.setText(String.format("%.5f",  ub));
        _lblCPL.setText(String.format("%.5f",  lb));
    }

    // Private constructor
    private CalcPI(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(7, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel lblPP   = new JLabel(_S("tb_cp_stat_conf"));
            JLabel lblN    = new JLabel(_S("tb_cp_n"));
            JLabel lblMean = new JLabel(_S("tb_cp_mean"));
            JLabel lblSD   = new JLabel(_S("tb_cp_sd"));
            JLabel lblCP   = new JLabel(_S("tb_cp_title_pi"));
            JLabel lblCPH  = new JLabel(_S("tb_cp_cp_h"));
            JLabel lblCPL  = new JLabel(_S("tb_cp_cp_l"));
            ipMainPanel.add(lblPP  ); ipMainPanel.add(_txtPP  );
            ipMainPanel.add(lblN   ); ipMainPanel.add(_txtN   );
            ipMainPanel.add(lblMean); ipMainPanel.add(_txtMean);
            ipMainPanel.add(lblSD  ); ipMainPanel.add(_txtSD  );
            ipMainPanel.add(lblCP  ); ipMainPanel.add(_lblCP  );
            ipMainPanel.add(lblCPH ); ipMainPanel.add(_lblCPH );
            ipMainPanel.add(lblCPL ); ipMainPanel.add(_lblCPL );
            lblPP   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblN    .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblMean .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblSD   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblCPH  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblCPL  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            _txtPP  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtN   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtMean.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtSD  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblCP  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblCPH .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblCPL .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtPP  .setDocument(new NumericDocument(5, false));
            _txtN   .setDocument(new NumericDocument(0, false));
            _txtMean.setDocument(new NumericDocument(3, false));
            _txtSD  .setDocument(new NumericDocument(3, false));
            _txtPP  .setText("90" );
            _txtN   .setText("0"  );
            _txtMean.setText("0.0");
            _txtSD  .setText("0.0");
            GUtil.disableCutAndPasteOnTextField(_txtPP);
            GUtil.disableCutAndPasteOnTextField(_txtN);
            GUtil.disableCutAndPasteOnTextField(_txtMean);
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
        _txtPP.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtPP.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
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
        CalcPI rb = new CalcPI(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_cp_title_pi"), JRootPane.PLAIN_DIALOG);
    }
}
