/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.dialog.*;
import anemonesoft.i18n.*;

//
// A standard plot-caption-setting panel
//
public class StdPlotCaptionSettingPanel extends JPanel implements Saveable, FocusListener, ActionListener {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Controls
    private JTextField _txtMainCaption   = new JTextField();
    private JTextField _txtSubCaption    = new JTextField();
    private JTextField _txtXCaption      = new JTextField();
    private JTextField _txtLeftYCaption  = new JTextField();
    private JTextField _txtRightYCaption = new JTextField();
    private JButton    _btnInsertSymbol  = new JButton();
    private JButton    _btnItalic        = new JButton();
    private JButton    _btnSuperscript   = new JButton();
    private JButton    _btnSubscript     = new JButton();
    private Timer      _tmrToolbox       = null;
    private JTextField _lastFocusedTF    = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Add a setting
    private void _addSetting(JPanel parent, String title, JTextField txt)
    {
        final int WIDTH  = 250;
        final int HEIGHT = GUtil.DEFAULT_BOX_HEIGHT;

        txt.setMinimumSize  (new Dimension(WIDTH, HEIGHT));
        txt.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        txt.setMaximumSize  (new Dimension(Short.MAX_VALUE, HEIGHT));

        GUtil.enableCutAndPasteOnTextField(txt);

        parent.add(new JLabel(title, JLabel.LEFT));
        parent.add(txt);
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeUTF(_txtMainCaption  .getText());
        ds.writeUTF(_txtSubCaption   .getText());
        ds.writeUTF(_txtXCaption     .getText());
        ds.writeUTF(_txtLeftYCaption .getText());
        ds.writeUTF(_txtRightYCaption.getText());
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;
        
        _txtMainCaption  .setText(ds.readUTF());
        _txtSubCaption   .setText(ds.readUTF());
        _txtXCaption     .setText(ds.readUTF());
        _txtLeftYCaption .setText(ds.readUTF());
        _txtRightYCaption.setText(ds.readUTF());

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-caption-setting panel
    public StdPlotCaptionSettingPanel(boolean minimal, boolean canUseRightYAxis)
    {
        super(new BorderLayout(5, 0), true);

        // Add the caption panel
        JPanel pnlCaption = minimal
                          ? new JPanel(new FlexGridLayout(2, 2, 5, 5), true)
                          : ( canUseRightYAxis
                              ? new JPanel(new FlexGridLayout(5, 2, 5, 5), true)
                              : new JPanel(new FlexGridLayout(4, 2, 5, 5), true)
                            );
            pnlCaption.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            _addSetting(pnlCaption, _S("str_main_caption"), _txtMainCaption);
            _addSetting(pnlCaption, _S("str_sub_caption" ), _txtSubCaption );
           if(!minimal) {
                _addSetting(pnlCaption, _S("str_x_caption"   ), _txtXCaption   );
                if(canUseRightYAxis) {
                    _addSetting(pnlCaption, _S("str_ly_caption"), _txtLeftYCaption );
                    _addSetting(pnlCaption, _S("str_ry_caption"), _txtRightYCaption);
                }
                else {
                    _addSetting(pnlCaption, _S("str_y_caption"), _txtLeftYCaption);
                }
            }
        add(pnlCaption, BorderLayout.CENTER);

        // Add the tool panel
        JPanel pnlTool = new JPanel(new GridLayout(2, 2, 5, 5), true);
            pnlTool.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                _btnInsertSymbol = new JButton(GUtil.newImageIcon("mnu_edit_insert_symbol"));
                _btnItalic       = new JButton(GUtil.newImageIcon("mnu_edit_format_italic"));
                _btnSuperscript  = new JButton(GUtil.newImageIcon("mnu_edit_format_superscript"));
                _btnSubscript    = new JButton(GUtil.newImageIcon("mnu_edit_format_subscript"));
                _btnInsertSymbol.setToolTipText(_S("str_insert_sym_tooltip" ));
                _btnItalic      .setToolTipText(_S("str_insert_ita_tooltip" ));
                _btnSuperscript .setToolTipText(_S("str_insert_sups_tooltip"));
                _btnSubscript   .setToolTipText(_S("str_insert_subs_tooltip"));
            pnlTool.add(_btnInsertSymbol);
            pnlTool.add(_btnItalic);
            pnlTool.add(_btnSuperscript);
            pnlTool.add(_btnSubscript);
        add(pnlTool, BorderLayout.EAST);

        // Set the default captions
        if(minimal) {
            setMainCaption  (_S("str_min_mcaption" ));
            setSubCaption   (_S("str_min_scaption" ));
        }
        else {
            setMainCaption  (_S("str_def_mcaption" ));
            setSubCaption   (_S("str_def_scaption" ));
            setXCaption     (_S("str_def_xcaption" ));
            setLeftYCaption (_S("str_def_lycaption"));
            setRightYCaption(_S("str_def_rycaption"));
        }

        // Initialize the toolbox
        _btnInsertSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                _tmrToolbox.stop();
                
                JTextField lastFocusedTF = _lastFocusedTF;
                if(lastFocusedTF == null) return;

                String sym = SymbolSelectorBox.showDialog(GUIMain.instance.getRootFrame(), _S("dlg_ssel_caption"));
                lastFocusedTF.replaceSelection(sym);
                lastFocusedTF.requestFocus();
            }

        });
        _btnItalic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                _tmrToolbox.stop();

                JTextField lastFocusedTF = _lastFocusedTF;
                if(lastFocusedTF == null) return;

                String selText = lastFocusedTF.getSelectedText();
                if(selText == null) selText = "";
                lastFocusedTF.replaceSelection("<i>" + selText + "</i>");
                lastFocusedTF.requestFocus();
            }

        });
        _btnSuperscript.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                _tmrToolbox.stop();

                JTextField lastFocusedTF = _lastFocusedTF;
                if(lastFocusedTF == null) return;

                String selText = lastFocusedTF.getSelectedText();
                if(selText == null) selText = "";
                lastFocusedTF.replaceSelection("<sup>" + selText + "</sup>");
                lastFocusedTF.requestFocus();
            }

        });
        _btnSubscript.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                _tmrToolbox.stop();

                JTextField lastFocusedTF = _lastFocusedTF;
                if(lastFocusedTF == null) return;

                String selText = lastFocusedTF.getSelectedText();
                if(selText == null) selText = "";
                lastFocusedTF.replaceSelection("<sub>" + selText + "</sub>");
                lastFocusedTF.requestFocus();
            }
        });

        _txtMainCaption  .addFocusListener(this);
        _txtSubCaption   .addFocusListener(this);
        _txtXCaption     .addFocusListener(this);
        _txtLeftYCaption .addFocusListener(this);
        _txtRightYCaption.addFocusListener(this);

        _btnInsertSymbol.setEnabled(false);
        _btnItalic      .setEnabled(false);
        _btnSuperscript .setEnabled(false);
        _btnSubscript   .setEnabled(false);
        
        _tmrToolbox = new Timer(250, this);
    }

    // Getters
    public String getMainCaption  () { return GUIMain.instance.getSpreadsheetPanel().getRawValueByAddress(_txtMainCaption  .getText()); }
    public String getSubCaption   () { return GUIMain.instance.getSpreadsheetPanel().getRawValueByAddress(_txtSubCaption   .getText()); }
    public String getXCaption     () { return GUIMain.instance.getSpreadsheetPanel().getRawValueByAddress(_txtXCaption     .getText()); }
    public String getLeftYCaption () { return GUIMain.instance.getSpreadsheetPanel().getRawValueByAddress(_txtLeftYCaption .getText()); }
    public String getRightYCaption() { return GUIMain.instance.getSpreadsheetPanel().getRawValueByAddress(_txtRightYCaption.getText()); }

    // Setters
    public void setMainCaption  (String str) { _txtMainCaption  .setText(str); }
    public void setSubCaption   (String str) { _txtSubCaption   .setText(str); }
    public void setXCaption     (String str) { _txtXCaption     .setText(str); }
    public void setLeftYCaption (String str) { _txtLeftYCaption .setText(str); }
    public void setRightYCaption(String str) { _txtRightYCaption.setText(str); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for the text boxes
    public void focusGained(FocusEvent event)
    {
        _tmrToolbox.stop();
        _lastFocusedTF = null;

        _btnInsertSymbol.setEnabled(true);
        _btnItalic      .setEnabled(true);
        _btnSuperscript .setEnabled(true);
        _btnSubscript   .setEnabled(true);
    }

    // Event handler for the text boxes
    public void focusLost(FocusEvent event)
    {
        _lastFocusedTF = (JTextField) event.getSource();
        _tmrToolbox.start();
    }

    // Event handler for timer
    public void actionPerformed(ActionEvent event)
    {
        _tmrToolbox.stop();
        _lastFocusedTF = null;

        _btnInsertSymbol.setEnabled(false);
        _btnItalic      .setEnabled(false);
        _btnSuperscript .setEnabled(false);
        _btnSubscript   .setEnabled(false);

        this.requestFocus();
    }
}

