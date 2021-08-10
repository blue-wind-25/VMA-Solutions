/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

//
// A Grubbs-test class
//
public class GrubbsTest {
    // Data
    private int      _N       = 0;
    private int      _S       = 0;

    private int      _N2      = 0;
    private double   _pp      = 0;
    private double   _t2      = 0;
    private double   _gc      = 0;

    private double[] _nValues = null;
    private double[] _sValues = null;
    private double[] _gValues = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct Grubbs-test class
    public GrubbsTest(double[] n, double[] s, double pp) throws Exception
    {
        // Check the number of data
        if(n.length < 3 || s.length < 1) throw new RuntimeException("Not enough data!");

        // Store the number of data
        _N = n.length;
        _S = s.length;

        // Allocate memory for the value arrays
        _nValues = new double[_N];
        _sValues = new double[_S];
        _gValues = new double[_S];

        // Copy the input values
        System.arraycopy(n, 0, _nValues, 0, _N);
        System.arraycopy(s, 0, _sValues, 0, _S);

        // Calculate N2 and t2
        _pp = pp;
        _N2 = _N + 1 - 2; // +1 because of the addition of one suspect
        _t2 = DistTable.t2(_pp, _N2);

        // Calculate the G-Crit
        _gc = ( (_N + 1 - 1) * _t2 ) // +1 because of the addition of one suspect
              /
              Math.sqrt
              (
                  (_N + 1) // +1 because of the addition of one suspect
                  *
                  (_N + 1 - 2 + _t2 * _t2) // +1 because of the addition of one suspect
              );

        // Calculate the G
        for(int i = 0; i < _sValues.length; ++i) {
            // Calculate the mean of the control values
            double mean = _sValues[i];
            for(int j = 0; j < _nValues.length; ++j) {
                mean += _nValues[j];
            }
            mean /= (_N + 1); // +1 because of the addition of one suspect
            System.out.println(mean);
            // Calculate the Sd
            double sd = 0;
            for(int j = 0; j < _nValues.length; ++j) {
                double dif = _nValues[j] - mean;
                sd += (dif * dif);
            }
            if(true) {
                double dif = _sValues[i] - mean;
                sd += (dif * dif);
            }
            sd = Math.sqrt(sd / (_N + 0)); // +1 because of the addition of one suspect
            System.out.println(sd);
            System.out.println();
            // Calculate G
            _gValues[i] = Math.abs( (_sValues[i] - mean) / sd );
        }
    }

    // Getters
    public int      getN()       { return _N; }
    public int      getS()       { return _S; }
    public int      getN2()      { return _N2; }

    public double   getPP()      { return _pp; }
    public double   getT2()      { return _t2; }

    public double   getGC()      { return _gc; }
    public double   getG(int i)  { return _gValues[i]; }
}
