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
public class SymbolSelectorBox extends JDialog implements ActionListener {
    // Data and controls
    private JButton[] _btnSym    = null;
    private JButton   _btnCancel = new JButton(_S("dlg_cancel"));
    private String    _selSym    = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Private constructor
    private SymbolSelectorBox(Frame parentFrame)
    {
        super(parentFrame);

        // Create the symbol buttons
        String symbols = "αβγδεζηθικλμνξοπρςστυφχψωΓΔΘΛΞΠΣΦΨΩ‰‱°±∓≅≆≇≈≉≊≠≡≢≤≥≦≧≨≩≪≫≮≯≰≱≴≵≶≷≸≹⋘⋙⋚⋛⋜⋝∀∃∄∈∉∋∌";
        _btnSym = new JButton[symbols.length()];

        // Symbol panel
        JPanel symbolPanel = new JPanel(new GridLayout(8, 10), true);
            Font font = new Font(GUtil.getSysFontName("Monospaced"), Font.BOLD, symbolPanel.getFont().getSize());
            symbolPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 5, 0),
                BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            for(int i = 0; i < _btnSym.length; ++i) {
                _btnSym[i] = new JButton("" + symbols.charAt(i));
                _btnSym[i].setFont(font);
                _btnSym[i].addActionListener(this);
                symbolPanel.add(_btnSym[i]);
            }

        // Create the dialog
        JPanel mainPanel = new JPanel(new BorderLayout(), true);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            mainPanel.add(symbolPanel, BorderLayout.CENTER);
            mainPanel.add(_btnCancel, BorderLayout.SOUTH);
        add(mainPanel);

        _btnCancel.setIcon(GUtil.newImageIcon("btn_cancel"));
        _btnCancel.addActionListener(this);

        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension parentSize = parentFrame.getSize();
        Point     parentPos  = parentFrame.getLocation();
        setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);

        // Allow the dialog to be closed by pressing escape
        final JDialog _THIS = this;
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                _selSym = null;
                _THIS.setVisible(false);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show the dialog
    public static String showDialog(Frame parentFrame, String title)
    {
        SymbolSelectorBox ssb = new SymbolSelectorBox(parentFrame);
        GUtil.showModalDialog(ssb, null, title, JRootPane.QUESTION_DIALOG);
        return ssb._selSym;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for buttons
    public void actionPerformed(ActionEvent event)
    {
        JButton button = (JButton) event.getSource();

        if(button == _btnCancel)
            _selSym = null;
        else
            _selSym = button.getText();

        this.setVisible(false);
    }
}
