/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import javax.swing.text.*;

import anemonesoft.i18n.*;

public class NumericDocument extends PlainDocument {
    // Decimal separator
    private final String DECIMAL_SEPARATOR = ".";

    // Data
    private int     _decimalPrecision = 0;
    private boolean _allowNegative    = false;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public NumericDocument(int decimalPrecision, boolean allowNegative)
    {
        super();
        _decimalPrecision = decimalPrecision;
        _allowNegative    = allowNegative;
    }

    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
    {
        // Is it a null or empty string?
        if(str == null || str.equals("")) return;

        // Is it a multi-character string
        if(str.length() > 1) {
            try {
                double val = Double.parseDouble(str);
                
                String fmt = "%f";
                     if(_decimalPrecision == 0) fmt = "%.0f";
                else if(_decimalPrecision >  0) fmt = "%." + _decimalPrecision + "f";
                str = StringTranslator.format(fmt, val);

                super.insertString(offset, str, attr);
            }
            catch(Exception e) {}
            return;
        }

        // Is it a valid character?
        if(!Character.isDigit(str.charAt(0)) && !str.equals(DECIMAL_SEPARATOR) && !str.equals("-")) return;

        // Can we put a negative sign in this location?
        if(str.equals("-") && (!_allowNegative || offset > 0)) return;

        // Can we place a decimal separator in this location?
        if(str.equals(DECIMAL_SEPARATOR) && (_decimalPrecision == 0 || super.getText(0, super.getLength()).contains(DECIMAL_SEPARATOR))) return;

        // Do we still within the decimal precision limit?
        if(_decimalPrecision >= 0) {
            final int dsi = super.getText(0, super.getLength()).indexOf(DECIMAL_SEPARATOR);
            if(dsi != -1 && offset > dsi && (super.getLength() - dsi) > _decimalPrecision) return;
        }
    
        // All OK, add the character to the text box
        super.insertString(offset, str, attr);
    }
}
