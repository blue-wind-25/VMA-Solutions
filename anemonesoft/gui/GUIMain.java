/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.*;

import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.gui.dialog.*;
import anemonesoft.gui.tab.*;
import anemonesoft.gui.toolbox.*;
import anemonesoft.i18n.*;
import anemonesoft.lic.*;

//
// The main applet class
// This class must not be instantiated more than once
//
public class GUIMain extends JApplet implements ActionListener {
    // Application information constants
    public static final String APP_VERSION       = "1.4.3b"; // Synchronize it with 'vma.nsi'
    public static final String APP_COPY_YEAR     = "2011-2021";
    public static final String APP_COPY_NAME     = "AnemoneSoft.com\u2122";

    public static final String APP_DATA_MAGIC    = "VMA-DS";
    public static final int    APP_DATA_VERSION  = 2;

    public static final String APP_TAB_MAGIC     = "TAB-DS";
    public static final int    APP_TAB_VERSION   = 2;

    public static final String APP_LICENSE_FILE  = "license.vma1.dat";

    public static final String APP_ACT_NODE_NAME = "659c66c4f3d8b6215404af78f401d725";
    public static final String APP_ACT_KEY       = "4a3feb634d119d0ec2bbb3a39c132a30";
    public static final String APP_ACT_VALUE     = "20a02a029d3db14b358deae23cedd100";

    public static final String APP_DEMO_MODE_STR = "*DEMO*"; /** From v1.2.0a */

    // Reference to this class' only instance that will ever created
    public static GUIMain instance = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // List of examples
    private static String[] EXAMPLE_LIST = new String[] {
        "ScatterLinePlot.vma",          "mnu_plot_scatter_line",
        "BoxWhiskerPlot.vma",           "mnu_plot_box_whisker",
        null,                           null,

        "mnu_anal_linearity",           "",
        "FirstOrderRegression.vma",     "mnu_anal_for",
        "FirstOrderRegression-LOF.vma", "mnu_anal_for",
        "SecondOrderRegression-1.vma",  "mnu_anal_sor",
        "SecondOrderRegression-2.vma",  "mnu_anal_sor",
        "MandelTest.vma",               "mnu_anal_mandel",
        "HomogeneityTest.vma",          "mnu_anal_homogen",
        "",                             "",

        "mnu_anal_precision",           "",
        "PrecisionRSD.vma",             "mnu_anal_prec_rsd",
        "PrecisionOWANO.vma",           "mnu_anal_prec_owano",
        "",                             "",

        "Accuracy.vma",                 "mnu_anal_accuracy",
        "Robustness.vma",               "mnu_anal_robustness",

        "mnu_anal_ap",                  "",
        "APGonzalesWithRob.vma",        "mnu_anal_ap",
        "APGonzalesRozet.vma",          "mnu_anal_ap",
        "",                             "",

        "QC-Shewhart.vma",              "mnu_anal_qcshewhart",
        null,                           null,

        "CellsAsCaptionsAndLabels.vma",  "mnu_cell_as_cl"
    };

    // Reference to the root frame object (if any)
    private JFrame _rootFrame = null;

    // Controls
    private JMenuBar         _mnbMain            = new JMenuBar();

    private JButton          _btnNewProject      = new JButton();
    private JButton          _btnOpenProject     = new JButton();
    private JButton          _btnSaveProject     = new JButton();
    private JButton          _btnSaveProjectAs   = new JButton();
    private JLabel           _lblFilePath        = new JLabel("", JLabel.CENTER);
    private JButton          _btnCutCells        = new JButton();
    private JButton          _btnCopyCells       = new JButton();
    private JButton          _btnPasteCells      = new JButton();
    private JButton          _btnClearCells      = new JButton();

    private JMenuItem        _mnuFileNew         = null;
    private JMenuItem        _mnuFileOpen        = null;
    private JMenuItem        _mnuFileSave        = null;
    private JMenuItem        _mnuFileSaveAs      = null;
    private JMenuItem        _mnuFileSetOName    = null;
    private JMenuItem        _mnuFilePSetup      = null;
    private JMenuItem        _mnuFileQuit        = null;

    private JMenu            _mnuSSheet          = null;
    private JMenuItem        _mnuSSheetCut       = null;
    private JMenuItem        _mnuSSheetCopy      = null;
    private JMenuItem        _mnuSSheetPaste     = null;
    private JMenuItem        _mnuSSheetClear     = null;
    private JMenuItem        _mnuSSheetPrint     = null;
    private JMenuItem        _mnuSSheetPBTmpl    = null;

    private JMenuItem        _mnuPlotScatterLine = null;
    private JMenuItem        _mnuPlotBoxWhisker = null;

    private JMenuItem        _mnuAnalFor         = null;
    private JMenuItem        _mnuAnalSor         = null;
    private JMenuItem        _mnuAnalMandel      = null;
    private JMenuItem        _mnuAnalHomogen     = null;
    private JMenuItem        _mnuAnalAccuracy    = null;
    private JMenuItem        _mnuAnalPrecisionR  = null;
    private JMenuItem        _mnuAnalPrecisionA  = null;
    private JMenuItem        _mnuAnalAPGonzales  = null;
    private JMenuItem        _mnuAnalAPRozet     = null;
    private JMenuItem        _mnuAnalRobustness  = null;
    private JMenuItem        _mnuAnalQCShewhart  = null;

    private JMenuItem        _mnuToolboxStudTTab = null;
    private JMenuItem        _mnuToolboxFishFTab = null;
    private JMenuItem        _mnuToolboxCalcPMC  = null;
    private JMenuItem        _mnuToolboxCalcTI   = null;
    private JMenuItem        _mnuToolboxCalcND   = null;
    private JMenuItem        _mnuToolboxCalcDLQL = null;
    private JMenuItem        _mnuToolboxCOWPD    = null;

    private JMenuItem        _mnuHelpTOC         = null;
    private JMenuItem        _mnuHelpRefs        = null;
    private JMenuItem        _mnuHelpAbout       = null;

    private JTabbedPane      _tbpMain            = new JTabbedPane();
    private SpreadsheetPanel _tabSpreadsheet     = null;
    private String           _operatorName       = null;

    // Data
    private boolean    _isApplet   = false;
    private boolean    _isDemoMode = false; /** From v1.2.0a */
    private String     _filePath   = null;
    private PageFormat _pageFormat = null;

    // Shortcut for obtaining i18n string
    private static String _S(String str)
    { return StringTranslator.getString(str); }

    // Return the tab ID for the given tab object
    private static String _tabIDForTabObject(Object tab)
    {
        Class cls = tab.getClass();
        if(cls.equals(SpreadsheetPanel    .class)) return "Spreadsheet";
        if(cls.equals(BoxWhiskerPlotPanel .class)) return "BoxWhiskerPlot";
        if(cls.equals(ScatterLinePlotPanel.class)) return "ScatterLinePlot";
        if(cls.equals(FirstOrderRegPanel  .class)) return "FirstOrderReg";
        if(cls.equals(SecondOrderRegPanel .class)) return "SecondOrderReg";
        if(cls.equals(MandelTestPanel     .class)) return "MandelTest";
        if(cls.equals(HomogeneityPanel    .class)) return "Homogeneity";
        if(cls.equals(AccuracyPanel       .class)) return "Accuracy";
        if(cls.equals(PrecisionRSDPanel   .class)) return "PrecisionRSD";
        if(cls.equals(PrecisionOWANOPanel .class)) return "PrecisionOWANO";
        if(cls.equals(APGonzalesPanel     .class)) return "APGonzales";
        if(cls.equals(APRozetPanel        .class)) return "APRozet";
        if(cls.equals(RobustnessPanel     .class)) return "Robustness";
        if(cls.equals(QCShewhartPanel     .class)) return "QCShewhart";
        return null;
    }

    // Return the tab class for the given tab ID
    private static Class _tabClassForTabID(String name)
    {
        if(name.equals("Spreadsheet"    )) return SpreadsheetPanel    .class;
        if(name.equals("BoxWhiskerPlot" )) return BoxWhiskerPlotPanel .class;
        if(name.equals("ScatterLinePlot")) return ScatterLinePlotPanel.class;
        if(name.equals("FirstOrderReg"  )) return FirstOrderRegPanel  .class;
        if(name.equals("SecondOrderReg" )) return SecondOrderRegPanel .class;
        if(name.equals("MandelTest"     )) return MandelTestPanel     .class;
        if(name.equals("Homogeneity"    )) return HomogeneityPanel    .class;
        if(name.equals("Accuracy"       )) return AccuracyPanel       .class;
        if(name.equals("PrecisionRSD"   )) return PrecisionRSDPanel   .class;
        if(name.equals("PrecisionOWANO" )) return PrecisionOWANOPanel .class;
        if(name.equals("APGonzales"     )) return APGonzalesPanel     .class;
        if(name.equals("APRozet"        )) return APRozetPanel        .class;
        if(name.equals("Robustness"     )) return RobustnessPanel     .class;
        if(name.equals("QCShewhart"     )) return QCShewhartPanel     .class;
        return null;
    }

    // New project
    private void _newProject()
    {
        // Clear the file path
        _filePath = null;
        _lblFilePath.setText(_filePath);

        // Clear the tabs
        _tbpMain.removeAll();
        _tabSpreadsheet = new SpreadsheetPanel();
        GUtil.addTab(_tbpMain, _S("str_data_input"), GUtil.newImageIcon("tab_input"), _S("str_data_input"), _tabSpreadsheet, true);
        System.gc();

        // Set the default operator name
        _operatorName = _S("dlg_def_oname");
    }

    // Open examples
    private void _openExample(String name)
    {
        // Flag
        boolean ok = false;

        // Load the project
        showWaitCursor();
        ok = _openProject_exec(getClass().getResourceAsStream("/examples/" + name));
        showDefaultCursor();

        // Check for error
        if(!ok) {
            _newProject();
            return;
        }

        // Examples does not have file paths
        _filePath = null;
        _lblFilePath.setText(_filePath);
    }

    // Open project
    private void _openProject(String filePath)
    {
        // Flag
        boolean ok = false;

        // Load the project
        showWaitCursor();
        try {
            ok = _openProject_exec(new FileInputStream(filePath));
        }
        catch(Exception e) {
            e.printStackTrace();
            GUtil.showErrorDialog(getRootFrame(), _S("err_open_file_fail"));
        }
        showDefaultCursor();

        // Check for error
        if(!ok) {
            _newProject();
            return;
        }

        // Store and display the file path
        _filePath = filePath;
        _lblFilePath.setText(_filePath);
    }

    private boolean _openProject_exec(InputStream is)
    {
        // Load the project
        try {
            // Open an input stream
            DataInputStream ds = new DataInputStream(new BufferedInputStream(is));
            // Load and check the data-stream's magic number and version
            String dsmnum = ds.readUTF();
            int    dsver  = ds.readInt();
            if(!dsmnum.equals(APP_DATA_MAGIC) || dsver > APP_DATA_VERSION) {
                GUtil.showErrorDialog(getRootFrame(), _S("err_open_proj_inv"));
                return false;
            }
            // Create a new project
            _newProject();
            // Load the operator name
            if(dsver >= 2) _operatorName = ds.readUTF(); /** Available from data interface version 2 */
            // Load the spreadsheet
            int ssifv = ds.readInt();
            if(!_tabSpreadsheet.load(ssifv, ds)) {
                GUtil.showErrorDialog(getRootFrame(), _S("err_proj_inv_ssheet"));
                return false;
            }
            // Load and check the tab data-stream's magic number and version
            String tdsmnum = ds.readUTF();
            int    tdsver  = ds.readInt();
            if(!tdsmnum.equals(APP_TAB_MAGIC) || tdsver > APP_TAB_VERSION) {
                GUtil.showErrorDialog(getRootFrame(), _S("err_proj_inv_tab"));
                return false;
            }
            // Load the spreadsheet-tab's caption
            if(tdsver >= 2) { /** Available from tab interface version 2 */
                ((TabButtonComponent) _tbpMain.getTabComponentAt(0)).setCaption(ds.readUTF());
            }
            // Get the number of tabs (minus the spreadsheet tab)
            int tcount = ds.readInt();
            // Load the tabs
            for(int i = 0; i < tcount; ++i) {
                // Get the class
                String cname = ds.readUTF();
                Class  cls   = _tabClassForTabID(cname);
                if(cls == null) {
                    GUtil.showErrorDialog(getRootFrame(), _S("err_proj_inv_tab_mod"));
                    return false;
                }
                // Instantiate the class
                ResultPanel resultPanel = (ResultPanel) cls.newInstance();
                // Read the tab's caption
                String tabCaption = (tdsver >= 2) ? ds.readUTF() : resultPanel.getTabCaption(); /** Available from tab interface version 2 */
                // Add the class to the tab
                resultPanel.init(false);
                GUtil.addTab(_tbpMain, tabCaption, resultPanel.getTabIcon(), resultPanel.getTabCaption(), resultPanel, false);
                // Load the tab's interface version and data
                int      tifv = ds.readInt();
                Saveable tab  = (Saveable) resultPanel;
                if(!tab.load(tifv, ds)) {
                    GUtil.showErrorDialog(getRootFrame(), _S("err_proj_inv_tab_mod"));
                    return false;
                }
                // Force update the report
                resultPanel.updateReport();
            }
            // Close the input stream
            ds.close();

        }
        catch(Exception e) {
            e.printStackTrace();
            GUtil.showErrorDialog(getRootFrame(), _S("err_open_proj_fail"));
            System.gc();
            return false;
        }

        // Done
        System.gc();
        return true;
    }

    // Save project
    private void _saveProject(String filePath)
    {
        // Show wait cursor
        showWaitCursor();

        // Save project
        boolean error = false;
        try {
            // Open an output stream
            DataOutputStream ds = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
            // Save the data-stream's magic number and version
            ds.writeUTF(APP_DATA_MAGIC);
            ds.writeInt(APP_DATA_VERSION);
            // Save the operator name
            ds.writeUTF(_operatorName); /** Available from data interface version 2 */
            // Save the spreadsheet
            ds.writeInt(_tabSpreadsheet.interfaceVersion());
            _tabSpreadsheet.save(ds);
            // Save the tab data-stream's magic number and version
            ds.writeUTF(APP_TAB_MAGIC);
            ds.writeInt(APP_TAB_VERSION);
            // Save the spreadsheet-tab's caption
            ds.writeUTF(((TabButtonComponent) _tbpMain.getTabComponentAt(0)).getCaption()); /** Available from tab interface version 2 */
            // Save the number of tabs (minus the spreadsheet tab)
            int tcount = _tbpMain.getTabCount() - 1;
            ds.writeInt(tcount);
            // Save the tabs
            for(int i = 1; i <= tcount; ++i) {
                // Get the component
                Saveable           tab = (Saveable          ) _tbpMain.getComponentAt   (i);
                TabButtonComponent com = (TabButtonComponent) _tbpMain.getTabComponentAt(i);
                String   tid = _tabIDForTabObject(tab);
                if(tid == null) continue;
                // Save the class name, caption, interface version, and data
                ds.writeUTF(tid);
                ds.writeUTF(com.getCaption()); /** Available from tab interface version 2 */
                ds.writeInt(tab.interfaceVersion());
                tab.save(ds);
            }
            // Close the output stream
            ds.close();
        }
        catch(Exception e) {
            error = true;
            e.printStackTrace();
            GUtil.showErrorDialog(getRootFrame(), _S("err_save_proj_fail"));
        }

        // Show default cursor
        showDefaultCursor();
        if(error) return;

        // Store and display the file path
        _filePath = filePath;
        _lblFilePath.setText(_filePath);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a default applet
    public GUIMain()
    {
        super();
        instance = this;

        _isApplet = true;
    }

    // Construct a wrapped applet
    public GUIMain(JFrame rootFrame)
    {
        super();
        instance = this;

        _isApplet  = false;
        _rootFrame = rootFrame;
    }

    // Initialize the application
    public void init()
    {
        // Show wait cursor
        showWaitCursor();

        // Initialize look and feel
        try { UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); }
        catch(Exception e) {}

        ToolTipManager.sharedInstance().setInitialDelay(0);
        UIManager.put("ToolTip.font",       new FontUIResource(GUtil.getSysFontName("SansSerif"), Font.BOLD, 12));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.black));
        UIManager.put("ToolTip.background", new ColorUIResource(200, 255, 200));
        UIManager.put("ToolTip.border",     BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(new Color( 50,  64,  50)),
                                                BorderFactory.createLineBorder(new Color(100, 128, 100))
                                            ));

        // Initialize the string translator
        String country  = getParameter("country");
        String language = getParameter("language");
        StringTranslator.init((country != null) ? country : "US", (language != null) ? language : "en");

        // Public key raw data
        final String pubKeyRawData = "c33c81b3072eb935585d7c29b3fa4fa14fed4cffc4deeea241a2c009c9bef2ccc6d256640e7b73c9cb2a0c63755f87be7ebac7abf43b627eae2f7429b05b8e4bc2e4de7814d4061e7619c64d26a6217203693ca6978df7e5de1c0171eedac3dd1dd58435587d79d6b6efacac81e91670e1b82ccfe26a193e2592efd1673890459191f2511d88618aa1daa3590af9744a10156bfcb84179d766381380234f7f151e57bc155a9b3a63f5f18db379ecda167a815fb65cdeb396bd70caaa76726d591669f6fc118e74954348c78c9946d0efd6f9c24f5798ef039fae5e6cea2b87848f3ea7b588f192be0630bbed184ae9c1d05a05bed5dfd4dde9617bd2f214cbf4798d4e47230d278801cb4390ebc34ea7c4aef52e1e8f3bc4b4838ff3fe1b3450e495be5c560998b7a7332df58959f30e0cb6ae9ad891ada22aa5e6656bae535ea588d61aa9863197bf7a709681cc0e50f5c6769aab35644315ac686c959db7392c9212c68c98d964364b8fe6f7188f618879a7045ae3662720adf224ef5d14eea29778487c349681c304b456840e5fa989357cea544b5278653830910c4349ebcab5df6c77f464f44a2c5b51c60b11d3a262655b5973e65780cdfb495059d78765fc4446280e24efdf548385e0b30ef375ea2c26b9f80eeaab596166efa307a22e0ec0f643ed6ac64ff162b6fb8b8950db264fe6e3545e66e8f435285b0280f5\n10001";

        // Get the default user's directory
        String defUserDir = "";
        try {
            JFileChooser   fr = new JFileChooser();
            FileSystemView fw = fr.getFileSystemView();
            defUserDir = fw.getDefaultDirectory().toString() + System.getProperty("file.separator");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        /*
        // Check if the application is already activated or there is a valid license file
        try {
            // Check if the application is already activated
            // ~/.java/.userPrefs/659c66c4f3d8b6215404af78f401d725/prefs.xml
            // HKEY_CURRENT_USER\Software\JavaSoft\Prefs\659c66c4f3d8b6215404af78f401d725
            // HKEY_USERS\<user_name>\Software\JavaSoft\Prefs\659c66c4f3d8b6215404af78f401d725
            Preferences prefs = Preferences.userRoot().node(APP_ACT_NODE_NAME); //userNodeForPackage(GUIMain.class);
            String      astr  = prefs.get(APP_ACT_KEY, "");
            if(astr.equals(APP_ACT_VALUE)) {
                // Nothing to do
            }
            else {
                // Check all the possible locations of the license files one by one as needed
                int          ret = Licensing.checkLicenseFile("C:\\Windows\\" + APP_LICENSE_FILE, pubKeyRawData);
                if(ret != 1) ret = Licensing.checkLicenseFile("/etc/"         + APP_LICENSE_FILE, pubKeyRawData);
                if(ret != 1) ret = Licensing.checkLicenseFile(defUserDir      + APP_LICENSE_FILE, pubKeyRawData);
                if(ret != 1) ret = Licensing.checkLicenseFile(                  APP_LICENSE_FILE, pubKeyRawData);
                // A valid license file was found
                if(ret == 1) {
                    // Activate the application
                    prefs.put(APP_ACT_KEY, APP_ACT_VALUE);
                    prefs.flush();
                }
                // A valid license file was not found
                else {
                    while(true) {
                        // Show dialog to ask the user to obtain a license data
                        String licData = GUtil.showGetLicenseDialog(getRootFrame(), Licensing.getHostKey());
                        if(licData == null) {
                            System.exit(0);
                            break;
                        }
                        else if(licData.equals(APP_DEMO_MODE_STR)) { /** From v1.2.0a * /
                            _isDemoMode = true;
                            break;
                        }
                        // Remove all kind of space characters
                        licData = licData.replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", "").replaceAll(" ", "");
                        // Write the license file
                        try {
                            // Try to write the license file
                            if(Licensing.writeLicenseFile(defUserDir + APP_LICENSE_FILE, licData, pubKeyRawData)) break;
                        }
                        catch(Exception e1) {
                            e1.printStackTrace();
                            try {
                                // Try to write the license file again
                                if(Licensing.writeLicenseFile(APP_LICENSE_FILE, licData, pubKeyRawData)) break;
                            }
                            catch(Exception e2) {
                                e2.printStackTrace();
                                GUtil.showErrorDialog(getRootFrame(), _S("err_help_write_lic"));
                                System.exit(0);
                            }
                        }
                        // Invalid license
                        GUtil.showErrorDialog(getRootFrame(), _S("dlg_glbox_invl"));
                    }
                    // Activate the application (only if it is not in demo mode)
                    if(!_isDemoMode) { /** From v1.2.0a * /
                        prefs.put(APP_ACT_KEY, APP_ACT_VALUE);
                        prefs.flush();
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        */

        // Show splash screen (only if not in applet mode)
        if(!_isApplet) AboutBox.showSplashScreen();

        // Initialize the menu bar
        _mnbMain.setBorder(BorderFactory.createRaisedBevelBorder());
        setJMenuBar(_mnbMain);

        // Initialize the tabbed-pane
        _tbpMain.setTabPlacement(JTabbedPane.TOP);
        _tbpMain.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        _tbpMain.setBorder(BorderFactory.createRaisedBevelBorder());
        _tbpMain.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event)
            {
                boolean enabled = (_tbpMain.getSelectedComponent() == _tabSpreadsheet);
                _mnuSSheet    .setEnabled(enabled);
                _btnCutCells  .setEnabled(enabled);
                _btnCopyCells .setEnabled(enabled);
                _btnPasteCells.setEnabled(enabled);
                _btnClearCells.setEnabled(enabled);
            }
        });

        // Initialize the top bar
        Dimension tbbz = new Dimension(28, 24);
        JPanel pnlTopBar = new JPanel(new BorderLayout(), true);
            JPanel pnlLeftButton = new JPanel(new GridLayout(1, 3, 1, 0), true);
                _btnNewProject   .setIcon(GUtil.newImageIcon("mnu_file_new"));
                _btnNewProject   .setToolTipText(_S("mnu_new_proj_tt"));
                _btnNewProject   .setFocusable(false);
                _btnNewProject   .addActionListener(this);
                _btnNewProject   .setPreferredSize(tbbz);
                _btnOpenProject  .setIcon(GUtil.newImageIcon("mnu_file_open"));
                _btnOpenProject  .setToolTipText(_S("mnu_open_proj_tt"));
                _btnOpenProject  .setFocusable(false);
                _btnOpenProject  .addActionListener(this);
                _btnOpenProject  .setPreferredSize(tbbz);
                _btnSaveProject  .setIcon(GUtil.newImageIcon("mnu_file_save"));
                _btnSaveProject  .setToolTipText(_S("mnu_save_proj_tt"));
                _btnSaveProject  .setFocusable(false);
                _btnSaveProject  .addActionListener(this);
                _btnSaveProject  .setPreferredSize(tbbz);
                _btnSaveProjectAs.setIcon(GUtil.newImageIcon("mnu_file_save_as"));
                _btnSaveProjectAs.setToolTipText(_S("mnu_save_proj_as_tt"));
                _btnSaveProjectAs.setFocusable(false);
                _btnSaveProjectAs.addActionListener(this);
                _btnSaveProjectAs.setPreferredSize(tbbz);
                pnlLeftButton.add(_btnNewProject   );
                pnlLeftButton.add(_btnOpenProject  );
                pnlLeftButton.add(_btnSaveProject  );
                pnlLeftButton.add(_btnSaveProjectAs);
                pnlLeftButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            JPanel pnlRightButton = new JPanel(new GridLayout(1, 3, 1, 0), true);
                _btnCutCells  .setIcon(GUtil.newImageIcon("mnu_edit_cut"));
                _btnCutCells  .setToolTipText(_S("mnu_c_cut_tt"));
                _btnCutCells  .setFocusable(false);
                _btnCutCells  .addActionListener(this);
                _btnCutCells  .setPreferredSize(tbbz);
                _btnCopyCells .setIcon(GUtil.newImageIcon("mnu_edit_copy"));
                _btnCopyCells .setToolTipText(_S("mnu_c_copy_tt"));
                _btnCopyCells .setFocusable(false);
                _btnCopyCells .addActionListener(this);
                _btnCopyCells .setPreferredSize(tbbz);
                _btnPasteCells.setIcon(GUtil.newImageIcon("mnu_edit_paste"));
                _btnPasteCells.setToolTipText(_S("mnu_c_paste_tt"));
                _btnPasteCells.setFocusable(false);
                _btnPasteCells.addActionListener(this);
                _btnPasteCells.setPreferredSize(tbbz);
                _btnClearCells.setIcon(GUtil.newImageIcon("mnu_edit_clear"));
                _btnClearCells.setToolTipText(_S("mnu_c_clear_tt"));
                _btnClearCells.setFocusable(false);
                _btnClearCells.addActionListener(this);
                _btnClearCells.setPreferredSize(tbbz);
                pnlRightButton.add(_btnCutCells  );
                pnlRightButton.add(_btnCopyCells );
                pnlRightButton.add(_btnPasteCells);
                pnlRightButton.add(_btnClearCells);
                pnlRightButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            _lblFilePath.setBorder(BorderFactory.createRaisedBevelBorder());
            pnlTopBar.add(pnlLeftButton, BorderLayout.WEST);
            pnlTopBar.add(_lblFilePath, BorderLayout.CENTER);
            pnlTopBar.add(pnlRightButton, BorderLayout.EAST);

        // Initialize the "File" menu
        JMenu mnuFile = new JMenu(_S("mnu_file"));
            mnuFile.setMnemonic(KeyEvent.VK_F);
            _mnuFileNew    = GUtil.newJMenuItem(mnuFile, _S("mnu_file_new"    ), GUtil.newImageIcon("mnu_file_new"    ), KeyEvent.VK_N,  0, this);
            _mnuFileOpen   = GUtil.newJMenuItem(mnuFile, _S("mnu_file_open"   ), GUtil.newImageIcon("mnu_file_open"   ), KeyEvent.VK_O,  0, this);
            _mnuFileSave   = GUtil.newJMenuItem(mnuFile, _S("mnu_file_save"   ), GUtil.newImageIcon("mnu_file_save"   ), KeyEvent.VK_S,  0, this);
            _mnuFileSaveAs = GUtil.newJMenuItem(mnuFile, _S("mnu_file_save_as"), GUtil.newImageIcon("mnu_file_save_as"), KeyEvent.VK_A, -1, this);
            _mnuFileSaveAs.setDisplayedMnemonicIndex(_S("mnu_file_save_as").lastIndexOf('A'));
            mnuFile.addSeparator();
            _mnuFileSetOName = GUtil.newJMenuItem(mnuFile, _S("mnu_file_set_oname"), GUtil.newImageIcon("mnu_file_set_oname"), KeyEvent.VK_R, -1, this);
            _mnuFilePSetup   = GUtil.newJMenuItem(mnuFile, _S("mnu_file_psetup"   ), GUtil.newImageIcon("mnu_file_print"    ), KeyEvent.VK_P, -1, this);
            if(_rootFrame != null) {
                mnuFile.addSeparator();
                _mnuFileQuit = GUtil.newJMenuItem(mnuFile, _S("mnu_file_quit"), GUtil.newImageIcon("mnu_file_quit"), KeyEvent.VK_Q, 0, this);
            }
        _mnbMain.add(mnuFile);

        // Initialize the "Spreadsheet" menu
        _mnuSSheet = new JMenu(_S("mnu_ssheet"));
            _mnuSSheet.setMnemonic(KeyEvent.VK_S);
            _mnuSSheetCut    = GUtil.newJMenuItem(_mnuSSheet, _S("mnu_ssheet_cut"   ), GUtil.newImageIcon("mnu_edit_cut"  ), KeyEvent.VK_T,  KeyEvent.VK_X,      this);
            _mnuSSheetCopy   = GUtil.newJMenuItem(_mnuSSheet, _S("mnu_ssheet_copy"  ), GUtil.newImageIcon("mnu_edit_copy" ), KeyEvent.VK_C,  0,                  this);
            _mnuSSheetPaste  = GUtil.newJMenuItem(_mnuSSheet, _S("mnu_ssheet_paste" ), GUtil.newImageIcon("mnu_edit_paste"), KeyEvent.VK_P,  KeyEvent.VK_V,      this);
            _mnuSSheetClear  = GUtil.newJMenuItem(_mnuSSheet, _S("mnu_ssheet_clear" ), GUtil.newImageIcon("mnu_edit_clear"), KeyEvent.VK_R,  KeyEvent.VK_DELETE, this);
            _mnuSSheet.addSeparator();
            _mnuSSheetPrint  = GUtil.newJMenuItem(_mnuSSheet, _S("mnu_ssheet_print" ), GUtil.newImageIcon("mnu_file_print"), KeyEvent.VK_N,  -1,                 this);
            _mnuSSheet.addSeparator();
            _mnuSSheetPBTmpl = GUtil.newJMenuItem(_mnuSSheet, _S("mnu_ssheet_pbtmpl"), GUtil.newImageIcon("mnu_tmpl_pbdes"), KeyEvent.VK_B,  -1,                 this);
        _mnbMain.add(_mnuSSheet);

        // Initialize the "Plot" menu
        JMenu mnuPlot = new JMenu(_S("mnu_plot"));
            mnuPlot.setMnemonic(KeyEvent.VK_P);
            _mnuPlotScatterLine = GUtil.newJMenuItem(mnuPlot, _S("mnu_plot_sct_ln"), GUtil.newImageIcon("mnu_plot_scatter_line"), KeyEvent.VK_S, -1, this);
            _mnuPlotBoxWhisker  = GUtil.newJMenuItem(mnuPlot, _S("mnu_plot_box_wk"), GUtil.newImageIcon("mnu_plot_box_whisker" ), KeyEvent.VK_B, -1, this);
        _mnbMain.add(mnuPlot);

        // Initialize the "Analysis" menu
        JMenu mnuAnal = new JMenu(_S("mnu_anal"));
            mnuAnal.setMnemonic(KeyEvent.VK_A);
            JMenu mnuAnalLin   = GUtil.newSubJMenu (mnuAnal,    _S("mnu_anal_linearity" ), GUtil.newImageIcon("mnu_anal_linearity" ), KeyEvent.VK_L,     this);
            _mnuAnalFor        = GUtil.newJMenuItem(mnuAnalLin, _S("mnu_anal_for"       ), GUtil.newImageIcon("mnu_anal_for"       ), KeyEvent.VK_F, -1, this);
            _mnuAnalSor        = GUtil.newJMenuItem(mnuAnalLin, _S("mnu_anal_sor"       ), GUtil.newImageIcon("mnu_anal_sor"       ), KeyEvent.VK_S, -1, this);
            _mnuAnalMandel     = GUtil.newJMenuItem(mnuAnalLin, _S("mnu_anal_mandel"    ), GUtil.newImageIcon("mnu_anal_mandel"    ), KeyEvent.VK_M, -1, this);
            _mnuAnalHomogen    = GUtil.newJMenuItem(mnuAnalLin, _S("mnu_anal_homogen"   ), GUtil.newImageIcon("mnu_anal_homogen"   ), KeyEvent.VK_H, -1, this);

            JMenu mnuAnalPre   = GUtil.newSubJMenu (mnuAnal,    _S("mnu_anal_precision" ), GUtil.newImageIcon("mnu_anal_precision" ), KeyEvent.VK_P,     this);
            _mnuAnalPrecisionR = GUtil.newJMenuItem(mnuAnalPre, _S("mnu_anal_prec_rsd"  ), GUtil.newImageIcon("mnu_anal_prec_rsd"  ), KeyEvent.VK_R, -1, this);
            _mnuAnalPrecisionA = GUtil.newJMenuItem(mnuAnalPre, _S("mnu_anal_prec_owano"), GUtil.newImageIcon("mnu_anal_prec_owano"), KeyEvent.VK_N, -1, this);

            _mnuAnalAccuracy   = GUtil.newJMenuItem(mnuAnal,    _S("mnu_anal_accuracy"  ), GUtil.newImageIcon("mnu_anal_accuracy"  ), KeyEvent.VK_A, -1, this);
            _mnuAnalRobustness = GUtil.newJMenuItem(mnuAnal,    _S("mnu_anal_robustness"), GUtil.newImageIcon("mnu_anal_robustness"), KeyEvent.VK_R, -1, this);

            JMenu mnuAnalAP    = GUtil.newSubJMenu (mnuAnal,    _S("mnu_anal_ap"        ), GUtil.newImageIcon("mnu_anal_ap"        ), KeyEvent.VK_A,     this);
            _mnuAnalAPGonzales = GUtil.newJMenuItem(mnuAnalAP,  _S("mnu_anal_ap_gonza"  ), GUtil.newImageIcon("mnu_anal_ap"        ), KeyEvent.VK_G, -1, this);
            _mnuAnalAPRozet    = GUtil.newJMenuItem(mnuAnalAP,  _S("mnu_anal_ap_rozet"  ), GUtil.newImageIcon("mnu_anal_ap"        ), KeyEvent.VK_R, -1, this);

            _mnuAnalQCShewhart = GUtil.newJMenuItem(mnuAnal,    _S("mnu_anal_qcshewhart"), GUtil.newImageIcon("mnu_anal_qcshewhart"), KeyEvent.VK_Q, -1, this);
        _mnbMain.add(mnuAnal);

        // Initialize the "Toolbox" menu
        JMenu mnuToolbox = new JMenu(_S("mnu_toolbox"));
            mnuToolbox.setMnemonic(KeyEvent.VK_T);
            _mnuToolboxStudTTab = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_studtt"), GUtil.newImageIcon("mnu_toolbox_ttable"), KeyEvent.VK_S, -1, this);
            _mnuToolboxFishFTab = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_ft"),     GUtil.newImageIcon("mnu_toolbox_ftable"), KeyEvent.VK_F, -1, this);
            mnuToolbox.addSeparator();
            _mnuToolboxCalcPMC  = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_pmc"),    GUtil.newImageIcon("mnu_toolbox_pmc"),    KeyEvent.VK_C, -1, this);
            _mnuToolboxCalcTI   = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_ti"),     GUtil.newImageIcon("mnu_toolbox_ti"),     KeyEvent.VK_T, -1, this);
            _mnuToolboxCalcND   = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_nd"),     GUtil.newImageIcon("mnu_toolbox_nd"),     KeyEvent.VK_P, -1, this);
            _mnuToolboxCalcDLQL = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_dlql"),   GUtil.newImageIcon("mnu_toolbox_dlql"),   KeyEvent.VK_D, -1, this);
          //mnuToolbox.addSeparator();
          //_mnuToolboxCOWPD    = GUtil.newJMenuItem(mnuToolbox, _S("mnu_toolbox_cowpd"),  GUtil.newImageIcon("mnu_toolbox_cowpd" ), KeyEvent.VK_C, -1, this);
        _mnbMain.add(mnuToolbox);

        // Initialize the "Help" menu
        _mnbMain.add(Box.createHorizontalGlue());
        JMenu mnuHelp = new JMenu(_S("mnu_help"));
            mnuHelp.setMnemonic(KeyEvent.VK_H);
            _mnuHelpTOC = GUtil.newJMenuItem(mnuHelp, _S("mnu_help_show_help"), GUtil.newImageIcon("mnu_help_show_help"), KeyEvent.VK_H, KeyEvent.VK_F1, false, this);
            _mnuHelpTOC.setDisplayedMnemonicIndex(_S("mnu_help_show_help").lastIndexOf('H'));
            JMenu mnuHelpEx = GUtil.newSubJMenu(mnuHelp, _S("mnu_help_example"), GUtil.newImageIcon("mnu_help_example"), KeyEvent.VK_E, this);
            mnuHelp.addSeparator();
            _mnuHelpRefs  = GUtil.newJMenuItem(mnuHelp, _S("mnu_help_refs" ), GUtil.newImageIcon("mnu_help_refs" ), KeyEvent.VK_R, -1, this);
            _mnuHelpAbout = GUtil.newJMenuItem(mnuHelp, _S("mnu_help_about"), GUtil.newImageIcon("mnu_help_about"), KeyEvent.VK_A, -1, this);
        _mnbMain.add(mnuHelp);

        // Initialize the "Example" menu
        JMenu mnuCurPar  = mnuHelpEx;
            for(int i = 0; i < EXAMPLE_LIST.length; i += 2) {
                String fn = EXAMPLE_LIST[i];
                String fi = EXAMPLE_LIST[i + 1];
                if(fn == null) {
                    mnuHelpEx.addSeparator();
                    continue;
                }
                if(fi.equals("")) {
                    if(fn.equals("")) mnuCurPar = mnuHelpEx;
                    else              mnuCurPar = GUtil.newSubJMenu (mnuHelpEx, _S(fn), GUtil.newImageIcon(fn), -1, this);
                continue;
                }
                GUtil.newJMenuItem(mnuCurPar, EXAMPLE_LIST[i], GUtil.newImageIcon(fi), -1, -1, this);
            }

        // Initialize the root panel
        JPanel rootPanel = new JPanel(new BorderLayout(), true);
            rootPanel.add(pnlTopBar, BorderLayout.NORTH);
            rootPanel.add(_tbpMain, BorderLayout.CENTER);
        add(rootPanel);

        // Disable some functionalities in demo mode
        if(_isDemoMode) { /** From v1.2.0a */
            _btnOpenProject   .setEnabled(false);
            _btnSaveProject   .setEnabled(false);
            _btnSaveProjectAs .setEnabled(false);
            _mnuFileOpen      .setEnabled(false);
            _mnuFileSave      .setEnabled(false);
            _mnuFileSaveAs    .setEnabled(false);
        }

        // Start a new project
        _newProject();

        // Show default cursor
        showDefaultCursor();
    }

    // Return true if this application is run as an applet
    public boolean isApplet()
    { return _isApplet; }

    // Return true if this application is run in demo mode
    public boolean isDemoMode()
    { return _isDemoMode; }

    // Get the page format object
    public PageFormat getPageFormat()
    {
        if(_pageFormat == null) _pageFormat = PrinterJob.getPrinterJob().defaultPage();
        return _pageFormat;
    }

    // Get the spreadsheet panel
    public final SpreadsheetPanel getSpreadsheetPanel()
    { return _tabSpreadsheet; }

    // Get the operator name
    public final String getOperatorName()
    { return _operatorName; }

    // Get the root frame
    public Frame getRootFrame()
    { return (_rootFrame != null) ? _rootFrame : (Frame) SwingUtilities.windowForComponent(this); }

    // Show wait cursor
    public void showWaitCursor()
    {
        getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getGlassPane().setVisible(true);
        Thread t = new Thread(new Runnable() {
            public void run()
            {
                getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                getGlassPane().setVisible(true);
            }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Show default cursor
    public void showDefaultCursor()
    {
        getGlassPane().setCursor(Cursor.getDefaultCursor());
        getGlassPane().setVisible(false);
        Thread t = new Thread(new Runnable() {
            public void run()
            {
                getGlassPane().setCursor(Cursor.getDefaultCursor());
                getGlassPane().setVisible(false);
            }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Get all tabs of the given class type
    public ResultPanel[] getTabsByClass(Class classType)
    {
        int tcount = _tbpMain.getTabCount() - 1;
        int icount = 0;

        for(int i = 1; i <= tcount; ++i) {
            ResultPanel tab = (ResultPanel) _tbpMain.getComponentAt(i);
            if(tab.getClass().equals(classType)) ++icount;
        }
        if(icount <= 0) return null;

        ResultPanel[] tabs = new ResultPanel[icount];
        int           idx  = 0;
        for(int i = 1; i <= tcount; ++i) {
            ResultPanel tab = (ResultPanel) _tbpMain.getComponentAt(i);
            if(tab.getClass().equals(classType)) tabs[idx++] = tab;;
        }

        return tabs;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for menu
    public void actionPerformed(ActionEvent event)
    {
        // Get the component that fired the event
        JComponent eventSource = (JComponent) event.getSource();

        // For creating a new tab
        Class nrpClass = null;

        // File - New
        if(eventSource == _mnuFileNew || eventSource == _btnNewProject) {
            // Ask for confirmation
            if(!GUtil.showYesNoQuestionDialog(getRootFrame(), _S("dlg_new_project_confirm"))) return;
            // Create a new project
            _newProject();
        }
        // File - Open
        else if(eventSource == _mnuFileOpen || eventSource == _btnOpenProject) {
            String filePath = FileSelectorBox.showFileOpenDialog(
                getRootFrame(),
                _S("dlg_fsel_title_oproj"),
                new String[] { _S("dlg_fsel_ft_all"), _S("dlg_fsel_ft_vma") },
                new String[] { "*",                  "vma"                  }
            );
            if(filePath != null) _openProject(filePath);
        }
        // File - Save
        else if(eventSource == _mnuFileSave || eventSource == _btnSaveProject) {
            String filePath = _filePath;
            if(filePath == null) {
                filePath = FileSelectorBox.showFileSaveDialog(
                    getRootFrame(),
                    _S("dlg_fsel_title_sproj"),
                    new String[] { _S("dlg_fsel_ft_all"), _S("dlg_fsel_ft_vma") },
                    new String[] { "*",                  "vma"                  }
                );
            }
            if(filePath != null) _saveProject(filePath);
        }
        // File - Save As
        else if(eventSource == _mnuFileSaveAs || eventSource == _btnSaveProjectAs) {
            String filePath = FileSelectorBox.showFileSaveDialog(
                getRootFrame(),
                _S("dlg_fsel_title_sproj"),
                new String[] { _S("dlg_fsel_ft_all"), _S("dlg_fsel_ft_vma") },
                new String[] { "*",                  "vma"                  }
            );
            if(filePath != null) _saveProject(filePath);
        }
        // File - Set the Operator Name
        else if(eventSource == _mnuFileSetOName) {
            _operatorName = GUtil.showInputDialog(getRootFrame(), _S("dlg_get_oname"), _operatorName);
        }
        // File - Print Setup
        else if(eventSource == _mnuFilePSetup) {
            PrinterJob job = PrinterJob.getPrinterJob();
            if(_pageFormat == null) _pageFormat = job.defaultPage();
            _pageFormat = job.pageDialog(_pageFormat);
        }

        // File - Quit
        else if(eventSource == _mnuFileQuit) {
            if(_rootFrame == null) return;
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(_rootFrame, WindowEvent.WINDOW_CLOSING));
        }

        // Spreadsheet - Cut
        else if(eventSource == _mnuSSheetCut || eventSource == _btnCutCells) {
            _tabSpreadsheet.cutSelectedCellsToClipboard();
        }
        // Spreadsheet - Copy
        else if(eventSource == _mnuSSheetCopy || eventSource == _btnCopyCells) {
            _tabSpreadsheet.printSelectedCells();
        }
        // Spreadsheet - Paste
        else if(eventSource == _mnuSSheetPaste || eventSource == _btnPasteCells) {
            _tabSpreadsheet.pasteClipboardToCells();
        }
        // Spreadsheet - Clear
        else if(eventSource == _mnuSSheetClear || eventSource == _btnClearCells) {
            _tabSpreadsheet.clearSelectedCells();
        }
        // Spreadsheet - Print
        else if(eventSource == _mnuSSheetPrint) {
            _tabSpreadsheet.printSelectedCells();
        }
        // Spreadsheet - Template for Plackett-Burmann Design
        else if(eventSource == _mnuSSheetPBTmpl) {
            int nof = PBDesignParam.showDialog(getRootFrame());
            if(nof >= 0) {
                String strData = PBDesignParam.genTemplate(nof);
                if(strData != null) _tabSpreadsheet.pasteToCells(strData);
            }
        }

        // Plot - Scatter-Line
        else if(eventSource == _mnuPlotScatterLine) {
            nrpClass = ScatterLinePlotPanel.class;
        }
        // Plot - Boxh-Whisker
        else if(eventSource == _mnuPlotBoxWhisker) {
            nrpClass = BoxWhiskerPlotPanel.class;
        }

        // Analysis - First-Order Calibration
        else if(eventSource == _mnuAnalFor) {
            nrpClass = FirstOrderRegPanel.class;
        }
        // Analysis - Second-Order Calibration
        else if(eventSource == _mnuAnalSor) {
            nrpClass = SecondOrderRegPanel.class;
        }
        // Analysis - Mandel Test
        else if(eventSource == _mnuAnalMandel) {
            nrpClass = MandelTestPanel.class;
        }
        // Analysis - Homogeneity
        else if(eventSource == _mnuAnalHomogen) {
            nrpClass = HomogeneityPanel.class;
        }
        // Analysis - Accuracy
        else if(eventSource == _mnuAnalAccuracy) {
            nrpClass = AccuracyPanel.class;
        }
        // Analysis - Precision - RSD
        else if(eventSource == _mnuAnalPrecisionR) {
            nrpClass = PrecisionRSDPanel.class;
        }
        // Analysis - Precision - 1-Way-ANOVA
        else if(eventSource == _mnuAnalPrecisionA) {
            nrpClass = PrecisionOWANOPanel.class;
        }
        // Analysis - Accuracy Profile - Gonzales
        else if(eventSource == _mnuAnalAPGonzales) {
            nrpClass = APGonzalesPanel.class;
        }
        // Analysis - Accuracy Profile - Rozet
        else if(eventSource == _mnuAnalAPRozet) {
            nrpClass = APRozetPanel.class;
        }
        // Analysis - Robustness
        else if(eventSource == _mnuAnalRobustness) {
            nrpClass = RobustnessPanel.class;
        }
        // Analysis - Quality Control (Shewhart Chart)
        else if(eventSource == _mnuAnalQCShewhart) {
            nrpClass = QCShewhartPanel.class;
        }

        // Toolbox - COW & Peak Detection
        else if(eventSource == _mnuToolboxStudTTab) {
            StudentTTable.showToolbox(getRootFrame());
        }
        else if(eventSource == _mnuToolboxFishFTab) {
            FisherFTable.showToolbox(getRootFrame());
        }
        else if(eventSource == _mnuToolboxCalcPMC) {
            CalcPMC.showToolbox(getRootFrame());
        }
        else if(eventSource == _mnuToolboxCalcTI) {
            CalcTI.showToolbox(getRootFrame());
        }
        else if(eventSource == _mnuToolboxCalcND) {
            CalcND.showToolbox(getRootFrame());
        }
        else if(eventSource == _mnuToolboxCalcDLQL) {
            CalcDLQL.showToolbox(getRootFrame());
        }
        else if(eventSource == _mnuToolboxCOWPD) {
            COWAndPeakDetection.showToolbox(getRootFrame());
        }

        // Help - Show TOC
        else if(eventSource == _mnuHelpTOC) {
            HelpBox.showDialog();
        }
        // Help - References
        else if(eventSource == _mnuHelpRefs) {
            RefsBox.showDialog(getRootFrame());
        }
        // Help - About
        else if(eventSource == _mnuHelpAbout) {
            AboutBox.showDialog(getRootFrame());
        }

        // Other - assume examples
        else {
            // Ask for confirmation
            if(!GUtil.showYesNoQuestionDialog(getRootFrame(), _S("dlg_open_exmpl_confirm"))) return;
            // Load the example
            JMenuItem menuItem = (JMenuItem) eventSource;
            _openExample(menuItem.getText());
        }

        // Create a new tab?
        if(nrpClass != null) {
            showWaitCursor();
            try {
                ResultPanel newTab = (ResultPanel) nrpClass.newInstance();
                newTab.init(true);
                GUtil.addTab(_tbpMain, newTab.getTabCaption(), newTab.getTabIcon(), newTab.getTabCaption(), newTab, false);
                _tbpMain.setSelectedComponent(newTab);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            showDefaultCursor();
        }

    }
}

/*
    // Initialize look and feel
    try {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        GUtil.DEFAULT_BOX_HEIGHT = 29;
    }
    catch(Exception e1){
        try { UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel"); }
        catch(Exception e2){}
    }
*/
