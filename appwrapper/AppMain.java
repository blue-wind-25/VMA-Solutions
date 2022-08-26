/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package appwrapper;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//import anemonesoft.stat.*; ///

//
// The main application class
//
public class AppMain {
    // Size of the root frame
    private static final int WIDTH  = 720;
    private static final int HEIGHT = 540;

    // Instantiate the root frame and applet objects
    private static JFrame  frame  = new JFrame();
    private static JApplet applet = new GUIMain(frame);

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////
   
    // Program start-up point
    public static void main(String argv[])
    {
        /*
        double[] target = { 0.00, 0.00, 0.00, 0.00, 0.00, 0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.20, 0.25, 0.10, 0.05, 0.00, 0.00, 0.00, 0.00 };
        double[] signal = { 0.00, 0.00, 0.00, 0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.20, 0.25, 0.10, 0.05, 0.00, 0.00, 0.00, 0.00, 0.00 };
        double[] refres = { 0.00000,0.00000,0.00000,0.00000,0.00000,0.0250000,0.0500000,0.108333,0.166667,0.225000,0.283333,0.341667,0.200000,0.228571,0.0857143,0.0285714,0.00000,0.00000,0.00000,0.00000 };
        double[] result = Chromatogram.performCOW(target, signal, 5, 3, 1);
        SUtil._dump(refres); System.out.println();
        SUtil._dump(result); System.out.println();
        System.exit(0);
        */
        
        // Get the screen size
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Initialize the wrapper frame
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event)
            {
                if(!GUtil.showYesNoQuestionDialog(GUIMain.instance.getRootFrame(), _S("dlg_quit_app_confirm"))) return;

                applet.stop();
                applet.destroy();
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", applet);
        //frame.setUndecorated(true);
        //frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        frame.getRootPane().setDoubleBuffered(true);
        frame.setIconImage(GUtil.newImage("vma_icon.png").getImage());

        // Initialize the applet
        applet.setStub(new SimpleAppletStub(argv, applet));
        applet.init();
        applet.start();

        // Set the title of the main application frame
        frame.setTitle(_S("dlg_app_title"));

        // Resize and center the frame
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocation((scrSize.width - WIDTH) / 2, (scrSize.height - HEIGHT) / 2);
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
        frame.toFront();
        frame.setAlwaysOnTop(false);
    }
}
