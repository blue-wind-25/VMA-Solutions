/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A mandel-test class
//
public class MandelTest {
    // Data
    private int      _N   = 0;
    private int      _N3  = 0;
    private double   _pp  = 0;
    private double   _Sy1 = 0;
    private double   _Sy2 = 0;

    private double   _F1  = 0;
    private double   _DS2 = 0;
    private double   _TV  = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a mandel-test class
    public MandelTest(int N, double pp, double Sy1, double Sy2) throws Exception
    {
        // Store the input
        _N   = N;
        _pp  = pp;
        _Sy1 = Sy1;
        _Sy2 = Sy2;

        // Calculate N3, F1, DS2, and TV
        double Sy22 = _Sy2 * _Sy2;
        _N3  = _N - 3;
        _F1  = DistTable.F1(pp, 1, _N3);
        _DS2 = (_N - 2) * _Sy1 * _Sy1 - _N3 * Sy22;
        _TV  = _DS2 / Sy22;
    }

    // Getters
    public int    getN()   { return _N; }
    public int    getN3()  { return _N3; }
    public double getPP()  { return _pp; }
    public double getF1()  { return _F1; }
    public double getSy1() { return _Sy1; }
    public double getSy2() { return _Sy2; }
    public double getDS2() { return _DS2; }
    public double getTV()  { return _TV; }

}
