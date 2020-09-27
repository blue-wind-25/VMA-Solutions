/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import Jama.*;

//
// A robustness-test class
//
public class Robustness {
    // Data
    private int        _N         = 0;
    private int        _K         = 0;
    private int        _NK1       = 0;
    private int        _NK1s      = 0;
    private double     _pp        = 0;
    private double     _t2        = 0;
    private double     _t2s       = 0;

    private double[]   _response  = null;
    private double[][] _factors   = null;

    private double[]   _deltaReal = null;
    private double[]   _deltaTest = null;

    private double[]   _b         = null;
    private double[]   _e         = null;

    private double     _Sy        = 0;
    private double[]   _Sb        = null;

    private double[]   _CIb       = null;
    private double[]   _CIbRange  = null;

    private boolean[]  _isSig     = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a robustness-test class
    public Robustness(double[] deltaReal, double[] response, double[][] factors, double pp) throws Exception
    {
        // Check the number of data
        if(response.length < 3 || factors.length < 1) throw new RuntimeException("Not enough data!");

        // Store the number of response, number of response factors, and probability
        _N  = response.length;
        _K  = factors.length;
        _pp = pp;

        // Allocate memory for delta-test/real and copy the delta-real
        if(deltaReal != null) {
            _deltaTest = new double[factors.length];
            _deltaReal = new double[factors.length];
            System.arraycopy(deltaReal, 0, _deltaReal, 0, factors.length);
        }

        // Calculate the delta-test
        if(_deltaTest != null) {
            for(int i = 0; i < factors.length; ++i) {
                double min =  Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                for(int j = 0; j < factors[i].length; ++j) {
                    double val = factors[i][j];
                    if(val < min) min = val;
                    if(val > max) max = val;
                }
                _deltaTest[i] = max - min;
            }
        }

        // Calculate NK1 and t2
        _NK1 = _N - _K - 1;
        _t2  = DistTable.t2(_pp, _NK1);
        
        // Calculate NK1s and t2s
        _NK1s = _N * _K - 1;
        _t2s  = DistTable.t2(_pp, _NK1s);

        // Allocate memory for the value arrays
        _response = new double [_N];
        _factors  = new double [_K + 1][_N];
        _Sb       = new double [_K + 1];
        _CIb      = new double [_K + 1];
        _isSig    = new boolean[_K + 1];

        // Copy the response
        System.arraycopy(response, 0, _response, 0, _N);
        
        // Copy the factors
        double[] fctr = new double[_K];
        double[] frng = new double[_K];
        for(int k = 0; k < _K; ++k) {
            // For finding the maximum and minimum values
            double min =  Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            // The number of samples in the current factor
            int fcnt = Math.min(factors[k].length, _N);
            // Walk trough the samples
            for(int i = 0; i < fcnt; ++i) {
                double v = factors[k][i];
                _factors[k + 1][i] = v;
                if(v < min) min = v;
                if(v > max) max = v;
            }
            // Store the center and range
            fctr[k] = (min + max) / 2;
            frng[k] = max - min;
        }

        // Normalize the factors
        for(int k = 0; k < _K; ++k) {
            for(int i = 0; i < _N; ++i) {
                _factors[k + 1][i] = (_factors[k + 1][i] - fctr[k]) / frng[k];
            }
        }

        // Fill the first row of the factors with 1
        for(int i = 0; i < _N; ++i) {
            _factors[0][i] = 1;
        }

        // Initialize matrix
        Matrix Y  = new Matrix(_response, _N);
        Matrix XT = new Matrix(_factors);
        Matrix X  = XT.transpose();

        // Calculate b and e
        Matrix CI = XT.times(X);
        Matrix C  = CI.inverse();
        Matrix b  = C.times(XT).times(Y);
        Matrix e  = Y.minus(X.times(b));

        _b = b.getRowPackedCopy();
        _e = e.getRowPackedCopy();
        
        // Calculate Sy
        double[][] r_x = X.getArray();
        double     Sy2 = 0;
        for(int i = 0; i < _N; ++i) {
            double bxSum = 0;
            for(int k = 1; k <= _K; ++k) {
                bxSum += (_b[k] * r_x[i][k]);
            }
            double ybxSub = _response[i] - _b[0] - bxSum;
            Sy2 += (ybxSub * ybxSub);
        }
        Sy2 /= _NK1;
        _Sy = Math.sqrt(Sy2);

        // For finding the maximum and minimum values
        double min  =  Double.MAX_VALUE;
        double max  = -Double.MAX_VALUE;

        // Calculate Sb, CIb, and CIb_range, 
        double[][] r_ci = C.getArray();
        for(int k = 0; k <= _K; ++k) {
            double Sb2 = Sy2 * r_ci[k][k];
            double Sb  = Math.sqrt(Sb2);
            double l   = _b[k] - Sb;
            double h   = _b[k] + Sb;
            _Sb [k] = Sb;
            _CIb[k] = _t2 * Sb;
            if(l < min) min = l;
            if(l > max) max = l;
            if(h < min) min = h;
            if(h > max) max = h;
        }
        _CIbRange = new double[]{ min, max };

        // Check if the factors are significant
        double scl = Math.sqrt(2) / _Sy;
        for(int k = 0; k <= _K; ++k) {
            double ef = Math.abs(_b[k]);
            double TV = scl * ef;
            _isSig[k] = (TV > _t2s);
        }
    }

    // Getters
    public double[]   getResponse()  { return _response; }
    public double[][] getFactors()   { return _factors; }
    
    public double[]   getDeltaReal() { return _deltaReal; }
    public double[]   getDeltaTest() { return _deltaTest; }

    public double     getPP()       { return _pp; }
    public int        getN()        { return _N; }
    public int        getK()        { return _K; }
    public int        getNK1()      { return _NK1; }
    public int        getNK1s()     { return _NK1s; }
    public double     getT2()       { return _t2; }
    public double     getT2s()      { return _t2s; }

    public double[]   getB()        { return _b; }
    public double[]   getE()        { return _e; }

    public double     getSy()       { return _Sy; }
    public double[]   getSb()       { return _Sb; }

    public double[]   getCIb()      { return _CIb; }
    public double[]   getCIbRange() { return _CIbRange; }

    public boolean[]  getIsSig()    { return _isSig; }
}   

