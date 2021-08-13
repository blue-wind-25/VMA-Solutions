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
// Tab module - first-order-regression panel
//
public class FirstOrderRegPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 9;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _regCaptionSP     = null;
    private StdPlotCaptionSettingPanel   _resCaptionSP     = null;
    private StdAnalysisSettingPanel      _analysisSP       = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataPointSettingPanel _regPlotStyleSP   = null;
    private StdPlotDataPointSettingPanel _resPlotStyleSP   = null;
    private StdPlotAxisScaleSettingPanel _regAxisScaleSP   = null;
    private StdPlotAxisScaleSettingPanel _resAxisScaleSP   = null;
    private StdPlotMiscSettingPanel      _miscSP           = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the tab caption
    public String getTabCaption()
    { return _S("str_anal_for"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        _analysisSP = new StdAnalysisSettingPanel(true, true, false, false);
        title[0] = _S("str_acrs_anal");
        pane [0] = _analysisSP;

        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, null);
        title[1] = _S("str_acrs_data_range");
        pane [1] = _inputDataRangeSP;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[3];
        dpsSpec[0] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_regres_plot"), true,  true);
        dpsSpec[1] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_confidence" ), false, true);
        dpsSpec[2] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_prediction" ), false, true);
        _regPlotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[2] = _S("str_acrs_reg_pstyle");
        pane [2] = _regPlotStyleSP;

        _regAxisScaleSP = new StdPlotAxisScaleSettingPanel(this, false);
        title[3] = _S("str_acrs_reg_axis");
        pane [3] = _regAxisScaleSP;

        dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[2];
        dpsSpec[0] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_residual_plot"), true,  true);
        dpsSpec[1] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_res_stddev"   ), false, true);
        _resPlotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[4] = _S("str_acrs_res_pstyle");
        pane [4] = _resPlotStyleSP;

        _resAxisScaleSP = new StdPlotAxisScaleSettingPanel(this, false);
        title[5] = _S("str_acrs_lin_res_axis");
        pane [5] = _resAxisScaleSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[6] = _S("str_acrs_misc");
        pane [6] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _regCaptionSP = new StdPlotCaptionSettingPanel(false, false);
        _regCaptionSP.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        _resCaptionSP = new StdPlotCaptionSettingPanel(false, false);
        _resCaptionSP.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JTabbedPane tbpHolder = new JTabbedPane();;
            tbpHolder.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            GUtil.addTab(tbpHolder, _S("str_acrs_regres_plot"  ), null, null, _regCaptionSP, true);
            GUtil.addTab(tbpHolder, _S("str_acrs_residual_plot"), null, null, _resCaptionSP, true);

        return tbpHolder;
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
        StdPlotCaptionSettingPanel pnlCSetting = secondary ? _resCaptionSP : _regCaptionSP;
        String mcapt = pnlCSetting.getMainCaption ();
        String scapt = pnlCSetting.getSubCaption  ();
        String xcapt = pnlCSetting.getXCaption    ();
        String ycapt = pnlCSetting.getLeftYCaption();

        // Get the analysis setting
        double pp   = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];
        int    nofd = _analysisSP.getNumOfFDet();

        // Get the scale
        StdPlotAxisScaleSettingPanel pnlASetting = secondary ? _resAxisScaleSP : _regAxisScaleSP;
        double xmin = pnlASetting.getXMin();
        double xmax = pnlASetting.getXMax();
        double xstp = pnlASetting.getXStep();
        int    xsdv = pnlASetting.getXSDiv();
        double ymin = pnlASetting.getLeftYMin();
        double ymax = pnlASetting.getLeftYMax();
        double ystp = pnlASetting.getLeftYStep();
        int    ysdv = pnlASetting.getLeftYSDiv();

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
        FirstOrderRegression lin = new FirstOrderRegression(xda, yda, pp);

        // Determine the number of sampling
        int nos = draft ? (w / 4) : (w / 2);

        // --- Primary plot (regression) ---
        if(!secondary) {
            // Draw the confidence band
            Color colCB = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_regPlotStyleSP.getColor(1)];
            int   linCB = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_regPlotStyleSP.getLine (1)];
            if(linCB >= 0) {
                // Generate the data points
                double[][] data = new double[2][nos * 2];
                double     xi   = xmin;
                double     xinc = (xmax - xmin) / nos;
                int        idx  = 0;
                for(int i = 0; i < nos; ++i) {
                    double[] ci = lin.calcCIi(xi);
                    data[0][idx    ] = xi;
                    data[0][idx + 1] = ci[0];
                    data[1][idx    ] = xi;
                    data[1][idx + 1] = ci[1];
                    idx += 2;
                    xi  += xinc;
                }
                // Draw the lines
                pl.drawPolyline(data[0], colCB, linCB);
                pl.drawPolyline(data[1], colCB, linCB);
            }
            // Draw the prediction band
            Color colPB = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_regPlotStyleSP.getColor(2)];
            int   linPB = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_regPlotStyleSP.getLine (2)];
            if(linPB >= 0) {
                // Generate the data points
                double[][] data = new double[2][nos * 2];
                double     xi   = xmin;
                double     xinc = (xmax - xmin) / nos;
                int        idx  = 0;
                for(int i = 0; i < nos; ++i) {
                    double[] ci = lin.calcPIi(xi, nofd);
                    data[0][idx    ] = xi;
                    data[0][idx + 1] = ci[0];
                    data[1][idx    ] = xi;
                    data[1][idx + 1] = ci[1];
                    idx += 2;
                    xi  += xinc;
                }
                // Draw the lines
                pl.drawPolyline(data[0], colPB, linPB);
                pl.drawPolyline(data[1], colPB, linPB);
            }
            // Draw the regression plot
            Color colRP = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_regPlotStyleSP.getColor (0)];
            int   symRP = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [_regPlotStyleSP.getSymbol(0)];
            int   linRP = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_regPlotStyleSP.getLine  (0)];
            if(linRP >= 0) {
                // Generate the data points
                double[] data = new double[]{
                    xmin, lin.calcYi(xmin),
                    xmax, lin.calcYi(xmax),
                };
                // Draw the line
                pl.drawPolyline(data, colRP, linRP);
            }
            if(symRP >= 0) {
                /*
                // Generate the data points
                double[] xval = lin.getXValues();
                double[] yval = lin.getYValues();
                double[] data = new double[lin.getN() * 2];
                int      idx  = 0;
                for(int i = 0; i < xval.length; ++i) {
                    data[idx++] = xval[i];
                    data[idx++] = yval[i];
                }
                // Draw the data points
                pl.drawSymbolPoints(data, colRP, symRP);
                */
                // Get the values
                double[]   xval = lin.getXValues();
                double[][] yval = lin.getYValuesA();
                for(int c = 0; c < yval.length; ++c) {
                    // Generate the data points
                    double[]   data = new double[lin.getN() * 2];
                    int        idx  = 0;
                    for(int i = 0; i < xval.length; ++i) {
                        data[idx++] = xval[i];
                        data[idx++] = yval[c][i];
                    }
                    // Draw the data points
                    pl.drawSymbolPoints(data, colRP, symRP);
                }
            }
            // Draw the Xa
            if(true) {
                // Get the data
                double xa = lin.getXa();
                double ya = lin.getYa();
                double x1 = lin.getXValues()[0];
                // Draw the line
                pl.drawInfoVTick(xa, ymin, 5, alColor);
                // Draw the text
                String ms = (xa <= x1) ? "Xa ≤ X₁" : "Xa > X₁";
                pl.drawInfoText(ms, xa, ymin, -1, 1, atColor, 1, 3);
            }
        }

        // --- Secondary plot (residual) ---
        else {
            // Draw the residual plot
            Color colSP = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_resPlotStyleSP.getColor (0)];
            int   symSP = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [_resPlotStyleSP.getSymbol(0)];
            int   linSP = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_resPlotStyleSP.getLine  (0)];
            Color colSD = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_resPlotStyleSP.getColor (1)];
            int   linSD = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_resPlotStyleSP.getLine  (1)];
            if(linSP >= 0) {
                // Generate the data points
                double[] data = new double[]{
                    xmin, 0,
                    xmax, 0,
                };
                // Draw the line
                pl.drawPolyline(data, colSP, linSP);
            }
            if(symSP >= 0 && linSD >= 0) {
                // Generate the data points
                double[] xval = lin.getXValues();
                double[] rval = lin.getRValues();
                double[] data = new double[lin.getN() * 3];
                int      idx  = 0;
                for(int i = 0; i < xval.length; ++i) {
                    data[idx++] = xval[i];
                    data[idx++] = rval[i];
                    data[idx++] = lin.getSy();
                }
                // Draw the data points
                pl.drawSymbolPoints(data, colSP, symSP, colSD, linSD);
            }
            else if(symSP >= 0) {
                /*
                // Generate the data points
                double[] xval = lin.getXValues();
                double[] rval = lin.getRValues();
                double[] data = new double[lin.getN() * 2];
                int      idx  = 0;
                for(int i = 0; i < xval.length; ++i) {
                    data[idx++] = xval[i];
                    data[idx++] = rval[i];
                }
                // Draw the data points
                pl.drawSymbolPoints(data, colSP, symSP);
                */
                // Get the values
                double[]   xval = lin.getXValues();
                double[][] rval = lin.getRValuesA();
                for(int c = 0; c < rval.length; ++c) {
                    // Generate the data points
                    double[] data = new double[lin.getN() * 2];
                    int      idx  = 0;
                    for(int i = 0; i < xval.length; ++i) {
                        data[idx++] = xval[i];
                        data[idx++] = rval[c][i];
                    }
                    // Draw the data points
                    pl.drawSymbolPoints(data, colSP, symSP);
                }
            }
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

        FirstOrderRegression lin = new FirstOrderRegression(xda, yda, pp);

        double b  = lin.getB();
        double xa = lin.getXa();
        double ya = lin.getYa();
        double x1 = lin.getXValues()[0];

        boolean ed = (yda.length > 1);
        String  na = _S("res_res_na");
        double  Fr = lin.getFr();
        double  F1 = lin.getF1();

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_for"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "N",           "" + lin.getN(),
            "N2",          "" + lin.getN2(),
            "PP",          StringTranslator.format("%.1f", lin.getPP       ()),
            "t1",          StringTranslator.format("%.5g", lin.getT1       ()),
            "t2",          StringTranslator.format("%.5g", lin.getT2       ()),
            "a",           StringTranslator.format("%.5g", lin.getA        ()),
            "mo1",         (b < 0) ? "-" : "+",
            "b",           StringTranslator.format("%.5g", Math.abs(b)     ),
            "Xm",          StringTranslator.format("%.5g", lin.getXMean    ()),
            "Ym",          StringTranslator.format("%.5g", lin.getYMean    ()),
            "r",           StringTranslator.format("%.5g", lin.getR        ()),
            "RSS",         StringTranslator.format("%.5g", lin.getRSS      ()),
            "Sy",          StringTranslator.format("%.5g", lin.getSy       ()),
            "Sx0",         StringTranslator.format("%.5g", lin.getSx0      ()),
            "Vx0",         StringTranslator.format("%.5g", lin.getVx0      ()),
            "gValue",      StringTranslator.format("%.5g", lin.getGValue   ()),
            "QCmean",      StringTranslator.format("%.5g", lin.getQCMean   ()),
            "linearity",   StringTranslator.format("%.5g", lin.getLinearity()),
            "Sa",          StringTranslator.format("%.5g", lin.getSa       ()),
            "Sb",          StringTranslator.format("%.5g", lin.getSb       ()),
            "CIa",         StringTranslator.format("%.5g", lin.getCIa      ()),
            "CIb",         StringTranslator.format("%.5g", lin.getCIb      ()),
            "CL",          StringTranslator.format("%.5g", lin.getCL       ()),
            "CU",          StringTranslator.format("%.5g", lin.getCU       ()),
            "CIx1",        StringTranslator.format("%.5g", lin.getCIx1     ()),
            "CIrelx1",     StringTranslator.format("%.5g", lin.getCIrelx1  ()),
            "Ya",          StringTranslator.format("%.5g", ya                ),
            "Xa",          StringTranslator.format("%.5g", xa                ),
            "DL",          StringTranslator.format("%.5g", lin.getDL       ()),
            "QL",          StringTranslator.format("%.5g", lin.getQL       ()),
            "X1",          StringTranslator.format("%.5g", x1                ),
            "conclusion",  (xa <= x1) ? _S("res_res_good") : _S("res_res_bad"),
            "reason",      (xa <= x1) ? "(Xa ≤ X1)" : "(Xa > X1)",
            /* Fisher lack-of-fit test */
            "Nr",           "" + lin.getNr   (),
            "DFe",          "" + lin.getDFe  (),
            "DFlof",        "" + lin.getDFlof(),
            "F1",           StringTranslator.format("%.5g", F1            ),
            "SSr",          StringTranslator.format("%.5g", lin.getSSr  ()),
            "SSe",          StringTranslator.format("%.5g", lin.getSSe  ()),
            "SSlof",        StringTranslator.format("%.5g", lin.getSSlof()),
            "Vr",           StringTranslator.format("%.5g", lin.getVr   ()),
            "Ve",           StringTranslator.format("%.5g", lin.getVe   ()),
            "Vlof",         StringTranslator.format("%.5g", lin.getVlof ()),
            "Fr",           StringTranslator.format("%.5g", Fr            ),
            "conclusion2",  ( (Fr <= F1) ? _S("res_res_good") : _S("res_res_bad") ),
            "reason2",      ( (Fr <= F1) ? "(Fr ≤ F)" : "(Fr > F)" )
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate(
            ed ? "FirstOrderRegressionMulti" : "FirstOrderRegressionSingle",
            kvps, html, withNonEmptyDoubleLineBreak
        );
    }

    // Calculate a content value
    public double calcContentValue(double y) throws Exception
    {
        // Perform some calculations
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 2) {
            GUtil.showNoDDialogCalc();
            return 0;
        }

        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) {
            GUtil.showNoDDialogCalc();
            return 0;
        }

        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        FirstOrderRegression lin = new FirstOrderRegression(xda, yda, pp);

        // Return the value
        return lin.calcXh(y);
    }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    { return getXDataRange(_ssPanel, _inputDataRangeSP); }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    {
        // Return the normal data range for the regression plot
        if(requestor != _resAxisScaleSP) return getYDataRange(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, rightYAxis, 0);

        // For the residual plot, we need to do some calculations
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= 0) return new double[]{ 0, 0 };

        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) return new double[]{ 0, 0 };

        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        try { return (new FirstOrderRegression(xda, yda, pp)).getYrRange(); }
        catch(Exception e) { return new double[]{ 0, 0 }; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_regCaptionSP.interfaceVersion());
        _regCaptionSP.save(ds);

        ds.writeInt(_resCaptionSP.interfaceVersion());
        _resCaptionSP.save(ds);

        ds.writeInt(_analysisSP.interfaceVersion());
        _analysisSP.save(ds);

        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);

        ds.writeInt(_regPlotStyleSP.interfaceVersion());
        _regPlotStyleSP.save(ds);

        ds.writeInt(_regAxisScaleSP.interfaceVersion());
        _regAxisScaleSP.save(ds);

        ds.writeInt(_resPlotStyleSP.interfaceVersion());
        _resPlotStyleSP.save(ds);

        ds.writeInt(_resAxisScaleSP.interfaceVersion());
        _resAxisScaleSP.save(ds);

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
        if(!_resCaptionSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_analysisSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_regPlotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_regAxisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_resPlotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_resAxisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a first-order-regression class
    public FirstOrderRegPanel()
    {
        super(7, new String[]{ _S("str_reg_plot_view"), "tab_preg_for" }, new String[]{ _S("str_res_plot_view"), "tab_pres" }, true, true);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        String[] header = initResultPanel(_ssPanel, _inputDataRangeSP, _regAxisScaleSP, NUM_OF_Y_AXIS);
        _resAxisScaleSP.autoCalcScale();

        if(header != null) {
            if(header.length >= 1) {
                _regCaptionSP.setXCaption(header[0]);
                _resCaptionSP.setXCaption(header[0]);
            }
            if(header.length >= 2) {
                _regCaptionSP.setLeftYCaption(header[1]);
                _resCaptionSP.setLeftYCaption(header[1]);
            }
        }

        updateReport();
    }
}
