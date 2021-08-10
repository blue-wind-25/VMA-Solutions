/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import Jama.*;

//
// A Grubbs-test class
//
public class GrubbsTest {
    // Data
    private int      _N       = 0;
    private int      _S       = 0;

    private int      _N2      = 0;
    private double   _pp      = 0;
    private double   _t2      = 0;

    private double[] _nValues = null;
    private double[] _sValues = null;
    private double[] _gValues = null;
    private double[] _cValues = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct Grubbs-test class
    public GrubbsTest(double[] n, double[] s, double pp) throws Exception
    {
        // Check the number of data
        if(n.length < 3 || s.length < 1) throw new RuntimeException("Not enough data!");

        // Store the number of data
        _N = n.length;
        _S = s.length;

        // Allocate memory for the value arrays
        _nValues = new double[_N];
        _sValues = new double[_S];
        _gValues = new double[_S];
        _cValues = new double[_S];

        // Copy the input values
        System.arraycopy(n, 0, _nValues, 0, _N);
        System.arraycopy(s, 0, _sValues, 0, _S);

        // Calculate N2 and t2
        _pp = pp;
        _N2 = _N - 2;
        _t2 = DistTable.t2(_pp, _N2);




/*

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
        */
    }

    // Getters
    public int      getN()       { return _N; }
    public int      getS()       { return _S; }
    public int      getN2()      { return _N2; }

    public double   getPP()      { return _pp; }
    public double   getT2()      { return _t2; }

    public double   getG(int i)  { return _gValues[i]; }
    public double   getGc(int i) { return _cValues[i]; }
}
