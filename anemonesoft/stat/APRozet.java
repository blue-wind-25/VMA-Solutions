/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// An ap-rozet class
//
public class APRozet {
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

    // Construct an ap-rozet class
    public APRozet(PrecisionOWANO[] preInst, double pp, double lambda) throws Exception
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
            double Sb2 = preInst[i].getSb2();
            double Sr2 = preInst[i].getSr2();
            double Zmm = preInst[i].getZmm();
            // Calculate Rj and Bj
            double Rj  = Sb2 / Sw2;
            double Rj1 = Rj + 1;
            double Bj = Math.sqrt(Rj1 / (n * Rj + 1));
            // Calculate v and Qt
            double pn   = p * n;
            double in   = 1.0 / n;
            double Rjin = Rj + in;
            double v    = (Rj1 * Rj1) / ( (Rjin * Rjin) / (p - 1) + (1 - in) / pn );
            double Qt   = DistTable.ft1( (100.0 + pp) * 0.5, v );
            // Calculate the RSDip
            double RSDip = Math.sqrt(Sr2) / Zmm * 100;
            // Calculate the B-ETI
            double f  = Qt * Math.sqrt(1 + 1 / (pn * Bj * Bj) ) * RSDip;
            _BETIh[i] = _deltaP[i] + f;
            _BETIl[i] = _deltaP[i] - f;
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

