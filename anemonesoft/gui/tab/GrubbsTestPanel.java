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
import anemonesoft.stat.*;

//
// Tab module - Grubbs-test panel
//
public class GrubbsTestPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 1;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _regCaptionSP     = null;
    private StdAnalysisSettingPanel      _analysisSP       = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the tab caption
    public String getTabCaption()
    { return _S("str_anal_grubbs"); }

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
        drsCapt[0] = _S("str_x_values_grubbs_x");
        drsCapt[1] = _S("str_x_values_grubbs_y");

        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, drsCapt);
        title[1] = _S("str_acrs_data_range");
        pane [1] = _inputDataRangeSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _regCaptionSP = new StdPlotCaptionSettingPanel(true, false);
        return _regCaptionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    { return false; /* Not needed by this class */ }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception
    {
        // Perform some calculations
        double[] dda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(dda == null || dda.length <= 2) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double[] sda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0)[0];
        if(sda == null || sda.length <= 0) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        String mcapt = _regCaptionSP.getMainCaption();
        String scapt = _regCaptionSP.getSubCaption ();

        GrubbsTest grb = new GrubbsTest(dda, sda, pp);

        // Generate the details
        int maxSLen = _S("str_x_values_grubbs_y").length();
        for(int i = 0; i < sda.length; ++i) {
            final int len = StringTranslator.format("%.5g", sda[i]).length();
            if(len > maxSLen) maxSLen = len;
        }

        int maxMLen = _S("str_x_values_grubbs_m").length();
        for(int i = 0; i < sda.length; ++i) {
            final int len = StringTranslator.format("%+.5f", grb.getMean(i)).length();
            if(len > maxMLen) maxMLen = len;
        }

        int maxDLen = _S("str_x_values_grubbs_sd").length();
        for(int i = 0; i < sda.length; ++i) {
            final int len = StringTranslator.format("%+.5f", grb.getSd(i)).length();
            if(len > maxDLen) maxDLen = len;
        }

        int maxGLen = _S("str_x_values_grubbs_gl").length();
        for(int i = 0; i < sda.length; ++i) {
            final int len = StringTranslator.format("%+.5f", grb.getG(i)).length();
            if(len > maxGLen) maxGLen = len;
        }

        int maxOLen = _S("str_x_values_grubbs_out").length();

        String formatStrC = "    %-" + maxSLen +   "s    %-" + maxMLen +   "s    %-" + maxDLen +   "s    %-" + maxGLen +   "s    %-" + maxOLen + "s\n";
        String formatStrI = "    %"  + maxSLen + ".5g    %+" + maxMLen + ".5f    %+" + maxDLen + ".5f    %+" + maxGLen + ".5f    %-" + maxOLen + "c";

        StringBuilder details = new StringBuilder();

        details.append(StringTranslator.format(
            formatStrC,
            _S("str_x_values_grubbs_y"  ),
            _S("str_x_values_grubbs_m"  ),
            _S("str_x_values_grubbs_sd" ),
            _S("str_x_values_grubbs_gl" ),
            _S("str_x_values_grubbs_out")
        ));

        for(int i = 0; i < sda.length; ++i) {
            double g = grb.getG(i);
            details.append(StringTranslator.format(
                formatStrI,
                sda[i],
                grb.getMean(i),
                grb.getSd(i),
                g,
                (g > grb.getGC()) ? '*' : ' '
            ));
            if(i < sda.length - 1) details.append("\n");
        }

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_grubbs"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "N",           "" + grb.getN(),
            "S",           "" + grb.getS(),
            "N2",          "" + grb.getN2(),
            "PP",          StringTranslator.format("%.1f", grb.getPP()),
            "t2",          StringTranslator.format("%.5g", grb.getT2()),
            "gc",          StringTranslator.format("%.5g", grb.getGC()),
            "details",     details.toString()
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("GrubbsTest", kvps, html, withNonEmptyDoubleLineBreak);
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

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a Grubbs-test class
    public GrubbsTestPanel()
    {
        super(2, null, null, true, false);
        _ssPanel = GUIMain.instance.getSpreadsheetPanel();
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        super.init(initNewResult);
        if(!initNewResult) return;

        String[] header = initResultPanel(_ssPanel, _inputDataRangeSP, null, NUM_OF_Y_AXIS);

        updateReport();
    }
}