/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A class for displaying an about box
//
public class AboutBox extends JDialog implements ActionListener {
    // Controls
    private JButton _btnOK = new JButton(_S("dlg_ok"));
    private Timer   _timer = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Shortcut for formatting an i18n string
    private static String _F(String s, Object[] a)
    { return StringTranslator.formatString(s, a); }

    // Private constructor
    private AboutBox(Frame parentFrame, boolean asSplashScreen)
    {
        super(parentFrame);

        // Prepare the template data
        String[] version   = { GUIMain.APP_VERSION };
        String[] copyright = { GUIMain.APP_COPY_YEAR, GUIMain.APP_COPY_NAME };

        // Generate label for the application name
        JLabel lblTitle = new JLabel(_S("dlg_app_title"), null, JLabel.CENTER);
        Font   lblFont  = lblTitle.getFont();
        lblTitle.setFont(new Font(lblFont.getName(), Font.BOLD, lblFont.getSize() * 2));

        // Generate panels for the about dialog
        JPanel             mainPanel = new JPanel(new GridBagLayout(), true);
        GridBagConstraints gbc       = new GridBagConstraints();
        gbc.fill       = GridBagConstraints.BOTH;
        gbc.gridwidth  = 1;
        gbc.gridheight = 1;
        gbc.gridx      = 0;
        GUtil.addWithGbc(mainPanel, gbc, lblTitle);
        GUtil.addWithGbc(mainPanel, gbc, new JLabel(_F("dlg_abox_version_T", version), null, JLabel.CENTER));
        GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 25)));
        GUtil.addWithGbc(mainPanel, gbc, new JLabel(null, GUtil.newImage("vma_logo.png"), JLabel.CENTER));
        GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 25)));
        GUtil.addWithGbc(mainPanel, gbc, new JLabel(_S("dlg_abox_devel_by"), null, JLabel.CENTER));
        GUtil.addWithGbc(mainPanel, gbc, new JLabel("Aloysius Indrayanto " + _S("dlg_abox_and") + " Gunawan Indrayanto", null, JLabel.CENTER));
        GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 25)));
        GUtil.addWithGbc(mainPanel, gbc, new JLabel(_S("dlg_abox_freeware"), null, JLabel.CENTER));
        GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 25)));
        GUtil.addWithGbc(mainPanel, gbc, new JLabel(_F("dlg_abox_copyright_T", copyright), null, JLabel.CENTER));
        GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 5)));
        GUtil.addWithGbc(mainPanel, gbc, new JLabel(null, GUtil.newImage("anemonesoft.png"), JLabel.CENTER));

        // Add some reference texts
        if(!asSplashScreen) {
            String strRef = "<html><body>"
                          + "<u>JAMA : A Java Matrix Package</u><br>"
                          + "http://math.nist.gov/javanumerics/jama<br><br>"
                          + "JAMA is a cooperative product of The MathWorks and the<br>"
                          + "National Institute of Standards and Technology (NIST)<br>"
                          + "which has been released to the public domain.<br>"
                          + "</body></html>";
            JPanel pnlRefs = new JPanel(null, true);
                pnlRefs.setLayout(new BoxLayout(pnlRefs, BoxLayout.Y_AXIS));
                pnlRefs.add(new JLabel(strRef));
            GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 25)));
            GUtil.addWithGbc(mainPanel, gbc, pnlRefs);
            GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 25)));
        }
        else
            GUtil.addWithGbc(mainPanel, gbc, Box.createRigidArea(new Dimension(0, 5)));

        // Create the dialog
        JPanel dialogPanel = new JPanel(new BorderLayout(), true);
            dialogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            dialogPanel.add(mainPanel, BorderLayout.CENTER);
            if(!asSplashScreen) {
                dialogPanel.add(_btnOK, BorderLayout.SOUTH);
                _btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
                _btnOK.addActionListener(this);
            }
            else {
                dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            }
        add(dialogPanel);

        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent/desktop
        Dimension dialogSize = getSize();
        if(parentFrame != null) {
            Dimension parentSize = parentFrame.getSize();
            Point     parentPos  = parentFrame.getLocation();
            setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);
        }
        else {
            Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation((scrSize.width - dialogSize.width) / 2, (scrSize.height - dialogSize.height) / 2);
        }

        // Splash-screen mode - close the dialog after a few seconds
        if(asSplashScreen) {
            _timer = new Timer(3000, this);
            _timer.start();
        }

        // Allow the dialog to be closed by pressing escape
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            { _closeDialog(); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // Close the dialog
    private void _closeDialog()
    {
        if(_timer != null) {
            _timer.stop();
            _timer = null;
        }
        setModal(false);
        setVisible(false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show an about box
    public static void showDialog(Frame parentFrame)
    {
        AboutBox ab = new AboutBox(parentFrame, false);
        GUtil.showModalDialog(ab, null, _S("dlg_abox_title"), JRootPane.INFORMATION_DIALOG);
    }

    // Show a splash-screen
    public static void showSplashScreen()
    {
        AboutBox ab = new AboutBox(null, true);

        ab.setTitle(_S("dlg_app_title"));

        ab.setModal(true);
        ab.setResizable(false);

        ab.dispose();
        ab.setUndecorated(true);
        ab.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);

        ab.setAlwaysOnTop(true);
        ab.setVisible(true);
        ab.toFront();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for button and timer
    public void actionPerformed(ActionEvent event)
    { _closeDialog(); }
}
