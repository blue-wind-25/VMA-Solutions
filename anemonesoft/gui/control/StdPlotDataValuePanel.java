/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
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
// A standard plot-data-value panel
//
public class StdPlotDataValuePanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Controls
    private JTextField[] _txtValue = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Add an axis setting
    private void _addSetting(JPanel parent, String title, JTextField value, boolean lastPart)
    {
        final int WIDTH  = GUtil.DEFAULT_LARGE_BOX_WIDTH;
        final int HEIGHT = GUtil.DEFAULT_BOX_HEIGHT;

        value.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        value.setDocument(new NumericDocument(5, false));
        value.setText("0.00000");

        JPanel pnlCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            pnlCaption.add(new JLabel(title, JLabel.LEFT));
        parent.add(pnlCaption);

        JPanel pnlSetting = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            pnlSetting.add(value);
        parent.add(pnlSetting);

        if(!lastPart) parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        // Save the number of Y-axis
        ds.writeInt(_txtValue.length);

        // Save the Y data value
        for(int i = 0; i < _txtValue.length; ++i) {
            ds.writeDouble(getValue(i));
        }
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;

        int s, e, c;
        boolean b;

        // Load and check the number of Y-axis
        int noya = ds.readInt();
        if(noya != _txtValue.length) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_proj_inv_drc"));
            return false;
        }

        // Load the Y data caption
        for(int i = 0; i < _txtValue.length; ++i) {
            _txtValue[i].setText("" + ds.readDouble());
        }

        // Done
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-data-value panel
    public StdPlotDataValuePanel(int numOfTick, String[] customName)
    {
        super(new BorderLayout(), true);

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Add the input setting for the ticks
        _txtValue = new JTextField[numOfTick];

        if(numOfTick == 1) {
            _txtValue[0] = new JTextField();
            _addSetting(mainPanel, (customName != null) ? customName[0] : _S("str_s_value"), _txtValue[0], true);
        }
        else {
            for(int i = 0; i < numOfTick; ++i) {
                String title = (customName != null) ? customName[i] : _F("str_s_value_T", new Object[] { i + 1 });
                _txtValue[i] = new JTextField();
                _addSetting(mainPanel, title, _txtValue[i], i == (numOfTick - 1));
            }
        }
    }

    // Get the number of ticks
    public int getNumberOfTicks()
    { return _txtValue.length; }

    // Get the Y caption with the given index
    public double getValue(int idx)
    { return GUtil.str2d(_txtValue[idx].getText()); }

    // Reset all values to the given value
    public void resetAllValue(double v)
    {
        for(int i = 0; i < _txtValue.length; ++i) {
            _txtValue[i].setText("" + v);
        }
    }
}
