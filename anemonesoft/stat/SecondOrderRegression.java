/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import anemonesoft.i18n.*;

//
// A second-order-regression class
//
public class SecondOrderRegression {
    // Data
    private int      _N       = 0;
    private double   _pp      = 0;

    private int      _N3      = 0;
    private double   _Ni      = 0;
    private double   _t1      = 0;
    private double   _t2      = 0;

    private double[] _xValues = null;
    private double[] _yValues = null;

    private double   _xMean   = 0;
    private double   _yMean   = 0;

    private double   _Qxx     = 0;
    private double   _Qxy     = 0;
    private double   _Qx3     = 0;
    private double   _Qx4     = 0;
    private double   _Qx2y    = 0;

    private double   _c       = 0;
    private double   _b       = 0;
    private double   _a       = 0;

    private double[] _rValues = null;
    private double[] _rRange  = new double[2];
    private double   _RSS     = 0;
    private double   _Sy      = 0;

    private double   _E       = 0;
    private double   _Sx0     = 0;
    private double   _Vx0     = 0;

    private double   _r       = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a second-order-regression class
    public SecondOrderRegression(double[] x, double[][] y, double pp) throws Exception
    {
        // Check the number of data
        if(x.length < 4 || y.length < 1) throw new RuntimeException(StringTranslator.strNED());

        // Store the number of data and probability
        _N  = x.length;
        _pp = pp;

        // Calculate N3, Ni, t1, and t2
        _N3 = _N - 3;
        _Ni = 1.0 / _N;
        _t1 = DistTable.t1(_pp, _N3);
        _t2 = DistTable.t2(_pp, _N3);

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

        // Calculate the X-mean and Y-mean
        for(int i = 0; i < _N; ++i) {
            _xMean += _xValues[i];
            _yMean += _yValues[i];
        }
        _xMean /= _N;
        _yMean /= _N;

        // Calculate the sumX, sumX2, sumX3, sumX4, sumXY, sumX2Y, and sumY
        double sumX   = 0;
        double sumX2  = 0;
        double sumX3  = 0;
        double sumX4  = 0;
        double sumXY  = 0;
        double sumX2Y = 0;
        double sumY   = 0;
        for(int i = 0; i < _N; ++i) {
            double xi  = _xValues[i];
            double yi  = _yValues[i];
            double xi2 = xi  * xi;
            double xi3 = xi2 * xi;
            double xi4 = xi2 * xi2;
            sumX   += xi;
            sumX2  += xi2;
            sumX3  += xi3;
            sumX4  += xi4;
            sumXY  += (xi  * yi);
            sumX2Y += (xi2 * yi);
            sumY   += yi;
        }

        // Calculate Qxx, Qxy, Qx3, Qx4, and Qx2y
        _Qxx  = sumX2  - _Ni * sumX  * sumX;
        _Qxy  = sumXY  - _Ni * sumX  * sumY;
        _Qx3  = sumX3  - _Ni * sumX  * sumX2;
        _Qx4  = sumX4  - _Ni * sumX2 * sumX2;
        _Qx2y = sumX2Y - _Ni * sumX2 * sumY;

        // Calculate c, b, and a
        _c = (_Qxy * _Qx3 - _Qx2y * _Qxx) / (_Qx3 * _Qx3 - _Qxx * _Qx4);
        _b = (_Qxy - _c * _Qx3) / _Qxx;
        _a = _yMean - _b * _xMean - _c * _Ni * sumX2;

        // Calculate the residual values, their range, RSS, and Sy
        double min =  Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for(int i = 0; i < _N; ++i) {
            double xi = _xValues[i];
            double yi = _yValues[i];
            double ri = (yi - (_a + _b * xi + _c * xi * xi));
            _rValues[i] = ri;
            _RSS += (ri * ri);
            if(ri < min) min = ri;
            if(ri > max) max = ri;
        }
        _rRange[0] = min;
        _rRange[1] = max;
        _Sy        = Math.sqrt(_RSS / _N3);

        // Calculate E, Sx0, and Vx0
        _E   = _b + 2 * _c * _xMean;
        _Sx0 = _Sy / _E;
        _Vx0 = 100 * _Sy / (_b * _xMean);

        // Calculate r
        double dyiSum = 0;
        double dyhSum = 0;
        for(int i = 0; i < _N; ++i) {
            double xi  = _xValues[i];
            double yi  = _yValues[i];
            double yh  = _a + _b * xi + _c * xi * xi;
            double dyi = yi - _yMean;
            double dyh = yh - _yMean;
            dyiSum += dyi * dyi;
            dyhSum += dyh * dyh;
        }
        _r = Math.sqrt(dyhSum / dyiSum);
    }

    // Calculate and return the yi at xi
    public double calcYi(double xi)
    { return _a + _b * xi + _c * xi * xi; }

    // Calculate xh from the given yh
    public double calcXh(double yh)
    {
        double v1 = -_b / (2 * _c);
        double v2 = Math.sqrt(v1 * v1 - (_a - yh) / _c);

        return (_c >= 0) ? (v1 + v2) : (v1 - v2);
    }

    // Calculate CIxh from the given xh and m
    public double[] calcCIxh(double xh, double m)
    {
        double Nai = (m > 0) ? (1 / m) : 0;
        double Nci = _Ni;

        double sumX2 = 0;
        for(int i = 0; i < _xValues.length; ++i) {
            sumX2 += _xValues[i] * _xValues[i];
        }

        double xhSubM = xh - _xMean;
        double xhSubQ = xh * xh - Nci * sumX2;

        double v1  = _Sy * _t2 / (_b + 2 * _c + xh);
        double v2  =  1 / (_Qx4 * _Qxx - _Qx3 * _Qx3);
        double v21 = xhSubM * xhSubM * _Qx4;
        double v22 = xhSubQ * xhSubQ * _Qxx;
        double v23 = 2 * xhSubM * xhSubQ * _Qx3;

        double cix = v1 * Math.sqrt(Nci + Nai + v2 * (v21 + v22 - v23));

        return new double[]{ xh - cix, xh + cix };
    }

    // Calculate and return the confidence interval at xi
    public double[] calcCIi(double xi)
    {
        double yi = _a + _b * xi + _c * xi * xi;
        double xd = (xi - _xMean);
        double ci = _t2 * _Sy * Math.sqrt(_Ni + xd * xd / _Qxx);
        return new double[]{ yi - ci, yi + ci };
    }

    // Calculate and return the prediction interval at xi
    public double[] calcPIi(double xi, double m)
    {
        double yi = _a + _b * xi + _c * xi * xi;
        double xd = (xi - _xMean);
        double pi = _t2 * _Sy * Math.sqrt(1 / m + _Ni + xd * xd / _Qxx);
        return new double[]{ yi - pi, yi + pi };
    }

    // Getters
    public int      getN()       { return _N; }
    public int      getN3()      { return _N3; }
    public double   getPP()      { return _pp; }
    public double   getT1()      { return _t1; }
    public double   getT2()      { return _t2; }

    public double[] getXValues() { return _xValues; }
    public double[] getYValues() { return _yValues; }

    public double   getXMean()   { return _xMean; }
    public double   getYMean()   { return _yMean; }

    public double   getA()       { return _a; }
    public double   getB()       { return _b; }
    public double   getC()       { return _c; }

    public double[] getRValues() { return _rValues; }
    public double[] getYrRange() { return _rRange; }
    public double   getRSS()     { return _RSS; }
    public double   getSy()      { return _Sy; }

    public double   getE()       { return _E; }
    public double   getSx0()     { return _Sx0; }
    public double   getVx0()     { return _Vx0; }

    public double   getR()       { return _r; }
}
