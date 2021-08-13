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
// Tab module - homogeneity-test panel
//
public class HomogeneityPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 3;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 2;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _captionSP        = null;
    private StdAnalysisSettingPanel      _analysisSP       = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the tab caption
    public String getTabCaption()
    { return _S("str_anal_homogen"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        _analysisSP = new StdAnalysisSettingPanel(true, false, false, false);
        title[0] = _S("str_acrs_anal");
        pane [0] = _analysisSP;

        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, new String[] {
            null, // No X-Input
            _S("str_y_values_homogen_1"),
            _S("str_y_values_homogen_2")
        });
        title[1] = _S("str_acrs_data_range");
        pane [1] = _inputDataRangeSP;
    }

    // Initialize the caption setting panel
    public Container initPlotCaptionSettingPanel()
    {
        _captionSP = new StdPlotCaptionSettingPanel(true, false);
        return _captionSP;
    }

    // Draw the primary/secondary plot to the given graphics context
    public boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception
    { return true; /* Not needed by this class */ }

    // Generate and return the report string
    public String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception
    {
        _ssPanel.resetLastInvalidColumn();

        // Get the data and settings
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length < 2 || yda[0].length <= 1 || yda[1].length <= 1) {
            GUtil.showNoDDialogReport();
            return "";
        }

        double pp = StdAnalysisSettingPanel.PREDEFINED_PROBABILITY[_analysisSP.getProbability()];

        // Perform some calculations
        Homogeneity hom = new Homogeneity(yda[0], yda[1], pp);

        double F1 = hom.getF1();
        double TV = hom.getTV();

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_homogen"),
            "caption",     _captionSP.getMainCaption(),
            "sub_caption", _captionSP.getSubCaption (),
            "N",           "" + hom.getN(),
            "N1",          "" + hom.getN1(),
            "PP",          StringTranslator.format("%.1f", hom.getPP    ()),
            "F1",          StringTranslator.format("%.5g", F1             ),
            "Ym1",         StringTranslator.format("%.5g", hom.getY1Mean()),
            "Ym2",         StringTranslator.format("%.5g", hom.getY2Mean()),
            "Sy1",         StringTranslator.format("%.5g", hom.getSy1   ()),
            "Sy2",         StringTranslator.format("%.5g", hom.getSy2   ()),
            "TV",          StringTranslator.format("%.5g", TV             ),
            "conclusion",  (TV <= F1) ? _S("res_hom_good") : _S("res_hom_bad"),
            "reason",      (TV <= F1) ? "(TV ≤ F)" : "(TV > F)"
        };

        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("Homogeneity", kvps, html, withNonEmptyDoubleLineBreak);
    }

    // Calculate a content value
    public double calcContentValue(double y) throws Exception
    { return 0; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the X data
    public double[] getXDataRange(Object requestor)
    { return null; /* Not needed by this class */ }

    // Return the data range (minimum and maximum values) of the Y data
    public double[] getYDataRange(Object requestor, boolean rightYAxis)
    { return null; /* Not needed by this class */ }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_captionSP.interfaceVersion()); /** Available from interface version 2 */
        _captionSP.save(ds);

        ds.writeInt(_analysisSP.interfaceVersion());
        _analysisSP.save(ds);

        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion > INTERFACE_VERSION) return false;

        int ifv;

        if(interfaceVersion >= 2) { /** Available from interface version 2 */
            ifv = ds.readInt();
            if(!_captionSP.load(ifv, ds)) return false;
        }

        ifv = ds.readInt();
        if(!_analysisSP.load(ifv, ds)) return false;

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a homogeneity-test class
    public HomogeneityPanel()
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

        if(header != null) {
            if(header.length >= 1) _captionSP.setMainCaption(header[0]);
            if(header.length >= 2) _captionSP.setSubCaption (header[1]);
        }

        updateReport();
    }
}
