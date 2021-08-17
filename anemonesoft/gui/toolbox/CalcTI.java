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
// A toolbox for calculating tolerance interval
//
public class CalcTI extends JDialog {
    // Controls
    private JTextField _txtN    = new JTextField();
    private JTextField _txtMean = new JTextField();
    private JTextField _txtSD   = new JTextField();
    private JTextField _txtVar  = new JTextField();
    private JTextField _txtCL   = new JTextField();
    private JTextField _txtCov  = new JTextField();
    private JLabel     _lblKf   = new JLabel();
    private JLabel     _lblTIH  = new JLabel();
    private JLabel     _lblTIL  = new JLabel();

    private boolean useVariance = false;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Calculation helper function
    private static double norm(double z)
    {
        double q = z * z;

        if(Math.abs(z) > 7.0)
            return (1.0 - 1.0 / q + 3.0 / (q * q )) * Math.exp(-q / 2.0) / (Math.abs(z) * Math.sqrt(Math.PI / 2.0));
        else
            return chisq(q, 1);
    }

    // Calculation helper function
    private static double chisq(double x, int n)
    {
        if(x > 1000.0 | n > 1000) {
            double q = norm((Math.pow(x / n, 1.0 / 3.0) + 2.0 / (9.0 * n) - 1.0) / Math.sqrt(2.0 / (9.0 * n ))) / 2.0;
            if(x > n) return q;
            else      return (1.0 - q);
        }

        double p = Math.exp(-0.5 * x);
        if((n % 2) == 1) p = p * Math.sqrt(2.0 * x / Math.PI);

        int k = n;
        while(k >= 2) {
             p = p * x / k;
             k = k - 2;
         }

        double t = p;
        int    a = n;
        while(t > 1e-15 * p) {
            a = a + 2;
            t = t * x / a;
            p = p + t;
        }

        return (1.0 - p);
    }

    // Calculation helper function
    private static double achisq(double p, int n)
    {
        double v  = 0.5;
        double dv = 0.5;
        double x  = 0.0;

        while(dv > 1e-15) {
            x  = 1.0 / v - 1.0;
            dv = dv / 2.0;
            if(chisq(x, n) > p) v = v - dv;
            else                v = v + dv;
        }

        return x;
    }

    // Update result
    private void _updateResult()
    {
        int    n    = GUtil.str2i(_txtN   .getText());
        double mean = GUtil.str2d(_txtMean.getText());
        double sd   = GUtil.str2d(_txtSD  .getText());
        double var  = GUtil.str2d(_txtVar .getText());
        double cl   = GUtil.str2i(_txtCL  .getText()) / 100.0;
        double cov  = GUtil.str2i(_txtCov .getText()) / 100.0;

        if(useVariance) {
            sd = Math.sqrt(var);
            _txtSD.setText(String.format("%.3f", sd));
        }
        else {
            var = sd * sd;
            _txtVar.setText(String.format("%.3f", var));
        }

        double csg = achisq(cl, n - 1);
        double zp2 = Math.pow(achisq(1.0 - cov, 1), 0.5);
        double k2  = zp2 * Math.sqrt((n - 1) * (1.0 + 1.0 / n) / csg);

        double Lo2 = mean - k2 * sd;
        double Up2 = mean + k2 * sd;

        _lblKf .setText(String.format("±%.5f", k2));
        _lblTIL.setText(String.format("%.5f", Lo2));
        _lblTIH.setText(String.format("%.5f", Up2));
    }

    // Private constructor
    private CalcTI(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        JPanel ipMainPanel = new JPanel(new GridLayout(9, 2, 5, 5), true);
            ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            JLabel lblN      = new JLabel(_S("tb_ti_n"));
            JLabel lblMean   = new JLabel(_S("tb_ti_mean"));
            JLabel lblSD     = new JLabel(_S("tb_ti_sd"));
            JLabel lblVar    = new JLabel(_S("tb_ti_var"));
            JLabel lblCL     = new JLabel(_S("tb_ti_cl"));
            JLabel lblCov    = new JLabel(_S("tb_ti_coverage"));
            JLabel lblKf     = new JLabel(_S("tb_ti_kf"));
            JLabel lblTIH    = new JLabel(_S("tb_ti_ti_h"));
            JLabel lblTIL    = new JLabel(_S("tb_ti_ti_l"));
            ipMainPanel.add(lblN   ); ipMainPanel.add(_txtN   );
            ipMainPanel.add(lblMean); ipMainPanel.add(_txtMean);
            ipMainPanel.add(lblSD  ); ipMainPanel.add(_txtSD  );
            ipMainPanel.add(lblVar ); ipMainPanel.add(_txtVar );
            ipMainPanel.add(lblCL  ); ipMainPanel.add(_txtCL  );
            ipMainPanel.add(lblCov ); ipMainPanel.add(_txtCov );
            ipMainPanel.add(lblKf  ); ipMainPanel.add(_lblKf  );
            ipMainPanel.add(lblTIH ); ipMainPanel.add(_lblTIH );
            ipMainPanel.add(lblTIL ); ipMainPanel.add(_lblTIL );
            lblN    .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblMean .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblSD   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblVar  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblCL   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblCov  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblKf   .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblTIH  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            lblTIL  .setPreferredSize(new Dimension(200, GUtil.DEFAULT_BOX_HEIGHT));
            _txtN   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtMean.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtSD  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtVar .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtCL  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtCov .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblKf  .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblTIH .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _lblTIL .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
            _txtN   .setDocument(new NumericDocument(0, false));
            _txtMean.setDocument(new NumericDocument(3, false));
            _txtSD  .setDocument(new NumericDocument(3, false));
            _txtVar .setDocument(new NumericDocument(3, false));
            _txtCL  .setDocument(new NumericDocument(0, false));
            _txtCov .setDocument(new NumericDocument(0, false));
            _txtN   .setText("0"  );
            _txtMean.setText("0.0");
            _txtSD  .setText("0.0");
            _txtVar .setText("0.0");
            _txtCL  .setText("95" );
            _txtCov .setText("90" );
            GUtil.disableCutAndPasteOnTextField(_txtN);
            GUtil.disableCutAndPasteOnTextField(_txtMean);
            GUtil.disableCutAndPasteOnTextField(_txtSD);
            GUtil.disableCutAndPasteOnTextField(_txtCL);
            GUtil.disableCutAndPasteOnTextField(_txtCov);
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
        _txtCL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtCL.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                _updateResult();
            }
        });
        _txtCov.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _updateResult();
            }
        });
        _txtCov.addFocusListener(new FocusListener() {
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
        CalcTI rb = new CalcTI(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_ti_title"), JRootPane.PLAIN_DIALOG);
    }
}
