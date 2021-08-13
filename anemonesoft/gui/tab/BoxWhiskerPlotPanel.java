/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.tab;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;
import anemonesoft.stat.*;

//
// Tab module - box-whisker-plot panel
//
public class BoxWhiskerPlotPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 15;

    // Number of custom tick labels
    private static int NUM_OF_CUSTOM_TICK_LABELS = 25;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _captionSP        = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataRangeCaptionPanel _inputDataCaptSP  = null;
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
    { return _S("str_plot_box_wk"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_plot"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        String[] drsCapt = new String[NUM_OF_Y_AXIS + 1];
        drsCapt[0] = null; // No X-Input
        for(int i = 0; i < NUM_OF_Y_AXIS; ++i) {
            char chr = (char) (i + 65);
            drsCapt[i + 1] = _F("str_y_values_series_T", new String[] { "" + chr });
        }
        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, true, drsCapt);
        title[0] = _S("str_acrs_data_range");
        pane [0] = _inputDataRangeSP;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[NUM_OF_Y_AXIS];
        for(int i = 0; i < NUM_OF_Y_AXIS; ++i) {
            char chr = (char) (i + 65);
            dpsSpec[i] = new StdPlotDataPointSettingPanel.DPSSpec(_F("str_acrs_tick_T", new String[]{ "" + chr }), false, true);
        }
        _plotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[1] = _S("str_acrs_data_style");
        pane [1] = _plotStyleSP;

        _axisScaleSP = new StdPlotAxisScaleSettingPanel(this, true);
        title[2] = _S("str_acrs_axis_scale");
        pane [2] = _axisScaleSP;

        String[] drcCapt = new String[NUM_OF_CUSTOM_TICK_LABELS];
        for(int i = 0; i < NUM_OF_CUSTOM_TICK_LABELS; ++i) {
            drcCapt[i] = _F("str_acrs_tick_T", new String[] { "" + (i + 1) });
        }
        _inputDataCaptSP = new StdPlotDataRangeCaptionPanel(NUM_OF_CUSTOM_TICK_LABELS, drcCapt, "T-");
        title[3] = _S("str_acrs_drcapt_xtlbl");
        pane [3] = _inputDataCaptSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[4] = _S("str_acrs_misc");
        pane [4] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        JLabel lblInfo = new JLabel(_S("str_box_wk_capt_note"), JLabel.LEFT);
        lblInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        _captionSP = new StdPlotCaptionSettingPanel(false, true);

        JPanel pnlHodler = new JPanel(new BorderLayout(0, 5), true);
            pnlHodler.add(lblInfo, BorderLayout.NORTH);
            pnlHodler.add(_captionSP, BorderLayout.CENTER);

        return pnlHodler;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    {
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

        // Generate the axis tick strings
        ArrayList<String> customXAxisTick = new ArrayList<String>();
        for(int i = 0; i < NUM_OF_CUSTOM_TICK_LABELS; ++i) {
            customXAxisTick.add(_inputDataCaptSP.getCaption(i));
        }
        String[] customXAxisTickStr = null;
        if(customXAxisTick.size() > 0) {
            customXAxisTickStr = new String[customXAxisTick.size()];
            customXAxisTick.toArray(customXAxisTickStr);
        }

        // Process the left Y-axis
        if(hasLeft) {
            // Extract the data and instantiate the analysis class
            double[][] ly = new double[lyda.length][];
            for(int i = 0; i < lyda.length; ++i) {
                ly[i] = lyda[i].data;
            }
            BoxWhisker lbox = new BoxWhisker(ly);
            // Instantiate the plot renderer class
            PlotRenderer pl = new PlotRenderer(g, w, h, xmin, xmax, xstp, xsdv, lymin, lymax, lystp, lysdv, draft);
            pl.setGeneralStyle(pbColor, pcColor, olColor, olStyle, alColor, glColor, glStyle, acColor, atColor);
            // Draw the background
            pl.drawBackground();
            // Draw the captions
            pl.drawCaption(mcapt, scapt);
            pl.drawAxisCaption(xcapt, hasLeft ? lycapt : null, hasRight ? rycapt : null);
            // Draw the axis
            pl.drawAxis(drawGrid, drawOriginAxis, true, true, false, customXAxisTickStr);
            // Draw the series results
            pl.clipToPlotArea(true, true);
            for(int i = 0; i < lbox.getNumOfSeries(); ++i) {
                // Get the result
                BoxWhisker.Result res = lbox.getSeriesResult(i);
                if(!res.valid) continue;
                // Generate the line data
                double ofs = Math.abs(xstp) / 10;
                double x0  = res.x - ofs;
                double x1  = res.x + ofs;
                double[] line = new double[]{
                    x0,    res.q1,  x1,    res.q1,  // Bottom bar
                    x0,    res.q3,  x1,    res.q3,  // Top bar
                    x0,    res.q1,  x0,    res.q3,  // From Q1 to Q3 (left)
                    x1,    res.q1,  x1,    res.q3,  // From Q1 to Q3 (right)
                    res.x, res.min, res.x, res.q1,  // From min to Q1 (middle)
                    res.x, res.q3,  res.x, res.max, // From Q3 to max (middle)
                    x0,    res.min, x1,    res.min, // Minimum bar
                    x0,    res.max, x1,    res.max  // Maximum bar
                };
                // Draw the lines
                pl.drawLines(line, StdPlotDataPointSettingPanel.PREDEFINED_COLOR[lyda[i].color], StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[lyda[i].line]);
                // Draw the mean
                pl.drawSymbolPoint(res.x, res.mean, PlotRenderer.SYM_CHAR_PLUS);
                // Draw the outliers
                for(int j = 0; j < res.outliers.length; ++j) {
                    pl.drawSymbolPoint(res.x, res.outliers[j], PlotRenderer.SYM_OPEN_CIRCLE);
                }
            }
            pl.clipToPlotArea(false, false);
        }

        // Process the right Y-axis
        if(hasRight) {
            // Extract the data and instantiate the analysis class
            double[][] ry = new double[ryda.length][];
            for(int i = 0; i < ryda.length; ++i) {
                ry[i] = ryda[i].data;
            }
            BoxWhisker rbox = new BoxWhisker(ry);
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
            // Draw the series results
            pl.clipToPlotArea(true, true);
            for(int i = 0; i < rbox.getNumOfSeries(); ++i) {
                // Get the result
                BoxWhisker.Result res = rbox.getSeriesResult(i);
                if(!res.valid) continue;
                // Generate the line data
                double ofs = Math.abs(xstp) / 10;
                double x0  = res.x - ofs;
                double x1  = res.x + ofs;
                double[] line = new double[]{
                    x0,    res.q1,  x1,    res.q1,  // Bottom bar
                    x0,    res.q3,  x1,    res.q3,  // Top bar
                    x0,    res.q1,  x0,    res.q3,  // From Q1 to Q3 (left)
                    x1,    res.q1,  x1,    res.q3,  // From Q1 to Q3 (right)
                    res.x, res.min, res.x, res.q1,  // From min to Q1 (middle)
                    res.x, res.q3,  res.x, res.max, // From Q3 to max (middle)
                    x0,    res.min, x1,    res.min, // Minimum bar
                    x0,    res.max, x1,    res.max  // Maximum bar
                };
                // Draw the lines
                pl.drawLines(line, StdPlotDataPointSettingPanel.PREDEFINED_COLOR[ryda[i].color], StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[ryda[i].line]);
                // Draw the mean
                pl.drawSymbolPoint(res.x, res.mean, PlotRenderer.SYM_CHAR_PLUS);
                // Draw the outliers
                for(int j = 0; j < res.outliers.length; ++j) {
                    pl.drawSymbolPoint(res.x, res.outliers[j], PlotRenderer.SYM_OPEN_CIRCLE);
                }
            }
            pl.clipToPlotArea(false, false);
        }

        // Done
        return true;
    }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak)
    { return ""; /* Not needed by this class */ }

    // Calculate a content value
    public double calcContentValue(double y)
    { return 0; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    { return getXDataRangeFromFirstRowOfYInputs(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS); }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    { return getYDataRange(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, rightYAxis, 1); }

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

        ds.writeInt(_inputDataCaptSP.interfaceVersion());
        _inputDataCaptSP.save(ds);

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
        if(!_inputDataCaptSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_plotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_axisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a box-whisker-plot panel
    public BoxWhiskerPlotPanel()
    {
        super(5, new String[]{ _S("str_plot_view"), null }, null, false, false);
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

