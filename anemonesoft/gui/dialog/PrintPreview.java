/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import anemonesoft.gui.*;
import anemonesoft.gui.control.*;
import anemonesoft.i18n.*;

public class PrintPreview extends JDialog implements Runnable
{
    // Data
    private PageFormat pageFormat = null;
    private Printable  printable  = null;
    private int        wPage      = 0;
    private int        hPage      = 0;
    private int        width      = 0;
    private int        height     = 0;

    // Controls
    private JComboBox        cmbScale         = null;
    private PreviewGrid previewContainer = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Create the top-panel's controls
    private void createTopPanelControls(JPanel pnlTop)
    {
        // Print button
        JButton btnPrint = new JButton(_S("dlg_pp_print"), GUtil.newImageIcon("mnu_file_print"));
        btnPrint.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    PrinterJob prnJob = PrinterJob.getPrinterJob();
                    prnJob.setJobName(_S("dlg_app_title"));
                    prnJob.setPrintable(printable, pageFormat);
                    if(prnJob.printDialog()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        prnJob.print();
                        System.gc();
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                    dispose();
                }
                catch(Exception e) {
                    e.printStackTrace();
                    GUtil.showErrorDialog(GUIMain.instance.getRootFrame(), _S("err_print_page_fail"));
                }
            }
        });
        pnlTop.add(btnPrint);

        // Scale combo-box
        cmbScale = new JComboBox(new String[]{ "10 %", "25 %", "50 %", "75 %", "100 %", "150 %", "200 %"});
        cmbScale.setSelectedIndex(1);
        cmbScale.setMaximumSize(cmbScale.getPreferredSize());
        cmbScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Thread runner = new Thread(PrintPreview.this);
                runner.start();
            }
        });
        pnlTop.add(cmbScale);

        // Close button
        JButton btnClose = new JButton(_S("dlg_pp_close"), GUtil.newImageIcon("mnu_file_quit"));
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev)
            { dispose(); }
        });
        pnlTop.add(btnClose);
    }

    // Get the display scale
    private int getDisplayScale()
    {
        String str = cmbScale.getSelectedItem().toString();
        str = str.substring(0, str.length() - 1).trim();
        try {
            return Integer.parseInt(str);
        }
        catch(Exception e) {}
        return 25;
    }

    // Generate the preview pages
    private void generateThePreviewPages() throws Exception
    {
        // Get the scale
        int scale = getDisplayScale();
       
        // Determine the page size
        wPage = (int) pageFormat.getWidth ();
        hPage = (int) pageFormat.getHeight();
        
        // Determine the preview size
        width  = (int) Math.ceil(wPage * scale / 100);
        height = (int) Math.ceil(hPage * scale / 100);

        // Print preview the pages
        int pageIndex = 0;
        while(true) {
            // Create a new image buffer
            BufferedImage img = new BufferedImage(wPage, hPage, BufferedImage.TYPE_INT_RGB);
            Graphics      g   = img.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, wPage, hPage);
            // Print the current page
            if(printable.print(g, pageFormat, pageIndex) !=  Printable.PAGE_EXISTS) break;
            pageIndex++;
            // Add the printed page to the preview container
            PreviewCell pp = new PreviewCell(width, height, img);
            previewContainer.add(pp);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a print-preview window using the given printable instance
    public PrintPreview(Frame parent, Printable target) throws Exception
    {
        super(parent);

        // Show wait cursor
        GUIMain.instance.showWaitCursor();

        try {
            // Set title and icon
            setTitle(_S("dlg_pp_title"));
            setIconImage(GUtil.newImageIcon("mnu_file_print").getImage());

            // Copy some references
            pageFormat = GUIMain.instance.getPageFormat();
            printable  = target;

            // Initialize the toolbar
            JPanel pnlTop = new JPanel(new GridLayout(1, 3, 1, 0), true);
            pnlTop.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            createTopPanelControls(pnlTop);

            // Initialize the preview area
            previewContainer = new PreviewGrid();
            previewContainer.setBorder(BorderFactory.createRaisedBevelBorder());
            generateThePreviewPages();

            // Initialize the root panel
            JPanel rootPanel = new JPanel(new BorderLayout(), true);
                rootPanel.add(pnlTop, BorderLayout.NORTH);
                rootPanel.add(new JScrollPane(previewContainer), BorderLayout.CENTER);
            add(rootPanel);

            // Allow the dialog to be closed by pressing escape
            getRootPane().registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent evt)
                { dispose(); }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

            // Change the window decoration if the application is run as an applet
            if(GUIMain.instance.isApplet()) {
                dispose();
                getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
                setUndecorated(true);
            }

            // Resize and position the dialog
            Dimension refSize = GUIMain.instance.getRootFrame().getSize();
            if(GUIMain.instance.isApplet()) {
                Point refPos = GUIMain.instance.getRootFrame().getLocation();
                setSize(refSize.width - 40, refSize.height - 40);
                setLocation(refPos.x + 20, refPos.y + 20);
            }
            else {
                setSize(refSize.width, refSize.height);
                setLocation(0, 0);
            }
        }
        catch(Exception e) {
            GUIMain.instance.showDefaultCursor();
            throw e;    
        }

        // Show default cursor
        GUIMain.instance.showDefaultCursor();

        // Show the dialog
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setModal(true);
        setVisible(true);
    }

    // The thread procedure
    public void run() {
        // Get the scale
        int scale = getDisplayScale();

        // Determine the preview size
        width  = (int) (wPage * scale / 100);
        height = (int) (hPage * scale / 100);

        // Rescale the preview
        Component[] comps = previewContainer.getComponents();
        for(int i = 0; i < comps.length; ++i) {
            if(comps[i] instanceof PreviewCell) ((PreviewCell) comps[i]).setScaledSize(width, height);
        }
        previewContainer.doLayout();
        previewContainer.getParent().getParent().validate();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Internal preview-grid class
    class PreviewGrid extends JPanel
    {
        private static final int H_GAP = 16;
        private static final int V_GAP = 10;

        public void doLayout()
        {
            Insets ins = getInsets();
            int x = ins.left + H_GAP;
            int y = ins.top  + V_GAP;

            int n = getComponentCount();
            if(n == 0) return;

            Component comp = getComponent(0);
            Dimension dc   = comp.getPreferredSize();
            int       w    = dc.width;
            int       h    = dc.height;

            Dimension dp   = getParent().getSize();
            int       nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
            int       nRow = n / nCol;
            if(nRow * nCol < n) nRow++;

            int index = 0;
            for(int r = 0; r < nRow; ++r) {
                for(int c = 0; c < nCol; ++c) {
                    if(index >= n) return;
                    comp = getComponent(index++);
                    comp.setBounds(x, y, w, h);
                    x += w+H_GAP;
                }
                y += (h + V_GAP);
                x  = ins.left + H_GAP;
            }
        }

        public Dimension getPreferredSize()
        {
            int n = getComponentCount();
            if(n == 0) return new Dimension(H_GAP, V_GAP);
            
            Component comp = getComponent(0);
            Dimension dc   = comp.getPreferredSize();
            int       w    = dc.width;
            int       h    = dc.height;

            Dimension dp   = getParent().getSize();
            int       nCol = Math.max((dp.width - H_GAP) / (w + H_GAP), 1);
            int       nRow = n / nCol;
            if(nRow * nCol < n) nRow++;

            int ww = nCol * (w + H_GAP) + H_GAP;
            int hh = nRow * (h + V_GAP) + V_GAP;

            Insets ins = getInsets();
            return new Dimension(ww + ins.left + ins.right, hh + ins.top + ins.bottom);
        }
        
        public Dimension getMaximumSize() { return getPreferredSize(); }
        
        public Dimension getMinimumSize() { return getPreferredSize(); }

    }

    // Internal preview-cell class
    class PreviewCell extends JPanel
    {
        private Image orgImage;
        private Image sclImage;
        private int   sclW;
        private int   sclH;

        public PreviewCell(int w, int h, Image source)
        {
            // Set background and border
            setBackground(Color.white);
            setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 1, Color.gray),
                new MatteBorder(1, 1, 1, 1, Color.black)
            ));

            // Copy the original image and create the scaled version of the image for the first time
            orgImage = source;
            setScaledSize(w, h);
        }

        public void setScaledSize(int w, int h)
        {
            sclW = w;
            sclH = h;

            BufferedImage bi = new BufferedImage(sclW, sclH, BufferedImage.TYPE_INT_RGB);
            Graphics2D   g2d = bi.createGraphics();
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
            g2d.drawImage(
                orgImage,
                0, 0, sclW, sclH,
                null
            );
            g2d.dispose();

            sclImage = bi;
        }

        public Dimension getPreferredSize()
        {
            Insets ins = getInsets();
            return new Dimension(sclW + ins.left + ins.right,  sclH + ins.top + ins.bottom);
        }
        
        public Dimension getMaximumSize() { return getPreferredSize(); }
        
        public Dimension getMinimumSize() { return getPreferredSize(); }

        public void paint(Graphics g)
        {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(sclImage, 0, 0, this);
            paintBorder(g);
        }
    }

}
