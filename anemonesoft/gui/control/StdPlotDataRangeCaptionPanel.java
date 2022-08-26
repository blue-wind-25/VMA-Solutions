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
// A standard plot-data-range-caption panel
//
public class StdPlotDataRangeCaptionPanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Controls
    private JTextField[] _txtCaption = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Add an axis setting
    private void _addSetting(JPanel parent, String title, JTextField caption, boolean lastPart, String defVal)
    {
        final int WIDTH  = GUtil.DEFAULT_LARGE_BOX_WIDTH;
        final int HEIGHT = GUtil.DEFAULT_BOX_HEIGHT;

        caption.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        caption.setText(defVal);

        JPanel pnlCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            pnlCaption.add(new JLabel(title, JLabel.LEFT));
        parent.add(pnlCaption);

        JPanel pnlSetting = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            pnlSetting.add(caption);
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
        ds.writeInt(_txtCaption.length);

        // Save the Y data caption
        for(int i = 0; i < _txtCaption.length; ++i) {
            ds.writeUTF(_txtCaption[i].getText());
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
        if(noya != _txtCaption.length) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_proj_inv_drc"));
            return false;
        }

        // Load the Y data caption
        for(int i = 0; i < _txtCaption.length; ++i) {
            _txtCaption[i].setText(ds.readUTF());
        }

        // Done
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-data-range-caption panel
    public StdPlotDataRangeCaptionPanel(int numOfTick, String[] customName, String prefix)
    {
        super(new BorderLayout(), true);

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Add the input setting for the ticks
        _txtCaption = new JTextField[numOfTick];

        if(numOfTick == 1) {
            _txtCaption[0] = new JTextField();
            _addSetting(mainPanel, (customName != null) ? customName[0] : _S("str_y_values"), _txtCaption[0], true, prefix + 1);
        }
        else {
            for(int i = 0; i < numOfTick; ++i) {
                String title = (customName != null) ? customName[i] : _F("str_y_values_T", new Object[] { i + 1 });
                _txtCaption[i] = new JTextField();
                _addSetting(mainPanel, title, _txtCaption[i], i == (numOfTick - 1), prefix + (i + 1));
            }
        }
    }

    // Get the number of ticks
    public int getNumberOfTicks()
    { return _txtCaption.length; }

    // Get the Y caption with the given index
    public String getCaption(int idx)
    { return GUIMain.instance.getSpreadsheetPanel().getRawValueByAddress(_txtCaption[idx].getText()); }

    // Set the Y caption with the given index
    public void setCaption(int idx, String caption)
    { _txtCaption[idx].setText(caption); }
}
