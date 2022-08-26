/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

//
// An homogeneity-test class
//
public class Homogeneity {
    // Data
    private int      _N        = 0;
    private double   _pp       = 0;

    private int      _N1       = 0;
    private double   _F1       = 0;

    private double[] _y1Values = null;
    private double[] _y2Values = null;

    private double   _y1Mean   = 0;
    private double   _y2Mean   = 0;

    private double   _Sy1      = 0;
    private double   _Sy2      = 0;
    private double   _TV       = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct an homogeneity-test class
    public Homogeneity(double[] y1, double[] y2, double pp) throws Exception
    {
        // Check the number of data
        if(y1.length <= 1 || y2.length <= 1) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of data and probability
        _N  = y1.length;
        _pp = pp;

        // Calculate N1 and F
        _N1 = _N - 1;
        _F1 = DistTable.F1(_pp, _N1, _N1);

        // Allocate memory for the value arrays
        _y1Values = new double[_N];
        _y2Values = new double[_N];

        // Copy the values
        System.arraycopy(y1, 0, _y1Values, 0, _N);
        System.arraycopy(y2, 0, _y2Values, 0, _N);

        // Calculate the Y1-mean and Y2-mean
        for(int i = 0; i < _N; ++i) {
            _y1Mean += _y1Values[i];
            _y2Mean += _y2Values[i];
        }
        _y1Mean /= _N;
        _y2Mean /= _N;

        // Calculate the sum-of-squares of the differences
        double y1SS = 0;
        double y2SS = 0;
        for(int i = 0; i < _N; ++i) {
            double y1d = _y1Values[i] - _y1Mean;
            double y2d = _y2Values[i] - _y2Mean;
            y1SS += y1d * y1d;
            y2SS += y2d * y2d;
        }

        // Calculate squared residual standard deviations
        double Sy1q = y1SS / _N1;
        double Sy2q = y2SS / _N1;

        // Calculate Sy1, Sy2, and TV
        _Sy1 = Math.sqrt(Sy1q);
        _Sy2 = Math.sqrt(Sy2q);
        _TV  = Sy2q / Sy1q;
    }

    // Getters
    public int      getN()        { return _N; }
    public int      getN1()       { return _N1; }
    public double   getPP()       { return _pp; }
    public double   getF1()       { return _F1; }

    public double[] getY1Values() { return _y1Values; }
    public double[] getY2Values() { return _y2Values; }

    public double   getY1Mean()   { return _y1Mean; }
    public double   getY2Mean()   { return _y2Mean; }

    public double   getSy1()      { return _Sy1; }
    public double   getSy2()      { return _Sy2; }
    public double   getTV()       { return _TV; }
}
