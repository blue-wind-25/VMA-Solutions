/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import javax.swing.text.*;

import anemonesoft.i18n.*;

public class SheetRangeDocument extends PlainDocument {
    // Data
    private char _maxCol = 0;
    private int  _maxLen = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public SheetRangeDocument(int maxCol, int maxLen)
    {
        super();
        _maxCol = (char) ('A' + maxCol);
        _maxLen = maxLen;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
    {
        // Is it a null or empty string?
        if(str == null || str.equals("")) return;

        // Make upper-case
        str = str.toUpperCase();
        
        // Is it a multi-character string
        if(str.length() > 1) {
            char chr0 = str.charAt(0);
            if(chr0 < 'A' || chr0 > _maxCol) return;

            char chr1 = str.charAt(1);
            if(!Character.isDigit(chr1)) return;

            if(str.length() <= _maxLen)
                super.insertString(offset, str, attr);
            else {
                char chr2 = str.charAt(1);
                if(!Character.isDigit(chr2)) return;
                super.insertString(offset, str.substring(0, _maxLen), attr);
            }

            return;
        }

        // Is it a valid character?
        char chr = str.charAt(0);
        if(!Character.isDigit(chr) && (chr < 'A' || chr > _maxCol)) return;

        // Do we still within the length limit?
        if(offset >= _maxLen) return;

        // Can we put alphabet in this location?
        if(!Character.isDigit(chr) && offset > 0) return;
        
        // Can we put number in this location?
        if(Character.isDigit(chr) && offset <= 0) return;

        // All OK, add the character to the text box
        super.insertString(offset, str, attr);
    }
}
