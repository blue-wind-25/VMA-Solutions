/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.gui.dialog.*;
import anemonesoft.gui.tab.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;

//
// The parent class for all result panels
//
public abstract class ResultPanel extends JPanel implements ActionListener {
    // Constants
    private static String STR_DEMO_D = "D"; /** From v1.2.0a */
    private static String STR_DEMO_E = "E"; /** From v1.2.0a */
    private static String STR_DEMO_M = "M"; /** From v1.2.0a */
    private static String STR_DEMO_O = "O"; /** From v1.2.0a */

    // Data
    private int      _numOfAccordionPanes;
    private String[] _specPPlotTab;
    private String[] _specSPlotTab;
    private boolean  _withResultTab;
    private boolean  _withCalcTab;

    // Buffers
    private BufferedImage _imgPPlotArea    = null;
    private BufferedImage _imgSPlotArea    = null;

    // Controls
    private PlotArea      _pnlPPlotArea    = null;
    private JButton       _btnPrintPPlot   = null;
    private JButton       _btnUpdatePPlot  = null;
    private JButton       _btnSavePPlot    = null;
    private JButton       _btnZoomPPlot    = null;

    private PlotArea      _pnlSPlotArea    = null;
    private JButton       _btnPrintSPlot   = null;
    private JButton       _btnUpdateSPlot  = null;
    private JButton       _btnSaveSPlot    = null;
    private JButton       _btnZoomSPlot    = null;

    private JTextArea     _txtReport       = null;
    private JButton       _btnPrintReport  = null;
    private JButton       _btnUpdateReport = null;
    private JButton       _btnSaveReport   = null;

    private JTextField    _txtInputY       = null;
    private JTextField    _txtOutputX      = null;
    private JButton       _btnCalculate    = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Compound Y-data class
    public static class YData {
        public double[] data;
        public int      color;
        public int      symbol;
        public int      line;

        public YData(double[] d, int c, int s, int l)
        { data = d; color = c; symbol = s; line = l; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Override this to return the tab caption
    public abstract String getTabCaption();

    // Override this to return the tab icon
    public abstract ImageIcon getTabIcon();

    // Override this to initialize the settings accordion
    public abstract void initSettingsAccordion(String[] title, JComponent pane[]);

    // Override this to initialize the caption setting panel
    public abstract Container initPlotCaptionSettingPanel();

    // Override this to draw the primary/secondary plot to the given graphics context
    public abstract boolean drawPlot(Graphics2D g, int w, int h, boolean draft, boolean secondary) throws Exception;

    // Override this to generate and return the report string
    public abstract String genReport(boolean html, boolean withNonEmptyDoubleLineBreak) throws Exception;

    // Override this to calculate a content value
    public abstract double calcContentValue(double y) throws Exception;

    // Override this to return the data range (minimum and maximum values) of the X data
    public abstract double[] getXDataRange(Object requestor);

    // Override this to return the data range (minimum and maximum values) of the Y data
    public abstract double[] getYDataRange(Object requestor, boolean rightYAxis);

    // Override this to return the analyzer instance (the implementation class that perform the analysis)
    public Object getAnalyzer() throws Exception
    { return null; }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a panel with a spreadsheet
    public ResultPanel(int numOfAccordionPanes, String[] specPPlotTab, String[] specSPlotTab, boolean withResultTab, boolean withCalcTab)
    {
        super(new BorderLayout(), true);
        _numOfAccordionPanes = numOfAccordionPanes;
        _specPPlotTab        = specPPlotTab;
        _specSPlotTab        = specSPlotTab;
        _withResultTab       = withResultTab;
        _withCalcTab         = withCalcTab;
    }

    // Initialize the panel contents
    public void init(boolean initNewResult)
    {
        // Initialize the settings accordion
        String[]     title = new String[_numOfAccordionPanes];
        JComponent[] pane  = new JComponent[_numOfAccordionPanes];
        initSettingsAccordion(title, pane);

        // Determine the width of the accordion panel based on the JRE version
        int jver = Integer.parseInt("" + System.getProperty("java.version").charAt(2));
        int acrw = (jver >= 7) ? 217 : 193;

        // Left panel
        JPanel leftPanel = new JPanel(new BorderLayout(), true);
            AccordionPanel acrPanel = new AccordionPanel(title, pane, GUtil.newImageIcon("acr_open"), GUtil.newImageIcon("acr_closed"));
                acrPanel.setMinimumSize  (new Dimension(acrw, 0));
                acrPanel.setPreferredSize(new Dimension(acrw, 0));
            leftPanel.add(acrPanel, BorderLayout.CENTER);
            leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 0),
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));

        // Number of tabs
        int numOfTabs = 0;

        // Right-top panel (plot)
        JPanel pplotPanel = null;
        if(_specPPlotTab != null) {
            pplotPanel = new JPanel(new BorderLayout(5, 5), true);
            JPanel pnlPlotBorder = new JPanel(new BorderLayout(), true);
                _pnlPPlotArea = new PlotArea(false);
                pnlPlotBorder.add(_pnlPPlotArea, BorderLayout.CENTER);
                pnlPlotBorder.setBorder(BorderFactory.createEtchedBorder());
            JPanel pbtnPanel = new JPanel(new GridLayout(1, 4, 5, 5), true);
                _btnUpdatePPlot = new JButton(GUtil.newImageIcon("mnu_edit_update"));
                    _btnUpdatePPlot.setToolTipText(_S("str_updt_plot_tooltip"));
                    _btnUpdatePPlot.addActionListener(this);
                _btnZoomPPlot = new JButton(GUtil.newImageIcon("mnu_edit_zoom"));
                    _btnZoomPPlot.setToolTipText(_S("str_zoom_plot_tooltip"));
                    _btnZoomPPlot.addActionListener(this);
                _btnPrintPPlot = new JButton(GUtil.newImageIcon("mnu_file_print"));
                    _btnPrintPPlot.setToolTipText(_S("str_print_plot_tooltip"));
                    _btnPrintPPlot.addActionListener(this);
                _btnSavePPlot = new JButton(GUtil.newImageIcon("mnu_file_save"));
                    _btnSavePPlot.setToolTipText(_S("str_save_plot_tooltip"));
                    _btnSavePPlot.addActionListener(this);
                pbtnPanel.add(_btnUpdatePPlot);
                pbtnPanel.add(_btnZoomPPlot);
                pbtnPanel.add(_btnPrintPPlot);
                pbtnPanel.add(_btnSavePPlot);
            pplotPanel.add(pnlPlotBorder, BorderLayout.CENTER);
            pplotPanel.add(pbtnPanel, BorderLayout.SOUTH);
            pplotPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            ++numOfTabs;
        }

        // Right-top panel (secondary plot)
        JPanel splotPanel = null;
        if(_specSPlotTab != null) {
            splotPanel = new JPanel(new BorderLayout(5, 5), true);
            JPanel pnlPlotBorder = new JPanel(new BorderLayout(), true);
                _pnlSPlotArea = new PlotArea(true);
                pnlPlotBorder.add(_pnlSPlotArea, BorderLayout.CENTER);
                pnlPlotBorder.setBorder(BorderFactory.createEtchedBorder());
            JPanel pbtnPanel = new JPanel(new GridLayout(1, 4, 5, 5), true);
                _btnUpdateSPlot = new JButton(GUtil.newImageIcon("mnu_edit_update"));
                    _btnUpdateSPlot.setToolTipText(_S("str_updt_plot_tooltip"));
                    _btnUpdateSPlot.addActionListener(this);
                _btnZoomSPlot = new JButton(GUtil.newImageIcon("mnu_edit_zoom"));
                    _btnZoomSPlot.setToolTipText(_S("str_zoom_plot_tooltip"));
                    _btnZoomSPlot.addActionListener(this);
                _btnPrintSPlot = new JButton(GUtil.newImageIcon("mnu_file_print"));
                    _btnPrintSPlot.setToolTipText(_S("str_print_plot_tooltip"));
                    _btnPrintSPlot.addActionListener(this);
                _btnSaveSPlot = new JButton(GUtil.newImageIcon("mnu_file_save"));
                    _btnSaveSPlot.setToolTipText(_S("str_save_plot_tooltip"));
                    _btnSaveSPlot.addActionListener(this);
                pbtnPanel.add(_btnUpdateSPlot);
                pbtnPanel.add(_btnZoomSPlot);
                pbtnPanel.add(_btnPrintSPlot);
                pbtnPanel.add(_btnSaveSPlot);
            splotPanel.add(pnlPlotBorder, BorderLayout.CENTER);
            splotPanel.add(pbtnPanel, BorderLayout.SOUTH);
            splotPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            ++numOfTabs;
        }

        // Right-top panel (report)
        JPanel reportPanel = null;
        if(_withResultTab) {
            reportPanel = new JPanel(new BorderLayout(5, 5), true);
            JPanel pnlReportBorder = new JPanel(new BorderLayout(), true);
                _txtReport = new JTextArea();
                _txtReport.setEditable(false);
                _txtReport.setFont(new Font(GUtil.getSysFontName("Monospaced"), Font.BOLD, (int) (_txtReport.getFont().getSize() * 0.95)));
                _txtReport.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JScrollPane scrReport = new JScrollPane(_txtReport);
                    scrReport.setBorder(null);
                pnlReportBorder.add(scrReport, BorderLayout.CENTER);
                pnlReportBorder.setBorder(BorderFactory.createEtchedBorder());
            JPanel rbtnPanel = new JPanel(new GridLayout(1, 3, 5, 5), true);
                _btnUpdateReport = new JButton(GUtil.newImageIcon("mnu_edit_update"));
                    _btnUpdateReport.setToolTipText(_S("str_updt_report_tooltip"));
                    _btnUpdateReport.addActionListener(this);
                _btnPrintReport = new JButton(GUtil.newImageIcon("mnu_file_print"));
                    _btnPrintReport.setToolTipText(_S("str_print_report_tooltip"));
                    _btnPrintReport.addActionListener(this);
                _btnSaveReport = new JButton(GUtil.newImageIcon("mnu_file_save"));
                    _btnSaveReport.setToolTipText(_S("str_save_report_tooltip"));
                    _btnSaveReport.addActionListener(this);
                rbtnPanel.add(_btnUpdateReport);
                rbtnPanel.add(_btnPrintReport);
                rbtnPanel.add(_btnSaveReport);
            reportPanel.add(pnlReportBorder, BorderLayout.CENTER);
            reportPanel.add(rbtnPanel, BorderLayout.SOUTH);
            reportPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            ++numOfTabs;
        }

        // Right-top panel (calculation)
        JPanel calcPanel = null;
        if(_withCalcTab) {
            calcPanel = new JPanel(new BorderLayout(), true);
                JPanel pnlHolder = new JPanel(new GridLayout(4, 1, 0, 5), true);
                    JPanel pnlInput = new JPanel(new BorderLayout(5, 0), true);
                        _txtInputY = new JTextField();
                            _txtInputY.setBorder(BorderFactory.createCompoundBorder(_txtInputY.getBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                            _txtInputY.setDocument(new NumericDocument(5, true));
                            GUtil.disableCutAndPasteOnTextField(_txtInputY);
                        pnlInput.add(new JLabel(_S("str_test_y")), BorderLayout.WEST);
                        pnlInput.add(_txtInputY, BorderLayout.CENTER);
                    JPanel pnlOutput = new JPanel(new BorderLayout(5, 0), true);
                        _txtOutputX = new JTextField();
                            _txtOutputX.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                            _txtOutputX.setEnabled(false);
                            _txtOutputX.setDisabledTextColor(SystemColor.controlText);
                            _txtOutputX.setBackground(SystemColor.window);
                        pnlOutput.add(new JLabel(_S("str_test_x")), BorderLayout.WEST);
                        pnlOutput.add(_txtOutputX, BorderLayout.CENTER);
                    _btnCalculate = new JButton(_S("str_test_calc"));
                        _btnCalculate.addActionListener(this);
                    pnlHolder.add(new JLabel(_S("str_test_ival")));
                    pnlHolder.add(pnlInput);
                    pnlHolder.add(pnlOutput);
                    pnlHolder.add(_btnCalculate);
                calcPanel.add(pnlHolder, BorderLayout.NORTH);
            calcPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            ++numOfTabs;
        }

        // Right-top panel (tab)
        JTabbedPane tbpResult = null;
        if(numOfTabs > 1) {
            tbpResult = new JTabbedPane();
            tbpResult.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            tbpResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5))
            ));
            if(pplotPanel != null) {
                pplotPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                GUtil.addTab(tbpResult, null, GUtil.newImageIcon(_specPPlotTab[1]), _specPPlotTab[0], pplotPanel, true);
            }
            if(splotPanel != null) {
                splotPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                GUtil.addTab(tbpResult, null, GUtil.newImageIcon(_specSPlotTab[1]), _specSPlotTab[0], splotPanel, true);
            }
            if(reportPanel != null) {
                reportPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                GUtil.addTab(tbpResult, null, GUtil.newImageIcon("tab_report"), _S("str_report_view"), reportPanel, true);
            }
            if(calcPanel != null) {
                calcPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                GUtil.addTab(tbpResult, null, GUtil.newImageIcon("tab_calculate"), _S("str_test_view"), calcPanel, true);
            }
        }

        // Right-bottom panel
        Container captionSetting = initPlotCaptionSettingPanel();
        JPanel    captionPanel = null;
        if(captionSetting != null) {
            captionPanel = new JPanel(new BorderLayout(), true);
            captionPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
            captionPanel.add(captionSetting, BorderLayout.SOUTH);
        }

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout(), true);
             if(tbpResult   != null) rightPanel.add(tbpResult,   BorderLayout.CENTER);
        else if(pplotPanel  != null) rightPanel.add(pplotPanel,  BorderLayout.CENTER);
        else if(splotPanel  != null) rightPanel.add(splotPanel,  BorderLayout.CENTER);
        else if(reportPanel != null) rightPanel.add(reportPanel, BorderLayout.CENTER);
        else if(calcPanel   != null) rightPanel.add(calcPanel,   BorderLayout.CENTER);
        if(captionPanel != null) rightPanel.add(captionPanel, BorderLayout.SOUTH);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(), true);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Disable some functionalities in demo mode
        if(GUIMain.instance.isDemoMode()) { /** From v1.2.0a */
            if(_btnSavePPlot  != null) _btnSavePPlot .setEnabled(false);
            if(_btnSaveSPlot  != null) _btnSaveSPlot .setEnabled(false);
            if(_btnSaveReport != null) _btnSaveReport.setEnabled(false);
        }
    }

    // Initialize the result panel for new plot/analysis
    public String[] initResultPanel(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel, StdPlotAxisScaleSettingPanel assPanel, int numOfYAxis)
    {
        // Get the selection range
        SpreadsheetPanel.SelectionArea ssa = ssPanel.getSelectionArea();
        if(!ssa.isValid()) return null;

        // Set the data range
        if(drsPanel != null) {
            // X
            int scol = ssa.startCol;
            if(drsPanel.hasXInput()) {
                drsPanel.setXDataRange(ssa.startRow, ssa.endRow, scol);
                ++scol;
            }
            // Y
            int idx = 0;
            for(int i = scol; i <= ssa.endCol; ++i) {
                if(idx >= numOfYAxis) break;
                drsPanel.setYDataRange(idx, ssa.startRow, ssa.endRow, i, false);
                ++idx;
            }
        }

        // Auto-calculate all the scales for the first time
        if(assPanel != null) assPanel.autoCalcScale();

        // Return the header (if any)
        return ssa.header;
    }

    // Return the data range (minimum and maximum values) of the X data from the given spreadsheet and data-range-setting panels
    public double[] getXDataRange(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel)
    {
        StdPlotDataRangeSettingPanel.DataRangeSpec dr = drsPanel.getXDataRange();
        if(!dr.isValid()) return new double[]{ 0, 0 };

        double[] vals = ssPanel.getColValues(dr.col, dr.startRow, dr.endRow);
        if(vals == null || vals.length <= 0) return new double[]{ 0, 0 };

        double   min =  Double.MAX_VALUE;
        double   max = -Double.MAX_VALUE;
        for(int i = 0; i < vals.length; ++i) {
            double cur = vals[i];
            if(cur < min) min = cur;
            if(cur > max) max = cur;
        }

        return new double[]{ min, max };
    }

    // Return the data range (minimum and maximum values) of the X data from the given spreadsheet and data-range-setting panels
    public double[] getXDataRangeFromFirstRowOfYInputs(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel, int numOfYAxis)
    {
        boolean any = false;
        double  min =  Double.MAX_VALUE;
        double  max = -Double.MAX_VALUE;

        for(int i = 0; i < numOfYAxis; ++i) {
            StdPlotDataRangeSettingPanel.DataRangeSpec dr = drsPanel.getYDataRange(i);
            if(dr == null) break;
            if(!dr.isValid()) continue;

            double[] vals = ssPanel.getColValues(dr.col, dr.startRow, dr.endRow);
            if(vals == null || vals.length <= 0) continue;

            double cur = vals[0];
            if(cur < min) min = cur;
            if(cur > max) max = cur;

            any = true;
        }

        return any ? (new double[]{ min, max }) : (new double[]{ 0, 0 });
    }

    // Return the data range (minimum and maximum values) of the Y data from the given spreadsheet and data-range-setting panels
    public double[] getYDataRange(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel, int numOfYAxis, boolean rightYAxis, int rowOffset)
    {
        boolean any = false;
        double  min =  Double.MAX_VALUE;
        double  max = -Double.MAX_VALUE;

        for(int i = 0; i < numOfYAxis; ++i) {
            StdPlotDataRangeSettingPanel.DataRangeSpec dr = drsPanel.getYDataRange(i);
            if(dr == null) break;
            if(!dr.isValid() || dr.useRightYAxis ^ rightYAxis) continue;

            double[] vals = ssPanel.getColValues(dr.col, dr.startRow, dr.endRow);
            if(vals == null || vals.length <= 0) continue;

            for(int j = rowOffset; j < vals.length; ++j) {
                double cur = vals[j];
                if(cur < min) min = cur;
                if(cur > max) max = cur;
            }
            any = true;
        }

        return any ? (new double[]{ min, max }) : (new double[]{ 0, 0 });
    }

    // Return the value-array of the X data from the given spreadsheet and data-range-setting panels.
    public double[] getXDataArray(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel)
    {
        StdPlotDataRangeSettingPanel.DataRangeSpec dr = drsPanel.getXDataRange();
        return dr.isValid() ? ssPanel.getColValues(dr.col, dr.startRow, dr.endRow) : null;
    }

    // Return the value-array of the X data from the given spreadsheet and data-range-setting panels.
    public double[] getXDataArrayFromFirstRowOfYInputs(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel, int numOfYAxis)
    {
        StdPlotDataRangeSettingPanel.DataRangeSpec[] dra = new StdPlotDataRangeSettingPanel.DataRangeSpec[numOfYAxis];

        int avail = 0;
        for(int i = 0; i < numOfYAxis; ++i) {
            dra[i] = drsPanel.getYDataRange(i);
            if(!dra[i].isValid()) dra[i] = null;
            else                  ++avail;
        }
        if(avail <= 0) return null;

        double[] xData = new double[numOfYAxis];
        int      vaIdx = 0;
        for(int i = 0; i < numOfYAxis; ++i) {
            if(dra[i] == null) continue;
            xData[vaIdx++] = ssPanel.getValueAt(dra[i].startRow, dra[i].col);
        }

        return xData;
    }

    // Return the value-array of the Y data from the given spreadsheet, data-range-setting, and data-point-setting panels
    public YData[] getYDataArray(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel, StdPlotDataPointSettingPanel dpsPanel, int numOfYAxis, boolean rightYAxis, int rowOffset)
    {
        StdPlotDataRangeSettingPanel.DataRangeSpec[] dra = new StdPlotDataRangeSettingPanel.DataRangeSpec[numOfYAxis];

        int avail = 0;
        for(int i = 0; i < numOfYAxis; ++i) {
            dra[i] = drsPanel.getYDataRange(i);
            if(!dra[i].isValid() || dra[i].useRightYAxis ^ rightYAxis) dra[i] = null;
            else                                                       ++avail;
        }
        if(avail <= 0) return null;

        YData[] yData = new YData[avail];
        int     vaIdx = 0;
        for(int i = 0; i < numOfYAxis; ++i) {
            if(dra[i] == null) continue;
            yData[vaIdx++] = new YData(
                ssPanel.getColValues(dra[i].col, dra[i].startRow + rowOffset, dra[i].endRow),
                dpsPanel.getColor (i),
                dpsPanel.getSymbol(i),
                dpsPanel.getLine  (i)
            );
        }

        return yData;
    }

    // Return the value-array of the Y data from the given spreadsheet and data-range-setting panels.
    public double[][] getYDataArray(SpreadsheetPanel ssPanel, StdPlotDataRangeSettingPanel drsPanel, int numOfYAxis, boolean rightYAxis, int rowOffset)
    {
        StdPlotDataRangeSettingPanel.DataRangeSpec[] dra = new StdPlotDataRangeSettingPanel.DataRangeSpec[numOfYAxis];

        int avail = 0;
        for(int i = 0; i < numOfYAxis; ++i) {
            dra[i] = drsPanel.getYDataRange(i);
            if(!dra[i].isValid() || dra[i].useRightYAxis ^ rightYAxis) dra[i] = null;
            else                                                       ++avail;
        }
        if(avail <= 0) return null;

        double[][] yData = new double[avail][];
        int        vaIdx = 0;
        for(int i = 0; i < numOfYAxis; ++i) {
            if(dra[i] == null) continue;
            yData[vaIdx++] = ssPanel.getColValues(dra[i].col, dra[i].startRow + rowOffset, dra[i].endRow);
        }

        return yData;
    }

    // Force update the report
    public void updateReport()
    { if(_btnUpdateReport != null) _btnUpdateReport.doClick(); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for the buttons
    public void actionPerformed(ActionEvent event)
    {
        // Get the button that fired the event
        JButton button = (JButton) event.getSource();

        // Update primary plot
        if(button == _btnUpdatePPlot) {
            _imgPPlotArea = null;
            _pnlPPlotArea.repaint();
        }
        // Update secondary plot
        else if(button == _btnUpdateSPlot) {
            _imgSPlotArea = null;
            _pnlSPlotArea.repaint();
        }
        // Zoom primary/secondary plot
        else if(button == _btnZoomPPlot || button == _btnZoomSPlot) {
            // Get the screen size
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double avw = screenSize.width;
            double avh = screenSize.height;
            // Determine the size of the image
            int biWidth  = 0;
            int biHeight = 0;
                 if(avw >= 1500) { biWidth = 3000; biHeight = 2250; }
            else if(avw >= 1000) { biWidth = 2000; biHeight = 1500; }
            else                 { biWidth = 1000; biHeight =  750; }
            // Show wait cursor
            GUIMain.instance.showWaitCursor();
            // Create a buffered image and a graphics context
            BufferedImage imgPlot  = (BufferedImage) createImage(biWidth, biHeight);
            Graphics2D    grpPlot  = imgPlot.createGraphics();
            // Draw and show the plot
            try {
                // Draw the plot to the buffered image
                boolean plotOK = drawPlot(grpPlot, biWidth, biHeight, false, (button == _btnZoomSPlot));
                // Show the plot
                if(plotOK) {
                    ImageBox.showDialog(GUIMain.instance.getRootFrame(), imgPlot, _S("str_plot_view"));
                    System.gc();
                }
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogPlot();
            }
            grpPlot.dispose();
            // Show default cursor
            GUIMain.instance.showDefaultCursor();
        }
        // Print primary/secondary plot
        else if(button == _btnPrintPPlot || button == _btnPrintSPlot) {
            // Print plot
            try {
                new PrintPreview(GUIMain.instance.getRootFrame(), new PrintPlot(button == _btnPrintSPlot));
            }
            catch(Exception e) {}
        }
        // Save primary/secondary plot
        else if(button == _btnSavePPlot || button == _btnSaveSPlot) {
            // Get the resolution
            int[] res = ResolutionSelectorBox.showDialog(GUIMain.instance.getRootFrame(), _S("dlg_rsel_caption"));
            if(res == null) return;
            // Get the file name
            String filePath = FileSelectorBox.showFileSaveDialog(
                GUIMain.instance.getRootFrame(),
                _S("dlg_fsel_title_splot"),
                new String[] { _S("dlg_fsel_ft_bmp"), _S("dlg_fsel_ft_jpg"), _S("dlg_fsel_ft_png") },
                new String[] { "bmp",                 "jpg",                 "png"                 }
            );
            if(filePath == null) return;
            // Get the image type based on the file extension
            String lcfp = filePath.toLowerCase();
            int    dpos = lcfp.lastIndexOf(".");
            String type = lcfp.substring(dpos + 1, dpos + 4);
            if(!type.equals("png") && !type.equals("jpg") && !type.equals("bmp")) {
                GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_img_type_unknown"));
                return;
            }
            // Show wait cursor
            GUIMain.instance.showWaitCursor();
            // Create a buffered image and a graphics context
            BufferedImage imgPlot  = (BufferedImage) createImage(res[0], res[1]);
            Graphics2D    grpPlot  = imgPlot.createGraphics();
            // Draw the plot
            boolean plotOK = true;
            try {
                plotOK = drawPlot(grpPlot, res[0], res[1], false, (button == _btnSaveSPlot));
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogPlot();
                plotOK = false;
            }
            // Save the plot
            if(plotOK) {
                try {
                    File file = new File(filePath);
                    if(!ImageIO.write(imgPlot, type, file))
                        GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_save_plot_fail"));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_save_plot_fail"));
                }
            }
            // Dispose the plot
            grpPlot.dispose();
            System.gc();
            // Show default cursor
            GUIMain.instance.showDefaultCursor();
        }

        // Update report
        else if(button == _btnUpdateReport) {
            GUIMain.instance.showWaitCursor();
            try {
                _txtReport.setText(genReport(false, false));
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogReport();
            }
            GUIMain.instance.showDefaultCursor();
        }
        // Print report
        else if(button == _btnPrintReport) {
            try {
                new PrintPreview(GUIMain.instance.getRootFrame(), new PrintReport());
            }
            catch(Exception e) {}
        }
        // Save report
        else if(button == _btnSaveReport) {
            // Get the file name
            final String filePath = FileSelectorBox.showFileSaveDialog(
                GUIMain.instance.getRootFrame(),
                _S("dlg_fsel_title_sreport"),
                new String[] { _S("dlg_fsel_ft_htm"), _S("dlg_fsel_ft_txt") },
                new String[] { "html",                "txt",                }
            );
            if(filePath == null) return;
            // Get the file type based on the file extension
            String lcfp = filePath.toLowerCase();
            int    dpos = lcfp.lastIndexOf(".");
            String type = lcfp.substring(dpos + 1, lcfp.length());
            if(!type.equals("txt") && !type.equals("html") && !type.equals("htm")) {
                GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_rep_type_unknown"));
                return;
            }
            // Show wait cursor
            GUIMain.instance.showWaitCursor();
            // Generate the report
            String report = "";
            try {
                report = genReport(!type.equals("txt"), false);
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogReport();
                report = "";
            }
            // Save the report
            if(!report.equals("")) {
                // Adjust the new-line character
                String os = System.getProperty("os.name").toLowerCase();
                if((os.indexOf("win") >= 0)) report = report.replaceAll("\n", "\r\n");
                try {
                    // Save the report
                    FileWriter  fw = new FileWriter(filePath);
                    PrintWriter pw = new PrintWriter(fw);
                    pw.print(report);
                    fw.close();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_save_report_fail"));
                }
            }
            // Show default cursor
            GUIMain.instance.showDefaultCursor();
        }

        // Calculate a test value
        else if(button == _btnCalculate) {
            GUIMain.instance.showWaitCursor();
            double y = GUtil.str2d(_txtInputY.getText());
            try {
                double x = calcContentValue(y);
                _txtOutputX.setText(StringTranslator.format("%.5g", x));
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogCalc();
            }
            GUIMain.instance.showDefaultCursor();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Internal plot-area class
    private class PlotArea extends JPanel {
        private static final int BI_WIDTH  = 1000;
        private static final int BI_HEIGHT =  750;

        private boolean _secondary;

        public PlotArea(boolean secondary)
        {
            super(null, true);
            _secondary = secondary;
        }

        public void paintComponent(Graphics g)
        {
            // Buffered image to where the plot will be drawn
            BufferedImage bi = null;

            // Create a new buffered image for the secondary plot?
            if(_secondary) {
                if(_imgSPlotArea == null) {
                    // Create a new buffered image for the plot
                    _imgSPlotArea = (BufferedImage) createImage(BI_WIDTH, BI_HEIGHT);
                    bi = _imgSPlotArea;
                }
            }
            // Create a new buffered image for the primary plot?
            else if(_imgPPlotArea == null) {
                _imgPPlotArea = (BufferedImage) createImage(BI_WIDTH, BI_HEIGHT);
                bi = _imgPPlotArea;
            }

            // Draw the plot to the buffered image (if needed)
            if(bi != null) {
                GUIMain.instance.showWaitCursor();
                Graphics2D grpPlot = bi.createGraphics();
                try {
                    drawPlot(grpPlot, BI_WIDTH, BI_HEIGHT, true, _secondary);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showNoDDialogPlot();
                }
                grpPlot.dispose();
                GUIMain.instance.showDefaultCursor();
            }

            // Blit the buffered image to the panel
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
            g2d.drawImage(
                _secondary ? _imgSPlotArea : _imgPPlotArea,
                0, 0, getWidth(), getHeight(),
                0, 0, BI_WIDTH, BI_HEIGHT,
                new Color(0, 0, 0, 0),
                null
            );
            g2d.dispose();
            System.gc();
        }
    }

    // Internal print-plot class
    private class PrintPlot implements Printable {
        private boolean _secondary;

        PrintPlot(boolean secondary)
        {
            super();
            _secondary = secondary;
        }

        public int print(Graphics g, PageFormat pf, int page) throws PrinterException
        {
            // We have only one page
            if(page > 0) return NO_SUCH_PAGE;

            // Get the page-area size
            double avw = pf.getImageableWidth ();
            double avh = pf.getImageableHeight();

            // Determine the size of the image
            int biWidth  = 0;
            int biHeight = 0;
            if(avw >= 2400) { biWidth = 4800; biHeight = 3600; }
            else            { biWidth = 2400; biHeight = 1800; }

            // Create a buffered image and a graphics context
            BufferedImage imgPlot = (BufferedImage) createImage(biWidth, biHeight);
            Graphics2D    grpPlot = imgPlot.createGraphics();

            // Draw the plot to the buffered image
            boolean plotOK = true;
            try {
                plotOK = drawPlot(grpPlot, biWidth, biHeight, false, _secondary);
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogPlot();
                plotOK = false;
            }
            grpPlot.dispose();
            if(!plotOK) throw new PrinterException(_S("err_anal_plot_fail"));

            // Calculate the destination image size
            double scw = 0;
            double sch = 0;
            if(avw <= avh) {
                scw = avw;
                sch = avw * biHeight / biWidth;
            }
            else {
                scw = avh * biWidth / biHeight;
                sch = avh;
            }

            // Blit the buffered image to the printer's graphics context
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
            g2d.drawImage(
                imgPlot,
                (int) pf.getImageableX(), (int) pf.getImageableY(), (int) (pf.getImageableX() + scw), (int) (pf.getImageableY() + sch),
                0, 0, biWidth, biHeight,
                new Color(0, 0, 0, 0),
                null
            );
            if(GUIMain.instance.isDemoMode()) { /** From v1.2.0a */
                g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
                g2d.setFont(new Font(GUtil.getSysFontName("SansSerif"), Font.BOLD, (int) scw / 4));
                g2d.setColor(new Color(128, 128, 128, 128));
                FontMetrics fm = g2d.getFontMetrics();
                int         sw = fm.stringWidth(STR_DEMO_D + STR_DEMO_E + STR_DEMO_M + STR_DEMO_O);
                int         xx =  (int) (pf.getImageableX() + scw / 2 - sw / 2);
                int         yy =  (int) (pf.getImageableY() + sch / 2 + (fm.getAscent() - fm.getDescent()) / 2);
                g2d.drawString(STR_DEMO_D + STR_DEMO_E + STR_DEMO_M + STR_DEMO_O, xx, yy);
            }
            g2d.dispose();

            // Done
            return PAGE_EXISTS;
        }
    }

    // Internal print-report class
    private class PrintReport implements Printable {
        private String[]    _reportLines  = null;
        private int         _biWidth      = 0;
        private int         _biHeight     = 0;
        private int         _fontSize     = 0;
        private Font        _font         = null;
        private FontMetrics _fontMetrics  = null;
        private Font        _fonti        = null;
        private FontMetrics _fontMetricsi = null;
        private int         _yInc         = 0;
        private int         _linesPerPage = 0;

        public PrintReport() throws Exception
        {
            super();

            // Generate the report
            String strReport = null;
            try {
                strReport = genReport(true, true);
                int s = strReport.indexOf("<pre>");
                int e = strReport.indexOf("</pre>");
                if(s >= 0 && e >= 0)
                    strReport = strReport.substring(s + 5 + 1, e - 1);
                else
                    strReport = null;
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogReport();
                strReport = null;
            }
            if(strReport == null) throw new Exception(_S("err_anal_report_fail"));

            // Split the report to lines
            StringTokenizer   st = new StringTokenizer(strReport, "\n");
            ArrayList<String> al = new ArrayList<String>();
            while(st.hasMoreTokens()) al.add(st.nextToken());
            _reportLines = new String[al.size()];
            al.toArray(_reportLines);
        }

        public int print(Graphics g, PageFormat pf, int page) throws PrinterException
        {
            // Determine the size of the image
            if(_biWidth <= 0 || _biHeight <= 0) {
                _biWidth  = (int) pf.getImageableWidth () * 4;
                _biHeight = (int) pf.getImageableHeight() * 4;
                _fontSize = (int) (7.7 * 4);
            }

            // Create a buffered image and a graphics context for printing the report
            BufferedImage imgReport = (BufferedImage) createImage(_biWidth, _biHeight);
            Graphics2D    grpReport = imgReport.createGraphics();

            // Calculate the font metrics, Y-increment, and number of lines per page
            if(_font == null | _fonti == null) {
                // Normal font
                _font = new Font(GUtil.getSysFontName("Monospaced"), Font.BOLD, _fontSize);
                grpReport.setFont(_font);
                _fontMetrics = grpReport.getFontMetrics();
                // Italic font
                _fonti = new Font(GUtil.getSysFontName("Monospaced"), Font.BOLD | Font.ITALIC, _fontSize);
                grpReport.setFont(_fonti);
                _fontMetricsi = grpReport.getFontMetrics();
                // Calculate the Y increment and the number of lines per page
                _yInc         = (_fontMetrics.getAscent() + _fontMetrics.getDescent());
                _linesPerPage = (_biHeight - _yInc) / _yInc;
            }

            // Determine the starting line index and check if there is no more lines left
            int lineIdx = page * _linesPerPage;
            if(lineIdx >= _reportLines.length) {
                grpReport.dispose();
                return NO_SUCH_PAGE;
            }

            // Set the quality and color
            // 0        1         2         3         4         5         6         7         8         9         0
            // 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
            grpReport.setRenderingHints(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,   RenderingHints.VALUE_COLOR_RENDER_QUALITY));
            grpReport.setRenderingHints(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON));
            grpReport.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY      ));
            grpReport.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON   ));
            grpReport.setColor(new Color(0, 0, 0));

            // Print the report to the buffered image
            int x    = 0;
            int y    = _yInc;
            int lCnt = 0;
            while(lineIdx < _reportLines.length) {
                PlotRenderer.renderHorizontalText(grpReport, _reportLines[lineIdx++], x, y, _font, _fontMetrics, _fonti, _fontMetricsi, -1);
                y += _yInc;
                if(++lCnt >= _linesPerPage) break;
            }
            grpReport.dispose();

            // Blit the buffered image to the printer's graphics context
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
            g2d.drawImage(
                imgReport,
                (int) pf.getImageableX(), (int) pf.getImageableY(), (int) (pf.getImageableX() + pf.getImageableWidth()), (int) (pf.getImageableY() + pf.getImageableHeight()),
                0, 0, _biWidth, _biHeight,
                new Color(0, 0, 0, 0),
                null
            );
            if(GUIMain.instance.isDemoMode()) { /** From v1.2.0a */
                g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
                g2d.setFont(new Font(GUtil.getSysFontName("SansSerif"), Font.BOLD, (int) pf.getImageableWidth() / 4));
                g2d.setColor(new Color(128, 128, 128, 128));
                FontMetrics fm = g2d.getFontMetrics();
                int         sw = fm.stringWidth(STR_DEMO_D + STR_DEMO_E + STR_DEMO_M + STR_DEMO_O);
                int         xx =  (int) (pf.getImageableX() + pf.getImageableWidth() / 2 - sw / 2);
                int         yy =  (int) (pf.getImageableY() + pf.getImageableHeight() / 2 + (fm.getAscent() - fm.getDescent()) / 2);
                g2d.drawString(STR_DEMO_D + STR_DEMO_E + STR_DEMO_M + STR_DEMO_O, xx, yy);
            }
            g2d.dispose();

            // Done
            return PAGE_EXISTS;
        }
    }

}
