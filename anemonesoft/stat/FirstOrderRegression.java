/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A first-order-regression class
//
public class FirstOrderRegression {
    // Data
    private int        _N         = 0;
    private double     _pp        = 0;

    private int        _N2        = 0;
    private double     _Ni        = 0;
    private double     _t1        = 0;
    private double     _t2        = 0;
    
    private double[]   _xValues   = null;
    private double[]   _yValues   = null;
    private double[][] _yValuesA  = null;

    private double     _xMean     = 0;
    private double     _yMean     = 0;

    private double     _Qxx       = 0;
    private double     _Qyy       = 0;
    private double     _Qxy       = 0;

    private double     _r         = 0;
    private double     _RSS       = 0;
    private double     _b         = 0;
    
    private double     _a         = 0;
    private double     _Sy        = 0;

    private double     _Sb        = 0;
    private double     _Sa        = 0;
    private double     _Sx0       = 0;
    private double     _Vx0       = 0;

    private double     _CIx1      = 0;
    private double     _CIrelx1   = 0;
    
    private double     _CIb       = 0;
    private double     _CIa       = 0;
    private double     _ya        = 0;

    private double     _xa        = 0;
    private double     _CL        = 0;
    private double     _CU        = 0;
    private double     _DL        = 0;
    private double     _QL        = 0;
    private double     _gValue    = 0;
    private double     _QCmean    = 0;
    private double     _linearity = 0;

    private double[]   _rValues  = null;
    private double[][] _rValuesA = null;
    private double[]   _rRange   = new double[2];

    private int        _Nr        = 0;

    private double     _SSr       = 0;
    private double     _SSe       = 0;
    private double     _SSlof     = 0;

    private int        _DFr       = 0;
    private int        _DFe       = 0;
    private int        _DFlof     = 0;

    private double     _Vr        = 0;
    private double     _Ve        = 0;
    private double     _Vlof      = 0;

    private double     _Fr        = 0;
    private double     _F1        = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a first-order-regression class
    public FirstOrderRegression(double[] x, double[][] y, double pp) throws Exception
    {
        // Check the number of data
        if(x.length < 3 || y.length < 1) throw new RuntimeException("Not enough data!");

        // Store the number of data, the number of replications, and probability
        _N  = x.length;
        _Nr = y.length;
        _pp = pp;

        // Calculate N2, Ni, t1, and t2
        _N2 = _N - 2;
        _Ni = 1.0 / _N;
        _t1 = DistTable.t1(_pp, _N2);
        _t2 = DistTable.t2(_pp, _N2);

        // Allocate memory for the value arrays
        _xValues = new double[_N];
        _yValues = new double[_N];
        _rValues = new double[_N];

        // Copy the X-values
        System.arraycopy(x, 0, _xValues, 0, _N);

        // Copy the Y-values array
        _yValuesA = y;

        // Calculate the average Y-values from the multiple Y-values
        for(int i = 0; i < _Nr; ++i) {
            double[] cy = y[i];
            for(int j = 0; j < _N; ++j) {
                if(j >= cy.length) break;
                _yValues[j] += cy[j];
            }
        }
        for(int i = 0; i < _N; ++i) {
            _yValues[i] /= _Nr;
        }

        // Calculate the X-mean and Y-mean
        for(int i = 0; i < _N; ++i) {
            _xMean += _xValues[i];
            _yMean += _yValues[i];
        }
        _xMean /= _N;
        _yMean /= _N;

        // Calculate the X-difference and Y-difference (relative to their mean)
        double[] xDiffs = new double[_N];
        double[] yDiffs = new double[_N];
        for(int i = 0; i < _N; ++i) {
            xDiffs[i] = _xValues[i] - _xMean;
            yDiffs[i] = _yValues[i] - _yMean;
        }
        
        // Calculate Qxx, Qyy, and Qxy
        for(int i = 0; i < _N; ++i) _Qxx += xDiffs[i] * xDiffs[i];
        for(int i = 0; i < _N; ++i) _Qyy += yDiffs[i] * yDiffs[i];
        for(int i = 0; i < _N; ++i) _Qxy += xDiffs[i] * yDiffs[i];

        // Calculate r, RSS, and b
        _r   = _Qxy / Math.sqrt(_Qxx * _Qyy);
        _RSS = _Qyy - _Qxy * _Qxy / _Qxx;
        _b   = _Qxy / _Qxx;

        // Calculate a and Sy
        _a  = _yMean - _b * _xMean;
        _Sy = Math.sqrt(_RSS / _N2);
        
        // Calculate Sb, Sa, _Sx0, and _Vx0
        _Sb  = Math.sqrt(_Sy * _Sy / _Qxx);
        _Sa  = _Sy * Math.sqrt( _Ni + (_xMean * _xMean / _Qxx) );
        _Sx0 = _Sy / _b;
        _Vx0 = 100 * _Sy / (_b * _xMean);

        // Calculate CIx1 and CIrelx1
        double xd1 = _xValues[0] - _xMean;
        _CIx1    = _Sx0 * _t2 * Math.sqrt(_Ni + 1 + xd1 * xd1 / _Qxx);
        _CIrelx1 = 100 * _CIx1 / _xValues[0];
        
        // Calculate CIb, CIa, and ya
        _CIb = 100 * _t2 * _Sb / _b;
        _CIa = _t2 * _Sa;
        _ya  = _a + _Sy * _t1 * Math.sqrt( _Ni + 1 + (_xMean * _xMean / _Qxx) );
        
        // Calculate xa, CL, CU, DL, and QL
        double yaym  = _ya - _yMean;
        double bbqxx = _b * _b * _Qxx;
        double er    = _t2 * _Sy * Math.sqrt(_Ni + _xMean * _xMean / _Qxx);
        _xa = 2 * _Sx0 * _t1 * Math.sqrt( _Ni + 1 + (yaym * yaym / bbqxx) );
        _CL = _a - er;
        _CU = _a + er;
        _DL = _xa;
        _QL = 3 * _DL;

        // Calculate the g-value
        _gValue = _t2 * _t2 * _Sy * _Sy / (_b * _b * _Qxx);

        // Calculcate the QC-mean
        double qcsum = 0;
        for(int i = 0; i < _N; ++i) {
            double ri = (_yValues[i] - (_a + _b * _xValues[i])) / _yMean;
            qcsum += (ri * ri);
        }
        _QCmean = Math.sqrt(qcsum / (_N - 1)) * 100;

        // Calculate the linearity
        _linearity = 1 - _Sb / _b;
        
        // Calculate the residual values and their range
        double min =  Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for(int i = 0; i < _N; ++i) {
            double ri = _yValues[i] - (_a + _b * _xValues[i]);
            _rValues[i] = ri;
            if(ri < min) min = ri;
            if(ri > max) max = ri;
        }
        _rRange[0] = min;
        _rRange[1] = max;

        _rValuesA = new double[_yValuesA.length][_N];
        for(int c = 0; c < _yValuesA.length; ++c) {
            for(int i = 0; i < _N; ++i) {
                _rValuesA[c][i] = _yValuesA[c][i] - (_a + _b * _xValues[i]);
            }
        }

        // No need to continue if there is only one set of Y-value
        if(_Nr <= 1) return;
        
        // Calculate the average Y at every X
        double[] yim = new double[_N];
        for(int i = 0; i < _N; ++i) {
            for(int j = 0; j < _Nr; ++j) {
                if(i >= y[j].length) break;
                yim[i] +=  y[j][i];
            }
            yim[i] /= _Nr;
        }
        
        // Calculate SSr, SSe, and SSlof
        for(int i = 0; i < _N; ++i) {
            double yi = _a + _b * _xValues[i];
            for(int j = 0; j < _Nr; ++j) {
                double r = y[j][i] - yi;
                double e = y[j][i] - yim[i];
                double l = yim[i] - yi;
                _SSr   += (r * r);
                _SSe   += (e * e);
                _SSlof += (l * l);
            }
        }

        // Calculate DFr, DFe, and DFlof
        _DFr   = _N * _Nr - 2;
        _DFe   = _N * _Nr - _N;
        _DFlof = _N2;

        // Calculate _Vr, _Ve, and _Vlof
        _Vr   = _SSr   / _DFr;
        _Ve   = _SSe   / _DFe;
        _Vlof = _SSlof / _DFlof;

        // Calculate Fr and F
        _Fr = _Vlof / _Ve;
        _F1 = DistTable.F1(pp, _DFlof, _DFe);
    }

    // Calculate and return the yi at xi
    public double calcYi(double xi)
    { return _a + _b * xi; }

    // Calculate and return the xh at yh
    public double calcXh(double yh)
    { return  (yh - _a) / _b; }

    // Calculate and return the confidence interval at xi
    public double[] calcCIi(double xi)
    {
        double yi = _a + _b * xi;
        double xd = (xi - _xMean);
        double ci = _t2 * _Sy * Math.sqrt(_Ni + xd * xd / _Qxx);
        return new double[]{ yi - ci, yi + ci };
    }
    
    // Calculate and return the prediction interval at xi
    public double[] calcPIi(double xi, double m)
    {
        double yi = _a + _b * xi;
        double xd = (xi - _xMean);
        double pi = _t2 * _Sy * Math.sqrt(1 / m + _Ni + xd * xd / _Qxx);
        return new double[]{ yi - pi, yi + pi };
    }

    // Getters
    public int        getN()         { return _N; }
    public int        getN2()        { return _N2; }
    public double     getPP()        { return _pp; }
    public double     getT1()        { return _t1; }
    public double     getT2()        { return _t2; }
    
    public double[]   getXValues()   { return _xValues; }
    public double[]   getYValues()   { return _yValues; }
    public double[][] getYValuesA()  { return _yValuesA; }
    
    public double     getXMean()     { return _xMean; }
    public double     getYMean()     { return _yMean; }

    public double     getA()         { return _a; }
    public double     getSa()        { return _Sa; }
    public double     getCIa()       { return _CIa; }
    
    public double     getB()         { return _b; }
    public double     getSb()        { return _Sb; }
    public double     getCIb()       { return _CIb; }

    public double     getRSS()       { return _RSS; }
    public double     getR()         { return _r; }
    public double     getSy()        { return _Sy; }
    public double     getSx0()       { return _Sx0; }
    public double     getVx0()       { return _Vx0; }

    public double     getXa()        { return _xa; }
    public double     getYa()        { return _ya; }
    public double     getDL()        { return _DL; }
    public double     getQL()        { return _QL; }

    public double     getGValue()    { return _gValue; }
    public double     getQCMean()    { return _QCmean; }
    public double     getLinearity() { return _linearity; }
        
    public double     getCL()        { return _CL; }
    public double     getCU()        { return _CU; }

    public double     getCIx1()      { return _CIx1; }
    public double     getCIrelx1()   { return _CIrelx1; }

    public double[]   getRValues()   { return _rValues; }
    public double[][] getRValuesA()   { return _rValuesA; }
    public double[]   getYrRange()   { return _rRange; }

    public int        getNr()        { return _Nr; }

    public double     getSSr()       { return _SSr; }
    public double     getSSe()       { return _SSe; }
    public double     getSSlof()     { return _SSlof; }

    public int        getDFr()       { return _DFr; }
    public int        getDFe()       { return _DFe; }
    public int        getDFlof()     { return _DFlof; }

    public double     getVr()        { return _Vr; }
    public double     getVe()        { return _Ve; }
    public double     getVlof()      { return _Vlof; }

    public double     getFr()        { return _Fr; }
    public double     getF1()        { return _F1; }
}

