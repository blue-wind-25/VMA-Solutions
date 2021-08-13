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
// Tab module - ap-gonzales panel
//
public class APGonzalesPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of concentrations
    private static int NUM_OF_CONCS = 25;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _apfCaptionSP     = null;
    private StdAnalysisSettingPanel      _analysisSP       = null;
    private StdPlotDataPointSettingPanel _apfPlotStyleSP   = null;
    private StdPlotAxisScaleSettingPanel _apfAxisScaleSP   = null;
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
    { return _S("str_anal_ap_gonza"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        _analysisSP = new StdAnalysisSettingPanel(true, false, true, false);
        title[0] = _S("str_acrs_anal");
        pane [0] = _analysisSP;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[4];
        dpsSpec[0] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_beti_plot"  ), true,  true);
        dpsSpec[1] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_delta_plot" ), true,  true);
        dpsSpec[2] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_lambda_line"), false, true);
        dpsSpec[3] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_limit_line" ), false, true);
        _apfPlotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[1] = _S("str_acrs_ap_pstyle");
        pane [1] = _apfPlotStyleSP;

        _apfAxisScaleSP = new StdPlotAxisScaleSettingPanel(this, false);
        title[2] = _S("str_acrs_ap_axis");
        pane [2] = _apfAxisScaleSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[3] = _S("str_acrs_misc");
        pane [3] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _apfCaptionSP = new StdPlotCaptionSettingPanel(false, false);
        return _apfCaptionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    {
        _ssPanel.resetLastInvalidColumn();

        // Get the prerequisite analysis classes
        ResultPanel[] resOWANO      = GUIMain.instance.getTabsByClass(PrecisionOWANOPanel.class);
        ResultPanel[] resRobustness = GUIMain.instance.getTabsByClass(RobustnessPanel    .class);
        if(resOWANO == null || (resRobustness != null && resRobustness.length != resOWANO.length)) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        // Get the analyzer instances
        PrecisionOWANO[] anlOWANO      = new PrecisionOWANO[resOWANO.length];
        Robustness[]     anlRobustness = null;
        for(int i = 0; i < resOWANO.length; ++i) anlOWANO[i] = (PrecisionOWANO) resOWANO[i].getAnalyzer();
        if(resRobustness != null) {
            anlRobustness = new Robustness[resRobustness.length];
            for(int i = 0; i < resRobustness.length; ++i) anlRobustness[i] = (Robustness) resRobustness[i].getAnalyzer();
        }

        // Limit the number of factors
        if(anlRobustness != null) {
            for(int i = 0; i < anlRobustness.length; ++i) {
                if(anlRobustness[i].getFactors().length > 7 + 1) {
                    GUtil.showNoDDialogPlot();
                    return false;
                }
            }
        }

        // Get the captions
        String mcapt = _apfCaptionSP.getMainCaption ();
        String scapt = _apfCaptionSP.getSubCaption  ();
        String xcapt = _apfCaptionSP.getXCaption    ();
        String ycapt = _apfCaptionSP.getLeftYCaption();

        // Get the analysis setting
        double pp     = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];
        double lambda = _analysisSP.getLambda();

        // Get the scale
        double xmin = _apfAxisScaleSP.getXMin();
        double xmax = _apfAxisScaleSP.getXMax();
        double xstp = _apfAxisScaleSP.getXStep();
        int    xsdv = _apfAxisScaleSP.getXSDiv();
        double ymin = _apfAxisScaleSP.getLeftYMin();
        double ymax = _apfAxisScaleSP.getLeftYMax();
        double ystp = _apfAxisScaleSP.getLeftYStep();
        int    ysdv = _apfAxisScaleSP.getLeftYSDiv();

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
        APGonzales apf = new APGonzales(anlOWANO, anlRobustness, pp, lambda);

        // Determine the number of sampling
        int nos = draft ? (w / 4) : (w / 2);

        // Draw the lambda lines
        Color colLL = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_apfPlotStyleSP.getColor(2)];
        int   linLL = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_apfPlotStyleSP.getLine (2)];
        if(true) {
            // Draw the lines
            pl.drawLines(new double[]{ xmin,  lambda, xmax,  lambda }, colLL, linLL);
            pl.drawLines(new double[]{ xmin, -lambda, xmax, -lambda }, colLL, linLL);
            // Draw the texts
            pl.drawInfoText("+\u03BB", xmin,  lambda, -1,  1, colLL, 2, 1);
            pl.drawInfoText("-\u03BB", xmin, -lambda, -1, -1, colLL, 2, 1);
        }

        // Draw the delta-plot
        Color colDP = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_apfPlotStyleSP.getColor (1)];
        int   symDP = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [_apfPlotStyleSP.getSymbol(1)];
        int   linDP = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_apfPlotStyleSP.getLine  (1)];
        if(true) {
            double[] deltaP = apf.getDeltaP();
            double[] dlData = new double[deltaP.length * 2];
            int      idx    = 0;
            for(int i = 0; i < deltaP.length; ++i) {
                dlData[idx++] = anlOWANO[i].getTVal();
                dlData[idx++] = deltaP[i];
            }
            if(symDP >= 0) pl.drawSymbolPoints(dlData, colDP, symDP);
            if(linDP >= 0) pl.drawPolyline    (dlData, colDP, linDP);
        }

        // Draw the β-ETI plot
        Color colBE = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_apfPlotStyleSP.getColor (0)];
        int   symBE = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL    [_apfPlotStyleSP.getSymbol(0)];
        int   linBE = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_apfPlotStyleSP.getLine  (0)];
        if(true) {
            double[] beti   = apf.getBETIh();
            double[] dlData = new double[beti.length * 2];
            int      idx    = 0;
            for(int i = 0; i < beti.length; ++i) {
                dlData[idx++] = anlOWANO[i].getTVal();
                dlData[idx++] = beti[i];
            }
            if(symDP >= 0) pl.drawSymbolPoints(dlData, colBE, symBE);
            if(linDP >= 0) pl.drawPolyline    (dlData, colBE, linBE);
        }
        if(true) {
            double[] beti   = apf.getBETIl();
            double[] dlData = new double[beti.length * 2];
            int      idx    = 0;
            for(int i = 0; i < beti.length; ++i) {
                dlData[idx++] = anlOWANO[i].getTVal();
                dlData[idx++] = beti[i];
            }
            if(symDP >= 0) pl.drawSymbolPoints(dlData, colBE, symBE);
            if(linDP >= 0) pl.drawPolyline    (dlData, colBE, linBE);
        }

        // Draw the limit lines
        Color colLM = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_apfPlotStyleSP.getColor(3)];
        int   linLM = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_apfPlotStyleSP.getLine (3)];
        if(true) {
            if(apf.getLL() >= 0) pl.drawLines(new double[]{ apf.getLL(), ymin, apf.getLL(), ymax }, colLM, linLM);
            if(apf.getUL() >= 0) pl.drawLines(new double[]{ apf.getUL(), ymin, apf.getUL(), ymax }, colLM, linLM);
        }

        // Done
        return true;
    }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception
    {
        _ssPanel.resetLastInvalidColumn();

        // Get the prerequisite analysis classes
        ResultPanel[] resOWANO      = GUIMain.instance.getTabsByClass(PrecisionOWANOPanel.class);
        ResultPanel[] resRobustness = GUIMain.instance.getTabsByClass(RobustnessPanel    .class);
        if(resOWANO == null || (resRobustness != null && resRobustness.length != resOWANO.length)) {
            GUtil.showNoDDialogReport();
            return "";
        }

        // Get the analyzer instances
        PrecisionOWANO[] anlOWANO      = new PrecisionOWANO[resOWANO.length];
        Robustness[]     anlRobustness = null;
        for(int i = 0; i < resOWANO.length; ++i) anlOWANO[i] = (PrecisionOWANO) resOWANO[i].getAnalyzer();
        if(resRobustness != null) {
            anlRobustness = new Robustness[resRobustness.length];
            for(int i = 0; i < resRobustness.length; ++i) anlRobustness[i] = (Robustness) resRobustness[i].getAnalyzer();
        }

        // Limit the number of factors
        if(anlRobustness != null) {
            for(int i = 0; i < anlRobustness.length; ++i) {
                if(anlRobustness[i].getFactors().length > 7 + 1) {
                    GUtil.showNoDDialogPlot();
                    return "";
                }
            }
        }

        // Get the captions
        String mcapt = _apfCaptionSP.getMainCaption();
        String scapt = _apfCaptionSP.getSubCaption ();

        // Get the analysis setting
        double pp     = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];
        double lambda = _analysisSP.getLambda();

        // Perform some calculations
        APGonzales apf = new APGonzales(anlOWANO, anlRobustness, pp, _analysisSP.getLambda());

        // Generate the data for generating the result-details
        int maxIdxLen = (anlOWANO.length > 9) ? 2 : 1;

        int maxNameLen = _S("res_apf_conc").length();
        for(int i = 0; i < anlOWANO.length; ++i) {
            String nm = (new Double(anlOWANO[i].getTVal())).toString();
            int    ln = nm.length();
            if(ln > maxNameLen) maxNameLen = ln;
        }

        String[] strDelta = StringTranslator.formatDoubles(apf.getDeltaP(), 7);
        int      maxDLen  = _S("res_apf_delta").length();
        for(int i = 0; i < strDelta.length; ++i) {
            int ln = strDelta[i].length();
            if(ln > maxDLen) maxDLen = ln;
        }

        String[] strBETIh = StringTranslator.formatDoubles(apf.getBETIh(), 7);
        int      maxBLenH = _S("res_apf_beti").length();
        for(int i = 0; i < strBETIh.length; ++i) {
            int ln = strBETIh[i].length();
            if(ln > maxBLenH) maxBLenH = ln;
        }

        String[] strBETIl = StringTranslator.formatDoubles(apf.getBETIl(), 7);
        int      maxBLenL = _S("res_apf_beti").length();
        for(int i = 0; i < strBETIl.length; ++i) {
            int ln = strBETIl[i].length();
            if(ln > maxBLenL) maxBLenL = ln;
        }

        // Generate the result-details
        StringBuilder details = new StringBuilder();
        details.append(StringTranslator.format(
            "%-" + maxIdxLen + "s    %-" + maxNameLen + "s    %-" + maxDLen + "s    %s\n",
            "#", _S("res_apf_conc"), _S("res_apf_delta"), _S("res_apf_beti")
        ));

        String format = "%-" + maxIdxLen + "d    %-" + maxNameLen + "s    %" + maxDLen + "s    %" + maxBLenH + "s ; %" + maxBLenL + "s";
        for(int i = 0; i < anlOWANO.length; ++i) {
            String nm = (new Double(anlOWANO[i].getTVal())).toString();
            details.append(StringTranslator.format(format, i + 1, nm, strDelta[i], strBETIh[i], strBETIl[i]));
            if(i < anlOWANO.length - 1) details.append("\n");
        }

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_ap_gonza"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "C",           "" + anlOWANO.length,
            "lambda",      "" + lambda,
            "details",     details.toString(),
            "LL",          (apf.getLL() < 0) ? _S("res_res_na") : StringTranslator.format("%.5g", apf.getLL()),
            "UL",          (apf.getUL() < 0) ? _S("res_res_na") : StringTranslator.format("%.5g", apf.getUL())
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("APGonzales", kvps, html, withNonEmptyDoubleLineBreak);
    }

    // Calculate a content value
    public double calcContentValue(double y) throws Exception
    { return 0; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    {
        ResultPanel[] resOWANO = GUIMain.instance.getTabsByClass(PrecisionOWANOPanel.class);
        if(resOWANO == null) return new double[]{ 0, 0 };

        try {
            double min =  Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for(int i = 0; i < resOWANO.length; ++i) {
                double tval = ((PrecisionOWANO) resOWANO[i].getAnalyzer()).getTVal();
                if(tval < min) min = tval;
                if(tval > max) max = tval;
            }
            return new double[]{ min, max };
        }
        catch(Exception e) {}

        return new double[]{ 0, 0 };
    }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    {
        double lambda = _analysisSP.getLambda() * 1.5;
        return new double[]{ -lambda, lambda };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_apfCaptionSP.interfaceVersion());
        _apfCaptionSP.save(ds);

        ds.writeInt(_analysisSP.interfaceVersion());
        _analysisSP.save(ds);

        ds.writeInt(_apfPlotStyleSP.interfaceVersion());
        _apfPlotStyleSP.save(ds);

        ds.writeInt(_apfAxisScaleSP.interfaceVersion());
        _apfAxisScaleSP.save(ds);

        ds.writeInt(_miscSP.interfaceVersion());
        _miscSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int ifv;

        ifv = ds.readInt();
        if(!_apfCaptionSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_analysisSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_apfPlotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_apfAxisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct an ap-gonzales class
    public APGonzalesPanel()
    {
        super(4, new String[]{ _S("str_ap_plot_view"), "tab_ap" }, null, true, false);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        initResultPanel(_ssPanel, null, _apfAxisScaleSP, 0);

        updateReport();
    }
}
