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
// Tab module - z-score panel
//
public class ZScorePanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 1;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _regCaptionSP     = null;
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
    { return _S("str_anal_zscore"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        String[] drsCapt = new String[NUM_OF_Y_AXIS + 1];
        drsCapt[0] = _S("str_x_values_control");
        drsCapt[1] = _S("str_x_values_sample" );

        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, drsCapt);
        title[0] = _S("str_acrs_data_range");
        pane [0] = _inputDataRangeSP;
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
        double[] cda = getXDataArray(_ssPanel, _inputDataRangeSP);
        if(cda == null || cda.length <= 2) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double[] sda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0)[0];
        if(sda == null || sda.length <= 0) {
            GUtil.showNoDDialogReport();
            return "";
        }

        String mcapt = _regCaptionSP.getMainCaption();
        String scapt = _regCaptionSP.getSubCaption ();

        ZScore zsc = new ZScore(cda, sda);

        // Generate the details
        int maxSLen = _S("str_x_values_sample").length();
        for(int i = 0; i < sda.length; ++i) {
            final int len = StringTranslator.format("%.5g", sda[i]).length();
            if(len > maxSLen) maxSLen = len;
        }

        int maxZLen = 4; // "|Zs|"
        for(int i = 0; i < sda.length; ++i) {
            final int len = StringTranslator.format("%.5f", zsc.getZs(i)).length();
            if(len > maxZLen) maxZLen = len;
        }

        String formatStrC = "    %-" + maxSLen +   "s    %-" + maxZLen + "s\n";
        String formatStrI = "    %"  + maxSLen + ".5g    %"  + maxZLen + ".5f";

        StringBuilder details = new StringBuilder();

        details.append(StringTranslator.format(
            formatStrC,
            _S("str_x_values_sample"),
            html ? "|Z<sub>s</sub>|" : "|Zs|"
        ));

        for(int i = 0; i < sda.length; ++i) {
            details.append(StringTranslator.format(formatStrI, sda[i], zsc.getZs(i)));
            if(i < sda.length - 1) details.append("\n");
        }

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_zscore"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "C",           "" + zsc.getC(),
            "S",           "" + zsc.getS(),
            "Cm",          "" + StringTranslator.format("%.5g", zsc.getCm()),
            "Sc",          "" + StringTranslator.format("%.5g", zsc.getSc()),
            "details",     details.toString()
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("ZScore", kvps, html, withNonEmptyDoubleLineBreak);
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
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a z-score class
    public ZScorePanel()
    {
        super(1, null, null, true, false);
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
