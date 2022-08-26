/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
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
// A class for displaying a file-selector box
//
public class FileSelectorBox extends JDialog implements ActionListener {
    // Data and controls
    private int          _mode        = 0;
    private String       _filePath    = null;
    private JFileChooser _fileChooser = null;

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    // Private constructor
    private FileSelectorBox(Frame parentFrame, String[] fileFilterDesc, String[] fileFilterExt, int mode)
    {
        super(parentFrame);
        _mode = mode;
        
        // Initialize the file chooser
        _fileChooser = new JFileChooser(".");
        _fileChooser.setControlButtonsAreShown(true);
        _fileChooser.setDialogType((mode == 0) ? JFileChooser.OPEN_DIALOG : JFileChooser.SAVE_DIALOG);
        _fileChooser.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        _fileChooser.removeChoosableFileFilter(_fileChooser.getChoosableFileFilters()[0]);
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
    public static String showFileOpenDialog(Frame parentFrame, String title, String[] fileFilterDesc, String[] fileFilterExt)
    {
        FileSelectorBox fsb = new FileSelectorBox(parentFrame, fileFilterDesc, fileFilterExt, 0);
        GUtil.showModalDialog(fsb, null, title, JRootPane.FILE_CHOOSER_DIALOG);
        return fsb._filePath;
    }

    // Show a file save box
    public static String showFileSaveDialog(Frame parentFrame, String title, String[] fileFilterDesc, String[] fileFilterExt)
    {
        FileSelectorBox fsb = new FileSelectorBox(parentFrame, fileFilterDesc, fileFilterExt, 1);
        GUtil.showModalDialog(fsb, null, title, JRootPane.FILE_CHOOSER_DIALOG);
        return fsb._filePath;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for file chooser
    public void actionPerformed(ActionEvent event)
    {
        if(JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())) {
            // Get the selected file and ensure that it has the correct extension
            String desc = _fileChooser.getFileFilter().getDescription();
            int    dpos = desc.lastIndexOf(".");
            String ext  = (desc.charAt(dpos + 1) == '*') ? null : desc.substring(dpos, desc.length() - 1);
            File   file = _fileChooser.getSelectedFile();
            _filePath = (file == null) ? null : file.toString();
            if(_filePath != null && ext != null && !_filePath.toLowerCase().endsWith(ext.toLowerCase())) _filePath += ext;
            if(file.toString() != _filePath) file = new File(_filePath);
            // Check if the file does exist
            if(_mode == 0) {
                if(!file.exists()) {
                    Object ancst = SwingUtilities.getWindowAncestor(this);
                    Frame  frame = (ancst instanceof GUIMain) ? ((GUIMain) ancst).getRootFrame() : (Frame) ancst;
                    GUtil.showInformationDialog(frame, _S("dlg_fsel_info_fne"));
                    return;
                }
            }
            // Confirm overwrite
            else {
                if(file.exists()) {
                    Object ancst = SwingUtilities.getWindowAncestor(this);
                    Frame  frame = (ancst instanceof GUIMain) ? ((GUIMain) ancst).getRootFrame() : (Frame) ancst;
                    if(!GUtil.showYesNoQuestionDialog(frame, _S("dlg_fsel_conf_ovr"))) return;
                }
            }
            // Close the dialog
            this.setVisible(false);
        }
        else if (JFileChooser.CANCEL_SELECTION.equals(event.getActionCommand())) {
            _filePath = null;
            this.setVisible(false);
        }
    }
}
