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
        drsCapt[0] = _S("str_x_values_zscore_x");
        drsCapt[1] = _S("str_x_values_zscore_y");

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

        double[][] sda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(sda == null || sda.length <= 0) {
            GUtil.showNoDDialogReport();
            return "";
        }

        String mcapt = _regCaptionSP.getMainCaption();
        String scapt = _regCaptionSP.getSubCaption ();

        ZScore zsc = new ZScore(cda, sda[0]);

        // Generate the details
        /*
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
        */

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_zscore"),
            "caption",     mcapt,
            "sub_caption", scapt,
            "C",           "" + zsc.getC(),
            "S",           "" + zsc.getS(),
            "Cm",          "" + zsc.getCm(),
            "Sc",          "" + zsc.getSc(),
            "details",     ""
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
        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int ifv;

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a accuracy-test class
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
