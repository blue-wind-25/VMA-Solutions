/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A class for displaying a multi-file-selector box
//
public class MultiFileSelectorBox extends JDialog implements ActionListener {
    // Data and controls
    private String[]     _filePaths   = null;
    private JFileChooser _fileChooser = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Private constructor
    private MultiFileSelectorBox(Frame parentFrame, String[] fileFilterDesc, String[] fileFilterExt)
    {
        super(parentFrame);
        
        // Initialize the file chooser
        _fileChooser = new JFileChooser(".");
        _fileChooser.setControlButtonsAreShown(true);
        _fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        _fileChooser.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _fileChooser.removeChoosableFileFilter(_fileChooser.getChoosableFileFilters()[0]);
        _fileChooser.setMultiSelectionEnabled(true);
        for(int i = 0; i < fileFilterDesc.length; ++i) {
            final String desc = fileFilterDesc[i];
            final String ext  = fileFilterExt [i];
            _fileChooser.addChoosableFileFilter(new FileFilter(){
                public String getDescription()
                { return desc + " (*." + ext + ")"; }

                public boolean accept(File file) {
                    if(ext == "*" || file.isDirectory()) return true;
                    String path = file.getAbsolutePath().toLowerCase();
                    return (path.endsWith(ext.toLowerCase()) && (path.charAt(path.length() - ext.length() - 1)) == '.');
                }
            });
        }
        _fileChooser.addActionListener(this);

        // Create the dialog
        JPanel             mainPanel = new JPanel(new GridBagLayout(), true);
        GridBagConstraints gbc       = new GridBagConstraints();
        gbc.fill       = GridBagConstraints.BOTH;
        gbc.gridwidth  = 1;
        gbc.gridheight = 1;
        gbc.gridx      = 0;
        GUtil.addWithGbc(mainPanel, gbc, _fileChooser);
        add(mainPanel);
       
        // Ensure that the dialog size is calculated
        pack();

        // Center the dialog relative to its parent
        Dimension dialogSize = getSize();
        Dimension parentSize = parentFrame.getSize();
        Point     parentPos  = parentFrame.getLocation();
        setLocation((parentSize.width - dialogSize.width) / 2 + parentPos.x, (parentSize.height - dialogSize.height) / 2 + parentPos.y);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Show a file open box
    public static String[] showFileOpenDialog(Frame parentFrame, String title, String[] fileFilterDesc, String[] fileFilterExt)
    {
        MultiFileSelectorBox mfsb = new MultiFileSelectorBox(parentFrame, fileFilterDesc, fileFilterExt);
        GUtil.showModalDialog(mfsb, null, title, JRootPane.FILE_CHOOSER_DIALOG);
        return mfsb._filePaths;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for file chooser
    public void actionPerformed(ActionEvent event)
    {
        if(JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())) {
            // Get the selected files 
            String desc  = _fileChooser.getFileFilter().getDescription();
            int    dpos  = desc.lastIndexOf(".");
            String ext   = (desc.charAt(dpos + 1) == '*') ? null : desc.substring(dpos, desc.length() - 1);
            File[] files = _fileChooser.getSelectedFiles();
            // Ensure that the files have the correct extension
            if(files != null) {
                _filePaths = new String[files.length];
                for(int i = 0; i < files.length; ++i) {
                    _filePaths[i] = files[i].toString();
                    if(ext != null && !_filePaths[i].toLowerCase().endsWith(ext.toLowerCase())) _filePaths[i] += ext;
                    if(files[i].toString() != _filePaths[i]) files[i] = new File(_filePaths[i]);
                    // Check if the file does exist
                    if(!files[i].exists()) {
                        Object ancst = SwingUtilities.getWindowAncestor(this);
                        Frame  frame = (ancst instanceof GUIMain) ? ((GUIMain) ancst).getRootFrame() : (Frame) ancst;
                        GUtil.showInformationDialog(frame, _S("dlg_fsel_info_fne"));
                        return;
                    }
                }
            }
            // Close the dialog
            this.setVisible(false);
        }
        else if (JFileChooser.CANCEL_SELECTION.equals(event.getActionCommand())) {
            _filePaths = null;
            this.setVisible(false);
        }
    }
}
