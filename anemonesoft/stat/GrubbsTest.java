/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            VMA Consultant
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

//
// A Grubbs-test class
//
public class GrubbsTest {
    // Data
    private int      _N       = 0;
    private int      _N2      = 0;
    private double   _pp      = 0;

    private double   _ac      = 0;
    private double   _tc      = 0;
    private double   _gc      = 0;

    private double   _mean    = 0;
    private double   _Sd      = 0;

    private double[] _sValues = null;
    private double[] _gValues = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct Grubbs-test class
    public GrubbsTest(double[] s, double pp) throws Exception
    {
        // Check the number of data
        if(s.length < 3) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of data
        _N = s.length;

        // Allocate memory for the value arrays
        _sValues = new double[_N];
        _gValues = new double[_N];

        // Copy the input values
        System.arraycopy(s, 0, _sValues, 0, _N);

        // Save PP and calculate N2
        _pp = pp;
        _N2 = _N - 2;

        _ac = DistTable.grubbs_ac(_pp, _N);
        _tc = DistTable.grubbs_tc(_pp, _N);
        _gc = DistTable.grubbs_gc(_pp, _N);

        // Calculate the mean
        for(int i = 0; i < _sValues.length; ++i) {
            _mean += _sValues[i];
        }
        _mean /= _N;

        // Calculate the Sd
        for(int i = 0; i < _sValues.length; ++i) {
            double dif = _sValues[i] - _mean;
            _Sd += (dif * dif);
        }
        _Sd = Math.sqrt(_Sd / (_N - 1));

        // Calculate the G
        for(int i = 0; i < _sValues.length; ++i) {
            _gValues[i] = Math.abs((_sValues[i] - _mean) / _Sd);
        }
    }

    // Getters
    public int    getN()      { return _N; }
    public int    getN2()     { return _N2; }

    public double getPP()     { return _pp; }

    public double getAC()     { return _ac; }
    public double getTC()     { return _tc; }

    public double getMean()   { return _mean; }
    public double getSd()     { return _Sd; }

    public double getGC()     { return _gc; }
    public double getG(int i) { return _gValues[i]; }
}
