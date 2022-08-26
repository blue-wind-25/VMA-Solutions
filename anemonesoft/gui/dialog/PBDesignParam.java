/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.i18n.*;

//
// A class for displaying a pb-design-parameter box
//
public class PBDesignParam extends JDialog implements ActionListener {
    // Data and controls
    private JComboBox _cmbNumOfFactors = new JComboBox();
    private JButton   _btnOK           = new JButton(_S("dlg_ok"));
    private JButton   _btnCancel       = new JButton(_S("dlg_cancel"));
    private int       _selNOF          = -1;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Private constructor
    private PBDesignParam(Frame parentFrame)
    {
        super(parentFrame);

        // Factor panel
        JPanel facPanel = new JPanel(new FlexGridLayout(3, 1, 0, 5), true);
            facPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 5, 0),
                BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            facPanel.add(new JLabel(_S("dlg_pbdes_nof"), JLabel.LEFT));
            facPanel.add(_cmbNumOfFactors);
            facPanel.add(new JLabel(_S("dlg_pbdes_note"), JLabel.LEFT));

            for(int i = 2; i <= 23; ++i) _cmbNumOfFactors.addItem("" + i);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5), true);
            buttonPanel.add(_btnOK);
            buttonPanel.add(_btnCancel);
            _btnOK.addActionListener(this);
            _btnCancel.addActionListener(this);

        _btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        _btnCancel.setIcon(GUtil.newImageIcon("btn_cancel"));

        // Create the dialog
        JPanel mainPanel = new JPanel(new BorderLayout(), true);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            mainPanel.add(facPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension parentSize = parentFrame.getSize();
        Point     parentPos  = parentFrame.getLocation();
        setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);

        // Defines the default button
        getRootPane().setDefaultButton(_btnOK);

        // Allow the dialog to be closed by pressing escape
        final JDialog _THIS = this;
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                _selNOF = -1;
                _THIS.setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show the dialog
    public static int showDialog(Frame parentFrame)
    {
        PBDesignParam rsb = new PBDesignParam(parentFrame);
        GUtil.showModalDialog(rsb, null, _S("dlg_pbdes_caption"), JRootPane.QUESTION_DIALOG);
        return rsb._selNOF;
    }

    // The first rows for Plackett-Burman designs
    private static double[] _pbv  = { -1, 1 };
    private static byte[]   _pb4  = { 1, 0, 1                                                             };
    private static byte[]   _pb8  = { 1, 1, 1, 0, 1, 0, 0                                                 };
    private static byte[]   _pb12 = { 1, 1, 0, 1, 1, 1, 0, 0, 0, 1, 0                                     };
    private static byte[]   _pb16 = { 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0                         };
    private static byte[]   _pb20 = { 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 0, 1, 0, 0, 0, 0, 1, 1, 0             };
    private static byte[]   _pb24 = { 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0 };
    
    // Generate the template
    public static String genTemplate(int nof)
    {
        // Determine the array to be used
        byte[] pbf = null;
             if(nof >= 24) return null;
        else if(nof >= 20) pbf = _pb24;
        else if(nof >= 16) pbf = _pb20;
        else if(nof >= 12) pbf = _pb16;
        else if(nof >=  8) pbf = _pb12;
        else if(nof >=  4) pbf = _pb8;
        else               pbf = _pb4;

        // Generate the data
        StringBuilder sb = new StringBuilder();
        int           oz = 0;
        int           ln = pbf.length;
        for(int r = 0; r < ln; ++r) {
            sb.append("0\t");
            for(int c = 0; c < nof; ++c) {
                int i = c - oz;
                if(i < 0) i += ln;
                sb.append(_pbv[pbf[i]]);
                sb.append("\t");
            }
            sb.append("\n");
            ++oz;
        }
        sb.append("0\t");
        for(int c = 0; c < nof; ++c) {
            sb.append(-1);
            sb.append("\t");
        }

        // Return the data
        return sb.toString();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for buttons
    public void actionPerformed(ActionEvent event)
    {
        JButton button = (JButton) event.getSource();

        if(button == _btnOK)
            _selNOF = Integer.parseInt(_cmbNumOfFactors.getSelectedItem().toString());
        else
            _selNOF = -1;

        this.setVisible(false);
    }
}
