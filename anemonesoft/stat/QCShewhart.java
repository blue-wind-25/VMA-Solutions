/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

//
// A quality-control-shewhart class
//
public class QCShewhart {
    // Data
    private int      _N       = 0;

    private double[] _yValues = null;

    private double   _yMean   = 0;
    private double   _Sy      = 0;
    private double   _2Sy     = 0;
    private double   _3Sy     = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a quality-control-shewhart class
    public QCShewhart(double[] y) throws Exception
    {
        // Check the number of data
        if(y.length < 1) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of data
        _N = y.length;

        // Allocate memory for the value arrays and copy the Y-values
        _yValues = new double[_N];
        System.arraycopy(y, 0, _yValues, 0, _N);

        // Calculate the Y-mean
        for(int i = 0; i < _N; ++i) _yMean += _yValues[i];
        _yMean /= _N;

        // Calculate Sy, 2Sy, and 3Sy
        for(int i = 0; i < _N; ++i) {
            double dif = _yValues[i] - _yMean;
            _Sy += (dif * dif);
        }
        _Sy  = Math.sqrt(_Sy / (_N - 1));
        _2Sy = _Sy * 2;
        _3Sy = _Sy * 3;
    }

    // Getters
    public int      getN()       { return _N; }

    public double[] getYValues() { return _yValues; }

    public double   getYMean()   { return _yMean; }
    public double   getSy()      { return _Sy; }
    public double   get2Sy()     { return _2Sy; }
    public double   get3Sy()     { return _3Sy; }
}

