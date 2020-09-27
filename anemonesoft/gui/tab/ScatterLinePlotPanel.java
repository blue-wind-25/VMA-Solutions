/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.tab;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;

//
// Tab module - scatter-line-plot panel
//
public class ScatterLinePlotPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 9;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _captionSP        = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataPointSettingPanel _plotStyleSP      = null;
    private StdPlotAxisScaleSettingPanel _axisScaleSP      = null;
    private StdPlotMiscSettingPanel      _miscSP           = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    // Return the tab caption
    public String getTabCaption()
    { return _S("str_plot_sct_ln"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_plot"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, true, null);
        title[0] = _S("str_acrs_data_range");
        pane [0] = _inputDataRangeSP;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[NUM_OF_Y_AXIS];
        for(int i = 0; i < NUM_OF_Y_AXIS; ++i) {
            dpsSpec[i] = new StdPlotDataPointSettingPanel.DPSSpec(_F("str_y_values_T", new Integer[]{ i + 1 }), true, true);
        }
        _plotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[1] = _S("str_acrs_data_style");
        pane [1] = _plotStyleSP;

        _axisScaleSP = new StdPlotAxisScaleSettingPanel(this, true);
        title[2] = _S("str_acrs_axis_scale");
        pane [2] = _axisScaleSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[3] = _S("str_acrs_misc");
        pane [3] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _captionSP = new StdPlotCaptionSettingPanel(false, true);
        return _captionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary)
    {
        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 0) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        // Get the Y data array
        YData[] lyda = getYDataArray(_ssPanel, _inputDataRangeSP, _plotStyleSP, NUM_OF_Y_AXIS, false, 0);
        YData[] ryda = getYDataArray(_ssPanel, _inputDataRangeSP, _plotStyleSP, NUM_OF_Y_AXIS, true,  0);
        if(lyda == null && ryda == null) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        boolean hasLeft  = (lyda != null && lyda.length > 0);
        boolean hasRight = (ryda != null && ryda.length > 0);

        // Get the captions
        String  mcapt = _captionSP.getMainCaption  ();
        String  scapt = _captionSP.getSubCaption   ();
        String  xcapt = _captionSP.getXCaption     ();
        String lycapt = _captionSP.getLeftYCaption ();
        String rycapt = _captionSP.getRightYCaption();

        // Get the scale
        double xmin = _axisScaleSP.getXMin();
        double xmax = _axisScaleSP.getXMax();
        double xstp = _axisScaleSP.getXStep();
        int    xsdv = _axisScaleSP.getXSDiv();
        double lymin = _axisScaleSP.getLeftYMin();
        double lymax = _axisScaleSP.getLeftYMax();
        double lystp = _axisScaleSP.getLeftYStep();
        int    lysdv = _axisScaleSP.getLeftYSDiv();
        double rymin = _axisScaleSP.getRightYMin();
        double rymax = _axisScaleSP.getRightYMax();
        double rystp = _axisScaleSP.getRightYStep();
        int    rysdv = _axisScaleSP.getRightYSDiv();

        // Get the general settings
        Color pbColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getPlotBackgroundColor()];
        Color pcColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getPlotCaptionColor   ()];
        Color acColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getAxisCaptionColor   ()];
        Color atColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getAxisTickTextColor  ()];
        Color alColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getAxisLineColor      ()];
        Color olColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getOriginLineColor    ()];
        int   olStyle = StdPlotMiscSettingPanel.PREDEFINED_LINE_STYLE[_miscSP.getOriginLineStyle    ()];
        Color glColor = StdPlotMiscSettingPanel.PREDEFINED_COLOR     [_miscSP.getGridLineColor      ()];
        int   glStyle = StdPlotMiscSettingPanel.PREDEFINED_LINE_STYLE[_miscSP.getGridLineStyle      ()];

        boolean drawGrid       = (glStyle >= 0);
        boolean drawOriginAxis = (olStyle >= 0);

        // Process the left Y-axis
        if(hasLeft) {
            // Generate the data points
            double[][] ldata = new double[lyda.length][xda.length * 2];
            for(int i = 0; i < lyda.length; ++i) {
                double[] cury = lyda[i].data;
                int      leny = cury.length;
                int      pidx = 0;
                for(int j = 0; j < xda.length; ++j) {
                    ldata[i][pidx++] = xda[j];
                    ldata[i][pidx++] = (j >= leny) ? 0 : cury[j];
                }
            }
            // Instantiate the plot renderer class
            PlotRenderer pl = new PlotRenderer(g, w, h, xmin, xmax, xstp, xsdv, lymin, lymax, lystp, lysdv, draft);
            pl.setGeneralStyle(pbColor, pcColor, olColor, olStyle, alColor, glColor, glStyle, acColor, atColor);
            // Draw the background
            pl.drawBackground();
            // Draw the captions
            pl.drawCaption(mcapt, scapt);
            pl.drawAxisCaption(xcapt, hasLeft ? lycapt : null, hasRight ? rycapt : null);
            // Draw the axis
            pl.drawAxis(drawGrid, drawOriginAxis, true, true, false);
            // Draw the data points and lines
            pl.clipToPlotArea(true, true);
            for(int i = 0; i < lyda.length; ++i) {
                Color color  = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [lyda[i].color ];
                int   symbol = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [lyda[i].symbol];
                int   linest = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[lyda[i].line  ];
                if(symbol >= 0) pl.drawSymbolPoints(ldata[i], color, symbol);
                if(linest >= 0) pl.drawPolyline    (ldata[i], color, linest);
            }
            pl.clipToPlotArea(false, false);
        }

        // Process the right Y-axis
        if(hasRight) {
            // Generate the data points
            double[][] rdata = new double[ryda.length][xda.length * 2];
            for(int i = 0; i < ryda.length; ++i) {
                double[] cury = ryda[i].data;
                int      leny = cury.length;
                int      pidx = 0;
                for(int j = 0; j < xda.length; ++j) {
                    rdata[i][pidx++] = xda[j];
                    rdata[i][pidx++] = (j >= leny) ? 0 : cury[j];
                }
            }
            // Instantiate the plot renderer class
            PlotRenderer pl = new PlotRenderer(g, w, h, xmin, xmax, xstp, xsdv, rymin, rymax, rystp, rysdv, draft);
            pl.setGeneralStyle(pbColor, pcColor, olColor, olStyle, alColor, glColor, glStyle, acColor, atColor);
            // Draw the background, captions, and axis
            if(!hasLeft) {
                pl.drawBackground();
                pl.drawCaption(mcapt, scapt);
                pl.drawAxisCaption(xcapt, null, rycapt);
                pl.drawAxis(drawGrid, drawOriginAxis, true, false, true);
            }
            else
                pl.drawAxis(false, false, false, false, true);
            // Draw the data points and lines
            pl.clipToPlotArea(true, true);
            for(int i = 0; i < ryda.length; ++i) {
                Color color  = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [ryda[i].color ];
                int   symbol = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [ryda[i].symbol];
                int   linest = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[ryda[i].line  ];
                if(symbol >= 0) pl.drawSymbolPoints(rdata[i], color, symbol);
                if(linest >= 0) pl.drawPolyline    (rdata[i], color, linest);
            }
            pl.clipToPlotArea(false, false);
        }

        // Done
        return true;
    }

    // Calculate a content value
    public double calcContentValue(double y)
    { return 0; /* Not needed by this class */ }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak)
    { return ""; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    { return getXDataRange(_ssPanel, _inputDataRangeSP); }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    { return getYDataRange(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, rightYAxis, 0); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_captionSP.interfaceVersion());
        _captionSP.save(ds);
        
        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);

        ds.writeInt(_plotStyleSP.interfaceVersion());
        _plotStyleSP.save(ds);

        ds.writeInt(_axisScaleSP.interfaceVersion());
        _axisScaleSP.save(ds);

        ds.writeInt(_miscSP.interfaceVersion());
        _miscSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int ifv;
        
        ifv = ds.readInt();
        if(!_captionSP.load(ifv, ds)) return false;
        
        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;
        
        ifv = ds.readInt();
        if(!_plotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_axisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;
        
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a scatter-line-plot panel
    public ScatterLinePlotPanel()
    {
        super(4, new String[]{ _S("str_plot_view"), null }, null, false, false);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        String[] header = initResultPanel(_ssPanel, _inputDataRangeSP, _axisScaleSP, NUM_OF_Y_AXIS);

        if(header != null) {
            if(header.length >= 1) _captionSP.setXCaption     (header[0]);
            if(header.length >= 2) _captionSP.setLeftYCaption (header[1]);
            if(header.length >= 3) _captionSP.setRightYCaption(header[2]);
        }
    }
}

