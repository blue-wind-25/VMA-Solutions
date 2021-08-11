/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

//
// A z-score class
//
public class ZScore {
    // Data
    private int      _C       = 0;
    private int      _S       = 0;

    private double[] _cValues = null;
    private double[] _sValues = null;
    private double[] _zValues = null;

    private double   _Cm      = 0;
    private double   _Sc      = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct z-score class
    public ZScore(double[] c, double[] s) throws Exception
    {
        // Check the number of data
        if(c.length < 3 || s.length < 1) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of data
        _C = c.length;
        _S = s.length;

        // Allocate memory for the value arrays
        _cValues = new double[_C];
        _sValues = new double[_S];
        _zValues = new double[_S];

        // Copy the input values
        System.arraycopy(c, 0, _cValues, 0, _C);
        System.arraycopy(s, 0, _sValues, 0, _S);

        // Calculate the mean of the control values
        for(int i = 0; i < _cValues.length; ++i) {
            _Cm += _cValues[i];
        }
        _Cm /= _C;

        // Calculate the Sd
        for(int i = 0; i < _cValues.length; ++i) {
            double dif = _cValues[i] - _Cm;
            _Sc += (dif * dif);
        }
        _Sc = Math.sqrt(_Sc / (_C - 1));

        // Calculate the Zs
        for(int i = 0; i < _sValues.length; ++i) {
            _zValues[i] = (_sValues[i] - _Cm) / _Sc;
        }
    }

    // Getters
    public int    getC()       { return _C; }
    public int    getS()       { return _S; }

    public double getCm()      { return _Cm; }
    public double getSc()      { return _Sc; }

    public double getZs(int i) { return _zValues[i]; }
}
