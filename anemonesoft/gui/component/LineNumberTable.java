/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

//
// Auxiliary table class to add column and row headers to a table
//
public class LineNumberTable extends JTable  {
    // Reference to the main table object
    private JTable _mainTable;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a line-number table for the given table
    public LineNumberTable(JTable mainTable)
    {
        super();
        _mainTable = mainTable;

        setAutoscrolls(false);
        setAutoCreateColumnsFromModel(false);
        setModel(_mainTable.getModel());
        setSelectionModel(mainTable.getSelectionModel());

        addColumn(new TableColumn());
        getColumnModel().getColumn(0).setCellRenderer(_mainTable.getTableHeader().getDefaultRenderer());
        getColumnModel().getColumn(0).setPreferredWidth(50);

        setPreferredScrollableViewportSize(getPreferredSize());
    }

    // All header cells are not editable
    public boolean isCellEditable(int row, int column)
    { return false; }

    // Return the row number
    public Object getValueAt(int row, int column)
    { return Integer.valueOf(row + 1); }

    // Return the row height
    public int getRowHeight(int row)
    { return _mainTable.getRowHeight(); }
}

