/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

import Jama.*;

//
// A third-order-regression class
//
public class ThirdOrderRegression {
    // Data
    private int      _N       = 0;

    private double[] _xValues = null;
    private double[] _yValues = null;

    private double   _a       = 0;
    private double   _b       = 0;
    private double   _c       = 0;
    private double   _d       = 0;

    private double[] _rValues = null;
    private double   _Sy      = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a third-order-regression class
    public ThirdOrderRegression(double[] x, double[][] y) throws Exception
    {
        // Check the number of data
        if(x.length < 5 || y.length < 1) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of data and probability
        _N  = x.length;

        // Allocate memory for the value arrays
        _xValues = new double[_N];
        _yValues = new double[_N];
        _rValues = new double[_N];

        // Copy the X-values
        System.arraycopy(x, 0, _xValues, 0, _N);

        // Calculate the average Y-values from the multiple Y-values
        for(int i = 0; i < y.length; ++i) {
            double[] cy = y[i];
            for(int j = 0; j < _N; ++j) {
                if(j >= cy.length) break;
                _yValues[j] += cy[j];
            }
        }
        for(int i = 0; i < _N; ++i) {
            _yValues[i] /= y.length;
        }

        // Create the X matrix
        double Qx1 = 0;
        double Qx2 = 0;
        double Qx3 = 0;
        double Qx4 = 0;
        double Qx5 = 0;
        double Qx6 = 0;
        for(int i = 0; i < _xValues.length; ++i) {
            double x1 = _xValues[i];
            double x2 = x1 * x1;
            double x3 = x2 * x1;
            double x4 = x3 * x1;
            double x5 = x4 * x1;
            double x6 = x5 * x1;
            Qx1 += x1;
            Qx2 += x2;
            Qx3 += x3;
            Qx4 += x4;
            Qx5 += x5;
            Qx6 += x6;
        }
        Matrix X  = new Matrix(new double[][] { { _N,  Qx1, Qx2, Qx3 },
                                                { Qx1, Qx2, Qx3, Qx4 },
                                                { Qx2, Qx3, Qx4, Qx5 },
                                                { Qx3, Qx4, Qx5, Qx6 } });
        Matrix XI = X.inverse();

        // Create the Y matrix
        double Qx0y1 = 0;
        double Qx1y1 = 0;
        double Qx2y1 = 0;
        double Qx3y1 = 0;
        for(int i = 0; i < _xValues.length; ++i) {
            double x1   = _xValues[i];
            double x2   = x1 * x1;
            double x3   = x2 * x1;
            double y1   = _yValues[i];
            double x1y1 = x1 * y1;
            double x2y1 = x2 * y1;
            double x3y1 = x3 * y1;
            Qx0y1 += y1;
            Qx1y1 += x1y1;
            Qx2y1 += x2y1;
            Qx3y1 += x3y1;
        }
        Matrix Y = new Matrix(new double[] { Qx0y1, Qx1y1, Qx2y1, Qx3y1 }, 4);

        // Calculate the a, b, c, and d coefficients
        double[][] abcd = XI.times(Y).getArray();
        _a = abcd[0][0];
        _b = abcd[1][0];
        _c = abcd[2][0];
        _d = abcd[3][0];

        // Calculate the residual values and Sy
        for(int i = 0; i < _N; ++i) {
            double xi  = _xValues[i];
            double xi2 = xi  * xi;
            double xi3 = xi2 * xi;
            double yi  = _yValues[i];
            double ri = (yi - (_a + _b * xi + _c *xi2 + _d * xi3));
            _rValues[i] = ri;
            _Sy += (ri * ri);
        }
        _Sy = Math.sqrt(_Sy / (_N - 4));
    }

    // Calculate and return the yi at xi
    public double calcYi(double xi)
    {
        double xi2 = xi  * xi;
        double xi3 = xi2 * xi;
        return _a + _b * xi + _c * xi2 + _d * xi3;
    }

    // Getters
    public int      getN()       { return _N; }

    public double[] getXValues() { return _xValues; }
    public double[] getYValues() { return _yValues; }

    public double   getA()       { return _a; }
    public double   getB()       { return _b; }
    public double   getC()       { return _c; }
    public double   getD()       { return _d; }

    public double[] getRValues() { return _rValues; }
    public double   getSy()      { return _Sy; }
}
