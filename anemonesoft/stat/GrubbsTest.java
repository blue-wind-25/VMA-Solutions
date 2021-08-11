/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
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

    private double   _ppc1    = 0;
    private double   _tc1     = 0;
    private double   _gc1     = 0;

    private double   _ppc2    = 0;
    private double   _tc2     = 0;
    private double   _gc2     = 0;

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

        // Calculate N2, PPC1, tc1, PPC2, and tc2
        double a1  = (100.0 - pp) / 100.0;
        double ac1 = a1 / _N;

        double a2  = (100.0 - pp) / 100.0;
        double ac2 = a2 / (2 * _N);

        _N2   = _N - 2;
        _pp   = pp;

        _ppc1 = (1.0 - ac1) * 100.0;
        _tc1  = DistTable.t1(_ppc1, _N2);

        _ppc2 = (1.0 - ac2) * 100.0;
        _tc2  = DistTable.t2(_ppc2, _N2);

        // Calculate the G-Crit 1 and 2
        _gc1 = ( (_N - 1) * _tc1 )
               /
               Math.sqrt
               (
                   _N
                   *
                   (_N - 2 + _tc1 * _tc1)
               );

        _gc2 = ( (_N - 1) * _tc2 )
               /
               Math.sqrt
               (
                   _N
                   *
                   (_N - 2 + _tc2 * _tc2)
               );

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
            _gValues[i] = Math.abs( (_sValues[i] - _mean) / _Sd );
        }
    }

    // Getters
    public int    getN()      { return _N; }
    public int    getN2()     { return _N2; }

    public double getPP()     { return _pp; }

    public double getAC1()    { return (100.0 - _ppc1) / 100.0; }
    public double getTC1()    { return _tc1; }

    public double getAC2()    { return (100.0 - _ppc2) / 100.0; }
    public double getTC2()    { return _tc2; }

    public double getMean()   { return _mean; }
    public double getSd()     { return _Sd; }

    public double getGC1()    { return _gc1; }
    public double getGC2()    { return _gc2; }
    public double getG(int i) { return _gValues[i]; }
}
