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
import anemonesoft.stat.*;

//
// Tab module - quality-control-shewhart panel
//
public class QCShewhartPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 9;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _qctCaptionSP     = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataPointSettingPanel _qctPlotStyleSP   = null;
    private StdPlotAxisScaleSettingPanel _qctAxisScaleSP   = null;
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
    { return _S("str_anal_qcshewhart"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        String[] drsCapt = new String[NUM_OF_Y_AXIS + 1];
        drsCapt[0] = _S("str_x_values_qcs_control");
        for(int i = 1; i <= NUM_OF_Y_AXIS; ++i) {
            drsCapt[i] = _F("str_y_values_qcs_smpl_T", new String[] { "" + i });
        }
        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, drsCapt);
        title[0] = _S("str_acrs_data_range");
        pane [0] = _inputDataRangeSP;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[3 + NUM_OF_Y_AXIS];
        dpsSpec[0] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_mean_plot"), true, true);
        dpsSpec[1] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_mean_2sy"), false, true);
        dpsSpec[2] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_mean_3sy"), false, true);
        for(int i = 1; i <= NUM_OF_Y_AXIS; ++i) {
            dpsSpec[i + 2] = new StdPlotDataPointSettingPanel.DPSSpec(_F("str_y_values_qcs_smpl_T", new String[] { "" + i }), true, false);
        }
        _qctPlotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[1] = _S("str_acrs_qcs_pstyle");
        pane [1] = _qctPlotStyleSP;

        _qctAxisScaleSP = new StdPlotAxisScaleSettingPanel(this, false);
        title[2] = _S("str_acrs_axis_scale");
        pane [2] = _qctAxisScaleSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[3] = _S("str_acrs_misc");
        pane [3] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _qctCaptionSP = new StdPlotCaptionSettingPanel(false, false);
        return _qctCaptionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    {
        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 1) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        // Get the captions
        String mcapt = _qctCaptionSP.getMainCaption ();
        String scapt = _qctCaptionSP.getSubCaption  ();
        String xcapt = _qctCaptionSP.getXCaption    ();
        String ycapt = _qctCaptionSP.getLeftYCaption();

        // Get the scale
        double xmin = _qctAxisScaleSP.getXMin();
        double xmax = _qctAxisScaleSP.getXMax();
        double xstp = _qctAxisScaleSP.getXStep();
        int    xsdv = _qctAxisScaleSP.getXSDiv();
        double ymin = _qctAxisScaleSP.getLeftYMin();
        double ymax = _qctAxisScaleSP.getLeftYMax();
        double ystp = _qctAxisScaleSP.getLeftYStep();
        int    ysdv = _qctAxisScaleSP.getLeftYSDiv();

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

        // Instantiate the plot renderer class
        PlotRenderer pl = new PlotRenderer(g, w, h, xmin, xmax, xstp, xsdv, ymin, ymax, ystp, ysdv, draft);
        pl.setGeneralStyle(pbColor, pcColor, olColor, olStyle, alColor, glColor, glStyle, acColor, atColor);

        // Draw the background, caption, and axis
        pl.drawBackground();
        pl.drawCaption(mcapt, scapt);
        pl.drawAxisCaption(xcapt, ycapt, null);
        pl.clipToPlotArea(true, false);
        pl.drawAxis(drawGrid, drawOriginAxis, true, true, false);
        pl.clipToPlotArea(true, true);

        // Instantiate the analysis class
        QCShewhart qcs = new QCShewhart(xda);

        // Determine the number of sampling
        int nos = draft ? (w / 4) : (w / 2);

        // Draw the mean plot
        Color colMP = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_qctPlotStyleSP.getColor (0)];
        int   symMP = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [_qctPlotStyleSP.getSymbol(0)];
        int   linMP = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_qctPlotStyleSP.getLine  (0)];
        if(linMP >= 0) {
            // Generate the data points
            double[] data = new double[]{
                xmin, qcs.getYMean(),
                xmax, qcs.getYMean()
            };
            // Draw the line
            pl.drawPolyline(data, colMP, linMP);
        }
        if(symMP >= 0) {
            // Generate the data points
            double[] yval = qcs.getYValues();
            double[] data = new double[yval.length * 2];
            int      idx  = 0;
            for(int i = 0; i < yval.length; ++i) {
                data[idx++] = i + 1;
                data[idx++] = yval[i];
            }
            // Draw the data points
            pl.drawSymbolPoints(data, colMP, symMP);
            // Draw the text
            pl.drawInfoText("\u2009s\u0305", xmin, qcs.getYMean(), -1,  1, colMP, 2, 1);
        }

        // Draw the (2 * Sy) band
        Color colIB = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_qctPlotStyleSP.getColor(1)];
        int   linIB = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_qctPlotStyleSP.getLine (1)];
        if(linMP >= 0) {
            // Generate the data points
            double     y0   = qcs.getYMean() - qcs.get2Sy();
            double     y1   = qcs.getYMean() + qcs.get2Sy();
            double[][] data = new double[][]{
                { xmin, y0, xmax, y0 },
                { xmin, y1, xmax, y1 }
            };
            // Draw the line
            pl.drawPolyline(data[0], colIB, linIB);
            pl.drawPolyline(data[1], colIB, linIB);
            // Draw the texts
            pl.drawInfoText("-2Sd", xmin, y0, -1,  1, colIB, 2, 1);
            pl.drawInfoText("+2Sd", xmin, y1, -1, -1, colIB, 2, 1);
        }
        
        // Draw the (3 * Sy) band
        Color colOB = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_qctPlotStyleSP.getColor(2)];
        int   linOB = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_qctPlotStyleSP.getLine (2)];
        if(linMP >= 0) {
            // Generate the data points
            double     y0   = qcs.getYMean() - qcs.get3Sy();
            double     y1   = qcs.getYMean() + qcs.get3Sy();
            double[][] data = new double[][]{
                { xmin, y0, xmax, y0 },
                { xmin, y1, xmax, y1 }
            };
            // Draw the line
            pl.drawPolyline(data[0], colOB, linOB);
            pl.drawPolyline(data[1], colOB, linOB);
            // Draw the texts
            pl.drawInfoText("-3Sd", xmin, y0, -1,  1, colOB, 2, 1);
            pl.drawInfoText("+3Sd", xmin, y1, -1, -1, colOB, 2, 1);
        }

        // Draw the samples
        for(int i = 0; i < yda.length; ++i) {
            // Get the settings
            Color colSM = StdPlotDataPointSettingPanel.PREDEFINED_COLOR [_qctPlotStyleSP.getColor (i + 3)];
            int   symSM = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL[_qctPlotStyleSP.getSymbol(i + 3)];
            if(symMP < 0) continue;
            // Generate the data points
            double[] yval = yda[i];
            double[] data = new double[yval.length * 2];
            int      idx  = 0;
            for(int j = 0; j < yval.length; ++j) {
                data[idx++] = j + 1;
                data[idx++] = yval[j];
            }
            // Draw the data points
            pl.drawSymbolPoints(data, colSM, symSM);
        }

        // Done
        return true;
    }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception
    {
        // Perform some calculations
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 1) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) {
            GUtil.showNoDDialogReport();
            return "";
        }

        String mcapt = _qctCaptionSP.getMainCaption();
        String scapt = _qctCaptionSP.getSubCaption ();

        QCShewhart qcs = new QCShewhart(xda);

        // Generate the details
        StringBuilder details = new StringBuilder();
        String        tab     = _S("res_qcs_smpl_tab");
        for(int i = 0; i < yda.length; ++i) {
            QCShewhart qcss = new QCShewhart(yda[i]);

            details.append(_F("res_qcs_smpl_T", new String[]{ "" + (i + 1) }));
            details.append("\n");

            details.append(tab);
            details.append(_F("res_qcs_smpl_n_T", new String[]{ "" + qcss.getN() }));
            details.append("\n");
            
            details.append(tab);
            details.append(_F("res_qcs_smpl_mn_T", new String[]{ StringTranslator.format("%.5g", qcss.getYMean()) }));
            details.append("\n");
            
            details.append(tab);
            details.append(_F("res_qcs_smpl_sy_T", new String[]{ StringTranslator.format("%.5g", qcss.getSy()) }));
            if(i < yda.length - 1) details.append("\n");
        }
        
        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_qcshewhart"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "CN",          "" + qcs.getN(),
            "CYm",         StringTranslator.format("%.5g", qcs.getYMean()),
            "CSy",         StringTranslator.format("%.5g", qcs.getSy   ()),
            "C2Sy",        StringTranslator.format("%.5g", qcs.get2Sy  ()),
            "C3Sy",        StringTranslator.format("%.5g", qcs.get3Sy  ()),
            "details",     details.toString()
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("QCShewhart", kvps, html, withNonEmptyDoubleLineBreak);
    }

    // Calculate a content value
    public double calcContentValue(double y) throws Exception
    { return 0; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    {
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null) return new double[]{ 0, 0 };
        
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null) return new double[]{ 0, 0 };

        int max = xda.length;
        for(int i = 0; i < yda.length; ++i) {
            if(yda[i].length > max) max = yda[i].length;
        }

        return new double[]{ 0, max };
    }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    {
        double[] xdr = getXDataRange(_ssPanel, _inputDataRangeSP);
        double[] ydr = getYDataRange(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, rightYAxis, 0);

        return new double[]{
            (xdr[0] <= ydr[0]) ? xdr[0] : ydr[0],
            (xdr[1] >= ydr[1]) ? xdr[1] : ydr[1]
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_qctCaptionSP.interfaceVersion());
        _qctCaptionSP.save(ds);

        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);

        ds.writeInt(_qctPlotStyleSP.interfaceVersion());
        _qctPlotStyleSP.save(ds);

        ds.writeInt(_qctAxisScaleSP.interfaceVersion());
        _qctAxisScaleSP.save(ds);

        ds.writeInt(_miscSP.interfaceVersion());
        _miscSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int ifv;

        ifv = ds.readInt();
        if(!_qctCaptionSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_qctPlotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_qctAxisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a quality-control-shewhart class
    public QCShewhartPanel()
    {
        super(4, new String[]{ _S("str_plot_view"), "tab_pqcshewhart" }, null, true, false);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        String[] header = initResultPanel(_ssPanel, _inputDataRangeSP, _qctAxisScaleSP, NUM_OF_Y_AXIS);

        if(header != null) {
            if(header.length >= 1) _qctCaptionSP.setXCaption    (header[0]);
            if(header.length >= 2) _qctCaptionSP.setLeftYCaption(header[1]);
        }

        updateReport();
    }
}
