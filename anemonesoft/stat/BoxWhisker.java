/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import java.util.*;

//
// A box-whisker  class
//
public class BoxWhisker {
    // A result class
    public class Result {
        public double   x;
        public double   min, q1, q2, q3, max, mean;
        public double[] outliers;
        public boolean  valid;

        Result()
        { x = 0; min = 0; q1 = 0; q2 = 0; q3 = 0; max = 0; mean = 0; outliers = null; valid = false; }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    // Result
    ArrayList<Result> _result = new ArrayList<Result>();

    // Process one series
    private Result _processOne(double seriesData[])
    {
        // The result
        Result res = new Result();

        // Copy and sort the data
        double   x = seriesData[0];
        double[] y = new double[seriesData.length - 1];

        System.arraycopy(seriesData, 1, y, 0, seriesData.length - 1);
        Arrays.sort(y);

        // Calculate the q1, q2, and q3
        if( (y.length % 2) != 0 ) { // Odd
            // N# 0 1 2 3 4 5 6 7 8 | 9 4
            // Q#   1 1   2   3 3
            // N# 0 1 2 3 4 5 6     | 7 3
            // Q#   1   2   3
            // N# 0 1 2 3 4         | 5 2
            // Q# 1 1 2 3 3
            int q2Pos = y.length / 2;
            res.q2 = y[q2Pos];
            if( (((y.length - 1) / 2) % 2) != 0 ) { // Odd
                int q1Pos = y.length / 4;
                int q3Pos = y.length * 3 / 4;
                res.q1 = y[q1Pos];
                res.q3 = y[q3Pos];
            }
            else { // Even
                int q1Pos = y.length / 4 - 1;
                int q3Pos = y.length * 3 / 4;
                res.q1 = ( y[q1Pos] + y[q1Pos + 1] ) / 2;
                res.q3 = ( y[q3Pos] + y[q3Pos + 1] ) / 2;
            }
        }
        else { // Even
            // N# 0 1 2 3 4 5 6 7 8 9 | 10 4 e
            // Q#     1   2 2   3  
            // N# 0 1 2 3 4 5 6 7     |  8 3 o
            // Q#   1 1 2 2 3 3
            // N# 0 1 2 3 4 5         |  6 2 e
            // Q#   1 2 2 3
            int q2Pos = y.length / 2 - 1;
            res.q2 = ( y[q2Pos] + y[q2Pos + 1] ) / 2;
            if( (((y.length - 2) / 2) % 2) != 0 ) { // Odd
                int q1Pos = y.length / 4 - 1;
                int q3Pos = y.length * 3 / 4 - 1;
                res.q1 = ( y[q1Pos] + y[q1Pos + 1] ) / 2;
                res.q3 = ( y[q3Pos] + y[q3Pos + 1] ) / 2;
            }
            else { // Even
                int q1Pos = y.length / 4;
                int q3Pos = y.length * 3 / 4;
                res.q1 = y[q1Pos];
                res.q3 = y[q3Pos];
            }
        }

        // Calculate the mean
        double sum = 0;
        for(int i = 0; i < y.length; ++i) {
            sum += y[i];
        }
        res.mean = sum / y.length;

        // Calculate the minimum and maximum limit
        double IQR15  = (res.q3 - res.q1) * 1.5;
        double minLim = res.q1 - IQR15;
        double maxLim = res.q3 + IQR15;

        // Find the minimum and maximum values of the data and the outliers
        ArrayList<Double> outliers = new ArrayList<Double>();
        double            min      =  Double.MAX_VALUE;
        double            max      = -Double.MAX_VALUE;
        for(int i = 0; i < y.length; ++i) {
            double yi = y[i];
            if(yi < minLim || yi > maxLim) {
                outliers.add(yi);
                continue;
            }
            if(yi < min) min = yi;
            if(yi > max) max = yi;
        }
        res.min = min;
        res.max = max;
        
        res.outliers = new double[outliers.size()];
        for(int i = 0; i < outliers.size(); ++i) {
            res.outliers[i] = outliers.get(i);
        }
        
        // Return the result
        res.x     = x;
        res.valid = true;
        return res;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a box-whisker class
    public BoxWhisker(double[][] y) throws Exception
    {
        for(int i = 0; i < y.length; ++i) {
            if(y[i].length < 4) {
                _result.add(new Result());
                continue;
            }
            _result.add(_processOne(y[i]));
        }
    }

    // Getters
    public int    getNumOfSeries()         { return _result.size(); }
    public Result getSeriesResult(int idx) { return _result.get(idx); }
}
