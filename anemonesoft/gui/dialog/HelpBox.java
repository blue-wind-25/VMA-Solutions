/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLDocument;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A class for displaying a help box
//
public class HelpBox extends JFrame {
    // Base URL of the help files
    private static String _baseURL  = null;
    private static URL    _indexURL = null;

    // Cache
    private static String _header1 = null;
    private static String _header2 = null;
    private static String _footer  = null;
    
    // Reference to this class' only instance that will ever created
    private static JFrame _inst = null;

    // Controls
    private JButton     _btnBack   = null;
    private JButton     _btnTOC    = null;
    private JButton     _btnClose  = null;
    private JEditorPane _htmlPane  = null;
    private JScrollPane _htmlScrl  = null;

    private String      _jumpTarget = null;
    private Timer       _jumpTimer  = null;

    // Current page URL
    private String _curPageUrl = "index.html";

    // History
    private ArrayList<String>       _histUrl = new ArrayList<String>();
    private ArrayList<HTMLDocument> _histDoc = new ArrayList<HTMLDocument>();
  //private ArrayList<Integer>      _histCrt = new ArrayList<Integer>();

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Read the content of a help file
    private static String _readHelpFileData(String fileName)
    {
        try {
            char[]            rb  = new char[1024];
            StringBuilder     sb  = new StringBuilder(1024);
            String            fr  = (_baseURL != null) ? (_baseURL + fileName) : fileName;
            InputStreamReader isr = new InputStreamReader(GUIMain.instance.getClass().getResourceAsStream(fr), "UTF-8");
            for(;;) {
                int len = isr.read(rb, 0, rb.length);
                if(len <= 0) break;
                sb.append(rb, 0, len);
            }
            return sb.toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Merge the content of a help file with the standard header and footer
    private static String _mergeHelpFileData(String name, boolean indexPage)
    { return (indexPage ? _header1 : _header2) + _readHelpFileData(name) + _footer; }

    // Merge the HTML with the standard header and footer
    private static String _mergeHelpHTML(String html)
    { return _header2 + html + _footer; }

    // Jump to the given target (or to the start of page if the target is null)
    private void _jumpToTarget(String target)
    {
        if(_jumpTimer != null) return;

        if(target == null) {
            _htmlPane.setCaretPosition(0);
            return;
        }

        _jumpTarget = target;
        _jumpTimer  = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent event)
            {
                _htmlPane.scrollToReference(_jumpTarget);
                _jumpTimer.stop();
                _jumpTimer = null;
            }
        });
        _jumpTimer.start();
    }

    // Private constructor
    private HelpBox() throws Exception
    {
        super(_S("dlg_help_caption"));
        setIconImage(GUtil.newImage("help_icon.png").getImage());

        // Initialize the button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 2, 0));
            _btnBack= new JButton(_S("dlg_help_back"));
                _btnBack.setIcon(GUtil.newImageIcon("mnu_help_back"));
                _btnBack.setFocusable(false);
                _btnBack.setEnabled(false);
            _btnTOC = new JButton(_S("dlg_help_toc"));
                _btnTOC.setIcon(GUtil.newImageIcon("mnu_help_toc"));
                _btnTOC.setFocusable(false);
                _btnTOC.setEnabled(false);
            _btnClose = new JButton(_S("dlg_help_close"));
                _btnClose.setIcon(GUtil.newImageIcon("mnu_file_quit"));
                _btnClose.setFocusable(false);
            buttonPanel.add(_btnBack);
            buttonPanel.add(_btnTOC);
            buttonPanel.add(_btnClose);
            buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

            _btnBack.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event)
                {
                    // No need to continue if the history is empty
                    if(_histUrl.isEmpty()) return;
                    // Restore the page
                    _htmlPane.setDocument(_histDoc.get(_histDoc.size() - 1));
                  //_htmlScrl.getVerticalScrollBar().setValue(_histCrt.get(_histCrt.size() - 1));
                    // Enable/disable buttons
                    _btnTOC.setEnabled(!_histUrl.get(_histUrl.size() - 1).equals("index.html"));
                    _btnBack.setEnabled(_histDoc.size() > 1);
                    // Pop the last entry form the history
                    _histUrl.remove(_histUrl.size() - 1);
                    _histDoc.remove(_histDoc.size() - 1);
                  //_histCrt.remove(_histCrt.size() - 1);
                }
            });

            _btnTOC.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event)
                {
                    try {
                        // Save the current document
                        HTMLDocument curDoc = (HTMLDocument) _htmlPane.getDocument();
                      //int          curCrt = _htmlScrl.getVerticalScrollBar().getValue();
                        // Load the TOC
                        _htmlPane.setDocument(_htmlPane.getEditorKit().createDefaultDocument());
                        _htmlPane.setText(_mergeHelpFileData("index.html", true));
                        ((HTMLDocument) _htmlPane.getDocument()).setBase(_indexURL);
                        _jumpToTarget(null);
                        // Add the previous document to history
                        if(_histUrl.size() > 10) {
                            _histUrl.remove(0);
                            _histDoc.remove(0);
                          //_histCrt.remove(0);
                        }
                        _histUrl.add(_curPageUrl);
                        _histDoc.add(curDoc);
                      //_histCrt.add(curCrt);
                        // Update the current page URL
                        _curPageUrl = "index.html";
                        // Enable/disable buttons
                        _btnBack.setEnabled(true);
                        _btnTOC.setEnabled(false);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                        GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_help_toc"));
                    }
                }
            });

            _btnClose.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event)
                { Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(_inst, WindowEvent.WINDOW_CLOSING)); }
            });
                
        // Initialize the HTML pane with the first page
        _htmlPane = new JEditorPane("text/html", _mergeHelpFileData(_curPageUrl, true));
        ((HTMLDocument) _htmlPane.getDocument()).setBase(_indexURL);
        _jumpToTarget(null);

        _htmlPane.setEditable(false);
        _htmlPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent event)
            {
                if(event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) return;
                try {
                    // Save the current document
                    HTMLDocument curDoc = (HTMLDocument) _htmlPane.getDocument();
                  //int          curCrt = _htmlScrl.getVerticalScrollBar().getValue();
                    // Get the URL and load the page
                    String url = event.getURL().toString();
                    String fnm = url.substring(url.lastIndexOf("/") + 1);
                    String trg = null;
                    int    idx = fnm.indexOf("#");
                    if(idx > 0) {
                        trg = fnm.substring(idx + 1);
                        fnm = fnm.substring(0, idx);
                    }
                    if(fnm.endsWith(".html")) {
                        _htmlPane.setDocument(_htmlPane.getEditorKit().createDefaultDocument());
                        _htmlPane.setText(_mergeHelpFileData(fnm, false));
                        ((HTMLDocument) _htmlPane.getDocument()).setBase(_indexURL);
                        _jumpToTarget(trg);
                    }
                    else {
                        String src = url.substring(url.indexOf("#") + 1);
                        _htmlPane.setDocument(_htmlPane.getEditorKit().createDefaultDocument());
                        _htmlPane.setText(_mergeHelpHTML("<center><img src='" + src + "'/></center>"));
                        ((HTMLDocument) _htmlPane.getDocument()).setBase(_indexURL);
                    }
                    // Add the previous document to history
                    if(_histUrl.size() > 10) {
                        _histUrl.remove(0);
                        _histDoc.remove(0);
                      //_histCrt.remove(0);
                    }
                    _histUrl.add(_curPageUrl);
                    _histDoc.add(curDoc);
                  //_histCrt.add(curCrt);
                    // Update the current page URL
                    _curPageUrl = fnm;
                    // Enable/disable buttons
                    _btnBack.setEnabled(true);
                    _btnTOC.setEnabled(true);
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_help_href"));
                }
            }
        });
        
        // Create the dialog
        JPanel mainPanel = new JPanel(new BorderLayout(), true);
            _htmlScrl = new JScrollPane(_htmlPane);
                _htmlScrl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createRaisedBevelBorder(),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                    ),
                    BorderFactory.createLoweredBevelBorder()
                ));
            mainPanel.add(buttonPanel, BorderLayout.NORTH);
            mainPanel.add(_htmlScrl, BorderLayout.CENTER);
        add(mainPanel);

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension refSize    = GUIMain.instance.getRootFrame().getSize();
        Point     refPos     = GUIMain.instance.getRootFrame().getLocation();
        pack();
        setLocation((refSize.width - dialogSize.width) / 2 + refPos.x, (refSize.height - dialogSize.height) / 2 + refPos.y);

        // Handle close-window event
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event)
            { _inst = null; }
        });

        // Allow the dialog to be closed by pressing escape
        final JFrame _THIS = this;
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            { Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(_THIS, WindowEvent.WINDOW_CLOSING)); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // Bring window to front
    public void toFront()
    {
        setAlwaysOnTop(true);
        super.toFront();
        requestFocus();
        setAlwaysOnTop(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show a help box
    public static void showDialog()
    {
        // Just bring the dialog to front if it already exists
        if(_inst != null) {
            _inst.setExtendedState(_inst.getExtendedState() & ~Frame.ICONIFIED);
            _inst.toFront();
            return;
        }

        // Initialize the header and footer cache (if not yet done)
        if(_header1 == null) {
            String appTitle = _S("dlg_app_title");
            String appVer   = _F("dlg_abox_version_T",   new String[]{ GUIMain.APP_VERSION });
            String appBy    = _F("dlg_abox_devel_by_T",  new String[]{ "Aloysius Indrayanto", "Gunawan Indrayanto" });
            String appCopy  = _F("dlg_abox_copyright_T", new String[]{ GUIMain.APP_COPY_YEAR, GUIMain.APP_COPY_NAME });
            _header1 = _readHelpFileData("/anemonesoft/i18n/help/_header1.html").replace("{APP_TITLE}",     appTitle).replace("{APP_VERSION}",   appVer );
            _header2 = _readHelpFileData("/anemonesoft/i18n/help/_header2.html").replace("{APP_TITLE}",     appTitle).replace("{APP_VERSION}",   appVer );
            _footer  = _readHelpFileData("/anemonesoft/i18n/help/_footer.html" ).replace("{APP_DEVELOPER}", appBy   ).replace("{APP_COPYRIGHT}", appCopy);
        }

        // Find the location of the help files (if not yet found)
        if(_baseURL == null) {
            // Location of the TOC file for the current locale
            String baseURL = "/anemonesoft/i18n/help/" + StringTranslator.getLocaleIDString() + "/";
            // If it is available, use it
            if(GUIMain.instance.getClass().getResourceAsStream(baseURL + "index.html") != null) {
                _baseURL = baseURL;
            }
            // Try to load the en-US version if the former one is not available
            else {
                // Location of the TOC file for en-US locale
                baseURL = "/anemonesoft/i18n/help/en_US/";
                // If it is available, use it
                if(GUIMain.instance.getClass().getResourceAsStream(baseURL + "index.html") != null) {
                    _baseURL = baseURL;
                }
                // Nothing is available, show error and exit
                else {
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_help_nf"));
                    return;
                }
            }
            // Save the name of the index file
            _indexURL = GUIMain.instance.getClass().getResource(_baseURL + "index.html");
        }

        // Create the dialog
        GUIMain.instance.showWaitCursor();
        try {
            _inst = new HelpBox();
            try {
                LookAndFeel curLaF = UIManager.getLookAndFeel();
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                SwingUtilities.updateComponentTreeUI(_inst);
                UIManager.setLookAndFeel(curLaF);
            }
            catch(Exception e){}
        }
        catch(Exception e) {
            e.printStackTrace();
            GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_help_toc"));
            GUIMain.instance.showDefaultCursor();
            return;
        }
        GUIMain.instance.showDefaultCursor();

        // Change the window decoration if the application is run as an applet
        /*
        if(GUIMain.instance.isApplet()) {
            _inst.dispose();
            _inst.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
            _inst.setUndecorated(true);
        }
        */

        // Show the dialog
        _inst.setVisible(true);
        _inst.toFront();

        // Resize and position the dialog
        Dimension refSize = GUIMain.instance.getRootFrame().getSize();
        if(GUIMain.instance.isApplet()) {
            Point refPos = GUIMain.instance.getRootFrame().getLocation();
            _inst.setSize(refSize.width - 40, refSize.height - 40);
            _inst.setLocation(refPos.x + 20, refPos.y + 20);
        }
        else {
            _inst.setSize(refSize.width, refSize.height);
            _inst.setLocation(0, 0);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}
