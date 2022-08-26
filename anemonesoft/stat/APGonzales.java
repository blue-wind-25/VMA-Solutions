/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// An ap-gonzales class
//
public class APGonzales {
    // Data
    double[] _delta  = null;
    double[] _deltaP = null;
    
    double[] _BETIh  = null;
    double[] _BETIl  = null;

    double   _LL     = -1;
    double   _UL     = -1;

    private double leftIntersect(PrecisionOWANO[] preInst, double[] BETI, double lambda)
    {
        int idx1 = -1;
        int idx2 = -1;
        for(int i = 0; i < (BETI.length - 1); ++i) {
            double be1 = BETI[i    ];
            double be2 = BETI[i + 1];
            if(be1 <= lambda && be2 >= lambda ||
               be1 >= lambda && be2 <= lambda ) {
                idx1 = i;
                idx2 = i + 1;
                break;
            }
        }

        // Found
        if(idx1 >= 0 && idx2 >= 0) {
            double x1 = preInst[idx1].getTVal();
            double y1 = BETI   [idx1];
            double x2 = preInst[idx2].getTVal();
            double y2 = BETI   [idx2];
            double m  = (x2 - x1) / (y2 - y1);
            return m * (lambda - y1) + x1;
        }

        // Not found
        return -1;
    }

    private double rightIntersect(PrecisionOWANO[] preInst, double[] BETI, double lambda)
    {
        int idx1 = -1;
        int idx2 = -1;
        for(int i = (BETI.length - 1); i > 0; --i) {
            double be1 = BETI[i - 1];
            double be2 = BETI[i    ];
            if(be1 <= lambda && be2 >= lambda ||
               be1 >= lambda && be2 <= lambda ) {
                idx1 = i - 1;
                idx2 = i;
                break;
            }
        }

        // Found
        if(idx1 >= 0 && idx2 >= 0) {
            double x1 = preInst[idx1].getTVal();
            double y1 = BETI   [idx1];
            double x2 = preInst[idx2].getTVal();
            double y2 = BETI   [idx2];
            double m  = (x2 - x1) / (y2 - y1);
            return m * (lambda - y1) + x1;
        }

        // Not found
        return -1;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct an ap-gonzales class
    public APGonzales(PrecisionOWANO[] preInst, Robustness[] robInst, double pp, double lambda) throws Exception
    {
        // Calculate the delta
        _delta  = new double[preInst.length];
        _deltaP = new double[preInst.length];
        for(int i = 0; i < preInst.length; ++i) {
            _delta [i] = preInst[i].getZmm() - preInst[i].getTVal();
            _deltaP[i] = _delta[i] / preInst[i].getTVal() * 100;
        }

        // Calculate the B-ETI
        _BETIh  = new double[preInst.length];
        _BETIl  = new double[preInst.length];
        for(int i = 0; i < preInst.length; ++i) {
            // Get some values
            int    p   = preInst[i].getP();
            int    n   = preInst[i].getN();
            double Sw2 = preInst[i].getSw2();
            double Sr2 = preInst[i].getSr2();
            double Sr  = Math.sqrt(Sr2);
            double t   = DistTable.t2(pp, p * n - 1);
            // Using robustness?
            double uRob2 = 0;
            if(robInst != null) {
                // Calculate the mean
                double[] rr = robInst[i].getResponse();
                double   rm = 0;
                for(int j = 0; j < rr.length; ++j) rm += rr[j];
                rm /= rr.length;

                // Calculate the RSD
                double[] deltaReal = robInst[i].getDeltaReal();
                double[] deltaTest = robInst[i].getDeltaTest();
                double   rsd       = 0;
                for(int k = 0; k < deltaReal.length; ++k) {
                    double deltaRatio = deltaReal[k] / deltaTest[k];
                    double uZx        = t * Sr / 1.96 / Math.sqrt(2) * deltaRatio;
                    rsd += (uZx * uZx);
                }
                rsd    = Math.sqrt(rsd / (rm * rm));
                uRob2  = rm * rsd;
                uRob2 *= uRob2;
            }
            // Calculate the B-ETI
            double gm  = Sw2 / Sr2;
            double uD2 = Sr2 * (1 - gm + gm / n) / p;
            double uZ2 = Sr2 + uD2 + uRob2;
            double uZ  = Math.sqrt(uZ2);
            double tuZ = t * uZ;
            double df  = 1 / preInst[i].getTVal() * 100;
            _BETIh[i] = (_delta[i] + tuZ) * df;
            _BETIl[i] = (_delta[i] - tuZ) * df;
        }

        // Find the lower-limit
        double lll = leftIntersect(preInst, _BETIl, -lambda);
        double llh = leftIntersect(preInst, _BETIh,  lambda);
             if(lll >= 0 && llh <  0) _LL = lll;
        else if(lll <  0 && llh >= 0) _LL = llh;
        else if(lll < llh           ) _LL = lll;
        else                          _LL = llh;

        // Find the upper-limit
        double ull = rightIntersect(preInst, _BETIl, -lambda);
        double ulh = rightIntersect(preInst, _BETIh,  lambda);
             if(ull >= 0 && ulh <  0) _UL = ull;
        else if(ull <  0 && ulh >= 0) _UL = ulh;
        else if(ull > ulh           ) _UL = ull;
        else                          _UL = ulh;
    }

    // Getters
    public double[] getDelta()  { return _delta; }
    public double[] getDeltaP() { return _deltaP; }

    public double[] getBETIh()  { return _BETIh; }
    public double[] getBETIl()  { return _BETIl; }

    public double   getLL()     { return _LL; }
    public double   getUL()     { return _UL; }
}

