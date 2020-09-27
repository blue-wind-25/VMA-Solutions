/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// An RSD-based precision-test class
//
public class PrecisionRSD {
    // Data
    private int      _N      = 0;
    private double[] _values = null;

    private double   _mean   = 0;
    private double   _Sd     = 0;
    private double   _RSD    = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct an RSD-based precision-test class
    public PrecisionRSD(double v[]) throws Exception
    {
        // Check the number of data
        if(v.length < 3) throw new RuntimeException("Not enough data!");
        
        // Store the number of data
        _N  = v.length;

        // Allocate memory for the value array and copy the values
        _values = new double[_N];
        System.arraycopy(v, 0, _values, 0, _N);

        // Calculate the mean
        for(int i = 0; i < _values.length; ++i) {
            _mean += _values[i];
        }
        _mean /= _N;

        // Calculate the Sd
        for(int i = 0; i < _values.length; ++i) {
            double dif = _values[i] - _mean;
            _Sd += (dif * dif);
        }
        _Sd = Math.sqrt(_Sd / (_N - 1));

        // Calculate the RSD
        _RSD = _Sd / _mean * 100;
    }

    // Getters
    public int      getN()       { return _N; }

    public double[] getXValues() { return _values; }
    
    public double   getMean()    { return _mean; }
    public double   getSd()      { return _Sd; }
    public double   getRSD()     { return _RSD; }
}
