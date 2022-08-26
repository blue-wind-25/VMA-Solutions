/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.control;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;

//
// A standard analysis-setting panel
//
public class StdAnalysisSettingPanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 2;

    public static final double[] PREDEFINED_PROBABILITY = new double[]{ 90.0, 95.0, 97.5, 99.0, 99.9 };

    // Default tolerance interval
    public static final double DEF_TOL_INTV = 2.0;
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Controls
    private JComboBox  _cmbProb    = null;
    private JTextField _txtNumDet  = null;
    private JTextField _txtLambda  = null;
    private JTextField _txtTolIntv = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        if(_cmbProb    != null) ds.writeInt   (getProbability());
        if(_txtNumDet  != null) ds.writeInt   (getNumOfFDet  ());
        if(_txtLambda  != null) ds.writeDouble(getLambda     ());

        if(_txtTolIntv != null) ds.writeDouble(getTolIntv    ());
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion > INTERFACE_VERSION) return false;

        if(_cmbProb   != null) setProbability(ds.readInt   ());
        if(_txtNumDet != null) setNumOfFDet  (ds.readInt   ());
        if(_txtLambda != null) setLambda     (ds.readDouble());

         if(_txtTolIntv != null) {
             if(interfaceVersion >= 2) { /** Available from interface version 2 */
                setTolIntv(ds.readDouble());
             }
             else {
                setTolIntv(DEF_TOL_INTV);
             }
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a standard analysis-setting panel
    public StdAnalysisSettingPanel(boolean withProbability, boolean withNumOfDet, boolean withLambda, boolean withTolIntv)
    {
        super(new BorderLayout(), true);

        final int IWIDTH  = GUtil.DEFAULT_SMALL_BOX_WIDTH;
        final int TWIDTH  = GUtil.DEFAULT_LARGE_BOX_WIDTH;
        final int THEIGHT = GUtil.DEFAULT_BOX_HEIGHT;

        // Create the main panel
        JPanel mainPanel = new JPanel(null, true);
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.NORTH);

        // Get font
        Font font = (new JLabel()).getFont();
             font = new Font(font.getName(), Font.BOLD, (int) (font.getSize() * 0.9));

        boolean putSpacer = false;
        
        // Generate the probability setting
        if(withProbability) {
            JPanel pnlPCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                JLabel lblPCaption = new JLabel(_S("str_probability"), JLabel.LEFT);
                    lblPCaption.setFont(font);
                pnlPCaption.add(lblPCaption);
            mainPanel.add(pnlPCaption);
            JPanel pnlProb = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                _cmbProb = new JComboBox();
                    _cmbProb.setFont(font);
                    _cmbProb.setPreferredSize(new Dimension(IWIDTH + TWIDTH - 1, THEIGHT));
                    _cmbProb.addItem("90.0%"); _cmbProb.addItem("95.0%");
                    _cmbProb.addItem("97.5%"); _cmbProb.addItem("99.0%");
                    _cmbProb.addItem("99.9%");
                    _cmbProb.setSelectedIndex(1);
                pnlProb.add(_cmbProb);
            mainPanel.add(pnlProb);
            putSpacer = true;
        }

        // Generate the number-of-determinations setting
        if(withNumOfDet) {
            if(putSpacer) mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel pnlDCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                JLabel lblDCaption = new JLabel(_S("str_num_of_det"), JLabel.LEFT);
                    lblDCaption.setFont(font);
                pnlDCaption.add(lblDCaption);
            mainPanel.add(pnlDCaption);
            JPanel pnlDet = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                _txtNumDet = new JTextField();
                    _txtNumDet.setPreferredSize(new Dimension(IWIDTH + TWIDTH - 1, THEIGHT));
                    _txtNumDet.setDocument(new NumericDocument(0, false));
                GUtil.disableCutAndPasteOnTextField(_txtNumDet);
                _txtNumDet.setText("1");
                pnlDet.add(_txtNumDet);
            mainPanel.add(pnlDet);
            putSpacer = true;
        }
        
        // Generate the lambda setting
        if(withLambda) {
            if(putSpacer) mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel pnlLCaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                JLabel lblLCaption = new JLabel(_S("str_lambda_procent"), JLabel.LEFT);
                    lblLCaption.setFont(font);
                pnlLCaption.add(lblLCaption);
            mainPanel.add(pnlLCaption);
            JPanel pnlLam = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                _txtLambda = new JTextField();
                    _txtLambda.setPreferredSize(new Dimension(IWIDTH + TWIDTH - 1, THEIGHT));
                    _txtLambda.setDocument(new NumericDocument(1, false));
                GUtil.disableCutAndPasteOnTextField(_txtLambda);
                _txtLambda.setText("5.0");
                pnlLam.add(_txtLambda);
            mainPanel.add(pnlLam);
            putSpacer = true;
        }

        // Generate the tolerance-interval setting
        if(withTolIntv) {
            if(putSpacer) mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel pnlTICaption = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                JLabel lblTICaption = new JLabel(_S("str_tol_interval"), JLabel.LEFT);
                    lblTICaption.setFont(font);
                pnlTICaption.add(lblTICaption);
            mainPanel.add(pnlTICaption);
            JPanel pnlTI = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), true);
                _txtTolIntv = new JTextField();
                    _txtTolIntv.setPreferredSize(new Dimension(IWIDTH + TWIDTH - 1, THEIGHT));
                    _txtTolIntv.setDocument(new NumericDocument(1, false));
                GUtil.disableCutAndPasteOnTextField(_txtTolIntv);
                _txtTolIntv.setText("" + DEF_TOL_INTV);
                pnlTI.add(_txtTolIntv);
            mainPanel.add(pnlTI);
        }
    }

    // Getters
    public int    getProbability() { return _cmbProb.getSelectedIndex(); }
    public int    getNumOfFDet  () { return GUtil.str2i(_txtNumDet.getText()); }
    public double getLambda     () { return GUtil.str2d(_txtLambda.getText()); }
    public double getTolIntv    () { return GUtil.str2d(_txtTolIntv.getText()); }

    // Setters
    public void setProbability(int    v) { _cmbProb.setSelectedIndex(v); }
    public void setNumOfFDet  (int    v) { _txtNumDet.setText("" + v); }
    public void setLambda     (double v) { _txtLambda.setText("" + v); }
    public void setTolIntv    (double v) { _txtTolIntv.setText("" + v); }
}
