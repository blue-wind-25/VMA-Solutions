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
import anemonesoft.i18n.*;

//
// A standard plot-axis-scale-setting panel
//
public class StdPlotAxisScaleSettingPanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 1;

    // Controls
    private JTextField _txtXMin   = new JTextField();
    private JTextField _txtXMax   = new JTextField();
    private JTextField _txtXStep  = new JTextField();
    private JTextField _txtXSDiv  = new JTextField();
    private JButton    _btnXAuto  = new JButton(_S("str_auto"));
    private JTextField _txtLYMin  = new JTextField();
    private JTextField _txtLYMax  = new JTextField();
    private JTextField _txtLYStep = new JTextField();
    private JTextField _txtLYSDiv = new JTextField();
    private JButton    _btnLYAuto = new JButton(_S("str_auto"));
    private JTextField _txtRYMin  = new JTextField();
    private JTextField _txtRYMax  = new JTextField();
    private JTextField _txtRYStep = new JTextField();
    private JTextField _txtRYSDiv = new JTextField();
    private JButton    _btnRYAuto = new JButton(_S("str_auto"));

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Add an axis setting
    private void _addSetting(JPanel parent, String title, JTextField min, JTextField max, JTextField step, JTextField sdiv, JButton auto, boolean lastPart)
    {
        final int IWIDTH  = GUtil.DEFAULT_LARGE_BOX_WIDTH;
        final int TWIDTH  = GUtil.DEFAULT_SMALL_BOX_WIDTH;
        final int THEIGHT = GUtil.DEFAULT_BOX_HEIGHT;

        Font font = (new JLabel()).getFont();
             font = new Font(font.getName(), Font.BOLD, (int) (font.getSize() * 0.9));
        
        min .setPreferredSize(new Dimension(TWIDTH, THEIGHT));
        max .setPreferredSize(new Dimension(TWIDTH, THEIGHT));
        step.setPreferredSize(new Dimension(TWIDTH, THEIGHT));
        sdiv.setPreferredSize(new Dimension(TWIDTH, THEIGHT));
        auto.setPreferredSize(new Dimension(IWIDTH + TWIDTH - 1, THEIGHT));

        min .setDocument(new NumericDocument( 3, true ));
        max .setDocument(new NumericDocument( 3, true ));
        step.setDocument(new NumericDocument( 3, true ));
        sdiv.setDocument(new NumericDocument(-1, false));

        GUtil.disableCutAndPasteOnTextField(min );
        GUtil.disableCutAndPasteOnTextField(max );
        GUtil.disableCutAndPasteOnTextField(step);
        GUtil.disableCutAndPasteOnTextField(sdiv);
    
        JPanel pnlCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblCaption = new JLabel(title, JLabel.LEFT);
            pnlCaption.add(lblCaption);
        parent.add(pnlCaption);

        JPanel pnlMin = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblMin = new JLabel(_S("str_min"), JLabel.LEFT);
                lblMin.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                lblMin.setFont(font);
            pnlMin.add(lblMin);
            pnlMin.add(min);
        parent.add(pnlMin);

        JPanel pnlMax = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblMax = new JLabel(_S("str_max"), JLabel.LEFT);
                lblMax.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                lblMax.setFont(font);
            pnlMax.add(lblMax);
            pnlMax.add(max);
        parent.add(pnlMax);
        
        JPanel pnlStep = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblStep = new JLabel(_S("str_step"), JLabel.LEFT);
                lblStep.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                lblStep.setFont(font);
            pnlStep.add(lblStep);
            pnlStep.add(step);
        parent.add(pnlStep);

        JPanel pnlSDiv = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            JLabel lblSDiv= new JLabel(_S("str_sdiv"), JLabel.LEFT);
                lblSDiv.setPreferredSize(new Dimension(IWIDTH, THEIGHT));
                lblSDiv.setFont(font);
            pnlSDiv.add(lblSDiv);
            pnlSDiv.add(sdiv);
        parent.add(pnlSDiv);

        JPanel pnlAuto = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
            pnlAuto.add(auto);
        parent.add(pnlAuto);
        
        if(!lastPart) parent.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    // Align the range no the nearest round number
    private double[] _alignRange(double mm[])
    {
        double min  = mm[0];
        double max  = mm[1];
        double amin = Math.abs(min);
        double amax = Math.abs(max);

        if(amin >= 10000 || amax >= 10000) {
            min = Math.floor(min / 10000) * 10000;
            max = Math.ceil (max / 10000) * 10000;
        }
        else if(amin >= 1000 || amax >= 1000) {
            min = Math.floor(min / 1000) * 1000;
            max = Math.ceil (max / 1000) * 1000;
        }
        else if(amin >= 100 || amax >= 100) {
            min = Math.floor(min / 100) * 100;
            max = Math.ceil (max / 100) * 100;
        }
        else if(amin >= 10 || amax >= 10) {
            min = Math.floor(min / 10) * 10;
            max = Math.ceil (max / 10) * 10;
        }
        else if(amin >= 5 || amax >= 5) {
            min = Math.floor(min / 5) * 5;
            max = Math.ceil (max / 5) * 5;
        }
        else if(amin < 0.1 || amax < 0.1) {
            min = Math.floor(min * 10) / 10;
            max = Math.ceil (max * 10) / 10;
        }
        else {
            min = Math.floor(min);
            max = Math.ceil (max);
        }

        return new double[]{ min, max };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        ds.writeDouble(getXMin ());
        ds.writeDouble(getXMax ());
        ds.writeDouble(getXStep());
        ds.writeInt   (getXSDiv());
        
        ds.writeDouble(getLeftYMin ());
        ds.writeDouble(getLeftYMax ());
        ds.writeDouble(getLeftYStep());
        ds.writeInt   (getLeftYSDiv());

        ds.writeDouble(getRightYMin ());
        ds.writeDouble(getRightYMax ());
        ds.writeDouble(getRightYStep());
        ds.writeInt   (getRightYSDiv());
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion != INTERFACE_VERSION) return false;
        
        double d;
        int    i;
        
        d = ds.readDouble(); setXMin (d);
        d = ds.readDouble(); setXMax (d);
        d = ds.readDouble(); setXStep(d);
        i = ds.readInt   (); setXSDiv(i);

        d = ds.readDouble(); setLeftYMin (d);
        d = ds.readDouble(); setLeftYMax (d);
        d = ds.readDouble(); setLeftYStep(d);
        i = ds.readInt   (); setLeftYSDiv(i);

        d = ds.readDouble(); setRightYMin (d);
        d = ds.readDouble(); setRightYMax (d);
        d = ds.readDouble(); setRightYStep(d);
        i = ds.readInt   (); setRightYSDiv(i);

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard plot-axis-scale-setting panel
    public StdPlotAxisScaleSettingPanel(final ResultPanel resultPanel, boolean canUseRightYAxis)
    {
        super(new BorderLayout(), true);

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Store a reference to this object
        final Object _THIS = this;

        // Add the input setting for the X-axis
        _addSetting(mainPanel, _S("str_x_axis"), _txtXMin, _txtXMax, _txtXStep, _txtXSDiv, _btnXAuto, false);
        _btnXAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                double[] dr = _alignRange(resultPanel.getXDataRange(_THIS));
                setXMin (dr[0]);
                setXMax (dr[1]);
                setXStep((dr[1] - dr[0]) / 5);
                setXSDiv(2);
            }
        });

        // Add the input setting for the Y-axis
        if(canUseRightYAxis) {
            _addSetting(mainPanel, _S("str_ly_axis"), _txtLYMin, _txtLYMax, _txtLYStep, _txtLYSDiv, _btnLYAuto, false);
            _addSetting(mainPanel, _S("str_ry_axis"), _txtRYMin, _txtRYMax, _txtRYStep, _txtRYSDiv, _btnRYAuto, true);
            _btnRYAuto.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event)
                {
                    double[] dr = _alignRange(resultPanel.getYDataRange(_THIS, true));
                    setRightYMin (dr[0]);
                    setRightYMax (dr[1]);
                    setRightYStep((dr[1] - dr[0]) / 5);
                    setRightYSDiv(2);
                }
            });
        }
        else {
            _addSetting(mainPanel, _S("str_y_axis"), _txtLYMin, _txtLYMax, _txtLYStep, _txtLYSDiv, _btnLYAuto, true);
        }
        _btnLYAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                double[] dr = _alignRange(resultPanel.getYDataRange(_THIS, false));
                setLeftYMin (dr[0]);
                setLeftYMax (dr[1]);
                setLeftYStep((dr[1] - dr[0]) / 5);
                setLeftYSDiv(2);
            }
        });
    }

    // Auto-calculate all the scales
    public void autoCalcScale()
    {
        _btnXAuto.doClick();
        _btnLYAuto.doClick();
        _btnRYAuto.doClick();
    }

    // Getters
    public double getXMin      () { return GUtil.str2d(_txtXMin  .getText()); }
    public double getXMax      () { return GUtil.str2d(_txtXMax  .getText()); }
    public double getXStep     () { return GUtil.str2d(_txtXStep .getText()); }
    public int    getXSDiv     () { return GUtil.str2i(_txtXSDiv .getText()); }
    public double getLeftYMin  () { return GUtil.str2d(_txtLYMin .getText()); }
    public double getLeftYMax  () { return GUtil.str2d(_txtLYMax .getText()); }
    public double getLeftYStep () { return GUtil.str2d(_txtLYStep.getText()); }
    public int    getLeftYSDiv () { return GUtil.str2i(_txtLYSDiv.getText()); }
    public double getRightYMin () { return GUtil.str2d(_txtRYMin .getText()); }
    public double getRightYMax () { return GUtil.str2d(_txtRYMax .getText()); }
    public double getRightYStep() { return GUtil.str2d(_txtRYStep.getText()); }
    public int    getRightYSDiv() { return GUtil.str2i(_txtRYSDiv.getText()); }

    // Setters
    public void setXMin      (double v) { _txtXMin  .setText("" + v); }
    public void setXMax      (double v) { _txtXMax  .setText("" + v); }
    public void setXStep     (double v) { _txtXStep .setText("" + v); }
    public void setXSDiv     (int    v) { _txtXSDiv .setText("" + v); }
    public void setLeftYMin  (double v) { _txtLYMin .setText("" + v); }
    public void setLeftYMax  (double v) { _txtLYMax .setText("" + v); }
    public void setLeftYStep (double v) { _txtLYStep.setText("" + v); }
    public void setLeftYSDiv (int    v) { _txtLYSDiv.setText("" + v); }
    public void setRightYMin (double v) { _txtRYMin .setText("" + v); }
    public void setRightYMax (double v) { _txtRYMax .setText("" + v); }
    public void setRightYStep(double v) { _txtRYStep.setText("" + v); }
    public void setRightYSDiv(int    v) { _txtRYSDiv.setText("" + v); }
}
