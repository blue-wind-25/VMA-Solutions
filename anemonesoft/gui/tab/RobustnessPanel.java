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
// Tab module - robustness-test panel
//
public class RobustnessPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 2;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 23;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _robCaptionSP     = null;
    private StdAnalysisSettingPanel      _analysisSP       = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataRangeCaptionPanel _inputDataCaptSP  = null;
    private StdPlotDataValuePanel        _apfDeltaReal     = null;
    private StdPlotDataPointSettingPanel _robPlotStyleSP   = null;
    private StdPlotAxisScaleSettingPanel _robAxisScaleSP   = null;
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
    { return _S("str_anal_robustness"); }

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
        drsCapt[0] = _S("str_x_values_response");
        for(int i = 1; i <= NUM_OF_Y_AXIS; ++i) {
            drsCapt[i] = _F("str_y_values_factor_T", new String[] { "" + i });
        }
        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, drsCapt);
        title[1] = _S("str_acrs_data_range");
        pane [1] = _inputDataRangeSP;

        String[] drcCapt = new String[NUM_OF_Y_AXIS + 1];
        for(int i = 0; i < NUM_OF_Y_AXIS; ++i) {
            drcCapt[i] = _F("str_y_values_factor_T", new String[] { "" + (i + 1) });
        }
        _inputDataCaptSP = new StdPlotDataRangeCaptionPanel(NUM_OF_Y_AXIS, drcCapt, "F-");
        title[2] = _S("str_acrs_drcapt_fname");
        pane [2] = _inputDataCaptSP;

        _apfDeltaReal = new StdPlotDataValuePanel(NUM_OF_Y_AXIS, drcCapt);
        title[3] = _S("str_acrs_dval_drname");
        pane [3] = _apfDeltaReal;

        StdPlotDataPointSettingPanel.DPSSpec[] dpsSpec = new StdPlotDataPointSettingPanel.DPSSpec[2];
        dpsSpec[0] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_rob_eff_bar"  ), false, true);
        dpsSpec[1] = new StdPlotDataPointSettingPanel.DPSSpec(_S("str_acrs_rob_eff_stdev"), false, true);
        _robPlotStyleSP = new StdPlotDataPointSettingPanel(dpsSpec);
        title[4] = _S("str_acrs_rob_pstyle");
        pane [4] = _robPlotStyleSP;

        _robAxisScaleSP = new StdPlotAxisScaleSettingPanel(this, false);
        title[5] = _S("str_acrs_rob_axis");
        pane [5] = _robAxisScaleSP;

        _miscSP = new StdPlotMiscSettingPanel();
        title[6] = _S("str_acrs_misc");
        pane [6] = _miscSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _robCaptionSP = new StdPlotCaptionSettingPanel(false, false);
        _robCaptionSP.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        return _robCaptionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    {
        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= (yda.length + 1)) {
            GUtil.showNoDDialogPlot();
            return false;
        }

        // Get the captions
        String mcapt = _robCaptionSP.getMainCaption ();
        String scapt = _robCaptionSP.getSubCaption  ();
        String xcapt = _robCaptionSP.getXCaption    ();
        String ycapt = _robCaptionSP.getLeftYCaption();

        // Get the analysis setting
        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        // Get the scale
        double xmin = _robAxisScaleSP.getXMin();
        double xmax = _robAxisScaleSP.getXMax();
        double xstp = _robAxisScaleSP.getXStep();
        int    xsdv = _robAxisScaleSP.getXSDiv();
        double ymin = _robAxisScaleSP.getLeftYMin();
        double ymax = _robAxisScaleSP.getLeftYMax();
        double ystp = _robAxisScaleSP.getLeftYStep();
        int    ysdv = _robAxisScaleSP.getLeftYSDiv();

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

        // Generate the axis tick strings
        ArrayList<String> customXAxisTick = new ArrayList<String>();
        if(Math.abs(xstp) >= PlotRenderer.MIN_STEP) {
            double max = xmax + xstp * 0.1;
            for(double x = xmin; x < max; x += xstp) {
                int ix = (int) Math.round(x);
                if(ix < 1 || ix > yda.length)
                    customXAxisTick.add(null);
                else
                    customXAxisTick.add(_inputDataCaptSP.getCaption(ix - 1));
            }
        }
        String[] customXAxisTickStr = null;
        if(customXAxisTick.size() > 0) {
            customXAxisTickStr = new String[customXAxisTick.size()];
            customXAxisTick.toArray(customXAxisTickStr);
        }

        // Draw the background, caption, and axis
        pl.drawBackground();
        pl.drawCaption(mcapt, scapt);
        pl.drawAxisCaption(xcapt, ycapt, null);
        pl.clipToPlotArea(true, false);
        pl.drawAxis(drawGrid, drawOriginAxis, true, true, false, customXAxisTickStr);
        pl.clipToPlotArea(true, true);

        // Instantiate the analysis class
        Robustness rob = new Robustness(null, xda, yda, pp);

        // Determine the number of sampling
        int nos = draft ? (w / 4) : (w / 2);

        // Draw the effect bar
        Color colEB = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_robPlotStyleSP.getColor(0)];
        int   linEB = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_robPlotStyleSP.getLine (0)];
        if(linEB >= 0) {
            // Generate the data points
            double   ofs  = 0.3;
            double[] b    = rob.getB();
            double[] line = new double[(b.length - 1) * 4];
            for(int i = 1; i < b.length; ++i) {
                // Calculate the starting index
                int s = (i - 1) * 4;
                // Calculate the bar's coordinates
                line[s    ] = i - ofs;
                line[s + 1] = 0;
                line[s + 2] = i + ofs;
                line[s + 3] = b[i];
            }
            // Draw the boxes
            pl.drawBoxes(line, colEB, linEB);
        }

        // Draw the standard-deviation band
        Color colSD = StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [_robPlotStyleSP.getColor(1)];
        int   linSD = StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[_robPlotStyleSP.getLine (1)];
        if(linEB >= 0) {
            // Generate the data points
            double   ofs  = 0.1;
            double[] b    = rob.getB();
            double[] CIb  = rob.getCIb();
            double[] line = new double[(b.length - 1) * 12];
            for(int i = 1; i < b.length; ++i) {
                // Calculate the starting index
                int s = (i - 1) * 12;
                // Calculate some common values
                double xl = i - ofs;
                double xh = i + ofs;
                double yl = b[i] - CIb[i];
                double yh = b[i] + CIb[i];
                line[s    ] = xl; line[s + 1] = yh; line[s +  2] = xh; line[s +  3] = yh; // Top line
                line[s + 4] = xl; line[s + 5] = yl; line[s +  6] = xh; line[s +  7] = yl; // Bottom line
                line[s + 8] = i;  line[s + 9] = yh; line[s + 10] = i;  line[s + 11] = yl; // Center line
            }
            // Draw the lines
            pl.drawLines(line, colSD, linSD);
        }

        // Done
        return true;
    }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception
    {
        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) {
            GUtil.showNoDDialogReport();
            return "";
        }

        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= (yda.length + 1)) {
            GUtil.showNoDDialogReport();
            return "";
        }

        // Perform some calculations
        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        String mcapt = _robCaptionSP.getMainCaption();
        String scapt = _robCaptionSP.getSubCaption ();

        Robustness rob = new Robustness(null, xda, yda, pp);

        // Generate the data for generating the result-details
        int maxIdxLen = (yda.length > 9) ? 2 : 1;

        int maxNameLen = _S("res_rob_factor").length();
        for(int i = 0; i < yda.length; ++i) {
            String nm = _inputDataCaptSP.getCaption(i);
            int    ln = nm.length();
            if(ln > maxNameLen) maxNameLen = ln;
        }

        String[] strB    = StringTranslator.formatDoubles(rob.getB(), 7);
        int      maxBLen = _S("res_rob_effect").length();
        for(int i = 0; i <= yda.length; ++i) {
            int ln = strB[i].length();
            if(ln > maxBLen) maxBLen = ln;
        }

        String[] strCIb    = StringTranslator.formatDoubles(rob.getCIb(), 7);
        int      maxCIbLen = _S("res_rob_eci").length();
        for(int i = 0; i <= yda.length; ++i) {
            int ln = strCIb[i].length();
            if(ln > maxCIbLen) maxCIbLen = ln;
        }

        boolean[] isSig = rob.getIsSig();

        // Generate the result-details
        String sigYes = _S("res_rob_sig_yes");
        String sigNo  = _S("res_rob_sig_no");

        StringBuilder details = new StringBuilder();
        details.append(StringTranslator.format(
            "%-" + maxIdxLen + "s    %-" + maxNameLen + "s    %-" + maxBLen + "s    %-" + maxCIbLen + "s      %s\n",
            "#", _S("res_rob_factor"), _S("res_rob_effect"), _S("res_rob_eci"), _S("res_rob_significant")
        ));

        String format = "%-" + maxIdxLen + "d    %-" + maxNameLen + "s    %" + maxBLen + "s    ± %" + maxCIbLen + "s    %s";
        for(int i = 0; i < yda.length; ++i) {
            String nm = _inputDataCaptSP.getCaption(i);
            details.append(StringTranslator.format(format, i + 1, nm, strB[i + 1], strCIb[i + 1], isSig[i + 1] ? sigYes : sigNo));
            if(i < yda.length - 1) details.append("\n");
        }

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_robustness"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "PP",          StringTranslator.format("%.1f", rob.getPP()),
            "N",           "" + rob.getN(),
            "K",           "" + rob.getK(),
            "NK1",         "" + rob.getNK1(),
            "NK1s",        "" + rob.getNK1s(),
            "t2",          StringTranslator.format("%.5g", rob.getT2 ()),
            "t2s",         StringTranslator.format("%.5g", rob.getT2s()),
            "Sy",          StringTranslator.format("%.5g", rob.getSy ()),
            "details",     details.toString()
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("Robustness", kvps, html, withNonEmptyDoubleLineBreak);
    }

    // Calculate a content value
    public double calcContentValue(double y) throws Exception
    { return 0; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    {
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null) return new double[]{ 0, 0 };

        return new double[]{ 0, yda.length + 1 };
    }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    {
        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) return new double[]{ 0, 0 };

        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= (yda.length + 1)) return new double[]{ 0, 0 };

        // Get the probability
        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        // Return the data range
        try {
            Robustness rob = new Robustness(null, xda, yda, pp);
            return rob.getCIbRange();
        }
        catch(Exception e) {
        }
        return new double[]{ 0, 0 };
    }

    // Return the analyzer instance
    public Object getAnalyzer() throws Exception
    {
        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 0) return null;

        // Get the X data array
        double[] xda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(xda == null || xda.length <= (yda.length + 1)) return null;

        // Get the probability
        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        // Get the delta-real
        double[] deltaReal = new double[NUM_OF_Y_AXIS];
        for(int i = 0; i < NUM_OF_Y_AXIS; ++i) deltaReal[i] = _apfDeltaReal.getValue(i);

        return new Robustness(deltaReal, xda, yda, pp);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_robCaptionSP.interfaceVersion());
        _robCaptionSP.save(ds);

        ds.writeInt(_analysisSP.interfaceVersion());
        _analysisSP.save(ds);

        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);

        ds.writeInt(_inputDataCaptSP.interfaceVersion());
        _inputDataCaptSP.save(ds);

        ds.writeInt(_apfDeltaReal.interfaceVersion()); /** Available from interface version 2 */
        _apfDeltaReal.save(ds);

        ds.writeInt(_robPlotStyleSP.interfaceVersion());
        _robPlotStyleSP.save(ds);

        ds.writeInt(_robAxisScaleSP.interfaceVersion());
        _robAxisScaleSP.save(ds);

        ds.writeInt(_miscSP.interfaceVersion());
        _miscSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion > INTERFACE_VERSION) return false;

        int ifv;

        ifv = ds.readInt();
        if(!_robCaptionSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_analysisSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_inputDataCaptSP.load(ifv, ds)) return false;

        if(interfaceVersion >= 2) { /** Available from interface version 2 */
            ifv = ds.readInt();
            if(!_apfDeltaReal.load(ifv, ds)) return false;
        }
        else
            _apfDeltaReal.resetAllValue(0);

        ifv = ds.readInt();
        if(!_robPlotStyleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_robAxisScaleSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_miscSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a robustness-test class
    public RobustnessPanel()
    {
        super(7, new String[]{ _S("str_eff_plot_view"), "tab_probustness" }, null, true, false);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        String[] header = initResultPanel(_ssPanel, _inputDataRangeSP, _robAxisScaleSP, NUM_OF_Y_AXIS);

        if(header != null) {
            if(header.length >= 1) _robCaptionSP.setXCaption(header[0]);
            if(header.length >= 2)  {
                int nof = header.length - 1;
                if(nof > NUM_OF_Y_AXIS) nof = NUM_OF_Y_AXIS;
                for(int i = 0; i < nof; ++i) _inputDataCaptSP.setCaption(i, header[i + 1]);
            }
        }

        updateReport();

        _robAxisScaleSP.setXStep(1);
        _robAxisScaleSP.setXSDiv(1);
    }
}
