/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import javax.swing.table.*;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A simple spreadhseet
//
public class Spreadsheet extends JTable implements ActionListener {
    // Data
    private Clipboard _clipboard;

    // Controls
    private JMenuItem _mnuSSheetCut   = null;
    private JMenuItem _mnuSSheetCopy  = null;
    private JMenuItem _mnuSSheetPaste = null;
    private JMenuItem _mnuSSheetClear = null;

    // Shortcut for obtaining i18n string
    private static String _S(String str)
    { return StringTranslator.getString(str); }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Double-value table-cell renderer
    public class DValTableCellRenderer extends JLabel implements TableCellRenderer {

        private Border _bdrNormal = null;
        private Border _bdrFocus  = null;

        DValTableCellRenderer()
        {
            super((String) null, JLabel.LEFT);
            setOpaque(true);

            _bdrNormal = BorderFactory.createEmptyBorder(0, 0, 0, 0);
            _bdrFocus  = BorderFactory.createLineBorder(getForeground());

            Font font = getFont();
            setFont(font.deriveFont(Font.PLAIN, font.getSize()));
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int colIndex)
        {
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setBorder(hasFocus ? _bdrFocus : _bdrNormal);

            String sval = (value == null) ? null : value.toString();
            try {
                Double dval = Double.parseDouble(sval);
                setText(dval.toString());
                setHorizontalAlignment(JLabel.RIGHT);
            }
            catch(Exception e) {
                setText(sval);
                setHorizontalAlignment(JLabel.LEFT);
            }
            
            return this;
        }

        public void validate()
        {}

        public void revalidate()
        {}

        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue)
        {}

        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
        {}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a spreadhseet
    public Spreadsheet(int numRows, int numCols)
    {
        super(new DefaultTableModel(numRows, numCols) {
            public Class<?> getColumnClass(int column)
            { return String.class; }
        });
        setDefaultRenderer(String.class, new DValTableCellRenderer());

        // Disable table tooltip
        ToolTipManager.sharedInstance().unregisterComponent(this);
        ToolTipManager.sharedInstance().unregisterComponent(getTableHeader());

        // Make deleting cell contents easier
        getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("DELETE"), "del_cell");
        getActionMap().put("del_cell", new AbstractAction() {
            public void actionPerformed(ActionEvent event)
            { getModel().setValueAt(null, getSelectedRow(), getSelectedColumn()); }
        });

        // Get a reference to the system's clipboard
        _clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Enable copy, cut, paste, and clear via keyboard short-cuts
        registerKeyboardAction(this, "Copy",  KeyStroke.getKeyStroke(KeyEvent.VK_C,      ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        registerKeyboardAction(this, "Cut",   KeyStroke.getKeyStroke(KeyEvent.VK_X,      ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        registerKeyboardAction(this, "Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V,      ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);
        registerKeyboardAction(this, "Clear", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.CTRL_MASK, false), JComponent.WHEN_FOCUSED);

        // Enable copy, cut, paste, and clear via right mouse click
        final JPopupMenu mnuPopUp = new JPopupMenu();
            _mnuSSheetCut    = new JMenuItem(_S("mnu_ssheet_cut"   ), GUtil.newImageIcon("mnu_edit_cut"  ));
            _mnuSSheetCopy   = new JMenuItem(_S("mnu_ssheet_copy"  ), GUtil.newImageIcon("mnu_edit_copy" ));
            _mnuSSheetPaste  = new JMenuItem(_S("mnu_ssheet_paste" ), GUtil.newImageIcon("mnu_edit_paste"));
            _mnuSSheetClear  = new JMenuItem(_S("mnu_ssheet_clear" ), GUtil.newImageIcon("mnu_edit_clear"));
            mnuPopUp.add(_mnuSSheetCut  ); _mnuSSheetCut  .addActionListener(this);
            mnuPopUp.add(_mnuSSheetCopy ); _mnuSSheetCopy .addActionListener(this);
            mnuPopUp.add(_mnuSSheetPaste); _mnuSSheetPaste.addActionListener(this);
            mnuPopUp.add(_mnuSSheetClear); _mnuSSheetClear.addActionListener(this);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e)
                {
                    // Is it right mouse click
                    if(!SwingUtilities.isRightMouseButton(e)) return;

                    // Check if the mouse is in selection area
                    int row = rowAtPoint   (e.getPoint());
                    int col = columnAtPoint(e.getPoint());
                    if(!isRowSelected(row) || !isColumnSelected(col)) return;

                    // Show the pop-up menu
                    mnuPopUp.show(e.getComponent(), e.getX(), e.getY());
                }
            });
    }

    // Define the behavior when a cell is to be edited
    public boolean editCellAt(int row, int column, EventObject e)
    {
        boolean result = super.editCellAt(row, column, e);

        final Component editor = getEditorComponent();

        if(editor != null && (editor instanceof JTextComponent)) {
            // Not from any event
            if(e == null) {
                ((JTextComponent) editor).selectAll();
            }
            // Typing in the cell was used to activate the editor
            else if(e instanceof KeyEvent) {
                int mod = ((KeyEvent) e).getModifiersEx();
                if(mod !=0 && mod != KeyEvent.SHIFT_DOWN_MASK) {
                    getCellEditor().stopCellEditing();
                    return false;
                }
                ((JTextComponent) editor).selectAll();
            }
            // F2 was used to activate the editor
            else if(e instanceof ActionEvent) {
                // Nothing to do here
            }
            // A mouse click was used to activate the editor
            else if(e instanceof MouseEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    { ((JTextComponent) editor).selectAll(); }
                });
            }
        }

        return result;
    }

    // Copy/cut the selected cell to clipboard
    public void copySelectedCellsToClipboard(boolean cut)
    {
        // Get and check the start and end positions of the selected cells
        int sr = getSelectedRow();
        int sc = getSelectedColumn();
        int er = getSelectionModel().getMaxSelectionIndex();
        int ec = getColumnModel().getSelectionModel().getMaxSelectionIndex();
        if(sr < 0 || sc < 0 || er < 0 || ec < 0) return;

        // Convert the selected values into string
        TableModel    tm = getModel();
        StringBuilder sb = new StringBuilder();
        for(int r = sr; r <= er; ++r) {
            for(int c = sc; c <= ec; ++c) {
                String valObj = (String) tm.getValueAt(r, c);
                if(cut) tm.setValueAt(null, r, c);
                sb.append((valObj == null) ? " " : valObj);
                if(c < ec) sb.append("\t");
            }
            if(r < er) sb.append("\n");
        }

        // Store the string into clipboard
        StringSelection ss = new StringSelection(sb.toString());
        _clipboard.setContents(ss, ss);
    }

    // Paste from clipboard to the cells
    public void pasteClipboardToCells()
    {
        try {
            String strData = (String) (_clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
            pasteToCells(strData);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Paste from string data to the cells
    public void pasteToCells(String strData)
    {
        // Get and check the start and end positions of the selected cells
        int sr = getSelectedRow();
        int sc = getSelectedColumn();
        int er = getSelectionModel().getMaxSelectionIndex();
        int ec = getColumnModel().getSelectionModel().getMaxSelectionIndex();
        if(sr < 0 || sc < 0 || er < 0 || ec < 0) return;

        // Empty the selected cells
        TableModel tm = getModel();
        for(int r = sr; r <= er; ++r) {
            for(int c = sc; c <= ec; ++c) {
                tm.setValueAt(null, r, c);
            }
        }

        // Process the rows
        StringTokenizer rst = new StringTokenizer(strData, "\n", true);
        String          prr = "";
        int             r = sr;
        while(rst.hasMoreTokens()) {
            // Stop if we have filled the last row
            if(r >= getRowCount()) break;
            // Skip the delimiter
            String curRow = rst.nextToken();
            if(curRow.equals("\n")) {
                if(prr.equals("\n")) ++r;
                prr = curRow;
                continue;
            }
            prr = curRow;
            // Process the columns
            StringTokenizer cst = new StringTokenizer(curRow, "\t", true);
            String          prc = "";
            int             c   = sc;
            while(cst.hasMoreTokens()) {
                // Stop if we have filled the last column
                if(c >= getColumnCount()) break;
                // Skip the delimiter
                String curCol = cst.nextToken();
                if(curCol.equals("\t")) {
                    if(prc.equals("\t")) ++c;
                    prc = curCol;
                    continue;
                }
                prc = curCol;
                // Set the value
                try {
                    tm.setValueAt(curCol, r, c);
                }
                catch(Exception e) {
                    tm.setValueAt(null, r, c);
                }
                // Increment the column index
                ++c;
            }
            // Increment the row index
            ++r;
        }
    }

    // Clear the selected cells
    public void clearSelectedCells()
    {
        // Get and check the start and end positions of the selected cells
        int sr = getSelectedRow();
        int sc = getSelectedColumn();
        int er = getSelectionModel().getMaxSelectionIndex();
        int ec = getColumnModel().getSelectionModel().getMaxSelectionIndex();
        if(sr < 0 || sc < 0 || er < 0 || ec < 0) return;

        // Empty the cells
        TableModel tm = getModel();
        for(int r = sr; r <= er; ++r) {
            for(int c = sc; c <= ec; ++c) {
                tm.setValueAt(null, r, c);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Event handler for copy, cut, paste, and clear
    public void actionPerformed(ActionEvent event)
    {
        // Get the component that fired the event
        JComponent eventSource = (JComponent) event.getSource();

        // Process pop-up menu
        if(eventSource == _mnuSSheetCut) {
            copySelectedCellsToClipboard(true);
            return;
        }
        else if(eventSource == _mnuSSheetCopy) {
            copySelectedCellsToClipboard(false);
            return;
        }
        else if(eventSource == _mnuSSheetPaste) {
            pasteClipboardToCells();
            return;
        }
        else if(eventSource == _mnuSSheetClear) {
            clearSelectedCells();
            return;
        }

        // Process keyboard short-cuts
        boolean copy = (event.getActionCommand().compareTo("Copy") == 0);
        boolean cut  = (event.getActionCommand().compareTo("Cut" ) == 0);

        if(copy || cut)
            copySelectedCellsToClipboard(cut);
        else if(event.getActionCommand().compareTo("Paste") == 0)
            pasteClipboardToCells();
        else if(event.getActionCommand().compareTo("Clear") == 0)
            clearSelectedCells();
    }
}
