/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A Grubbs-test class
//
public class GrubbsTest {
    // Data
    private int      _N       = 0;
    private int      _N2      = 0;
    private double   _pp      = 0;
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
        if(s.length < 3) throw new RuntimeException("Not enough data!");

        // Store the number of data
        _N = s.length;

        // Allocate memory for the value arrays
        _sValues = new double[_N];
        _gValues = new double[_N];

        // Copy the input values
        System.arraycopy(s, 0, _sValues, 0, _N);

        // Calculate N2 and tc
        _pp = pp;
        _N2 = _N - 2;
        _tc = DistTable.t2(100.0 - ((100.0 - _pp) / _N), _N2);

        // Calculate the G-Crit
        _gc = ( (_N - 1) * _tc )
              /
              Math.sqrt
              (
                  _N
                  *
                  (_N - 2 + _tc * _tc)
              );

        // Calculate the mean
        for(int j = 0; j < _sValues.length; ++j) {
            _mean += _sValues[j];
        }
        _mean /= (_N + 1);

        // Calculate the Sd
        for(int j = 0; j < _sValues.length; ++j) {
            double dif = _sValues[j] - _mean;
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
    public double getTC()     { return _tc; }

    public double getMean()   { return _mean; }
    public double getSd()     { return _Sd; }

    public double getGC()     { return _gc; }
    public double getG(int i) { return _gValues[i]; }
}
