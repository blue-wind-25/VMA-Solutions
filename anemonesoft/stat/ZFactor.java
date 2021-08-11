/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A z-factor class
//
public class ZFactor {
    // Data
    private int    _T   = 0;
    private int    _B   = 0;

    private double _Mt  = 0;
    private double _Mb  = 0;

    private double _Sdt = 0;
    private double _Sdb = 0;

    private double _Z   = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct z-factor class
    public ZFactor(double[] t, double[] b) throws Exception
    {
        // Check the number of data
        if(t.length < 3 || b.length < 3) throw new RuntimeException("Not enough data!");

        // Store the number of data
        _T = t.length;
        _B = b.length;

        // Calculate the mean of the T and B values
        for(int i = 0; i < t.length; ++i) {
            _Mt += t[i];
        }
        _Mt /= _T;

        for(int i = 0; i < b.length; ++i) {
            _Mb += b[i];
        }
        _Mb /= _B;

        // Calculate the Sd of the T and B values
        for(int i = 0; i < t.length; ++i) {
            double dif = t[i] - _Mt;
            _Sdt += (dif * dif);
        }
        _Sdt = Math.sqrt(_Sdt / (_T - 1));

        for(int i = 0; i < b.length; ++i) {
            double dif = b[i] - _Mb;
            _Sdb += (dif * dif);
        }
        _Sdb = Math.sqrt(_Sdb / (_B - 1));

        // Calculate the Z
        _Z = 1.0 - ((3.0 * (_Sdt - _Sdb)) / (_Mt - _Mb));
    }

    // Getters
    public int    getT()   { return _T; }
    public int    getB()   { return _B; }

    public double getMt()  { return _Mt; }
    public double getMb()  { return _Mb; }

    public double getSdt() { return _Sdt; }
    public double getSdb() { return _Sdb; }

    public double getZ()   { return _Z; }
}
