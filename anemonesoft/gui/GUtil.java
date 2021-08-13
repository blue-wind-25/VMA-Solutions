/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;
import javax.swing.*;
import javax.swing.text.*;

import anemonesoft.gui.component.*;
import anemonesoft.i18n.*;

// ⁰ⁱ²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ ⁿ
// ₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎ ₐₑₒₓₔ

//
// A class that holds all generic utility functions
//
public class GUtil {
    // Default size
    public static final int DEFAULT_SMALL_BOX_WIDTH = 50;
    public static final int DEFAULT_LARGE_BOX_WIDTH = 110;
    public static       int DEFAULT_BOX_HEIGHT      = 20;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Convert string to integer without exception
    public static int str2i(String str)
    {
        try { return Integer.parseInt(str); }
        catch(Exception e) {}
        return 0;
    }

    // Convert string to double without exception
    public static double str2d(String str)
    {
        try { return Double.parseDouble(str); }
        catch(Exception e) {}
        return 0.0;
    }

    // Convert string to integer without exception
    public static int str2i(String str, int defaultValue)
    {
        try { return Integer.parseInt(str); }
        catch(Exception e) {}
        return defaultValue;
    }

    // Convert string to double without exception
    public static double str2d(String str, double defaultValue)
    {
        try { return Double.parseDouble(str); }
        catch(Exception e) {}
        return defaultValue;
    }

    // Create and return a new image
    public static ImageIcon newImage(String fileName)
    {
        URL url = GUIMain.instance.getClass().getResource("/images/" + fileName);
        return (url == null) ? null : new ImageIcon(url);
    }

    // Create and return a new image icon
    public static ImageIcon newImageIcon(String name)
    {
        URL url = GUIMain.instance.getClass().getResource("/images/icons/" + name + ".gif");
        return (url == null) ? null : new ImageIcon(url);
/*
        InputStream is = GUIMain.instance.getClass().getResourceAsStream("/images/icons/" + name + ".gif");
        if(is == null) return null;

        Image im = null;
        try { im = ImageIO.read(is); }
        catch(Exception e) {}
        if(im == null) return null;

        return new ImageIcon(im);
*/    }

    // Get installed font name
    public static String getSysFontName(String logicalName)
    {
        /*
        GraphicsEnvironment g     = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String []           fonts = g.getAvailableFontFamilyNames();

        if(logicalName.equals("Monospaced")) {
            for(int i = 0; i < fonts.length; ++i) {
                if(fonts[i].equals("Liberation Mono")) return fonts[i];
                if(fonts[i].equals("Courier New"    )) return fonts[i];
            }
        }

        if(logicalName.equals("SansSerif")) {
            for(int i = 0; i < fonts.length; ++i) {
                if(fonts[i].equals("Liberation Serif"    )) return fonts[i];
                if(fonts[i].equals("Microsoft Sans Serif")) return fonts[i];
                if(fonts[i].equals("Sans Serif"          )) return fonts[i];
            }
        }
        */

        return logicalName;
    }

    // Create and return a new font from an external font data
    public static Font newFont(String name, String fbName, int fbStyle, int fbSize)
    {
        try {
            return Font.createFont(
                Font.TRUETYPE_FONT, GUIMain.instance.getClass().getResourceAsStream("/fonts/" + name + ".ttf")
            );
        }
        catch(Exception e) {
            e.printStackTrace();
            return new Font(fbName, fbStyle, fbSize);
        }
    }

    // Create and return a new font
    public static Font newFont(String name, String fbName, int fbStyle)
    { return newFont(name, fbName, fbStyle, 12); }

    // Create and return a new font
    public static Font newFont(String name, String fbName)
    { return newFont(name, fbName, Font.PLAIN, 12); }

    // Create and return a new sub-menu
    public static JMenu newSubJMenu(JMenu parent, String text, Icon icon, int mnemonic, ActionListener listener)
    {
        JMenu menu = new JMenu(text);
        menu.setIcon(icon);

        if(mnemonic > 0) menu.setMnemonic(mnemonic);
        parent.add(menu);

        return menu;
    }

    // Create and return a new menu item
    public static JMenuItem newJMenuItem(JMenu parent, String text, Icon icon, int mnemonic, int accelerator, boolean ctrlMask, ActionListener listener)
    {
        JMenuItem menuItem = new JMenuItem(text, icon);

        if(mnemonic > 0) menuItem.setMnemonic(mnemonic);
        if(accelerator >= 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke((accelerator > 0) ? accelerator : mnemonic, ctrlMask ? Event.CTRL_MASK : 0));
        }
        menuItem.addActionListener(listener);

        parent.add(menuItem);

        return menuItem;
    }

    // Create and return a new menu item
    public static JMenuItem newJMenuItem(JMenu parent, String text, Icon icon, int mnemonic, int accelerator, ActionListener listener)
    { return newJMenuItem(parent, text, icon, mnemonic, accelerator, true, listener); }

    // Add a component using grid bag layout
    public static void addWithGbc(JPanel panel, GridBagConstraints gbc, Component node)
    {
        ((GridBagLayout) panel.getLayout()).setConstraints(node, gbc);
        panel.add(node);
    }

    // Add a component to the given tab
    public static void addTab(JTabbedPane pane, String caption, ImageIcon icon, String tooltip, JComponent content, boolean noClose)
    {
        pane.add(content);
        pane.setTabComponentAt(pane.getTabCount() - 1, new TabButtonComponent(pane, content, caption, icon, tooltip, noClose));
    }

    // Disable cut-and-paste on text-field
    public static void disableCutAndPasteOnTextField(JTextField txt)
    {
        JTextComponent.KeyBinding[] kba = {
            new JTextComponent.KeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
                DefaultEditorKit.beepAction
            ),
            new JTextComponent.KeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
                DefaultEditorKit.beepAction
            )
        };
        JTextComponent.loadKeymap(txt.getKeymap(), kba, txt.getActions());
    }

    // Enable copy-cut-and-paste on text-field
    public static void enableCutAndPasteOnTextField(JTextField txt)
    {
        JTextComponent.KeyBinding[] kba = {
            new JTextComponent.KeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
                DefaultEditorKit.copyAction
            ),
            new JTextComponent.KeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
                DefaultEditorKit.cutAction
            ),
            new JTextComponent.KeyBinding(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
                DefaultEditorKit.pasteAction
            )
        };
        JTextComponent.loadKeymap(txt.getKeymap(), kba, txt.getActions());
    }

    // Show/create a modal dialog
    public static Object showModalDialog(Container optionPaneOrJDialog, Frame parentFrame, String title, int style)
    {
        boolean     useOP  = (optionPaneOrJDialog instanceof JOptionPane);
        JOptionPane opane  = useOP ? (JOptionPane) optionPaneOrJDialog : null;
        JDialog     dialog = useOP ? opane.createDialog(parentFrame, null) : (JDialog) optionPaneOrJDialog;

        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setResizable(false);

        if(GUIMain.instance.isApplet()) {
            dialog.dispose();
            dialog.getRootPane().setWindowDecorationStyle(style);
            dialog.setUndecorated(true);
        }

        dialog.pack();
        dialog.setVisible(true);
        dialog.pack();

        return useOP ? opane.getValue() : null;
    }

    // Show/create a modal dialog
    public static Object showModalDialog(JOptionPane optionPaneOrJDialog, JFrame parentFrame, String title, int style)
    { return showModalDialog(optionPaneOrJDialog, (Frame) parentFrame, title, style); }

    // Show an information dialog
    public static void showInformationDialog(Frame parentFrame, String message)
    {
        // Generate the dialog
        final JButton     btnOK = new JButton(_S("dlg_ok"));
        final Object[]    ans   = { btnOK };
        final JOptionPane op    = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_OPTION, null, ans, ans[0]);

        btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        // Show the dialog
        GUtil.showModalDialog(op, parentFrame, _S("dlg_information"), JRootPane.INFORMATION_DIALOG);
    }

    // Show a text dialog
    public static void showTextDialog(Frame parentFrame, String message)
    {
        // Generate the text display
        JTextArea txtMsg  = new JTextArea(message, 10, 64);
            txtMsg.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            txtMsg.setLineWrap(true);
            txtMsg.setEditable(false);
            txtMsg.setFont(new Font(GUtil.getSysFontName("Monospaced"), Font.PLAIN, txtMsg.getFont().getSize()));

        JScrollPane scrMsg = new JScrollPane(txtMsg);
            scrMsg.setBorder(BorderFactory.createLoweredBevelBorder());

        // Generate the dialog
        final JButton     btnOK = new JButton(_S("dlg_ok"));
        final Object[]    ans   = { btnOK };
        final JOptionPane op    = new JOptionPane(scrMsg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_OPTION, null, ans, ans[0]);

        btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        // Show the dialog
        GUtil.showModalDialog(op, parentFrame, _S("dlg_information"), JRootPane.INFORMATION_DIALOG);
    }

    // Show an error dialog
    public static void showErrorDialog(Frame parentFrame, String message)
    {
        // Generate the dialog
        final JButton     btnOK = new JButton(_S("dlg_ok"));
        final Object[]    ans   = { btnOK };
        final JOptionPane op    = new JOptionPane(message, JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, null, ans, ans[0]);

        btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        // Show the dialog
        GUtil.showModalDialog(op, parentFrame, _S("dlg_error"), JRootPane.ERROR_DIALOG);
    }

    // Show a yes-no question dialog
    public static boolean showYesNoQuestionDialog(Frame parentFrame, String message)
    {
        // Generate the dialog
        final JButton     btnYes = new JButton(_S("dlg_yes"));
        final JButton     btnNo  = new JButton(_S("dlg_no"));
        final Object[]    ans    = { btnYes, btnNo };
        final JOptionPane op     = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, ans, ans[0]);

        btnYes.setIcon(GUtil.newImageIcon("btn_ok"));
        btnYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnYes); }
        });

        btnNo.setIcon(GUtil.newImageIcon("btn_cancel"));
        btnNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnNo); }
        });

        // Show the dialog
        Object ret = GUtil.showModalDialog(op, parentFrame, _S("dlg_confirmation"), JRootPane.QUESTION_DIALOG);

        // Return true if the user has selected "Yes"
        return (ret == ans[0]);
    }
    // Show an input dialog
    public static String showInputDialog(Frame parentFrame, String message)
    { return showInputDialog(parentFrame, message, ""); }

    // Show an input dialog
    public static String showInputDialog(Frame parentFrame, String message, String initText)
    {
        // Generate the question panel
        final JPanel pnlQuestion = new JPanel(new GridLayout(2, 1, 0, 5), true);
            final JTextField txtAnswer = new JTextField();
            pnlQuestion.add(new JLabel(message));
            pnlQuestion.add(txtAnswer);
            txtAnswer.setText(initText);

        // Generate the dialog
        final JButton     btnOK     = new JButton(_S("dlg_ok"));
        final JButton     btnCancel = new JButton(_S("dlg_cancel"));
        final Object[]    ans       = { btnOK, btnCancel };
        final JOptionPane op        = new JOptionPane(pnlQuestion, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, ans, txtAnswer);

        btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        btnCancel.setIcon(GUtil.newImageIcon("btn_cancel"));
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnCancel); }
        });

        // Allow the user to confirm the answer by pressing enter
        txtAnswer.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            { btnOK.doClick(); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Show the dialog
        Object ret = GUtil.showModalDialog(op, parentFrame, _S("dlg_query"), JRootPane.QUESTION_DIALOG);

        // Return true if the user has selected "OK"
        return (ret == ans[0]) ? txtAnswer.getText() : null;
    }

    // Show a get-license  dialog
    public static String showGetLicenseDialog(Frame parentFrame, String hostKey)
    {
        // Generate the question panel
        final JPanel pnlQuestion = new JPanel(new FlexGridLayout(4, 1, 0, 5));
            final JTextArea txtHostKey = new JTextArea(hostKey, 10, 64);
            final JTextArea txtLicData = new JTextArea("", 10, 64);
            JScrollPane scrHostKey = new JScrollPane(txtHostKey);
            JScrollPane scrLicData = new JScrollPane(txtLicData);
            pnlQuestion.add(new JLabel(_S("dlg_glbox_info"), JLabel.LEFT));
            pnlQuestion.add(scrHostKey);
            pnlQuestion.add(new JLabel(_S("dlg_glbox_licd"), JLabel.LEFT));
            pnlQuestion.add(scrLicData);

            scrHostKey.setBorder(BorderFactory.createLoweredBevelBorder());
            scrLicData.setBorder(BorderFactory.createLoweredBevelBorder());

        Font font = new Font(GUtil.getSysFontName("Monospaced"), Font.PLAIN, txtHostKey.getFont().getSize());

            txtHostKey.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            txtHostKey.setLineWrap(true);
            txtHostKey.setEditable(false);
            txtHostKey.setFont(font);

            txtLicData.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            txtLicData.setLineWrap(true);
            txtLicData.setFont(font);

        // Generate the dialog
        final JButton     btnOK     = new JButton(_S("dlg_ok"));
        final JButton     btnCancel = new JButton(_S("dlg_cancel"));
        final JButton     btnDemo   = new JButton(_S("dlg_run_in_demo")); /** From v1.2.0a */
        final Object[]    ans       = { btnOK, btnCancel, btnDemo };
        final JOptionPane op        = new JOptionPane(pnlQuestion, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, ans, txtHostKey);

        btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnOK); }
        });

        btnCancel.setIcon(GUtil.newImageIcon("btn_cancel"));
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnCancel); }
        });
        btnDemo.setIcon(GUtil.newImageIcon("btn_demo")); /** From v1.2.0a */
        btnDemo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent)
            { op.setValue(btnDemo); }
        });

        // Show the dialog
        Object ret = GUtil.showModalDialog(op, parentFrame, _S("dlg_glbox_title"), JRootPane.QUESTION_DIALOG);

        // Return the user response
             if(ret == ans[0]) return txtLicData.getText();
        else if(ret == ans[1]) return null;
        else                   return GUIMain.APP_DEMO_MODE_STR; /** From v1.2.0a */
    }

    // Show a a dialog that says that the plot cannot be genereated
    public static void showNoDDialogPlot()
    {
        Thread t = new Thread(new Runnable() {
            public void run()
            { showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_anal_plot_fail")); }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Show a a dialog that says that the report cannot be genereated
    public static void showNoDDialogReport()
    {
        Thread t = new Thread(new Runnable() {
            public void run()
            { showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_anal_report_fail")); }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Show a a dialog that says that the calculation cannot be performed
    public static void showNoDDialogCalc()
    {
        Thread t = new Thread(new Runnable() {
            public void run()
            { showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_anal_calc_fail")); }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Show a dialog that says that an unexpected error has occurred
    public static void showUnexpectedError()
    {
        Thread t = new Thread(new Runnable() {
            public void run()
            { showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_unexpected")); }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Show wait cursor
    public static void showWaitCursor(final JDialog dialog)
    {
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Thread t = new Thread(new Runnable() {
            public void run()
            { dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

    // Show default cursor
    public static void showDefaultCursor(final JDialog dialog)
    {
        dialog.setCursor(Cursor.getDefaultCursor());
        Thread t = new Thread(new Runnable() {
            public void run()
            { dialog.setCursor(Cursor.getDefaultCursor()); }
        });
        t.start();
        try { t.wait(); }
        catch(Exception e) {}
    }

}
