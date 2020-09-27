/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A distribution-table lookup class
//
public class DistTable {
    private static double _pid2 = Math.PI * 0.5;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// F-Table                                     //////////////////////////////////////////////
    ///// Adapted from http://statpages.org/pdfs.html //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static double _statCom(double q, double i, double j, double b)
    {
        double zz = 1;
        double z  = zz;

        while(i <= j) {
            zz = zz * q * i / (i - b);
            z  = z + zz;
            i  = i + 2;
        }

        return z;
    }

    private static double _fishF(double f, double n1, double n2)
    {
        double x = n2 / (n1 * f + n2);

        if((n1 % 2) == 0) return _statCom(1 - x, n2, n1 + n2 - 4, n2 - 2) * Math.pow(x, n2 / 2);
        if((n2 % 2) == 0) return 1 - _statCom(x, n1, n1 + n2 - 4, n1 - 2) * Math.pow(1 - x, n1 / 2);

        double th  = Math.atan(Math.sqrt(n1 * f / n2));
        double a   = th / _pid2;
        double sth = Math.sin(th);
        double cth = Math.cos(th);

        if(n2 > 1) a = a + sth * cth * _statCom(cth * cth, 2, n2 - 3, -1) /_pid2;
        if(n1 == 1) return 1 - a;

        double c = 4 * _statCom(sth * sth, n2 + 1, n1 + n2 - 4, n2 - 2) * sth * Math.pow(cth, n2) / Math.PI;

        if(n2 == 1) return 1 - a + c / 2;

        double k = 2;
        while(k <= (n2 - 1) / 2) {
            c = c * k / (k - 0.5);
            k = k + 1;
        }

        return 1 -a + c;
    }

    // Calculate and return the one-tailed F-table value from the given probability (in procentage) and degrees of freedom.
    public static double F1(double pp, int df1, int df2)
    {
        double p = 1 - pp / 100;

        double v  = 0.5;
        double dv = 0.5;
        double f  = 0.5;

        while(dv > 0.0000000001) {
            f  = 1 / v - 1;
            dv = dv / 2;
            if(_fishF(f, df1, df2) > p) v = v - dv;
            else                        v = v + dv;
        }

        return f;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// t-Table                                     //////////////////////////////////////////////
    ///// Adapted from http://statpages.org/pdfs.html //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static double _studT(double t, int  n)
    {
        t = Math.abs(t);

        double w  = t / Math.sqrt(n);
        double th = Math.atan(w);

        if(n == 1) return 1 - th / _pid2;

        double sth = Math.sin(th);
        double cth = Math.cos(th);

        if((n % 2) == 1) return 1 - (th + sth * cth * _statCom(cth * cth, 2, n - 3, -1)) / _pid2;
        else             return 1 - sth * _statCom(cth * cth, 1, n - 3, -1);
    }

    private static double _t(double p, int df)
    {
        double v  = 0.5;
        double dv = 0.5;
        double t  = 0.5;

        while(dv > 0.000001) {
            t  = 1 / v -1;
            dv = dv / 2;
            if(_studT(t, df) > p) v = v - dv;
            else                  v = v + dv;
        }

        return t;
    }

    // Calculate and return the two-tailed t-table value from the given probability (in procentage) and degree of freedom.
    public static double t2(double pp, int df)
    { return _t(1 - pp / 100, df); }

    // Calculate and return the one-tailed t-table value from the given probability (in procentage) and degree of freedom.
    public static double t1(double pp, int df)
    { return _t(2 * (1 - pp / 100), df); }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// t-Table with fractional degree of freedom                                           //////
    ///// Adapted from http://www.tutor-homework.com/statistics_tables/statistics_tables.html //////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    private static double _normZ(double p) {
        double a0 =  2.5066282,  a1 = -18.6150006, a2 =  41.3911977, a3 = -25.4410605,
               b1 = -8.4735109,  b2 =  23.0833674, b3 = -21.0622410, b4 =   3.1308291,
               c0 = -2.7871893,  c1 =  -2.2979648, c2 =   4.8501413, c3 =   2.3212128,
               d1 =  3.5438892,  d2 =   1.6370678;

        double r, z;

        if (p > 0.42) {
            r = Math.sqrt(-Math.log(0.5 - p));
            z = (((c3 * r + c2) * r + c1) * r + c0) / ((d2 * r + d1) * r + 1);
        }
        else {
            r = p * p;
            z = p * (((a3 * r + a2) * r + a1) * r + a0) / ((((b4 * r + b3) * r + b2) * r + b1) * r + 1);
        }
        return z;
    }

    private static double _hillsInvT(double p, double df) {
        double a, b, c, d, t, x, y;

        if(df == 1)
            t = Math.cos(p * _pid2) / Math.sin(p * _pid2);
        else if(df == 2)
            t = Math.sqrt(2 / (p * (2 - p)) - 2);
        else {
            a = 1 / (df - 0.5);
            b = 48 / (a * a);
            c = ((20700 * a / b - 98) * a - 16) * a + 96.36;
            d = ((94.5 / (b + c) - 3) / b + 1) * Math.sqrt(a * _pid2) * df;
            x = d * p;
            y = Math.pow(x, 2 / df);
            if(y > 0.05 + a) {
                x = _normZ(0.5 * (1 - p));
                y = x * x;
                if(df < 5) c = c + 0.3 * (df - 4.5) * (x + 0.6);
                c = (((0.05 * d * x - 5) * x - 7) * x - 2) * x + b + c;
                y = (((((0.4 * y + 6.3) * y + 36) * y + 94.5) / c - y - 3) / b + 1) * x;
                y = a * y * y;
                if(y > 0.002) y = Math.exp(y) - 1;
                else          y = 0.5 * y * y + y;
                t = Math.sqrt(df * y);
            }
            else {
                y = ((1 / (((df + 6) / (df * y) - 0.089 * d - 0.822) * (df + 2) * 3) + 0.5 / (df + 4)) * y - 1) * (df + 1) / (df + 2) + 1 / y;
                t = Math.sqrt(df * y);
            }
        }

        return t;
    }
    
    private static double _T_z(double t, double df) {
        double A9 = df - 0.5;
        double B9 = 48 * A9 * A9;
        double T9 = t * t / df;

        double Z8;
        if(T9 >= 0.04) Z8 = A9 * Math.log(1 + T9);
        else           Z8 = A9 * (((1 - T9 * 0.75) * T9 / 3 - 0.5) * T9 + 1) * T9;

        double P7 = ((0.4 * Z8 + 3.3) * Z8 + 24) * Z8 + 85.5;
        double B7 = 0.8 * Math.pow(Z8, 2) + 100 + B9;

        return (1 + (-P7 / B7 + Z8 + 3) / B9) * Math.sqrt(Z8);
    }

    private static double _T_p(double t, double df) {
        double abst = Math.abs(t);
        double tsq =  t * t;

        double p;
             if(df == 1) p = 1 - 2 * Math.atan(abst) / Math.PI;
        else if(df == 2) p = 1 - abst / Math.sqrt(tsq + 2);
        else if(df == 3) p = 1 - 2 * (Math.atan(abst / Math.sqrt(3)) + abst * Math.sqrt(3) / (tsq + 3)) / Math.PI;
        else if(df == 4) p = 1 - abst * (1 + 2 / (tsq + 4)) / Math.sqrt(tsq + 4);
        else {
            double z = _T_z(abst, df);
            p = _normZ(z);
        }

        return p;
    }

    private static double _backwardT(double pp, double df)
    {
        double p0   = pp;
        double p1   = p0;
        double t    = 0;
        double diff = 1;

        while(Math.abs(diff) > 0.0000000001) {
            t = _hillsInvT(p1, df);
            diff = _T_p(t,df) - p0;
            p1 -= diff;
        }

        return t;
    }

    // Calculate and return the one-tailed t-table value from the given probability (in procentage) and degree of freedom.
    public static double ft1(double pp, double df)
    { return _backwardT(2 * (1 - pp / 100), df); }

    // Calculate and return the two-tailed t-table value from the given probability (in procentage) and degree of freedom.
    public static double ft2(double pp, double df)
    { return _backwardT(1 - pp / 100, df); }
}
