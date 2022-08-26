/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
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
// Tab module - accuracy-test panel
//
public class AccuracyPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 9;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _regCaptionSP     = null;
    private StdAnalysisSettingPanel      _analysisSP       = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataPointSettingPanel _regPlotStyleSP   = null;
    private StdPlotAxisScaleSettingPanel _regAxisScaleSP   = null;
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
    { return _S("str_anal_accuracy"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        _analysisSP = new StdAnalysisSettingPanel(true, false, false, false);
        title[0] = _S("str_acrs_anal");
        pane [0] = _analysisSP;

        String[] drsCapt = new String[NUM_OF_Y_AXIS + 1];
        drsCapt[0] = _S("str_x_values_acc_xc");
        for(int i = 1; i <= NUM_OF_Y_AXIS; ++i) {
            drsCapt[i] = _F("str_x_values_acc_xf_T", new String[] { "" + i });
        }

        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, drsCapt);
        title[1] = _S("str_acrs_data_range");
        pane [1] = _inputDataRangeSP;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[1];
        dpsSpec[0] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_regres_plot"), true,  true);
        _regPlotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[2] = _S("str_acrs_reg_pstyle");
        pane [2] = _regPlotStyleSP;

        _regAxisScaleSP = new StdPlotAxisScaleSettingPanel(this, false);
        title[3] = _S("str_acrs_reg_axis");
        pane [3] = _regAxisScaleSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[4] = _S("str_acrs_misc");
        pane [4] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _regCaptionSP = new StdPlotCaptionSettingPanel(false, false);
        return _regCaptionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    {
        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 2) {
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
        String mcapt = _regCaptionSP.getMainCaption ();
        String scapt = _regCaptionSP.getSubCaption  ();
        String xcapt = _regCaptionSP.getXCaption    ();
        String ycapt = _regCaptionSP.getLeftYCaption();

        // Get the analysis setting
        double pp   = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        // Get the scale
        double xmin = _regAxisScaleSP.getXMin();
        double xmax = _regAxisScaleSP.getXMax();
        double xstp = _regAxisScaleSP.getXStep();
        int    xsdv = _regAxisScaleSP.getXSDiv();
        double ymin = _regAxisScaleSP.getLeftYMin();
        double ymax = _regAxisScaleSP.getLeftYMax();
        double ystp = _regAxisScaleSP.getLeftYStep();
        int    ysdv = _regAxisScaleSP.getLeftYSDiv();

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
        Accuracy acc = new Accuracy(xda, yda, pp);

        // Determine the number of sampling
        int nos = draft ? (w / 4) : (w / 2);

        // Draw the regression plot
        Color colRP = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_regPlotStyleSP.getColor (0)];
        int   symRP = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [_regPlotStyleSP.getSymbol(0)];
        int   linRP = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_regPlotStyleSP.getLine  (0)];
        if(linRP >= 0) {
            // Generate the data points
            double[] data = new double[]{
                xmin, acc.calcYi(xmin),
                xmax, acc.calcYi(xmax)
            };
            // Draw the line
            pl.drawPolyline(data, colRP, linRP);
        }
        if(symRP >= 0) {
            // Generate the data points
            double[] xval = acc.getXValues();
            double[] yval = acc.getYValues();
            double[] data = new double[acc.getN() * 2];
            int      idx  = 0;
            for(int i = 0; i < xval.length; ++i) {
                data[idx++] = xval[i];
                data[idx++] = yval[i];
            }
            // Draw the data points
            pl.drawSymbolPoints(data, colRP, symRP);
        }

        // Done
        return true;
    }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception
    {
        // Perform some calculations
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 2) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        String mcapt = _regCaptionSP.getMainCaption();
        String scapt = _regCaptionSP.getSubCaption ();

        Accuracy acc = new Accuracy(xda, yda, pp);

        double  CIa = acc.getCIaf();
        double  a   = acc.getA();
        double  a0  = a - CIa;
        double  a1  = a + CIa;
        boolean aOK = (a0 <= 0) && (0 <= a1);

        double  CIb = acc.getCIbf();
        double  b   = acc.getB();
        double  b0  = b - CIb;
        double  b1  = b + CIb;
        boolean bOK = (b0 <= 1) && (1 <= b1);

        String resA = null;
        String resB = null;

        if(html) {
            if(aOK) resA = _F("res_acc_include",     new String[]{ "CI<sub>af</sub>", "af = 0" });
            else    resA = _F("res_acc_not_include", new String[]{ "CI<sub>af</sub>", "af = 0" });
            if(bOK) resB = _F("res_acc_include",     new String[]{ "CI<sub>bf</sub>", "bf = 1" });
            else    resB = _F("res_acc_not_include", new String[]{ "CI<sub>bf</sub>", "bf = 1" });
        }
        else {
            if(aOK) resA = _F("res_acc_include",     new String[]{ "CIaf", "af = 0" });
            else    resA = _F("res_acc_not_include", new String[]{ "CIaf", "af = 0" });
            if(bOK) resB = _F("res_acc_include",     new String[]{ "CIbf", "bf = 1" });
            else    resB = _F("res_acc_not_include", new String[]{ "CIbf", "bf = 1" });
        }

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_accuracy"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "N",           "" + acc.getN(),
            "N2",          "" + acc.getN2(),
            "PP",          StringTranslator.format("%.1f", acc.getPP   ()),
            "t2",          StringTranslator.format("%.5g", acc.getT2   ()),
            "af",          StringTranslator.format("%.5g", a             ),
            "mo1",         (b < 0) ? "-" : "+",
            "bf",          StringTranslator.format("%.5g", Math.abs(b)   ),
            "Xc",          StringTranslator.format("%.5g", acc.getXMean()),
            "Xf",          StringTranslator.format("%.5g", acc.getYMean()),
            "r",           StringTranslator.format("%.5g", acc.getR    ()),
            "RSS",         StringTranslator.format("%.5g", acc.getRSS  ()),
            "Sy",          StringTranslator.format("%.5g", acc.getSy   ()),
            "Sx0",         StringTranslator.format("%.5g", acc.getSx0  ()),
            "Vx0",         StringTranslator.format("%.5g", acc.getVx0  ()),
            "Saf",         StringTranslator.format("%.5g", acc.getSaf  ()),
            "Sbf",         StringTranslator.format("%.5g", acc.getSbf  ()),
            "CIaf",        StringTranslator.format("%.5g", CIa           ),
            "CIbf",        StringTranslator.format("%.5g", CIb           ),
            "pR",          StringTranslator.format("%.5g", acc.getPR   ()),
            "pRSd",        StringTranslator.format("%.5g", acc.getPRSd ()),
            "conclusion",  (aOK && bOK) ? _S("res_res_good") : _S("res_res_bad"),
            "reason",      "(" + resA + ", " + resB + ")"
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("Accuracy", kvps, html, withNonEmptyDoubleLineBreak);
    }

    // Calculate a content value
    public double calcContentValue(double y) throws Exception
    { return 0; /* Not needed by this class */ }

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
        ds.writeInt(_regCaptionSP.interfaceVersion());
        _regCaptionSP.save(ds);

        ds.writeInt(_analysisSP.interfaceVersion());
        _analysisSP.save(ds);

        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);

        ds.writeInt(_regPlotStyleSP.interfaceVersion());
        _regPlotStyleSP.save(ds);

        ds.writeInt(_regAxisScaleSP.interfaceVersion());
        _regAxisScaleSP.save(ds);

        ds.writeInt(_miscSP.interfaceVersion());
        _miscSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int ifv;

        ifv = ds.readInt();
        if(!_regCaptionSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_analysisSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_regPlotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_regAxisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a accuracy-test class
    public AccuracyPanel()
    {
        super(5, new String[]{ _S("str_reg_plot_view"), "tab_preg_accuracy" }, null, true, false);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        String[] header = initResultPanel(_ssPanel, _inputDataRangeSP, _regAxisScaleSP, NUM_OF_Y_AXIS);

        if(header != null) {
            if(header.length >= 1) _regCaptionSP.setXCaption    (header[0]);
            if(header.length >= 2) _regCaptionSP.setLeftYCaption(header[1]);
        }

        updateReport();
    }
}
