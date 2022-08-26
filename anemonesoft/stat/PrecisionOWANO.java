/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

//
// A 1-Way-ANOVA-based precision-test class
//
public class PrecisionOWANO {
    // Data
    private int      _P    = 0;
    private int      _N    = 0;

    private double   _tval = 0;

    private double[] _Zm   = null;
    private double   _Zmm  = 0;

    private double   _Sw2  = 0;
    private double   _Sb2  = 0;
    private double   _Sr2  = 0;

    private double   _RSDw = 0;
    private double   _RSDb = 0;
    private double   _RSDr = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a 1-Way-ANOVA-based precision-test class
    public PrecisionOWANO(double trueValue, double v[][]) throws Exception
    {
        // Check the number of data
        if(v.length < 2) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of series and data for each series
        _P = v.length;
        _N = v[0].length;

        // Copy the true-value
        _tval = trueValue;

        // Calculate Zmm
        for(int i = 0; i < _P; ++i) {
            for(int j = 0; j < _N; ++j) _Zmm += v[i][j];
        }
        _Zmm /= (_P * _N);

        // Calculate Zm
        _Zm = new double[_P];
        for(int i = 0; i < _P; ++i) {
            _Zm[i] = 0;
            for(int j = 0; j < _N; ++j) _Zm[i] += v[i][j];
            _Zm[i] /= _N;
        }

        // Calculate Sw2
        for(int i = 0; i < _P; ++i) {
            for(int j = 0; j < _N; ++j) {
                double diff = v[i][j] - _Zm[i];
                _Sw2 += (diff * diff);
            }
        }
        _Sw2 /= (_P * (_N - 1));

        // Calculate Sb2
        for(int i = 0; i < _P; ++i) {
            double diff = _Zm[i] - _Zmm;
            _Sb2 += (diff * diff);
        }
        _Sb2 /= (_P - 1);
        _Sb2 -= (_Sw2 / _N);
        if(_Sb2 < 0) _Sb2 = 0; // http://books.google.co.id/books?id=HbBlyvlRgwkC&pg=PA33&lpg=PA33&dq=negative+%22inter-serial%22+variance&source=bl&ots=nTCwzHCaLg&sig=NyQJ48xCJCM0SS2Gpb2u9HNuiHU&hl=en&sa=X&ei=aBgdT8yFEI-zrAeEoPnIDQ&redir_esc=y#v=onepage&q=negative%20%22inter-serial%22%20variance&f=false

        // Calculate Sr2
        _Sr2 = _Sw2 + _Sb2;

        // Calculate RSDw, RSDb, and RSDr
        _RSDw = Math.sqrt(_Sw2) / _Zmm * 100;
        _RSDb = Math.sqrt(_Sb2) / _Zmm * 100;
        _RSDr = Math.sqrt(_Sr2) / _Zmm * 100;
    }

    // Getters
    public int      getP()    { return _P; }
    public int      getN()    { return _N; }

    public double   getTVal() { return _tval; }

    public double[] getZm ()  { return _Zm; }
    public double   getZmm()  { return _Zmm; }

    public double   getSw2()  { return _Sw2; }
    public double   getSb2()  { return _Sb2; }
    public double   getSr2()  { return _Sr2; }

    public double   getRSDw() { return _RSDw; }
    public double   getRSDb() { return _RSDb; }
    public double   getRSDr() { return _RSDr; }
}
