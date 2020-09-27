/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A class for displaying an image
//
public class ImageBox extends JDialog implements ActionListener {
    // Data
    BufferedImage _bi = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Private constructor
    private ImageBox(Frame parentFrame, BufferedImage bi)
    {
        super(parentFrame);
        _bi = bi;

        // Image panel
        JPanel pnlImage = new JPanel() {
            public void paintComponent(Graphics g)
            {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
                g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
                g2d.drawImage(
                    _bi,
                    0, 0, getWidth(), getHeight(),
                    0, 0, _bi.getWidth(), _bi.getHeight(),
                    new Color(0, 0, 0, 0),
                    null
                );
                g2d.dispose();
            }
        };
        pnlImage.setMinimumSize(new Dimension(640, 480));

        // Image-holder panel
        JPanel pnlImageHolder = new JPanel(new BorderLayout(), true);
            pnlImageHolder.setBorder(BorderFactory.createLoweredBevelBorder());
            pnlImageHolder.add(pnlImage, BorderLayout.CENTER);

        // Create the dialog
        JPanel mainPanel = new JPanel(new BorderLayout(0, 5), true);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            mainPanel.add(pnlImageHolder, BorderLayout.CENTER);
            final JButton btnOK = new JButton(_S("dlg_ok"));
                btnOK.setIcon(GUtil.newImageIcon("btn_ok"));
                btnOK.addActionListener(this);
                mainPanel.add(btnOK, BorderLayout.SOUTH);
        add(mainPanel);
        
        // Maximize the dialog
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width, screenSize.height);
        setLocation(0, 0);

        // Defines the default button
        getRootPane().setDefaultButton(btnOK);

        // Allow the dialog to be closed by pressing escape
        final JDialog _THIS = this;
        getRootPane().registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            { _THIS.setVisible(false); }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        /*
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            public void actionPerformed(ActionEvent e)
            { btnOK.doClick(); }
        });
        */
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show the dialog
    public static void showDialog(Frame parentFrame, BufferedImage bi, String title)
    {
        ImageBox rsb = new ImageBox(parentFrame, bi);

        rsb.setTitle(title);
        rsb.setModal(true);
        if(GUIMain.instance.isApplet()) {
            rsb.dispose();
            rsb.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
            rsb.setUndecorated(true);
        }

        rsb.setVisible(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for buttons
    public void actionPerformed(ActionEvent event)
    { this.setVisible(false); }
}
