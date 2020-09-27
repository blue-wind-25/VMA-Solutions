/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A class for displaying a resolution-selector box
//
public class ResolutionSelectorBox extends JDialog implements ActionListener {
    // Data and controls
    private JRadioButton _rdo1000x0750 = new JRadioButton(_S("dlg_rsel_low"   ));
    private JRadioButton _rdo2000x1500 = new JRadioButton(_S("dlg_rsel_medium"));
    private JRadioButton _rdo4000x3000 = new JRadioButton(_S("dlg_rsel_high"  ));
    private JRadioButton _rdo8000x6000 = new JRadioButton(_S("dlg_rsel_ultra" ));
    private JButton      _btnOK        = new JButton(_S("dlg_ok"));
    private JButton      _btnCancel    = new JButton(_S("dlg_cancel"));
    private int[]        _selRes       = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Private constructor
    private ResolutionSelectorBox(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the radio buttons
        ButtonGroup rgp = new ButtonGroup();
        rgp.add(_rdo1000x0750);
        rgp.add(_rdo2000x1500);
        rgp.add(_rdo4000x3000);
        rgp.add(_rdo8000x6000);
        _rdo4000x3000.setSelected(true);

        Font font = new Font(GUtil.getSysFontName("Monospaced"), Font.BOLD, _rdo1000x0750.getFont().getSize());
        _rdo1000x0750.setFont(font);
        _rdo2000x1500.setFont(font);
        _rdo4000x3000.setFont(font);
        _rdo8000x6000.setFont(font);

        // Resolution panel
        JPanel resPanel = new JPanel(new GridLayout(4, 1), true);
            resPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 5, 0),
                BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            resPanel.add(_rdo1000x0750);
            resPanel.add(_rdo2000x1500);
            resPanel.add(_rdo4000x3000);
            resPanel.add(_rdo8000x6000);

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
            mainPanel.add(resPanel, BorderLayout.CENTER);
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
                _selRes = null;
                _THIS.setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show the dialog
    public static int[] showDialog(Frame parentFrame, String title)
    {
        ResolutionSelectorBox rsb = new ResolutionSelectorBox(parentFrame);
        GUtil.showModalDialog(rsb, null, title, JRootPane.QUESTION_DIALOG);
        return rsb._selRes;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for buttons
    public void actionPerformed(ActionEvent event)
    {
        JButton button = (JButton) event.getSource();

        if(button == _btnOK) {
                 if(_rdo8000x6000.isSelected()) _selRes = new int[]{ 8000, 6000 };
            else if(_rdo4000x3000.isSelected()) _selRes = new int[]{ 4000, 3000 };
            else if(_rdo2000x1500.isSelected()) _selRes = new int[]{ 2000, 1500 };
            else                                _selRes = new int[]{ 1000,  750 };
        }
        else {
            _selRes = null;
        }

        this.setVisible(false);
    }
}
