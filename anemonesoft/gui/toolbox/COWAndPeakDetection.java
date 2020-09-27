/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.toolbox;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.gui.dialog.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;
import anemonesoft.stat.*;

//
// A toolbox for COW and peak detection
//
public class COWAndPeakDetection extends JDialog {
    // Size of the tabbed-pane
    private static final int WIDTH  = 1000;
    private static final int HEIGHT = 650;

    // Default constants for analysis
    private static final double DEFAULT_CORR_POWER     = 1.0;
    private static final int    MINIMUM_SEGMENT_LENGTH = 4;
    private static final int    MINIMUM_SLACK          = 1;

    private static final double DEFAULT_SELECTIVITY    = 12.5;
    private static final double DEFAULT_THRESHOLD      = 5.0;
    private static final double DEFAULT_IPSM_TOLERANCE = 1.0;
    
    // Thread for running COW and peak detection on all the input files
    private Thread        _runThread           = null;
    private boolean       _runThreadShouldStop = false;

    // Buffers
    private BufferedImage _imgPlotArea      = null;
    private boolean       _imgPlotAreaDirty = true;

    // Instance of this dialog
    private JDialog _thisDialog = this;

    // Controls
    private JTabbedPane _tbpMain                   = new JTabbedPane();
    
    private JPanel      _tabInputsAndPreprocessing = null;
    private JPanel      _tabCOWParameters          = null;
    private JPanel      _tabPeakDetection          = null;
    private JPanel      _tabOutputFiles            = null;

    private JList       _lstIPInputFiles           = new JList(new DefaultListModel());
    private JButton     _btnIPAddFile              = new JButton("+");
    private JButton     _btnIPDelFile              = new JButton("-");
    private JTextField  _txtIPEdgeCutOff           = new JTextField();
    private JComboBox   _cmbIPLowPassFilter        = new JComboBox(new String[] { "0", "1", "2", "3", "4", "5", "10", "25", "50", "75", "100" });
    private JComboBox   _cmbIPBaseLineCorrection   = new JComboBox(new String[] { _S("tb_cowpd_blc_none"), _S("tb_cowpd_blc_constant"), _S("tb_cowpd_blc_linear"), _S("tb_cowpd_blc_poly2"), _S("tb_cowpd_blc_poly3"), _S("tb_cowpd_blc_poly4"), _S("tb_cowpd_blc_poly5") });
    private JTextField  _txtIPMagnitudeScalling    = new JTextField();
    private PlotArea    _pnlIPPreview              = new PlotArea(0);
    private String      _strIPEdgeCutOff           = "";
    private String      _strIPMagnitudeScalling    = "";

    private JList       _lstCPTarget               = new JList(new DefaultListModel());
    private JTextField  _txtCPCorrPower            = new JTextField();
    private JTextField  _txtCPSegmentLength        = new JTextField();
    private JTextField  _txtCPSlack                = new JTextField();
    private JButton     _btnCPAuto                 = new JButton(_S("tb_cowpd_cp_auto"));
    private JComboBox   _cmbCPSignal               = new JComboBox();
    private PlotArea    _pnlCPPreview              = new PlotArea(1);
    private String      _strCPCorrPower            = "";
    private String      _strCPSegmentLength        = "";
    private String      _strCPSlack                = "";

    private JTextField  _txtPDSelectivity          = new JTextField();
    private JTextField  _txtPDThreshold            = new JTextField();
    private JButton     _btnPDDefault              = new JButton(_S("tb_cowpd_pd_default"));
    private PlotArea    _pnlPDPreview              = new PlotArea(2);
    private String      _strPDSelectivity          = "";
    private String      _strPDThreshold            = "";

    private JTextField  _txtOFSamePeakTolerance    = new JTextField();
    private JTextField  _txtOFAlignedChromData     = new JTextField();
    private JButton     _btnOFAlignedChromData     = new JButton("...");
    private JTextField  _txtOFPeakData             = new JTextField();
    private JButton     _btnOFPeakData             = new JButton("...");
    private JPanel      _pnlOFCancel               = null;
    private JLabel      _lblOFProgess              = new JLabel("", SwingConstants.CENTER);
    private JButton     _btnCancelProcess          = new JButton(_S("tb_cowpd_of_cancel"));
    private JButton     _btnRunprocess             = new JButton(_S("tb_cowpd_of_run"));

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Private constructor
    private COWAndPeakDetection(Frame parentFrame)
    {
        super(parentFrame);

        // Initialize the "Inputs and Preprocessing" tab
        _tabInputsAndPreprocessing = new JPanel(new BorderLayout(), true);
        {
            JPanel ipMainPanel = new JPanel(new GridLayout(2, 1, 5, 5), true);
                ipMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JPanel ipTopPanel = new JPanel(new GridLayout(1, 2, 5, 5), true);
                    JPanel ipFilePanel = new JPanel(new BorderLayout(5, 5), true);
                        ipFilePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                        JPanel ipFilePanelButtonContainer = new JPanel(new BorderLayout(), true);
                            JPanel ipFilePanelButton = new JPanel(new GridLayout(2, 1, 5, 5), true);
                                ipFilePanelButton.add(_btnIPAddFile);
                                ipFilePanelButton.add(_btnIPDelFile);
                                _btnIPAddFile.setPreferredSize(new Dimension(50, GUtil.DEFAULT_BOX_HEIGHT));
                                _btnIPDelFile.setPreferredSize(new Dimension(50, GUtil.DEFAULT_BOX_HEIGHT));
                            ipFilePanelButtonContainer.add(ipFilePanelButton, BorderLayout.NORTH);
                        ipFilePanel.add(new JScrollPane(_lstIPInputFiles), BorderLayout.CENTER);
                        ipFilePanel.add(ipFilePanelButtonContainer, BorderLayout.EAST);
                    JPanel ipSettingPanelContainer = new JPanel(new BorderLayout(), true);
                        ipSettingPanelContainer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                        JPanel ipSettingPanel = new JPanel(new GridLayout(4, 2, 5, 5), true);
                            JLabel ipLabelECF = new JLabel(_S("tb_cowpd_ip_ecf"));
                            JLabel ipLabelLPF = new JLabel(_S("tb_cowpd_ip_lpf"));
                            JLabel ipLabelBLC = new JLabel(_S("tb_cowpd_ip_blc"));
                            JLabel ipLabelMSL = new JLabel(_S("tb_cowpd_ip_msl"));
                            ipSettingPanel.add(ipLabelECF); ipSettingPanel.add(_txtIPEdgeCutOff);
                            ipSettingPanel.add(ipLabelLPF); ipSettingPanel.add(_cmbIPLowPassFilter);
                            ipSettingPanel.add(ipLabelBLC); ipSettingPanel.add(_cmbIPBaseLineCorrection);
                            ipSettingPanel.add(ipLabelMSL); ipSettingPanel.add(_txtIPMagnitudeScalling);
                            ipLabelECF              .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            ipLabelLPF              .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            ipLabelBLC              .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            ipLabelMSL              .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtIPEdgeCutOff        .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _cmbIPLowPassFilter     .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _cmbIPBaseLineCorrection.setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtIPMagnitudeScalling .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtIPEdgeCutOff        .setDocument(new NumericDocument(3, false));
                            _txtIPMagnitudeScalling .setDocument(new NumericDocument(3, false));
                            _txtIPEdgeCutOff        .setText("2.000");
                            _txtIPMagnitudeScalling .setText("100.000");
                            GUtil.disableCutAndPasteOnTextField(_txtIPMagnitudeScalling);
                        ipSettingPanelContainer.add(ipSettingPanel, BorderLayout.NORTH);
                    ipTopPanel.add(ipFilePanel);
                    ipTopPanel.add(ipSettingPanelContainer);
                JPanel ipBottomPanel = new JPanel(new BorderLayout(5, 5), true);
                    ipBottomPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                    JLabel ipLabelPV = new JLabel(_S("tb_cowpd_preview"));
                        ipLabelPV.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                    JPanel ipPreviewContainer = new JPanel(new BorderLayout(), true);
                        ipPreviewContainer.setBorder(BorderFactory.createEtchedBorder());
                        ipPreviewContainer.add(_pnlIPPreview, BorderLayout.CENTER);
                    ipBottomPanel.add(ipLabelPV, BorderLayout.NORTH);
                    ipBottomPanel.add(ipPreviewContainer, BorderLayout.CENTER);
                ipMainPanel.add(ipTopPanel, BorderLayout.NORTH);
                ipMainPanel.add(ipBottomPanel, BorderLayout.CENTER);
            _tabInputsAndPreprocessing.add(ipMainPanel, BorderLayout.CENTER);
        }

        // Initialize the "COW Parameters" tab
        _tabCOWParameters = new JPanel(new BorderLayout(), true);
        {
            JPanel cpMainPanel = new JPanel(new GridLayout(2, 1, 5, 5), true);
                cpMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JPanel cpTopPanel = new JPanel(new GridLayout(1, 2, 5, 5), true);
                    JPanel cpTargetPanel = new JPanel(new BorderLayout(5, 5), true);
                        cpTargetPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                        cpTargetPanel.add(new JLabel(_S("tb_cowpd_cp_target")), BorderLayout.NORTH);
                        cpTargetPanel.add(new JScrollPane(_lstCPTarget), BorderLayout.CENTER);
                    JPanel cpSettingPanelContainer = new JPanel(new BorderLayout(), true);
                        cpSettingPanelContainer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                        JPanel cpSettingPanel = new JPanel(new GridLayout(4, 2, 5, 5), true);
                            JLabel cpLabelCP = new JLabel(_S("tb_cowpd_cp_corrpow"));
                            JLabel cpLabelSG = new JLabel(_S("tb_cowpd_cp_seglen"));
                            JLabel cpLabelSC = new JLabel(_S("tb_cowpd_cp_slack"));
                            JLabel cpLabelEM = new JLabel("");
                            cpSettingPanel.add(cpLabelCP); cpSettingPanel.add(_txtCPCorrPower);
                            cpSettingPanel.add(cpLabelSG); cpSettingPanel.add(_txtCPSegmentLength);
                            cpSettingPanel.add(cpLabelSC); cpSettingPanel.add(_txtCPSlack);
                            cpSettingPanel.add(cpLabelEM); cpSettingPanel.add(_btnCPAuto);
                            cpLabelCP          .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            cpLabelSG          .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            cpLabelSC          .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            cpLabelEM          .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtCPCorrPower    .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtCPSegmentLength.setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtCPSlack        .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _btnCPAuto         .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                            _txtCPCorrPower    .setDocument(new NumericDocument(1, false));
                            _txtCPSegmentLength.setDocument(new NumericDocument(0, false));
                            _txtCPSlack        .setDocument(new NumericDocument(0, false));
                            GUtil.disableCutAndPasteOnTextField(_txtCPSegmentLength);
                            GUtil.disableCutAndPasteOnTextField(_txtCPSlack);
                        cpSettingPanelContainer.add(cpSettingPanel, BorderLayout.NORTH);
                    cpTopPanel.add(cpTargetPanel);
                    cpTopPanel.add(cpSettingPanelContainer);
                JPanel cpBottomPanel = new JPanel(new BorderLayout(5, 5), true);
                    cpBottomPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                    JPanel cpPreviewContainer = new JPanel(new BorderLayout(), true);
                        JPanel cpPreviewTop = new JPanel(new BorderLayout(5, 5), true);
                            JLabel cpLabelPV = new JLabel(_S("tb_cowpd_preview"));
                                cpLabelPV   .setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                                _cmbCPSignal.setPreferredSize(new Dimension(  0, GUtil.DEFAULT_BOX_HEIGHT));
                            cpPreviewTop.add(cpLabelPV, BorderLayout.WEST);
                            cpPreviewTop.add(_cmbCPSignal, BorderLayout.CENTER);
                        cpPreviewContainer.setBorder(BorderFactory.createEtchedBorder());
                        cpPreviewContainer.add(_pnlCPPreview, BorderLayout.CENTER);
                    cpBottomPanel.add(cpPreviewTop, BorderLayout.NORTH);
                    cpBottomPanel.add(cpPreviewContainer, BorderLayout.CENTER);
                cpMainPanel.add(cpTopPanel, BorderLayout.NORTH);
                cpMainPanel.add(cpBottomPanel, BorderLayout.CENTER);
            _tabCOWParameters.add(cpMainPanel, BorderLayout.CENTER);
        }
        
        // Initialize the "Peak Detection" tab
        _tabPeakDetection = new JPanel(new BorderLayout(), true);
        {
            JPanel pdMainPanel = new JPanel(new GridLayout(2, 1, 5, 5), true);
                pdMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JPanel pdTopPanel = new JPanel(new BorderLayout(), true);
                    JPanel pdSettingPanel = new JPanel(new GridLayout(3, 2, 5, 5), true);
                        JLabel pdLabelSL = new JLabel(_S("tb_cowpd_pd_sel"));
                        JLabel pdLabelTH = new JLabel(_S("tb_cowpd_pd_thres"));
                        JLabel pdLabelEM = new JLabel("");
                        pdSettingPanel.add(pdLabelSL); pdSettingPanel.add(_txtPDSelectivity);
                        pdSettingPanel.add(pdLabelTH); pdSettingPanel.add(_txtPDThreshold);
                        pdSettingPanel.add(pdLabelEM); pdSettingPanel.add(_btnPDDefault);
                        pdLabelSL        .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                        pdLabelTH        .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                        pdLabelEM        .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtPDSelectivity.setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtPDThreshold  .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                        _btnPDDefault    .setPreferredSize(new Dimension(0, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtPDSelectivity.setDocument(new NumericDocument(3, false));
                        _txtPDThreshold  .setDocument(new NumericDocument(3, false));
                        GUtil.disableCutAndPasteOnTextField(_txtPDSelectivity);
                        GUtil.disableCutAndPasteOnTextField(_txtPDThreshold);
                    pdTopPanel.add(pdSettingPanel, BorderLayout.NORTH);
                JPanel pdBottomPanel = new JPanel(new BorderLayout(5, 5), true);
                    pdBottomPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
                    JLabel pdLabelPV = new JLabel(_S("tb_cowpd_preview"));
                        pdLabelPV.setPreferredSize(new Dimension(100, GUtil.DEFAULT_BOX_HEIGHT));
                    JPanel pdPreviewContainer = new JPanel(new BorderLayout(), true);
                        pdPreviewContainer.setBorder(BorderFactory.createEtchedBorder());
                        pdPreviewContainer.add(_pnlPDPreview, BorderLayout.CENTER);
                    pdBottomPanel.add(pdLabelPV, BorderLayout.NORTH);
                    pdBottomPanel.add(pdPreviewContainer, BorderLayout.CENTER);
                pdMainPanel.add(pdTopPanel, BorderLayout.NORTH);
                pdMainPanel.add(pdBottomPanel, BorderLayout.CENTER);
            _tabPeakDetection.add(pdMainPanel, BorderLayout.CENTER);
        }
        
        // Initialize the "OutputFiles" tab
        _tabOutputFiles = new JPanel(new BorderLayout(), true);
        {
            JPanel ofMainPanel = new JPanel(new BorderLayout(), true);
                ofMainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                JPanel ofTopPanel = new JPanel(new GridLayout(3, 1, 5, 5), true);
                    JPanel ofPanelPT = new JPanel(new BorderLayout(5, 5), true);
                        JLabel ofLabelPT = new JLabel(_S("tb_cowpd_of_ipsmt"));
                        JLabel ofLabelDM = new JLabel("");
                        ofPanelPT.add(ofLabelPT,               BorderLayout.WEST);
                        ofPanelPT.add(_txtOFSamePeakTolerance, BorderLayout.CENTER);
                        ofPanelPT.add(ofLabelDM,               BorderLayout.EAST);
                        ofLabelPT              .setPreferredSize(new Dimension(250, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtOFSamePeakTolerance.setPreferredSize(new Dimension(  0, GUtil.DEFAULT_BOX_HEIGHT));
                        ofLabelDM              .setPreferredSize(new Dimension( 50, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtOFSamePeakTolerance.setDocument(new NumericDocument(3, false));
                        GUtil.disableCutAndPasteOnTextField(_txtOFSamePeakTolerance);
                    JPanel ofPanelAC = new JPanel(new BorderLayout(5, 5), true);
                        JLabel ofLabelAC = new JLabel(_S("tb_cowpd_of_acdata"));
                        ofPanelAC.add(ofLabelAC,              BorderLayout.WEST);
                        ofPanelAC.add(_txtOFAlignedChromData, BorderLayout.CENTER);
                        ofPanelAC.add(_btnOFAlignedChromData, BorderLayout.EAST);
                        ofLabelAC             .setPreferredSize(new Dimension(250, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtOFAlignedChromData.setPreferredSize(new Dimension(  0, GUtil.DEFAULT_BOX_HEIGHT));
                        _btnOFAlignedChromData.setPreferredSize(new Dimension( 50, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtOFAlignedChromData.setEditable(false);
                    JPanel ofPanelPK = new JPanel(new BorderLayout(5, 5), true);
                        JLabel ofLabelPK = new JLabel(_S("tb_cowpd_of_pdata"));
                        ofPanelPK.add(ofLabelPK,      BorderLayout.WEST);
                        ofPanelPK.add(_txtOFPeakData, BorderLayout.CENTER);
                        ofPanelPK.add(_btnOFPeakData, BorderLayout.EAST);
                        ofLabelPK     .setPreferredSize(new Dimension(250, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtOFPeakData.setPreferredSize(new Dimension(  0, GUtil.DEFAULT_BOX_HEIGHT));
                        _btnOFPeakData.setPreferredSize(new Dimension( 50, GUtil.DEFAULT_BOX_HEIGHT));
                        _txtOFPeakData.setEditable(false);
                    ofTopPanel.add(ofPanelPT);
                    ofTopPanel.add(ofPanelAC);
                    ofTopPanel.add(ofPanelPK);
                _pnlOFCancel = new JPanel(new GridLayout(2, 1, 5, 5), true);
                    _pnlOFCancel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(150, 200, 250, 200),
                        BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(15, 15, 15, 15))
                    ));
                    _pnlOFCancel.add(_lblOFProgess);
                    _pnlOFCancel.add(_btnCancelProcess);
                    _pnlOFCancel.setVisible(false);
                ofMainPanel.add(ofTopPanel, BorderLayout.NORTH);
                ofMainPanel.add(_pnlOFCancel, BorderLayout.CENTER);
                ofMainPanel.add(_btnRunprocess, BorderLayout.SOUTH);
            _tabOutputFiles.add(ofMainPanel, BorderLayout.CENTER);
        }

        // Initialize the tabbed-pane
        _tbpMain.setTabPlacement(JTabbedPane.TOP);
        _tbpMain.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        _tbpMain.setBorder(BorderFactory.createRaisedBevelBorder());
        _tbpMain.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        GUtil.addTab(_tbpMain, _S("tb_cowpd_tab_ip"), null, null, _tabInputsAndPreprocessing, true);
        GUtil.addTab(_tbpMain, _S("tb_cowpd_tab_cp"), null, null, _tabCOWParameters,          true);
        GUtil.addTab(_tbpMain, _S("tb_cowpd_tab_pd"), null, null, _tabPeakDetection,          true);
        GUtil.addTab(_tbpMain, _S("tb_cowpd_tab_of"), null, null, _tabOutputFiles,            true);
        
        // Create the dialog
        JPanel dialogPanel = new JPanel(new BorderLayout(), true);
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            dialogPanel.add(_tbpMain, BorderLayout.CENTER);
        add(dialogPanel);

        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension parentSize = parentFrame.getSize();
        Point     parentPos  = parentFrame.getLocation();
        setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);

        // Initialize event handlers for the tab-change event
        _tbpMain.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                Object tab = _tbpMain.getSelectedComponent();
                     if(tab == _tabCOWParameters) _updateTabContentCP();
                else if(tab == _tabPeakDetection) _updateTabContentPD();
                else if(tab == _tabOutputFiles  ) _updateTabContentOF();
                _imgPlotAreaDirty = true;
            }
        });

        // Initialize event handlers for the "Inputs and Preprocessing" tab
        _lstIPInputFiles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if(!event.getValueIsAdjusting()) {
                    _imgPlotAreaDirty = true;
                    _pnlIPPreview.repaint();
                }
            }
        });
        _btnIPAddFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _addChromatogramFile();
            }
        });
        _btnIPDelFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _delChromatogramFile();
            }
        });
        _cmbIPLowPassFilter.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if(event.getStateChange() == ItemEvent.SELECTED) {
                    _imgPlotAreaDirty = true;
                    _pnlIPPreview.repaint();
                }
            }
        });
        _txtIPEdgeCutOff.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strIPEdgeCutOff.equals(_txtIPEdgeCutOff.getText())) return;
                _strIPEdgeCutOff = _txtIPEdgeCutOff.getText();
                _imgPlotAreaDirty = true;
                _pnlIPPreview.repaint();
            }
        });
        _txtIPEdgeCutOff.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strIPEdgeCutOff.equals(_txtIPEdgeCutOff.getText())) return;
                _strIPEdgeCutOff = _txtIPEdgeCutOff.getText();
                _imgPlotAreaDirty = true;
                _pnlIPPreview.repaint();
            }
        });
        _cmbIPBaseLineCorrection.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if(event.getStateChange() == ItemEvent.SELECTED) {
                    _imgPlotAreaDirty = true;
                    _pnlIPPreview.repaint();
                }
            }
        });
        _txtIPMagnitudeScalling.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strIPMagnitudeScalling.equals(_txtIPMagnitudeScalling.getText())) return;
                _strIPMagnitudeScalling = _txtIPMagnitudeScalling.getText();
                _imgPlotAreaDirty = true;
                _pnlIPPreview.repaint();
            }
        });
        _txtIPMagnitudeScalling.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strIPMagnitudeScalling.equals(_txtIPMagnitudeScalling.getText())) return;
                _strIPMagnitudeScalling = _txtIPMagnitudeScalling.getText();
                _imgPlotAreaDirty = true;
                _pnlIPPreview.repaint();
            }
        });

        // Initialize event handlers for the "COW Parameters" tab
        _lstCPTarget.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if(!event.getValueIsAdjusting()) {
                    _imgPlotAreaDirty = true;
                    _pnlCPPreview.repaint();
                }
            }
        });
        _txtCPCorrPower.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strCPCorrPower.equals(_txtCPCorrPower.getText())) return;
                _strCPCorrPower = _txtCPCorrPower.getText();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });
        _txtCPCorrPower.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strCPCorrPower.equals(_txtCPCorrPower.getText())) return;
                _strCPCorrPower = _txtCPCorrPower.getText();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });
        _txtCPSegmentLength.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strCPSegmentLength.equals(_txtCPSegmentLength.getText())) return;
                _strCPSegmentLength = _txtCPSegmentLength.getText();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });
        _txtCPSegmentLength.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strCPSegmentLength.equals(_txtCPSegmentLength.getText())) return;
                _strCPSegmentLength = _txtCPSegmentLength.getText();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });        
        _txtCPSlack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strCPSlack.equals(_txtCPSlack.getText())) return;
                _strCPSlack = _txtCPSlack.getText();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });
        _txtCPSlack.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strCPSlack.equals(_txtCPSlack.getText())) return;
                _strCPSlack = _txtCPSlack.getText();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });
        _btnCPAuto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _autoDetCorrPowerSegLenAndSlack();
                _imgPlotAreaDirty = true;
                _pnlCPPreview.repaint();
            }
        });        
        _cmbCPSignal.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if(event.getStateChange() == ItemEvent.SELECTED) {
                    _imgPlotAreaDirty = true;
                    _pnlCPPreview.repaint();
                }
            }
        });        

        // Initialize event handlers for the "Peak Detection" tab
        _txtPDSelectivity.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strPDSelectivity.equals(_txtPDSelectivity.getText())) return;
                _strPDSelectivity = _txtPDSelectivity.getText();
                _imgPlotAreaDirty = true;
                _pnlPDPreview.repaint();
            }
        });
        _txtPDSelectivity.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strPDSelectivity.equals(_txtPDSelectivity.getText())) return;
                _strPDSelectivity = _txtPDSelectivity.getText();
                _imgPlotAreaDirty = true;
                _pnlPDPreview.repaint();
            }
        });
        _txtPDThreshold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(_strPDThreshold.equals(_txtPDThreshold.getText())) return;
                _strPDThreshold = _txtPDThreshold.getText();
                _imgPlotAreaDirty = true;
                _pnlPDPreview.repaint();
            }
        });
        _txtPDThreshold.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent event) {}
            public void focusLost  (FocusEvent event) {
                if(_strPDThreshold.equals(_txtPDThreshold.getText())) return;
                _strPDThreshold = _txtPDThreshold.getText();
                _imgPlotAreaDirty = true;
                _pnlPDPreview.repaint();
            }
        });
        _btnPDDefault.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _autoDetSelAndThershold();
                _imgPlotAreaDirty = true;
                _pnlPDPreview.repaint();
            }
        });

        // Initialize event handlers for the "OutputFiles" tab
        _btnOFAlignedChromData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _setAlignedChromDataFile();
            }
        });
        _btnOFPeakData.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _setPeakDataFile();
            }
        });
        _btnCancelProcess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _cancelProcess();
            }
        });
        _btnRunprocess.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _runProcess();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show a the toolbox
    public static void showToolbox(Frame parentFrame)
    {
        COWAndPeakDetection rb = new COWAndPeakDetection(parentFrame);
        GUtil.showModalDialog(rb, null, _S("tb_cowpd_title"), JRootPane.PLAIN_DIALOG);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Update tab contents - COW parameters
    private void _updateTabContentCP()
    {
        // Update the target list-box
        int prevTarget = _lstCPTarget.getSelectedIndex();
        if(prevTarget < 0) prevTarget = 0;

        DefaultListModel dlmIFiles = (DefaultListModel) _lstIPInputFiles.getModel();
        DefaultListModel dlmTarget = (DefaultListModel) _lstCPTarget    .getModel();

        dlmTarget.clear();
        for(int i = 0; i < dlmIFiles.getSize(); ++i) {
            dlmTarget.addElement(dlmIFiles.getElementAt(i));
        }

        try { _lstCPTarget.setSelectedIndex(prevTarget); }
        catch(Exception e) {}

        // Update the correlation-power, segment-length, and slack text-boxes
        if(_txtCPSegmentLength.getText().length() == 0 || _txtCPSlack.getText().length() == 0)
            _autoDetCorrPowerSegLenAndSlack();
        
        // Update the signal combo-box
        int prevSignal = _cmbCPSignal.getSelectedIndex();
        if(prevSignal < 0) prevSignal = 0;
        
        _cmbCPSignal.removeAllItems();
        for(int i = 0; i < dlmIFiles.getSize(); ++i) {
            _cmbCPSignal.addItem(dlmIFiles.getElementAt(i));
        }

        try { _cmbCPSignal.setSelectedIndex(prevSignal); }
        catch(Exception e) {}
    }

    // Update tab contents - peak detection
    private void _updateTabContentPD()
    {
        // Ensure that we have a valid target
        _updateTabContentCP();

        // Update the segment-length and slack text-boxes
        if(_txtPDSelectivity.getText().length() == 0 || _txtPDThreshold.getText().length() == 0)
            _autoDetSelAndThershold();
    }

    // Update tab contents - output files
    private void _updateTabContentOF()
    {
        // Ensure that we have a valid parameters
        _updateTabContentPD();

        // Update the segment-length and slack text-boxes
        if(_txtOFSamePeakTolerance.getText().length() == 0)
            _txtOFSamePeakTolerance.setText(Double.toString(DEFAULT_IPSM_TOLERANCE));
    }

    // Add a chromatogram file
    private void _addChromatogramFile()
    {
        String filePaths[] = MultiFileSelectorBox.showFileOpenDialog(
            GUIMain.instance.getRootFrame(),
            _S("tb_cowpd_addf_title"),
            new String[] { _S("tb_cowpd_addf_MzML"), _S("tb_cowpd_addf_MzXML"), _S("tb_cowpd_addf_Chrom"), _S("tb_cowpd_addf_XY") },
            new String[] { "mzML",                   "mzXML",                   "chrom" ,                  "xy"                   }
        );
        if(filePaths != null) {
            DefaultListModel dlm = (DefaultListModel) _lstIPInputFiles.getModel();
            for(int i = 0; i < filePaths.length; ++i) dlm.addElement(filePaths[i]);
            _lstIPInputFiles.setSelectedIndex(dlm.getSize() - 1);
        }
    }

    // Delete a chromatogram file
    private void _delChromatogramFile()
    {
        int curIdx = _lstIPInputFiles.getSelectedIndex();
        if(curIdx < 0) return;

        DefaultListModel dlm = (DefaultListModel) _lstIPInputFiles.getModel();
        dlm.remove(curIdx);

        _lstIPInputFiles.setSelectedIndex(curIdx);
        if(_lstIPInputFiles.getSelectedIndex() < 0) _lstIPInputFiles.setSelectedIndex(curIdx - 1);
    }

    // Automatically determine the correlation-power, segment-length, and slack
    private void _autoDetCorrPowerSegLenAndSlack()
    {
        GUtil.showWaitCursor(_thisDialog);

        DefaultListModel dlmTarget = (DefaultListModel) _lstCPTarget.getModel();

        int minLength = Integer.MAX_VALUE;
        for(int i = 0; i < dlmTarget.getSize(); ++i) {
            try {
                double[] signal = _loadSignal((String) dlmTarget.getElementAt(i));
                if(signal.length < minLength) minLength = signal.length;
            }
            catch(Exception e) {
            }
        }
        if(minLength == Integer.MAX_VALUE) minLength = 0;

        int segLen = minLength / 5;
        if(segLen < MINIMUM_SEGMENT_LENGTH) segLen = MINIMUM_SEGMENT_LENGTH;

        int slack = segLen / 20;
        if(slack < MINIMUM_SLACK) slack = MINIMUM_SLACK;

        _txtCPCorrPower.setText(Double.toString(DEFAULT_CORR_POWER));
        _txtCPSegmentLength.setText(Integer.toString(segLen));
        _txtCPSlack.setText(Integer.toString(slack));

        GUtil.showDefaultCursor(_thisDialog);
    }

    // Automatically determine the selectivity and threshold
    private void _autoDetSelAndThershold()
    {
        _txtPDSelectivity.setText(Double.toString(DEFAULT_SELECTIVITY));
        _txtPDThreshold.setText(Double.toString(DEFAULT_THRESHOLD));
    }
    
    // Set aligned-chromatogram data-file
    private void _setAlignedChromDataFile()
    {
        String filePath = FileSelectorBox.showFileSaveDialog(
            GUIMain.instance.getRootFrame(),
            _S("tb_cowpd_of_s_acdf"),
            new String[] { _S("tb_cowpd_of_csv") },
            new String[] { "csv",                }
        );
        if(filePath == null) return;

        _txtOFAlignedChromData.setText(filePath);
    }

    // Set peak data-file
    private void _setPeakDataFile()
    {
        String filePath = FileSelectorBox.showFileSaveDialog(
            GUIMain.instance.getRootFrame(),
            _S("tb_cowpd_of_s_pdf"),
            new String[] { _S("tb_cowpd_of_csv") },
            new String[] { "csv",                }
        );
        if(filePath == null) return;

        _txtOFPeakData.setText(filePath);
    }

    // Cancel the COW and peak detection processes (if running)
    private void _cancelProcess()
    {
        // Check if not running
        if(_runThread == null) return;

        // Ask the thread to stop
        _runThreadShouldStop = true;
        _btnCancelProcess.setEnabled(false);

        // Display information
        _lblOFProgess.setText(_S("tb_cowpd_of_canceling"));
    }

    // Run the COW and peak detection processes on all the input files
    private void _runProcess()
    {
        // Check if already running
        if(_runThread != null) return;

        // Get the target chromatogram file name
        final String targetFileName = (String) _lstCPTarget.getSelectedValue();
        if(targetFileName == null) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_ntp"));
            return;
        }

        // Get and check the output file names
        final String ofACDFile = _txtOFAlignedChromData.getText();
        if(ofACDFile.length() == 0) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_nacdf"));
            return;
        }

        final String ofPDFile = _txtOFPeakData.getText();
        if(ofPDFile.length() == 0) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_npdf"));
            return;
        }

        if(ofACDFile.equals(ofPDFile)) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_sofs"));
            return;
        }

        // Open the output files
        PrintWriter pwACDFile_ = null;
        try {
            pwACDFile_ = new PrintWriter(new FileWriter(ofACDFile));
        }
        catch(Exception e) {
            e.printStackTrace();
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _F("tb_cowpd_error_ooutf", new String[] { ofACDFile }));
            return;
        }
        final PrintWriter pwACDFile = pwACDFile_;

        PrintWriter pwPDFile_ = null;
        try {
            pwPDFile_ = new PrintWriter(new FileWriter(ofPDFile));
        }
        catch(Exception e) {
            e.printStackTrace();
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _F("tb_cowpd_error_ooutf", new String[] { ofPDFile }));
            return;
        }
        final PrintWriter pwPDFile = pwPDFile_;

        // Load the target
        double[] target_ = null;
        try {
            target_ = _applyPreprocessing(_loadSignal(targetFileName));
        }
        catch(Exception e) {
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _F("tb_cowpd_error_lf", new String[] { targetFileName }));
            return;
        }
        final double[] target = target_;

        // Get the parameters
        final double corrPower   = GUtil.str2d(_txtCPCorrPower        .getText(), DEFAULT_CORR_POWER    );
        final int    segLen      = GUtil.str2i(_txtCPSegmentLength    .getText(), MINIMUM_SEGMENT_LENGTH);
        final int    slack       = GUtil.str2i(_txtCPSlack            .getText(), MINIMUM_SLACK         );
        final double selectivity = GUtil.str2d(_txtPDSelectivity      .getText(), DEFAULT_SELECTIVITY   );
        final double threshold   = GUtil.str2d(_txtPDThreshold        .getText(), DEFAULT_THRESHOLD     );
        final double ipsmtol     = GUtil.str2d(_txtOFSamePeakTolerance.getText(), DEFAULT_IPSM_TOLERANCE);

        // Get the list of input files and the number of input files
        final DefaultListModel dlmIFiles = (DefaultListModel) _lstIPInputFiles.getModel();
        final int              cntIFiles = dlmIFiles.getSize();

        // Prepare an array for storing found peak-data
        final Chromatogram.FPResult[] fprArray = new Chromatogram.FPResult[cntIFiles];

        // Start thread
        _runThread = new Thread(new Runnable() { public void run() {
            // Flag
            boolean error = false;
            // Loop through all the files
            for(int i = 0; i < cntIFiles; ++i) {
                // Update the progress information
                if(!_runThreadShouldStop) {
                    Integer[] param = new Integer[] { i + 1, cntIFiles };
                    _lblOFProgess.setText(_F("tb_cowpd_of_proc", param));
                }
                // Load the chromatogram input file
                final String   curIFileName = (String) dlmIFiles.getElementAt(i);
                final boolean  sameFile     = curIFileName.equals(targetFileName);
                double[]       signal       = null;
                try {
                    signal = sameFile ? target : _applyPreprocessing(_loadSignal(curIFileName));
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _F("tb_cowpd_error_rlf", new String[] { curIFileName }));
                    error = true;
                    break;
                }
                if(_runThreadShouldStop) break;
                // Perform COW
                try {
                    if(!sameFile) signal = Chromatogram.performCOW(target, signal, segLen, slack, corrPower);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _F("tb_cowpd_error_rac", new String[] { curIFileName }));
                    error = true;
                    break;
                }
                if(_runThreadShouldStop) break;
                // Find peaks
                try {
                    fprArray[i] = Chromatogram.findPeaks(signal, selectivity, threshold);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _F("tb_cowpd_error_rfp", new String[] { curIFileName }));
                    error = true;
                    break;
                }
                if(_runThreadShouldStop) break;
                // Write the aligned-chromatogram data to file
                pwACDFile.printf("\"%s\",", curIFileName);
                for(int j = 0; j < signal.length; ++j) {
                    if(j < signal.length - 1) pwACDFile.printf("%+.10f,", signal[j]);
                    else                      pwACDFile.printf("%+.10f",  signal[j]); 
                }
                pwACDFile.println("");
                if(_runThreadShouldStop) break;
            }
            // Perform same-peak-detection only if there was no error
            if(!error) {
                // Perform same-peak-detection on the aligned peaks
                double[][] alp = null;
                if(!_runThreadShouldStop) {
                    // Update the progress information
                    _lblOFProgess.setText(_S("tb_cowpd_of_gpeaks"));
                    // Perform same-peak-detection
                    alp = Chromatogram.detectSamePeaks(fprArray, (int) (target.length * ipsmtol / 100.0), _lstCPTarget.getSelectedIndex());
                }
                // Write the aligned peak data to file
                if(alp != null) {
                    for(int i = 0; i < cntIFiles; ++i) {
                        double[] cpd = alp[i];
                        pwPDFile.printf("\"%s\",", (String) dlmIFiles.getElementAt(i));
                        for(int j = 0; j < cpd.length; ++j) {
                            double mag = cpd[j];
                            if(mag == Double.NEGATIVE_INFINITY) {
                                if(j < cpd.length - 1) pwPDFile.printf(",");
                            }
                            else {
                                if(j < cpd.length - 1) pwPDFile.printf("%+.10f,", mag);
                                else                   pwPDFile.printf("%+.10f",  mag);
                            }
                        }
                        pwPDFile.println("");
                    }
                }
            }
            // Flush and close the output files
            pwACDFile.flush(); pwACDFile.close();
            pwPDFile .flush(); pwPDFile .close();
            // Clear the thread
            _runThread           = null;
            _runThreadShouldStop = false;
            // Enable/disable controls
            _txtOFSamePeakTolerance.setEnabled(true );
            _btnOFAlignedChromData .setEnabled(true );
            _btnOFPeakData         .setEnabled(true );
            _btnRunprocess         .setEnabled(true );
            _btnCancelProcess      .setEnabled(false);
            _pnlOFCancel           .setVisible(false);
            System.gc();
        }});
        _runThread.start();

        // Enable/disable controls
        _txtOFSamePeakTolerance.setEnabled(false);
        _btnOFAlignedChromData .setEnabled(false);
        _btnOFPeakData         .setEnabled(false);
        _btnRunprocess         .setEnabled(false);
        _btnCancelProcess      .setEnabled(true );
        _pnlOFCancel           .setVisible(true );
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Downsampling the given signal by half
    private double[] _downSamplingByHalf(double[] signal)
    {
        double[] ret = new double[signal.length / 2];

        for(int i = 0; i < ret.length; ++i) {
            double s1 = signal[i * 2 + 0];
            double s2 = signal[i * 2 + 1];
            double sa = (s1 + s2) * 0.5;
            ret[i] = sa;
        }
        
        return ret;
    }

    // Load signal data from the given file
    private double[] _loadSignal(String fileName) throws Exception
    {
        // Get the file's extension
        int dotPos = fileName.lastIndexOf('.');
        if(dotPos < 0) throw new RuntimeException("Could not determine the file format!");
        String fileExt = fileName.substring(dotPos + 1).toLowerCase();

        // Load the file data
        double[] signal = null;
             if(fileExt.equals("mzml" )) signal = Chromatogram.loadMzML (new FileInputStream(new File(fileName)));
        else if(fileExt.equals("mzxml")) signal = Chromatogram.loadMzXML(new FileInputStream(new File(fileName)));
        else if(fileExt.equals("chrom")) signal = Chromatogram.loadChrom(new FileInputStream(new File(fileName)));
        else if(fileExt.equals("xy"   )) signal = Chromatogram.loadXY   (new FileInputStream(new File(fileName)));
        else                             throw new RuntimeException("Unsupported file format!");

        // Downsampling as needed
        while(signal.length > 10000) {
            signal = _downSamplingByHalf(signal);
        }

        // Return the file data
        return signal;
    }

    // Apply preprocessing to the given signal
    private double[] _applyPreprocessing(double[] signal)
    {
        // Cut-off
        double coPercentage = Double.valueOf(_txtIPEdgeCutOff.getText());
        int    sigLen       = signal.length;
        int    sigCut       = (int) Math.round(signal.length * coPercentage / 100.0);
        signal = SUtil.slice(signal, sigCut, signal.length - sigCut - 1);
        
        // Low pass filter
        int lfpStrength = Integer.valueOf((String) _cmbIPLowPassFilter.getSelectedItem());
        if(lfpStrength > 0) signal = Chromatogram.lowPassFilter(signal, lfpStrength + 1);

        // Baseline correction
        try {
            int blc = _cmbIPBaseLineCorrection.getSelectedIndex();
                 if(blc == 1) signal = Chromatogram.constantBaselineCorrection   (signal);
            else if(blc == 2) signal = Chromatogram.linearBaselineCorrection     (signal);
            else if(blc == 3) signal = Chromatogram.polynomial2BaselineCorrection(signal);
            else if(blc == 4) signal = Chromatogram.polynomial3BaselineCorrection(signal);
            else if(blc == 5) signal = Chromatogram.polynomial4BaselineCorrection(signal);
            else if(blc == 6) signal = Chromatogram.polynomial5BaselineCorrection(signal);
        }
        catch(Exception e) {
            e.printStackTrace();
            Thread t = new Thread(new Runnable() {
                public void run()
                { GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_blc")); }
            });
            t.start();
        }

        // Magnitude scalling
        double msl = Double.valueOf(_txtIPMagnitudeScalling.getText());
        if(msl != 100.0) signal = SUtil.smul(msl / 100.0, signal);

        // Return back the signal
        return signal;
    }

    // Draw preview - empty plot
    private void _drawEmptyPlot(Graphics2D g, int w, int h)
    {
        g.setColor(StdPlotMiscSettingPanel.PREDEFINED_COLOR[15]); // White
        g.fillRect(0, 0, w, h);
    }

    // Draw preview - chromatogram plot
    private void _drawChromatogramPlot(Graphics2D g, int w, int h, double[] signal, Chromatogram.FPResult peak, boolean secondary)
    {
        // Get the minimum and maximum Y value
        double minY = SUtil.min(signal);
        double maxY = SUtil.max(signal);
        double incY = (maxY - minY) / 4;

        // Instantiate the plot renderer class
        PlotRenderer pl = new PlotRenderer(g, w, h, 0, signal.length, PlotRenderer.MIN_STEP * 0.1, 1, minY, maxY, incY, 1, false, true);
        pl.setGeneralStyle(
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [15], // Background
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [ 0], // Caption
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [ 0], // Origin-line
            StdPlotMiscSettingPanel.PREDEFINED_LINE_STYLE[ 0], // ---
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [ 0], // Axis-line
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [ 0], // Grid-line
            StdPlotMiscSettingPanel.PREDEFINED_LINE_STYLE[ 0], // ---
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [ 0], // Axis-caption
            StdPlotMiscSettingPanel.PREDEFINED_COLOR     [ 0]  // Axis-tick-text
        );

        // Draw the background, caption, and axis (for primary plot only)
        if(!secondary) {
            pl.drawBackground();
            pl.drawAxis(false, false, true, true, false);
        }

        // Draw the chromatogram
        Color ccol = secondary ? StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [10]  // Light blue
                               : StdPlotDataPointSettingPanel.PREDEFINED_COLOR     [ 8]; // Light red
        int   clin = secondary ? StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[ 2]  // Dashed
                               : StdPlotDataPointSettingPanel.PREDEFINED_LINE_STYLE[ 1]; // Continuous
        double[] cdata = new double[signal.length * 2];
        int      idx  = 0;
        for(int i = 0; i < signal.length; ++i) {
            cdata[idx++] = i;
            cdata[idx++] = signal[i];
        }
        pl.drawPolyline(cdata, ccol, clin);

        // Skip drawing the peak as needed
        if(peak == null) return;

        // Draw the peak
        Color    pcol  = StdPlotDataPointSettingPanel.PREDEFINED_COLOR [9]; // Light green
        int      psym  = StdPlotDataPointSettingPanel.PREDEFINED_SYMBOL[4]; // Open circle
        double[] pdata = new double[peak.loc.length * 2];
                 idx   = 0;
        for(int i = 0; i < peak.loc.length; ++i) {
            pdata[idx++] = peak.loc[i];
            pdata[idx++] = peak.mag[i];
        }
        pl.drawSymbolPoints(pdata, pcol, psym);
    }

    // Draw preview - inputs and preprocessing
    private void _drawIPPlot(Graphics2D g, int w, int h)
    {
        // Skip if not dirty
        if(!_imgPlotAreaDirty) return;
        _imgPlotAreaDirty = false;

        // Get the file name
        String fileName = (String) _lstIPInputFiles.getSelectedValue();
        if(fileName == null) {
            _drawEmptyPlot(g, w, h);
            return;
        }

        // Load the file data
        double[] signal;
        try {
            signal = _loadSignal(fileName);
        }
        catch(Exception e) {
            e.printStackTrace();
            _drawEmptyPlot(g, w, h);
            return;
        }

        // Apply preprocessing to the signal
        signal = _applyPreprocessing(signal);

        // Draw the plot
        _drawChromatogramPlot(g, w, h, signal, null, false);
    }
    
    // Draw preview - COW parameters
    private void _drawCPPlot(Graphics2D g, int w, int h)
    {
        // Skip if not dirty
        if(!_imgPlotAreaDirty) return;
        _imgPlotAreaDirty = false;

        // Get the file name of the target and signal
        String targetFileName = (String) _lstCPTarget.getSelectedValue();
        String signalFileName = (String) _cmbCPSignal.getSelectedItem ();
        if(targetFileName == null || signalFileName == null) {
            _drawEmptyPlot(g, w, h);
            return;
        }
        boolean sameFile = targetFileName.equals(signalFileName);

        // Load the files' data
        double[] target;
        double[] signal;
        try {
            target = _loadSignal(targetFileName);
            signal = sameFile ? target : _loadSignal(signalFileName);
        }
        catch(Exception e) {
            e.printStackTrace();
            _drawEmptyPlot(g, w, h);
            return;
        }

        // Apply preprocessing to the data
        target = _applyPreprocessing(target);
        signal = sameFile ? target : _applyPreprocessing(signal);

        // Perform COW
        if(!sameFile) {
            final double corrPower = GUtil.str2d(_txtCPCorrPower        .getText(), DEFAULT_CORR_POWER    );
            final int    segLen    = GUtil.str2i(_txtCPSegmentLength    .getText(), MINIMUM_SEGMENT_LENGTH);
            final int    slack     = GUtil.str2i(_txtCPSlack            .getText(), MINIMUM_SLACK         );

            try {
                signal = Chromatogram.performCOW(target, signal, segLen, slack, corrPower);
            }
            catch(Exception e) {
                e.printStackTrace();
                _drawEmptyPlot(g, w, h);
                Thread t = new Thread(new Runnable() {
                    public void run()
                    { GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_cow")); }
                });
                t.start();
                return;
            }
        }

        // Draw the plot
        _drawChromatogramPlot(g, w, h, target, null, false);
        _drawChromatogramPlot(g, w, h, signal, null, true );
    }

    // Draw preview - peak detection
    private void _drawPDPlot(Graphics2D g, int w, int h)
    {
        // Skip if not dirty
        if(!_imgPlotAreaDirty) return;
        _imgPlotAreaDirty = false;

        // Get the file name of the target
        String targetFileName = (String) _lstCPTarget.getSelectedValue();
        if(targetFileName == null) {
            _drawEmptyPlot(g, w, h);
            return;
        }

        // Load the files' data
        double[] target;
        try {
            target = _loadSignal(targetFileName);
        }
        catch(Exception e) {
            e.printStackTrace();
            _drawEmptyPlot(g, w, h);
            return;
        }

        // Apply preprocessing to the data
        target = _applyPreprocessing(target);

        // Find peaks
        double selectivity = DEFAULT_SELECTIVITY;
        try { selectivity = Double.valueOf(_txtPDSelectivity.getText()); }
        catch(Exception e) {}

        double threshold = DEFAULT_THRESHOLD;
        try { threshold = Double.valueOf(_txtPDThreshold.getText()); }
        catch(Exception e) {}
        
        Chromatogram.FPResult fpr = null;
        try {
            fpr = Chromatogram.findPeaks(target, selectivity, threshold);
        }
        catch(Exception e) {
            e.printStackTrace();
            _drawEmptyPlot(g, w, h);
            Thread t = new Thread(new Runnable() {
                public void run()
                { GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("tb_cowpd_error_fp")); }
            });
            t.start();
            return;
        }
        
        // Draw the plot
        _drawChromatogramPlot(g, w, h, target, fpr, false);
    }

    // Internal plot-area class
    private class PlotArea extends JPanel {
        private static final int BI_WIDTH  = 2000;
        private static final int BI_HEIGHT =  650;

        private int _index = -1;
        
        public PlotArea(int index)
        {
            super(null, true);
            _index = index;
        }

        public void paintComponent(Graphics g)
        {
            // Create a new buffered image for the plot?
            if(_imgPlotArea == null) _imgPlotArea = (BufferedImage) createImage(BI_WIDTH, BI_HEIGHT);

            // Draw the plot to the buffered image (if needed)
            GUtil.showWaitCursor(_thisDialog);
            Graphics2D grpPlot = _imgPlotArea.createGraphics();
            try {
                     if(_index == 0) _drawIPPlot(grpPlot, BI_WIDTH, BI_HEIGHT);
                else if(_index == 1) _drawCPPlot(grpPlot, BI_WIDTH, BI_HEIGHT);
                else if(_index == 2) _drawPDPlot(grpPlot, BI_WIDTH, BI_HEIGHT);
            }
            catch(Exception e) {
                e.printStackTrace();
                GUtil.showNoDDialogPlot();
            }
            grpPlot.dispose();
            GUtil.showDefaultCursor(_thisDialog);

            // Blit the buffered image to the panel
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
            g2d.drawImage(
                _imgPlotArea,
                0, 0, getWidth(), getHeight(),
                0, 0, BI_WIDTH, BI_HEIGHT,
                new Color(0, 0, 0, 0),
                null
            );
            g2d.dispose();
            System.gc();
        }
    }
}
