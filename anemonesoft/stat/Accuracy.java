/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A accuracy-test class
//
public class Accuracy extends FirstOrderRegression {
    // Data
    private double _pR   = 0;
    private double _pRSd = 0;
    
    // Construct an accuracy-test class
    public Accuracy(double[] x, double[][] y, double pp) throws Exception
    {
        super(x, y, pp);

        // Calculate the procent-recovery
        double[] xc = getXValues();
        double[] xf = getYValues();
        double[] rt = new double[getN()];
        double   mn = 0;
        for(int i = 0; i < xc.length; ++i) {
            rt[i] = xf[i] / xc[i];
            mn += rt[i];
        }
        mn /= xc.length;
        _pR = mn * 100;
        
        // Calculate the standard deviation of the procent-recovery
        double sum = 0;
        for(int i = 0; i < rt.length; ++i) {
            double df = rt[i] - mn;
            sum += (df * df);
        }
        _pRSd = Math.sqrt(sum / (getN() - 1)) * 100;
        
    }

    // Getters
    public double getSaf()  { return getSa(); }
    public double getCIaf() { return getCIa(); }
    
    public double getSbf()  { return getSb(); }
    public double getCIbf() { return getT2() * getSb(); }
    
    public double getPR()   { return _pR; }
    public double getPRSd() { return _pRSd; }
}

