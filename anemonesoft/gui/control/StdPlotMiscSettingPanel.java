/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;

//
// A standard plot-miscellaneous-setting panel
//
public class StdPlotMiscSettingPanel extends JPanel implements Saveable {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Controls
    private JComboBox _cmbPlotBgrColor      = new JComboBox();
    private JComboBox _cmbPlotCaptionColor  = new JComboBox();
    private JComboBox _cmbAxisCaptionColor  = new JComboBox();
    private JComboBox _cmbAxisTickTextColor = new JComboBox();
    private JComboBox _cmbAxisLineColor     = new JComboBox();
    private JComboBox _cmbOriginLineColor   = new JComboBox();
    private JComboBox _cmbOriginLineStyle   = new JComboBox();
    private JComboBox _cmbGridLineColor     = new JComboBox();
    private JComboBox _cmbGridLineStyle     = new JComboBox();
    
    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Add a color item
    private void _addColorItem(JComboBox cmb, String str, Color col)
    { cmb.addItem(new ColorBoxRenderer.Item(col, str)); }

    // Add an axis setting
    private void _addSetting(JPanel parent, String title, JComboBox color, JComboBox line, boolean lastPart)
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
        if(color == _cmbPlotBgrColor) color.setSelectedIndex(15);
        else                          color.setSelectedIndex( 0);

        if(line != null) {
            JPanel pnlLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                line.setFont(font);
                line.setPreferredSize(new Dimension(TWIDTH, THEIGHT));
                JLabel lblLine = new JLabel(_S("str_style"), JLabel.LEFT);
                    lblLine.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                    lblLine.setFont(font);
                pnlLine.add(lblLine);
                pnlLine.add(line);
            parent.add(pnlLine);
            line.addItem(_S("str_lin_none"      ));
            line.addItem(_S("str_lin_continuous"));
            line.addItem(_S("str_lin_dashed"    ));
            line.addItem(_S("str_lin_dotted"    ));
            line.setSelectedIndex(0);
        }

        if(!lastPart) parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(getPlotBackgroundColor());
        ds.writeInt(getPlotCaptionColor   ());
        ds.writeInt(getAxisCaptionColor   ());
        ds.writeInt(getAxisTickTextColor  ());
        ds.writeInt(getAxisLineColor      ());
        ds.writeInt(getOriginLineColor    ());
        ds.writeInt(getOriginLineStyle    ());
        ds.writeInt(getGridLineColor      ());
        ds.writeInt(getGridLineStyle      ());
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int v;

        v = ds.readInt(); setPlotBackgroundColor(v);
        v = ds.readInt(); setPlotCaptionColor   (v);
        v = ds.readInt(); setAxisCaptionColor   (v);
        v = ds.readInt(); setAxisTickTextColor  (v);
        v = ds.readInt(); setAxisLineColor      (v);
        v = ds.readInt(); setOriginLineColor    (v);
        v = ds.readInt(); setOriginLineStyle    (v);
        v = ds.readInt(); setGridLineColor      (v);
        v = ds.readInt(); setGridLineStyle      (v);

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-miscellaneous-setting panel
    public StdPlotMiscSettingPanel()
    {
        super(new BorderLayout(), true);

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Add the settings
        _addSetting(mainPanel, _S("str_plot_bgr"      ), _cmbPlotBgrColor,      null,               false);
        _addSetting(mainPanel, _S("str_plot_capt"     ), _cmbPlotCaptionColor,  null,               false);
        _addSetting(mainPanel, _S("str_axis_capt"     ), _cmbAxisCaptionColor,  null,               false);
        _addSetting(mainPanel, _S("str_axis_tick_text"), _cmbAxisTickTextColor, null,               false);
        _addSetting(mainPanel, _S("str_axis_line"     ), _cmbAxisLineColor,     null,               false);
        _addSetting(mainPanel, _S("str_org_line"      ), _cmbOriginLineColor,  _cmbOriginLineStyle, false);
        _addSetting(mainPanel, _S("str_grid_line"     ), _cmbGridLineColor,    _cmbGridLineStyle,   true );
    }

    // Getters
    public int getPlotBackgroundColor() { return _cmbPlotBgrColor     .getSelectedIndex(); }
    public int getPlotCaptionColor   () { return _cmbPlotCaptionColor .getSelectedIndex(); }
    public int getAxisCaptionColor   () { return _cmbAxisCaptionColor .getSelectedIndex(); }
    public int getAxisTickTextColor  () { return _cmbAxisTickTextColor.getSelectedIndex(); }
    public int getAxisLineColor      () { return _cmbAxisLineColor    .getSelectedIndex(); }
    public int getOriginLineColor    () { return _cmbOriginLineColor  .getSelectedIndex(); }
    public int getOriginLineStyle    () { return _cmbOriginLineStyle  .getSelectedIndex(); }
    public int getGridLineColor      () { return _cmbGridLineColor    .getSelectedIndex(); }
    public int getGridLineStyle      () { return _cmbGridLineStyle    .getSelectedIndex(); }

    // Setters
    public void setPlotBackgroundColor(int sel) { _cmbPlotBgrColor     .setSelectedIndex(sel); }
    public void setPlotCaptionColor   (int sel) { _cmbPlotCaptionColor .setSelectedIndex(sel); }
    public void setAxisCaptionColor   (int sel) { _cmbAxisCaptionColor .setSelectedIndex(sel); }
    public void setAxisTickTextColor  (int sel) { _cmbAxisTickTextColor.setSelectedIndex(sel); }
    public void setAxisLineColor      (int sel) { _cmbAxisLineColor    .setSelectedIndex(sel); }
    public void setOriginLineColor    (int sel) { _cmbOriginLineColor  .setSelectedIndex(sel); }
    public void setOriginLineStyle    (int sel) { _cmbOriginLineStyle  .setSelectedIndex(sel); }
    public void setGridLineColor      (int sel) { _cmbGridLineColor    .setSelectedIndex(sel); }
    public void setGridLineStyle      (int sel) { _cmbGridLineStyle    .setSelectedIndex(sel); }
}
