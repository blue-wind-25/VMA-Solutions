/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;

//
// A standard plot-data-point-setting panel
//
public class StdPlotDataPointSettingPanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Predefined colors
    public static final Color[] PREDEFINED_COLOR = new Color[]{
        new Color(  0,   0,   0),
        new Color(128,   0,   0), new Color(  0, 128,   0), new Color(  0,   0, 128),
        new Color(128, 128,   0), new Color(128,   0, 128), new Color(  0, 128, 128),
        new Color(128, 128, 128),
        new Color(255,   0,   0), new Color(  0, 255,   0), new Color(  0,   0, 255),
        new Color(255, 255,   0), new Color(255,   0, 255), new Color(  0, 255, 255),
        new Color(192, 192, 192),
        new Color(255, 255, 255)
    };

    // Predefined line styles
    public static final int[] PREDEFINED_LINE_STYLE = new int[]{
        -1,
        PlotRenderer.LINE_CONTINUOUS,
        PlotRenderer.LINE_DASHED,
        PlotRenderer.LINE_DOTTED
    };

    // Predefined symbols
    public static final int[] PREDEFINED_SYMBOL = new int[]{
        -1,
        PlotRenderer.SYM_CHAR_PLUS,
        PlotRenderer.SYM_CHAR_CROSS,
        PlotRenderer.SYM_CHAR_STAR,
        PlotRenderer.SYM_OPEN_CIRCLE,
        PlotRenderer.SYM_OPEN_SQUARE,
        PlotRenderer.SYM_OPEN_UP_TRIANGLE,
        PlotRenderer.SYM_OPEN_DOWN_TRIANGLE,
        PlotRenderer.SYM_OPEN_DIAMOND,
        PlotRenderer.SYM_CLOSED_CIRCLE,
        PlotRenderer.SYM_CLOSED_SQUARE,
        PlotRenderer.SYM_CLOSED_UP_TRIANGLE,
        PlotRenderer.SYM_CLOSED_DOWN_TRIANGLE,
        PlotRenderer.SYM_CLOSED_DIAMOND
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Controls
    private JComboBox[] _cmbColor  = null;
    private JComboBox[] _cmbSymbol = null;
    private JComboBox[] _cmbLine   = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Add a color item
    private void _addColorItem(JComboBox cmb, String str, Color col)
    { cmb.addItem(new ColorBoxRenderer.Item(col, str)); }

    // Add an axis setting
    private void _addSetting(JPanel parent, int idx, String title, JComboBox color, JComboBox symbol, JComboBox line, boolean lastPart)
    {
        final int IWIDTH  = GUtil.DEFAULT_SMALL_BOX_WIDTH;
        final int TWIDTH  = GUtil.DEFAULT_LARGE_BOX_WIDTH;
        final int THEIGHT = GUtil.DEFAULT_BOX_HEIGHT;
        
        Font font = (new JLabel()).getFont();
             font = new Font(font.getName(), Font.BOLD, (int) (font.getSize() * 0.9));

        JPanel pnlCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblCaption = new JLabel(title, JLabel.LEFT);
            pnlCaption.add(lblCaption);
        parent.add(pnlCaption);

        JPanel pnlColor = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            color.setFont(font);
            color.setPreferredSize(new Dimension(TWIDTH, THEIGHT));
            JLabel lblColor = new JLabel(_S("str_color"), JLabel.LEFT);
                lblColor.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                lblColor.setFont(font);
            pnlColor.add(lblColor);
            pnlColor.add(color);
        parent.add(pnlColor);
        _addColorItem(color, _S("str_col_black"   ), PREDEFINED_COLOR[ 0]);
        _addColorItem(color, _S("str_col_dred"    ), PREDEFINED_COLOR[ 1]);
        _addColorItem(color, _S("str_col_dgreen"  ), PREDEFINED_COLOR[ 2]);
        _addColorItem(color, _S("str_col_dblue"   ), PREDEFINED_COLOR[ 3]);
        _addColorItem(color, _S("str_col_dyellow" ), PREDEFINED_COLOR[ 4]);
        _addColorItem(color, _S("str_col_dmagenta"), PREDEFINED_COLOR[ 5]);
        _addColorItem(color, _S("str_col_dcyan"   ), PREDEFINED_COLOR[ 6]);
        _addColorItem(color, _S("str_col_dgray"   ), PREDEFINED_COLOR[ 7]);
        _addColorItem(color, _S("str_col_lred"    ), PREDEFINED_COLOR[ 8]);
        _addColorItem(color, _S("str_col_lgreen"  ), PREDEFINED_COLOR[ 9]);
        _addColorItem(color, _S("str_col_lblue"   ), PREDEFINED_COLOR[10]);
        _addColorItem(color, _S("str_col_lyellow" ), PREDEFINED_COLOR[11]);
        _addColorItem(color, _S("str_col_lmagenta"), PREDEFINED_COLOR[12]);
        _addColorItem(color, _S("str_col_lcyan"   ), PREDEFINED_COLOR[13]);
        _addColorItem(color, _S("str_col_lgray"   ), PREDEFINED_COLOR[14]);
        _addColorItem(color, _S("str_col_white"   ), PREDEFINED_COLOR[15]);
        color.setRenderer(new ColorBoxRenderer(font));
        color.setSelectedIndex(idx);

        if(symbol != null) {
            JPanel pnlSymbol = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                symbol.setFont(font);
                symbol.setPreferredSize(new Dimension(TWIDTH, THEIGHT));
                JLabel lblSymbol = new JLabel(_S("str_symbol"), JLabel.LEFT);
                    lblSymbol.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                    lblSymbol.setFont(font);
                pnlSymbol.add(lblSymbol);
                pnlSymbol.add(symbol);
            parent.add(pnlSymbol);
            symbol.addItem(_S("str_sym_none"           ));
            symbol.addItem(_S("str_sym_char_plus"      )); symbol.addItem(_S("str_sym_char_cross"    )); symbol.addItem(_S("str_sym_char_star"    ));
            symbol.addItem(_S("str_sym_open_circle"    )); symbol.addItem(_S("str_sym_open_square"   )); symbol.addItem(_S("str_sym_open_up_tri"  ));
            symbol.addItem(_S("str_sym_open_down_tri"  )); symbol.addItem(_S("str_sym_open_diamond"  ));
            symbol.addItem(_S("str_sym_closed_circle"  )); symbol.addItem(_S("str_sym_closed_square" )); symbol.addItem(_S("str_sym_closed_up_tri"));
            symbol.addItem(_S("str_sym_closed_down_tri")); symbol.addItem(_S("str_sym_closed_diamond"));
            symbol.setSelectedIndex(idx + 1);
        }

        if(line != null) {
            JPanel pnlLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                line.setFont(font);
                line.setPreferredSize(new Dimension(TWIDTH, THEIGHT));
                JLabel lblLine = new JLabel(_S("str_line"), JLabel.LEFT);
                    lblLine.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                    lblLine.setFont(font);
                pnlLine.add(lblLine);
                pnlLine.add(line);
            parent.add(pnlLine);
            line.addItem(_S("str_lin_none"      ));
            line.addItem(_S("str_lin_continuous"));
            line.addItem(_S("str_lin_dashed"    ));
            line.addItem(_S("str_lin_dotted"    ));
            line.setSelectedIndex(1);
        }

        if(!lastPart) parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Data-point-specification class
    public static class DPSSpec {
        public String  caption;
        public boolean hasSymbol;
        public boolean hasLine;

        public DPSSpec(String caption_, boolean hasSymbol_, boolean hasLine_)
        { caption = caption_; hasSymbol = hasSymbol_; hasLine = hasLine_; }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        // Write the number of settings
        ds.writeInt(_cmbColor.length);

        // Write the setting data
        for(int i = 0; i < _cmbColor.length; ++i) {
            ds.writeInt(getColor (i));
            ds.writeInt(getSymbol(i));
            ds.writeInt(getLine  (i));
        }
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;
        
        // Read and check the number of settings
        int nos = ds.readInt();
        if(nos != _cmbColor.length) return false;

        // Read the setting data
        int v;
        for(int i = 0; i < _cmbColor.length; ++i) {
            v = ds.readInt(); setColor (i, v);
            v = ds.readInt(); setSymbol(i, v);
            v = ds.readInt(); setLine  (i, v);
        }

        // Done
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-data-point-setting panel
    public StdPlotDataPointSettingPanel(DPSSpec[] dpsSpec)
    {
        super(new BorderLayout(), true);

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Allocate the array
        _cmbColor  = new JComboBox[dpsSpec.length];
        _cmbSymbol = new JComboBox[dpsSpec.length];
        _cmbLine   = new JComboBox[dpsSpec.length];

        // Add the settings
        int lastIdx = dpsSpec.length - 1;
        for(int i = 0; i <= lastIdx; ++i) {
                                     _cmbColor [i] = new JComboBox();
            if(dpsSpec[i].hasSymbol) _cmbSymbol[i] = new JComboBox();
            if(dpsSpec[i].hasLine  ) _cmbLine  [i] = new JComboBox();
            _addSetting(mainPanel, i, dpsSpec[i].caption, _cmbColor[i], _cmbSymbol[i], _cmbLine[i], i == lastIdx);
        }
    }

    // Getters
    public int getColor (int idx) { return (idx >= _cmbColor .length || _cmbColor [idx] == null) ? -1 : _cmbColor [idx].getSelectedIndex(); }
    public int getSymbol(int idx) { return (idx >= _cmbSymbol.length || _cmbSymbol[idx] == null) ? -1 : _cmbSymbol[idx].getSelectedIndex(); }
    public int getLine  (int idx) { return (idx >= _cmbLine  .length || _cmbLine  [idx] == null) ? -1 : _cmbLine  [idx].getSelectedIndex(); }

    // Setters
    public void setColor (int idx, int sel) { if(idx < _cmbColor .length && _cmbColor [idx] != null) _cmbColor [idx].setSelectedIndex(sel); }
    public void setSymbol(int idx, int sel) { if(idx < _cmbSymbol.length && _cmbSymbol[idx] != null) _cmbSymbol[idx].setSelectedIndex(sel); }
    public void setLine  (int idx, int sel) { if(idx < _cmbLine  .length && _cmbLine  [idx] != null) _cmbLine  [idx].setSelectedIndex(sel); }
}
