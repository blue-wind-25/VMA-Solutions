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
// Tab module - a 1-Way-ANOVA-based precision-test panel
//
public class PrecisionOWANOPanel extends ResultPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 3;

    // Number of Y axis
    private static int NUM_OF_Y_AXIS = 9;

    // Reference to the main spreadsheet panel
    private SpreadsheetPanel _ssPanel = null;

    // Controls
    private StdPlotCaptionSettingPanel   _captionSP        = null;
    private StdPlotDataRangeSettingPanel _inputDataRangeSP = null;
    private StdPlotDataValuePanel        _afpTrueValue     = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the tab caption
    public String getTabCaption()
    { return _S("str_anal_prec_owano"); }

    // Return the tab icon
    public ImageIcon getTabIcon()
    { return GUtil.newImageIcon("tab_analysis"); }

    // Initialize the settings accordion
    public void initSettingsAccordion(String[] title, JComponent pane[])
    {
        String[] drsCapt = new String[NUM_OF_Y_AXIS + 1];
        drsCapt[0] = null;
        for(int i = 1; i <= NUM_OF_Y_AXIS; ++i) {
            drsCapt[i] = _F("str_x_values_prec_x_T", new String[] { "" + i });
        }
        _inputDataRangeSP = new StdPlotDataRangeSettingPanel(NUM_OF_Y_AXIS, false, drsCapt);
        title[0] = _S("str_acrs_data_range");
        pane [0] = _inputDataRangeSP;

        String[] drcCapt = new String[]{ _S("str_acrs_dval_tvname") };
        _afpTrueValue = new StdPlotDataValuePanel(1, drcCapt);
        title[1] = _S("str_acrs_dval_tvname");
        pane [1] = _afpTrueValue;
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
        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 2) {
            GUtil.showNoDDialogReport();
            return "";
        }

        // Perform some calculations
        PrecisionOWANO pre = new PrecisionOWANO(0, yda);

        // Prepare the value-key pairs
        String[] kvps = new String[]{
            "anal_name",   _S("res_anal_prec_owano"),
            "caption",     _captionSP.getMainCaption(),
            "sub_caption", _captionSP.getSubCaption (),
            "P",           "" + pre.getP(),
            "N",           "" + pre.getN(),
            "Zmm",         StringTranslator.format("%.5g", pre.getZmm ()),
            "Sw2",         StringTranslator.format("%.5g", pre.getSw2 ()),
            "Sb2",         StringTranslator.format("%.5g", pre.getSb2 ()),
            "Sr2",         StringTranslator.format("%.5g", pre.getSr2 ()),
            "RSDw",        StringTranslator.format("%.5g", pre.getRSDw()),
            "RSDb",        StringTranslator.format("%.5g", pre.getRSDb()),
            "RSDr",        StringTranslator.format("%.5g", pre.getRSDr())
        };
        
        // Generate and return the report
        return StringTranslator.generateReportFromTemplate("PrecisionOWANO", kvps, html, withNonEmptyDoubleLineBreak);
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

    // Return the analyzer instance
    public Object getAnalyzer() throws Exception
    {
        // Get the Y data array
        double[][] yda = getYDataArray(_ssPanel, _inputDataRangeSP, NUM_OF_Y_AXIS, false, 0);
        if(yda == null || yda.length <= 2) return null;

        // Get the true value
        double trueValue = _afpTrueValue.getValue(0);

        // Perform some calculations
        return new PrecisionOWANO(trueValue, yda);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeInt(_captionSP.interfaceVersion()); /** Available from interface version 3 */
        _captionSP.save(ds);
        
        ds.writeInt(_inputDataRangeSP.interfaceVersion());
        _inputDataRangeSP.save(ds);
        
        ds.writeInt(_afpTrueValue.interfaceVersion()); /** Available from interface version 2 */
        _afpTrueValue.save(ds);
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion > INTERFACE_VERSION) return false;

        int ifv;
        
        if(interfaceVersion >= 3) { /** Available from interface version 3 */
            ifv = ds.readInt();
            if(!_captionSP.load(ifv, ds)) return false;
        }

        ifv = ds.readInt();
        if(!_inputDataRangeSP.load(ifv, ds)) return false;

        if(interfaceVersion >= 2) { /** Available from interface version 2 */
            ifv = ds.readInt();
            if(!_afpTrueValue.load(ifv, ds)) return false;
        }
        else
            _afpTrueValue.resetAllValue(0);
        
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a 1-Way-ANOVA-based  precision-test class
    public PrecisionOWANOPanel()
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
