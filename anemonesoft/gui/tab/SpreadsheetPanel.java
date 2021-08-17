/*
    Copyright (C) 2010-2021 Aloysius Indrayanto
                            VMA Consultant
*/

package anemonesoft.gui.tab;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.print.*;
import java.io.*;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import anemonesoft.gui.*;
import anemonesoft.gui.component.*;
import anemonesoft.gui.control.*;
import anemonesoft.gui.dialog.*;
import anemonesoft.i18n.*;
import anemonesoft.plot.*;

//
// Tab module - spreadsheet panel
//
public class SpreadsheetPanel extends JPanel implements Saveable {
    // Version of the panel interface
    public static final int INTERFACE_VERSION = 3;

    // Maximum number of rows and columns
    public static final int SS_NUM_ROWS = 999;
    public static final int SS_NUM_COLS =  26;

    // Data type
    public static final byte DT_NULL   = 0;
    public static final byte DT_DOUBLE = 1;
    public static final byte DT_STRING = 2;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // The table
    private Spreadsheet _tblInput = new Spreadsheet(SS_NUM_ROWS, SS_NUM_COLS);

    // Shortcut for obtaining an i18n string
    private static String _S(String s)
    { return StringTranslator.getString(s); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Selection-area class
    public static class SelectionArea {
        public int      startRow, endRow;
        public int      startCol, endCol;
        public String[] header;

        public SelectionArea()
        { startRow = -1; startCol = -1; endRow = -1; endCol =-1; header = null; }

        public SelectionArea(int sr, int sc, int er, int ec, String[] hd)
        { startRow = sr; startCol = sc; endRow = er; endCol = ec; header = hd; }

        public boolean isValid()
        { return startRow >= 0 && startCol >= 0 && endRow >= 0 && endCol >= 0; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Return the interface version
    public int interfaceVersion()
    { return INTERFACE_VERSION; }

    // Save data to the given stream
    public void save(DataOutputStream ds) throws Exception
    {
        TableModel tableModel =_tblInput.getModel();
        int        numRows    = tableModel.getRowCount();
        int        numCols    = tableModel.getColumnCount();

        ds.writeInt(numRows);
        ds.writeInt(numCols);

        for(int r = 0; r < numRows; ++r) { /** Always save in spreadsheet interface version 2 */
            for(int c = 0; c < numCols; ++c) {
                String sval = (String) tableModel.getValueAt(r, c);
                // Is it a null?
                if(sval == null) {
                    ds.writeByte(DT_NULL);
                    continue;
                }
                // Try as a double value
                try {
                    double dval = Double.parseDouble(sval);
                    ds.writeByte(DT_DOUBLE);
                    ds.writeDouble(dval);
                }
                // It is a string value
                catch(Exception e) {
                    ds.writeByte(DT_STRING);
                    ds.writeUTF(sval);
                }
            }
        }

        /** Available from interface version 3 */
        TableColumnModel colModel =_tblInput.getColumnModel();
        for(int i = 0; i < numCols; ++i) {
            TableColumn col = colModel.getColumn(i);
            ds.writeInt(col.getPreferredWidth()); // The default is 75
        }
    }

    // Load data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception
    {
        if(interfaceVersion > INTERFACE_VERSION) return false;

        TableModel tableModel =_tblInput.getModel();
        int        numRows    = tableModel.getRowCount();
        int        numCols    = tableModel.getColumnCount();

        int dnr = ds.readInt();
        int dnc = ds.readInt();
        if(dnr != numRows || dnc != numCols) return false;

        if(interfaceVersion >= 2) { /** Spreadsheet interface version 2 */
            for(int r = 0; r < numRows; ++r) {
                for(int c = 0; c < numCols; ++c) {
                    switch(ds.readByte()) {
                        case DT_NULL :
                            tableModel.setValueAt("", r, c);
                            break;
                        case DT_DOUBLE:
                            Double d = ds.readDouble();
                            tableModel.setValueAt(d.toString(), r, c);
                            break;
                        case DT_STRING:
                            tableModel.setValueAt(ds.readUTF(), r, c);
                            break;
                    }
                }
            }
        }
        else { /** Spreadsheet interface version 1 */
            for(int r = 0; r < numRows; ++r) {
                for(int c = 0; c < numCols; ++c) {
                    Double d = ds.readDouble();
                    if(!Double.isNaN(d)) tableModel.setValueAt(d.toString(), r, c);
                    else                 tableModel.setValueAt("", r, c);
                }
            }
        }

        /** Available from interface version 3 */
        if(interfaceVersion >= 3) {
            TableColumnModel colModel =_tblInput.getColumnModel();
            for(int i = 0; i < numCols; ++i) {
                TableColumn col = colModel.getColumn(i);
                col.setPreferredWidth(ds.readInt());
            }
        }

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a panel with a spreadsheet
    public SpreadsheetPanel()
    {
        super(new BorderLayout(), true);

        // Initialize the table
        JScrollPane scrollPane = new JScrollPane(_tblInput);
            scrollPane.setRowHeaderView(new LineNumberTable(_tblInput));
            add(scrollPane, BorderLayout.CENTER);

        _tblInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        _tblInput.setCellSelectionEnabled(true);
        _tblInput.getTableHeader().setReorderingAllowed(false);

        _tblInput.setRowSelectionInterval(0, 0);
        _tblInput.setColumnSelectionInterval(0, 0);
    }

    // Cut the selected cell to clipboard
    public void cutSelectedCellsToClipboard()
    { _tblInput.copySelectedCellsToClipboard(true); }

    // Copy the selected cell to clipboard
    public void copySelectedCellsToClipboard()
    { _tblInput.copySelectedCellsToClipboard(false); }

    // Paste from clipboard to the cells
    public void pasteClipboardToCells()
    { _tblInput.pasteClipboardToCells(); }

    // Paste from string data to the cells
    public void pasteToCells(String strData)
    { _tblInput.pasteToCells(strData); }

    // Clear the selected cells
    public void clearSelectedCells()
    { _tblInput.clearSelectedCells(); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Get the selected area (the header, if any, will be excluded from the selection area and stored as an array of strings)
    public final SelectionArea getSelectionArea()
    {
        // Get the selection area
        int sRow = _tblInput.getSelectedRow();
        int sCol = _tblInput.getSelectedColumn();
        int eRow = _tblInput.getSelectionModel().getMaxSelectionIndex();
        int eCol = _tblInput.getColumnModel().getSelectionModel().getMaxSelectionIndex();

        // Check if the first column is a header (all cells contain comments)
        boolean  frIsHeader = true;
        String[] strHeader  = new String[eCol - sCol + 1];
        for(int c = sCol; c <= eCol; ++c) {
            String val = (String) _tblInput.getModel().getValueAt(sRow, c);
            try {
                // If any of the column contains data, then assume the whole row as a non-header row
                Double.parseDouble(val);
                frIsHeader = false;
                break;
            }
            catch(Exception e) {
                // Store the header's cell address
                strHeader[c - sCol] = String.format("$[%c%d]", c + 65, sRow + 1);
            }
        }

        // If all of the columns contain comment, then assume the whole row as a header row
        // (hence, exclude the header row from the selection area)
        if(frIsHeader) ++sRow;

        // Return the selection area
        return new SelectionArea(sRow, sCol, eRow, eCol, frIsHeader ? strHeader : null);
    }

    // Get the values of the selected cells (the values are returned as a two-dimensional array of doubles)
    public double[][] getSelectedValues(final SelectionArea sa)
    {
        // Check for invalid selection area
        if(sa.startRow < 0 || sa.endRow < 0 || sa.startCol < 0 || sa.endCol < 0) return null;

        // Array of values
        double[][] valArray = new double[sa.endRow - sa.startRow + 1][sa.endCol - sa.startCol + 1];

        // Get the cell values
        TableModel tableModel =_tblInput.getModel();
        for(int r = sa.startRow; r <= sa.endRow; ++r) {
            for(int c = sa.startCol; c <= sa.endCol; ++c) {
                valArray[r - sa.startRow][c - sa.startCol] = GUtil.str2d((String) tableModel.getValueAt(r, c));
            }
        }

        // Return the array
        return valArray;
    }

    // Get the values of the cells in the give column and row-range (the values are returned as a one-dimensional array of doubles)
    private static Runnable        _errTDsp = null;
    private static long            _errTime = 0;
    private static List<Character> _errCols = new ArrayList<>();

    public double[] getColValues(int col, int startRow, int endRow)
    {
        // Check for invalid area
        if(col < 0 || startRow < 0 || endRow < 0) return null;

        // Array of values
        double[] valArray = new double[endRow - startRow + 1];

        // Get the cell values
        TableModel tableModel =_tblInput.getModel();
        boolean    invData    = false;
        for(int r = startRow; r <= endRow; ++r) {
            String str = (String) tableModel.getValueAt(r, col);
            try {
                valArray[r - startRow] = Double.parseDouble(str);
            }
            catch(Exception e) {
                valArray[r - startRow] = 0.0;
                invData                = true;
            }
        }

        // Qeueue warning message(s) as needed
        _errTime = System.currentTimeMillis();

        if(invData) {
            // Get the column
            char curInvalidCol = (char) ('A' + col);
            if(!_errCols.contains(curInvalidCol)) {
                _errCols.add(Character.valueOf(curInvalidCol));
            }
            // Create the message displayer thread as needed
            if(_errTDsp == null) {
                _errTDsp = new Runnable() {
                    public void run() {
                        while(System.currentTimeMillis() - _errTime <= 250);
                        Collections.sort(_errCols);
                        for(Character c : _errCols) {
                            GUtil.showInformationDialog(
                                GUIMain.instance.getRootFrame(),
                                StringTranslator.format(_S("err_cell_data_invalid"), c)
                            );
                        }
                        _errCols.clear();
                        _errTDsp = null;
                    }
                };
                SwingUtilities.invokeLater(_errTDsp);
            }
        }

        // Return the array
        return valArray;
    }

    // Get the value of the cell in the given row and column (the value is returned as a double)
    public double getValueAt(int r, int c)
    { return GUtil.str2d((String) _tblInput.getModel().getValueAt(r, c)); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Get the raw selected area (the header, if any, will be included in the selection area)
    public final SelectionArea getRawSelectionArea()
    {
        // Get the selection area
        int sRow = _tblInput.getSelectedRow();
        int sCol = _tblInput.getSelectedColumn();
        int eRow = _tblInput.getSelectionModel().getMaxSelectionIndex();
        int eCol = _tblInput.getColumnModel().getSelectionModel().getMaxSelectionIndex();

        // Return the selection area
        return new SelectionArea(sRow, sCol, eRow, eCol, null);
    }

    // Get the raw values of the selected cells (the values are returned as a two-dimensional array of strings)
    public String[][] getRawSelectedValues(final SelectionArea sa)
    {
        // Check for invalid selection area
        if(sa.startRow < 0 || sa.endRow < 0 || sa.startCol < 0 || sa.endCol < 0) return null;

        // Array of values
        String[][] valArray = new String[sa.endRow - sa.startRow + 1][sa.endCol - sa.startCol + 1];

        // Get the cell values
        TableModel tableModel =_tblInput.getModel();
        for(int r = sa.startRow; r <= sa.endRow; ++r) {
            for(int c = sa.startCol; c <= sa.endCol; ++c) {
                valArray[r - sa.startRow][c - sa.startCol] = (String) tableModel.getValueAt(r, c);
            }
        }

        // Return the array
        return valArray;
    }

    // Get the raw value of a cell by its string address (the value is returned as a string)
    public String getRawValueByAddress(String addr)
    {
        // Too short, simply return back the address
        if(addr.length() < 5) return addr;

        // Non a calid address, simply return back the address
        if(addr.charAt(0) != '$' || addr.charAt(1) != '[' || addr.charAt(addr.length() - 1) != ']') return addr;

        // Extract the column and the row
        int col = addr.toUpperCase().codePointAt(2) - 65;
        int row = -1;
        try {
            row = Integer.parseInt(addr.substring(3, addr.length() - 1)) - 1;
        }
        catch(Exception e) {}

        // Invalid column and/row row, just return an empty string
        if(col < 0 || col > 25 || row < 0 || row >= 999) return "";

        // Return the cell's value
        return (String) _tblInput.getModel().getValueAt(row, col);
    }

    // Print the selected cells
    public void printSelectedCells()
    {
        // Get the data
        String[][] g = getRawSelectedValues(getRawSelectionArea());

        // Print grid
        try {
            new PrintPreview(GUIMain.instance.getRootFrame(), new PrintGrid(g));
        }
        catch(Exception e) {}

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Internal print-grid class
    private class PrintGrid implements Printable {
        private String[][]  _gridData     = null;
        private int         _biWidth      = 0;
        private int         _biHeight     = 0;
        private int         _fontSize     = 0;
        private Font        _font         = null;
        private FontMetrics _fontMetrics  = null;
        private int         _yInc         = 0;
        private int         _linesPerPage = 0;
        private int[]       _colLength    = null;
        private int[][]     _colOffset    = null;
        private int         _colSepLength = 0;

        public PrintGrid(String[][] g) throws Exception
        {
            // Copy the grid data and allocate memory for the columns' lengths and offsets
            _gridData  = g;
            _colLength = new int[g[0].length];
            _colOffset = new int[g.length][g[0].length];

            // Find the maximum number of decimal-points for each column
            int[][] numOfDec    = new int[g.length][g[0].length];
            int[]   numOfDecMax = new int[g[0].length];
            for(int c = 0; c < _colLength.length; ++c) {
                numOfDecMax[c] = 0;
                for(int r = 0; r < _gridData.length; ++r) {
                    String txt = _gridData[r][c];
                    try { // Data
                        Double dval = Double.parseDouble(txt);
                        numOfDec[r][c] = txt.length() - txt.indexOf('.') - 1;
                    }
                    catch(Exception e) { // Comments
                        numOfDec[r][c] = -1;
                    }
                    if(numOfDec[r][c] > numOfDecMax[c]) numOfDecMax[c] = numOfDec[r][c];
                }
            }

            // Pad the column data with space(s) as needed to align the decimal-points
            for(int c = 0; c < _colLength.length; ++c) {
                if(numOfDecMax[c] <= 0) continue;
                for(int r = 0; r < _gridData.length; ++r) {
                    if(numOfDec[r][c] < 0) continue;
                    int nodDelta = numOfDecMax[c] - numOfDec[r][c];
                    for(int p = 0; p < nodDelta; ++p) _gridData[r][c] += ' ';
                }
            }
        }

        public int print(Graphics g, PageFormat pf, int page) throws PrinterException
        {
            // Determine the size of the image
            if(_biWidth <= 0 || _biHeight <= 0) {
                _biWidth  = (int) pf.getImageableWidth () * 4;
                _biHeight = (int) pf.getImageableHeight() * 4;
                _fontSize = (int) (7.7 * 4);
            }

            // Create a buffered image and a graphics context for printing the report
            BufferedImage imgGrid = (BufferedImage) createImage(_biWidth, _biHeight);
            Graphics2D    grpGrid = imgGrid.createGraphics();

            // Calculate the font metrics, Y-increment, number of lines per page, column length, column offset, and separator length
            if(_font == null) {
                // Normal font
                _font = new Font(GUtil.getSysFontName("Monospaced"), Font.BOLD, _fontSize);
                grpGrid.setFont(_font);
                _fontMetrics = grpGrid.getFontMetrics();
                // Calculate the Y increment and the number of lines per page
                _yInc         = (_fontMetrics.getAscent() + _fontMetrics.getDescent());
                _linesPerPage = (_biHeight - _yInc) / _yInc;
                // Calculate the column length
                for(int c = 0; c < _colLength.length; ++c) {
                    for(int r = 0; r < _gridData.length; ++r) {
                        _colOffset[r][c] = _fontMetrics.stringWidth(_gridData[r][c]);
                        if(_colOffset[r][c] > _colLength[c]) _colLength[c] = _colOffset[r][c];
                    }
                }
                // Calculate the column offset
                for(int c = 0; c < _colLength.length; ++c) {
                    for(int r = 0; r < _gridData.length; ++r) {
                        String txt = _gridData[r][c];
                        try { // Data - right aligned
                            Double dval = Double.parseDouble(txt);
                            _colOffset[r][c] = _colLength[c] - _colOffset[r][c];
                        }
                        catch(Exception e) { // Comments - left aligned
                            _colOffset[r][c] = 0;
                        }
                    }
                }
                // Calculate the separator length
                _colSepLength = _fontMetrics.stringWidth("WWW");
            }

            // Determine the starting row index and check if there is no more lines left
            int rowIdx = page * _linesPerPage;
            if(rowIdx >= _gridData.length) {
                grpGrid.dispose();
                return NO_SUCH_PAGE;
            }

            // Set the quality, color, and _font
            grpGrid.setRenderingHints(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,   RenderingHints.VALUE_COLOR_RENDER_QUALITY));
            grpGrid.setRenderingHints(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON));
            grpGrid.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY      ));
            grpGrid.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON   ));
            grpGrid.setColor(new Color(0, 0, 0));
            grpGrid.setFont(_font);

            // Print the report to the buffered image
            int y    = _yInc;
            int lCnt = 0;
            while(rowIdx < _gridData.length) {
                int x = 0;
                for(int c = 0; c < _colLength.length; ++c) {
                    grpGrid.drawString(_gridData[rowIdx][c], x + _colOffset[rowIdx][c], y);
                    x += (_colLength[c] + _colSepLength);
                }
                y += _yInc;
                ++rowIdx;
                if(++lCnt >= _linesPerPage) break;
            }
            grpGrid.dispose();

            // Blit the buffered image to the printer's graphics context
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC));
            g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY       ));
            g2d.drawImage(
                imgGrid,
                (int) pf.getImageableX(), (int) pf.getImageableY(), (int) (pf.getImageableX() + pf.getImageableWidth()), (int) (pf.getImageableY() + pf.getImageableHeight()),
                0, 0, _biWidth, _biHeight,
                new Color(0, 0, 0, 0),
                null
            );
            g2d.dispose();

            // Done
            return PAGE_EXISTS;
        }
    }

}
