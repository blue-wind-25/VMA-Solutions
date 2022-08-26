/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.tab.*;
import anemonesoft.i18n.*;

//
// A standard plot-data-range-setting panel
//
public class StdPlotDataRangeSettingPanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Controls
    private JTextField   _txtXStart = new JTextField();
    private JTextField   _txtXEnd   = new JTextField();
    private JTextField[] _txtYStart = null;
    private JTextField[] _txtYEnd   = null;
    private JCheckBox [] _chkYUseR  = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Add an axis setting
    private void _addSetting(JPanel parent, String title, JTextField start, JTextField end, JCheckBox useRightYAxis, boolean lastPart)
    {
        final int WIDTH  = GUtil.DEFAULT_SMALL_BOX_WIDTH;
        final int HEIGHT = GUtil.DEFAULT_BOX_HEIGHT;

        start.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        end  .setPreferredSize(new Dimension(WIDTH, HEIGHT));

        start.setDocument(new SheetRangeDocument(SpreadsheetPanel.SS_NUM_COLS - 1, 4));
        end  .setDocument(new SheetRangeDocument(SpreadsheetPanel.SS_NUM_COLS - 1, 4));
    
        GUtil.disableCutAndPasteOnTextField(start);
        GUtil.disableCutAndPasteOnTextField(end );

        JPanel pnlCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            pnlCaption.add(new JLabel(title, JLabel.LEFT));
        parent.add(pnlCaption);

        JPanel pnlSetting = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblSeparator = new JLabel("-", JLabel.CENTER);
                lblSeparator.setPreferredSize(new Dimension(15, HEIGHT));
            pnlSetting.add(start);
            pnlSetting.add(lblSeparator);
            pnlSetting.add(end);
            if(useRightYAxis != null) {
                useRightYAxis.setToolTipText(_S("str_check_for_ry_tooltip"));
                pnlSetting.add(useRightYAxis);
                useRightYAxis.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            }
        parent.add(pnlSetting);

        if(!lastPart) parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    // Set data range setting
    private void _setDataRange(JTextField txtStart, JTextField txtEnd, JCheckBox chkRight, int startRow, int endRow, int col, boolean useRightAxis)
    {
        if(txtStart == null || txtEnd == null) return;

        if( col      < 0 || col      >= SpreadsheetPanel.SS_NUM_COLS ||
            startRow < 0 || startRow >= SpreadsheetPanel.SS_NUM_ROWS ||
            endRow   < 0 || endRow   >= SpreadsheetPanel.SS_NUM_ROWS
          ) {
            txtStart.setText("");
            txtEnd  .setText("");
            if(chkRight != null) chkRight.setSelected(false);
            return;
        }
        
        char   strCol      = (char) (col + 65);
        String strStartRow = "" + (startRow + 1);
        String strEndRow   = "" + (endRow + 1);

        txtStart.setText(strCol + strStartRow);
        txtEnd  .setText(strCol + strEndRow  );

        if(chkRight != null) chkRight.setSelected(useRightAxis);
    }

    // Get data range setting
    private DataRangeSpec _getDataRange(JTextField txtStart, JTextField txtEnd, JCheckBox chkRight)
    {
        if(txtStart == null || txtEnd == null) return new DataRangeSpec(-1, -1, -1, false);

        String  strStart     = txtStart.getText();
        String  strEnd       = txtEnd  .getText();
        boolean useRightAxis = (chkRight != null) ? chkRight.isSelected() : false;

        int col1 = (strStart.length() >= 2) ? strStart.codePointAt(0) - 65 : -1;
        int col2 = (strEnd  .length() >= 2) ? strEnd  .codePointAt(0) - 65 : -1;
        if(col1 != col2 || col1 < 0 || col1 > 25) return new DataRangeSpec(-1, -1, -1, false);

        try {
            int srow = Integer.parseInt(strStart.substring(1)) - 1;
            int erow = Integer.parseInt(strEnd  .substring(1)) - 1;
            return new DataRangeSpec(srow, erow, col1, useRightAxis);
        }
        catch(Exception e) {
        }

        return new DataRangeSpec(-1, -1, -1, false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // A data-range specification class
    public static class DataRangeSpec {
        public int     startRow, endRow, col;
        public boolean useRightYAxis;

        public DataRangeSpec(int s, int e, int c, boolean r)
        { startRow = s; endRow = e; col = c; useRightYAxis = r; }

        public boolean isValid()
        { return (startRow >= 0 && endRow >= 0 && col >= 0); }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        DataRangeSpec dr = null;
        
        // Save the X data range
        if(_txtXStart != null) {
            dr = getXDataRange();
            ds.writeInt(dr.startRow);
            ds.writeInt(dr.endRow);
            ds.writeInt(dr.col);
        }

        // Save the number of Y-axis
        ds.writeInt(_txtYStart.length);

        // Save the Y data range
        for(int i = 0; i < _txtYStart.length; ++i) {
            dr = getYDataRange(i);
            ds.writeInt    (dr.startRow);
            ds.writeInt    (dr.endRow);
            ds.writeInt    (dr.col);
            ds.writeBoolean(dr.useRightYAxis);
        }
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;
        
        int s, e, c;
        boolean b;

        // Load the X data range
        if(_txtXStart != null) {
            s = ds.readInt();
            e = ds.readInt();
            c = ds.readInt();
            setXDataRange(s, e, c);
        }

        // Load and check the number of Y-axis
        int noya = ds.readInt();
        if(noya != _txtYStart.length) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_proj_inv_drs"));
            return false;
        }

        // Load the Y data range
        for(int i = 0; i < _txtYStart.length; ++i) {
            s = ds.readInt();
            e = ds.readInt();
            c = ds.readInt();
            b = ds.readBoolean();
            setYDataRange(i, s, e, c, b);
        }

        // Done
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-data-range-setting panel
    public StdPlotDataRangeSettingPanel(int numOfYInput, boolean canUseRightYAxis, String[] customName)
    {
        super(new BorderLayout(), true);

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Add the input setting for the X-axis
        if(customName == null)
            _addSetting(mainPanel, _S("str_x_values"), _txtXStart, _txtXEnd, null, false);
        else if(customName[0] != null)
            _addSetting(mainPanel, customName[0], _txtXStart, _txtXEnd, null, false);
        else {
            _txtXStart = null;
            _txtXEnd   = null;
        }

        // Add the input setting for the Y-axis
        _txtYStart = new JTextField[numOfYInput];
        _txtYEnd   = new JTextField[numOfYInput];
        if(canUseRightYAxis) _chkYUseR = new JCheckBox[numOfYInput];

        if(numOfYInput == 1) {
            _txtYStart[0] = new JTextField();
            _txtYEnd  [0] = new JTextField();
            _addSetting(mainPanel, (customName != null) ? customName[1] : _S("str_y_values"), _txtYStart[0], _txtYEnd[0], null, true);
        }
        else {
            for(int i = 0; i < numOfYInput; ++i) {
                String title = (customName != null) ? customName[i + 1] : _F("str_y_values_T", new Object[] { i + 1 });
                _txtYStart[i] = new JTextField();
                _txtYEnd  [i] = new JTextField();
                
                if(canUseRightYAxis) {
                    _chkYUseR[i] = new JCheckBox();
                    _addSetting(mainPanel, title, _txtYStart[i], _txtYEnd[i], _chkYUseR[i], i == (numOfYInput - 1));
                }
                else {
                    _addSetting(mainPanel, title, _txtYStart[i], _txtYEnd[i], null, i == (numOfYInput - 1));
                }
            }
        }
    }

    // Check if we have an X input
    public boolean hasXInput()
    { return _txtXStart != null; }

    // Get the number of Y inputs
    public int getNumberOfYInputs()
    { return _txtYStart.length; }

    // Get the X data range
    public DataRangeSpec getXDataRange()
    { return _getDataRange(_txtXStart, _txtXEnd, null); }

    // Get the Y data range
    public DataRangeSpec getYDataRange(int idx)
    { return _getDataRange(_txtYStart[idx], _txtYEnd[idx], (_chkYUseR != null) ? _chkYUseR[idx] : null); }

    // Set the X data range
    public void setXDataRange(int startRow, int endRow, int col)
    { _setDataRange(_txtXStart, _txtXEnd, null, startRow, endRow, col, false); }

    // Set the Y data range
    public void setYDataRange(int idx, int startRow, int endRow, int col, boolean useRightAxis)
    { _setDataRange(_txtYStart[idx], _txtYEnd[idx], (_chkYUseR != null) ? _chkYUseR[idx] : null, startRow, endRow, col, useRightAxis); }
}
