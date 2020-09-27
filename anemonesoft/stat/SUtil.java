/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import java.util.*;

//
// A class that contains some statistical-utility functions
//
public class SUtil {
    // Epsilon
    public static final double Epsilon = 2.220446049250313E-16;

    // Clone a matrix
    public static final int[][] clone(final int[][] mat)
    {
        final int     l   = mat[0].length;
        final int[][] ret = new int[mat.length][l];

        int i = 0;
        do {
            System.arraycopy(mat[i], 0, ret[i], 0, l);
        } while(++i < mat.length);

        return ret;
    }

    // Clone a matrix
    public static final double[][] clone(final double[][] mat)
    {
        final int        l   = mat[0].length;
        final double[][] ret = new double[mat.length][l];

        int i = 0;
        do {
            System.arraycopy(mat[i], 0, ret[i], 0, l);
        } while(++i < mat.length);

        return ret;
    }

    // Transpose the given matrix
    public static final double[][] transpose(final double[][] mat)
    {
        final int rc = mat.length;
        final int cc = mat[0].length;

        final double[][] ret = new double[cc][rc];

        int i = 0;
        do {
            int            j = 0;
            final double[] m = mat[i];
            do {
                ret[j][i] = m[j];
            } while(++j < cc);
        } while(++i < rc);

        return ret;
    }

    // Transpose the given matrix (with a scalar offset)
    public static final int[][] transpose(final int[][] mat, final int scalarOffset)
    {
        final int rc = mat.length;
        final int cc = mat[0].length;

        final int[][] ret = new int[cc][rc];

        int i = 0;
        do {
            int         j = 0;
            final int[] m = mat[i];
            do {
                ret[j][i] = m[j] + scalarOffset;
            } while(++j < cc);
        } while(++i < rc);

        return ret;
    }

    // Select a matrix's elements based on the given selector in row major mode (i.e. select the columns)
    public static final double[][] matselrm(final double[][] mat, final boolean[] selector)
    {
        int sumOfTrue = 0;
        int i         = 0;
        do {
            if(selector[i]) ++sumOfTrue;
        } while(++i < selector.length);
        
        final double[][] ret = new double[mat.length][sumOfTrue];
        int              k   = 0;
        do {
            int            inIdx = 0;
            int            w     = 0;
            final double[] r     = ret[k];
            final double[] m     = mat[k];
            do {
                if(selector[w]) r[inIdx++] = m[w];
            } while(++w < selector.length);
        } while(++k < ret.length);

        return ret;
    }

    // Select a matrix's elements based on the given selector in row major mode (i.e. select the columns) (with a scalar offset)
    public static final int[][] matselrm(final int[][] mat, final boolean[] selector, final int scalarOffset)
    {
        int sumOfTrue = 0;
        int i         = 0;
        do {
            if(selector[i]) ++sumOfTrue;
        } while(++i < selector.length);

        final int[][] ret = new int[mat.length][sumOfTrue];
        int           k   = 0;
        do {
            int         inIdx = 0;
            int         w     = 0;
            final int[] r     = ret[k];
            final int[] m     = mat[k];
            do {
                if(selector[w]) r[inIdx++] = m[w] + scalarOffset;
            } while(++w < selector.length);
        } while(++k < ret.length);

        return ret;
    }

    // Convert a matrix into a vector in column major mode
    public static final int[] mat2veccm(final int[][] mat)
    {
        final int   maxc = mat[0].length;
        final int[] ret  = new int[mat.length * maxc];

        int idx  = 0;
        int c    = 0;
        do {
            int r = 0;
            do {
                ret[idx++] = mat[r][c];
            } while(++r < mat.length);
        } while(++c < maxc);

        return ret;
    }

    // Convert a matrix into a vector in column major mode
    public static final double[] mat2veccm(final double[][] mat)
    {
        final int      maxc = mat[0].length;
        final double[] ret  = new double[mat.length * maxc];

        int idx  = 0;
        int c    = 0;
        do {
            int r = 0;
            do {
                ret[idx++] = mat[r][c];
            } while(++r < mat.length);
        } while(++c < maxc);

        return ret;
    }

    // Calculate the sum of all the matrix's elements (treats the columns as vectors, returning a row vector of the sums of each column)
    public static final double[] sum(final double[][] mat)
    {
        final int      l   = mat[0].length;
        final double[] ret = new double[l];

        int i = 0;
        do {
            int            j = 0;
            final double[] m = mat[i];
            do {
                ret[j] += m[j];
            } while(++j < l);
        } while(++i < mat.length);
        
        return ret;
    }

    // Calculate the sum of all the squared matrix's elements (treats the columns as vectors, returning a row vector of the squared-sums of each column)
    public static final double[] sumsqrelem(final double[][] mat)
    {
        final int      l   = mat[0].length;
        final double[] ret = new double[l];

        int i = 0;
        do {
            int            j = 0;
            final double[] m = mat[i];
            do {
                final double v = m[j];
                ret[j] += v * v;
            } while(++j < l);
        } while(++i < mat.length);

        return ret;
    }
    
    // Dump a matrix
    public static final void _dump(final double[][] mat)
    {
        for(int a = 0; a < mat.length; ++a) {
            for(int b = 0; b < mat[0].length; ++b) {
                System.out.printf("%g", mat[a][b]);
                if(b < mat[0].length - 1) System.out.printf(",");
            }
            System.out.printf("\n");
        }
    }

    // Dump an integer matrix
    public static final void _dump(final int[][] mat)
    {
        for(int a = 0; a < mat.length; ++a) {
            for(int b = 0; b < mat[0].length; ++b) {
                System.out.printf("%d", mat[a][b]);
                if(b < mat[0].length - 1) System.out.printf(",");
            }
            System.out.printf("\n");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Create a vector of ones
    public static final int[] ones(final int dim)
    {
        final int[] ret = new int[dim];

        Arrays.fill(ret, 1);

        return ret;
    }

    // Create a vector of values
    public static final int[] valuevec(final int dim, final int value)
    {
        final int[] ret = new int[dim];

        Arrays.fill(ret, value);

        return ret;
    }

    // Create a vector of values that fall between the given range
    public static final int[] rangevec(final int initial, final int increment, final int terminator)
    {
        /*
        int count = 0;
        int d     = initial;
        while(d <= terminator) {
            ++count;
            d += increment;
        }
        */
        final int   count = (terminator - initial) / increment + 1;
        final int[] ret   = new int[count];
        int         v     = initial;
        int         i     = 0;
        while(v <= terminator) {
            ret[i++] = v;
            v += increment;
        }

        return ret;
    }

    // Create a vector of values that fall between the given range (with a scalar scalling)
    public static final int[] rangevec(final int initial, final int increment, final int terminator, final int scalarScalling)
    {
        final int   count = (terminator - initial) / increment + 1;
        final int[] ret   = new int[count];
        int         v     = initial;
        int         i     = 0;
        while(v <= terminator) {
            ret[i++] = v * scalarScalling;
            v += increment;
        }

        return ret;
    }
    
    // Create a vector of values that fall between the given range (with a scalar scalling)
    public static final double[] rangevec(final int initial, final int increment, final int terminator, final double scalarScalling)
    {
        final int   count = (terminator - initial) / increment + 1;
        final double[] ret   = new double[count];
        double         v     = initial;
        int            i     = 0;
        while(v <= terminator) {
            ret[i++] = v * scalarScalling;
            v += increment;
        }

        return ret;
    }

    // Create a vector of values that fall between the given range (with a scalar scalling and a scalar offset)
    public static final double[] rangevec(final int initial, final int increment, final int terminator, final double scalarScalling, final double scalarOffset)
    {
        final int   count = (terminator - initial) / increment + 1;
        final double[] ret   = new double[count];
        double         v     = initial;
        int            i     = 0;
        while(v <= terminator) {
            ret[i++] = v * scalarScalling + scalarOffset;
            v += increment;
        }

        return ret;
    }

    // Select a vector's elements based on the given selector
    public static final int[] vecsel(final int[] vec, final boolean[] selector)
    {
        int sumOfTrue = 0;
        int i         = 0;
        do {
            if(selector[i]) ++sumOfTrue;
        } while(++i < selector.length);

        final int[] ret = new int[sumOfTrue];

        int k   = 0;
        int idx = 0;
        do {
            if(selector[k]) ret[idx++] = vec[k];
        } while(++k < selector.length);

        return ret;
    }

    // Select a vector's elements based on the given selector
    public static final double[] vecsel(final double[] vec, final boolean[] selector)
    {
        int sumOfTrue = 0;
        int i         = 0;
        do {
            if(selector[i]) ++sumOfTrue;
        } while(++i < selector.length);

        final double[] ret = new double[sumOfTrue];

        int k   = 0;
        int idx = 0;
        do {
            if(selector[k]) ret[idx++] = vec[k];
        } while(++k < selector.length);

        return ret;
    }

    // Select a vector's elements based on the given selector (with a scalar offset)
    public static final int[] vecsel(final int[] vec, final boolean[] selector, final int scalarOffset)
    {
        int sumOfTrue = 0;
        int i         = 0;
        do {
            if(selector[i]) ++sumOfTrue;
        } while(++i < selector.length);

        final int[] ret = new int[sumOfTrue];

        int k   = 0;
        int idx = 0;
        do {
            if(selector[k]) ret[idx++] = vec[k] + scalarOffset;
        } while(++k < selector.length);

        return ret;
    }
    
    // Reverse the vector's elements order and return them as a new vector
    public static final int[] reverse(final int[] vec)
    {
        final int[] ret = new int[vec.length];

        int       i = 0;
        final int l = vec.length - 1;
        do {
            ret[i] = vec[l - i];
        } while(++i < vec.length);

        return ret;
    }

    // Prepend an element to the beginning of vector
    public static final int[] prepend(final int value, final int[] vec)
    {
        final int[] ret = new int[1 + vec.length];

        ret[0] = value;
        System.arraycopy(vec, 0 , ret, 1, vec.length);

        return ret;
    }

    // Erase an element in the vector
    public static final int[] erase(final int[] vec, final int pos)
    {
        final int   l1  = vec.length - 1;
        final int[] ret = new int[l1];

        if(pos > 0             ) System.arraycopy(vec, 0,        ret, 0,   pos     );
        if(pos < vec.length - 1) System.arraycopy(vec, pos + 1 , ret, pos, l1 - pos);

        return ret;
    }

    // Erase an element in the vector
    public static final double[] erase(final double[] vec, final int pos)
    {
        final int      l1  = vec.length - 1;
        final double[] ret = new double[l1];

        if(pos > 0             ) System.arraycopy(vec, 0,        ret, 0,   pos         );
        if(pos < vec.length - 1) System.arraycopy(vec, pos + 1 , ret, pos, l1 - pos - 1);

        return ret;
    }
    
    // Slice a vector
    public static final double[] slice(final double[] vec, final int from, final int to)
    { return Arrays.copyOfRange(vec, from, to + 1); }

    // Convert a vector into a matrix in column major mode
    public static final double[][] vec2matcm(final double[] vec, final int dim0, final int dim1)
    {
        final double[][] ret = new double[dim0][dim1];

        int cmax = ret[0].length;
        int c    = 0;
        int idx  = 0;
        do {
            int r = 0;
            do {
                ret[r][c] = vec[idx++];
            } while(++r < ret.length);
        } while(++c < cmax);
        
        return ret;
    }

    // Add the vector's elements by the given scalar
    public static final int[] sadd(final int val, final int[] vec)
    {
        final int[] ret = new int[vec.length];

        int i = 0;
        do {
            ret[i] = vec[i] + val;
        } while (++i < vec.length);

        return ret;
    }
    
    // Add the vector's elements by the given scalar
    public static final double[] sadd(final double val, final double[] vec)
    {
        final double[] ret = new double[vec.length];

        int i = 0;
        do {
            ret[i] = vec[i] + val;
        } while (++i < vec.length);

        return ret;
    }

    // Add the vector's elements by the given scalar and then negate the result
    public static final int[] saddn(final int val, final int[] vec)
    {
        final int[] ret = new int[vec.length];

        int i = 0;
        do {
            ret[i] = -(vec[i] + val);
        } while (++i < vec.length);

        return ret;
    }

    // Multiply the vector's elements by the given scalar
    public static final double[] smul(final double val, final double[] vec)
    {
        final double[] ret = new double[vec.length];

        int i = 0;
        do {
            ret[i] = vec[i] * val;
        } while (++i < vec.length);

        return ret;
    }

    // Square the vector's elements (with scalling)
    public static final double[] sqrelem(final double[] vec, double scalling)
    {
        final double[] ret = new double[vec.length];

        int i = 0;
        do {
            final double v = vec[i];
            ret[i] = v * v * scalling;
        } while (++i < vec.length);

        return ret;
    }
    
    // Raise the vector's elements to the power of the given scalar
    public static final double[] powelem(final double[] vec, final double power)
    {
        final double[] ret = new double[vec.length];

        int i = 0;
        do {
            ret[i] = Math.pow(vec[i], power);
        } while (++i < vec.length);

        return ret;
    }
    
    // Add the two vectors (both vectors must have the same size)
    public static final int[] add(final int[] vec1, final int[] vec2)
    {
        final int[] ret = new int[vec1.length];

        int i = 0;
        do {
            ret[i] = vec1[i] + vec2[i];
        } while (++i < vec1.length);

        return ret;
    }

    // Add the vector 1 with the multiplication results of vector 2 and 3 (all three vectors must have the same size)
    public static final double[] addmul(final double[] vec1, final double[] vec2, final double[] vec3)
    {
        final double[] ret = new double[vec1.length];

        int i = 0;
        do {
            ret[i] = vec1[i] + (vec2[i] * vec3[i]);
        } while (++i < vec1.length);

        return ret;
    }

    // Add the two vectors (both vectors must have the same size)
    public static final double[] add(final double[] vec1, final double[] vec2)
    {
        final double[] ret = new double[vec1.length];

        int i = 0;
        do {
            ret[i] = vec1[i] + vec2[i];
        } while (++i < vec1.length);

        return ret;
    }

    // Substract the two vectors and square-root the result (both vectors must have the same size)
    public static final double[] sqrtsub(final double[] vec1, final double[] vec2)
    {
        final double[] ret = new double[vec1.length];

        int i = 0;
        do {
            ret[i] = Math.sqrt(vec1[i] - vec2[i]);
        } while (++i < vec1.length);

        return ret;
    }
    
    // Multiply the two vectors (both vectors must have the same size)
    public static final double[] mul(final double[] vec1, final double[] vec2)
    {
        final double[] ret = new double[vec1.length];

        int i = 0;
        do {
            ret[i] = vec1[i] * vec2[i];
        } while (++i < vec1.length);

        return ret;
    }
    
    // Divide the two vectors and multiply the result with the given scalar; if the final result is NaN, replace it with zero (both vectors must have the same size)
    public static final double[] divEx(final double[] vec1, final double[] vec2, final double scalarScalling)
    {
        final double[] ret = new double[vec1.length];

        int i = 0;
        do {
            final double res = vec1[i] / vec2[i] * scalarScalling;
            ret[i] = Double.isNaN(res) ? 0 : res;
        } while (++i < vec1.length);

        return ret;
    }
    
    // Get the minimum of the vector's elements
    public static final int min(final int[] vec)
    {
        int ret = vec[0];

        int i = 1;
        do {
            final int v = vec[i];
            if(v < ret) ret = v;
        } while (++i < vec.length);

        return ret;
    }

    // Get the minimum of the vector's elements
    public static final double min(final double[] vec)
    {
        double ret = vec[0];

        int i = 1;
        do {
            final double v = vec[i];
            if(v < ret) ret = v;
        } while (++i < vec.length);

        return ret;
    }

    // Get the maximum of the vector's elements
    public static final int max(final int[] vec)
    {
        int ret = vec[0];

        int i = 1;
        do {
            final int v = vec[i];
            if(v > ret) ret = v;
        } while (++i < vec.length);

        return ret;
    }

    // Get the maximum of the vector's elements
    public static final double max(final double[] vec)
    {
        double ret = vec[0];

        int i = 1;
        do {
            final double v = vec[i];
            if(v > ret) ret = v;
        } while (++i < vec.length);

        return ret;
    }

    // Calculate the 2-norm of the vectors's elements
    public static final double norm2(final double[] vec)
    {
        double ret = 0.0;

        int i = 0;
        do {
            final double v = vec[i];
            ret += v * v;
        } while(++i < vec.length);
        
        ret = Math.sqrt(ret);
        
        return ret;
    }
    
    // Calculate the sum of all the vector's elements
    public static final double sum(final double[] vec)
    {
        double ret = 0.0;

        int i = 0;
        do {
            ret += vec[i];
        } while(++i < vec.length);

        return ret;
    }

    // Calculate the cumulative-sum of all the vector's elements
    public static final int[] cumsum(final int[] vec)
    {
        final int[] ret = new int[vec.length];

        ret[0] = vec[0];

        int i = 1;
        do {
            ret[i] = vec[i] + ret[i - 1];
        } while(++i < vec.length);
        
        return ret;
    }

    // Find the sign of all the vector's elements
    public static final int[] sign(final double[] vec)
    {
        final int[] ret = new int[vec.length];

        int i = 0;
        do {
            double v = vec[i];
                 if(v < 0) ret[i] = -1;
            else if(v > 0) ret[i] =  1;
        } while(++i < vec.length);
        
        return ret;
    }
    
    // Calculate the differences between the vector's elements
    public static final double[] diff(final double[] vec)
    {
        final double[] ret = new double[vec.length - 1];

        int i    = 0;
        int imax = vec.length - 1;
        do {
            ret[i] = vec[i + 1] - vec[i];
        } while(++i < imax);

        return ret;
    }

    // Calculate the number of values in the vector that fall between the elements in the edges vector
    public static final class HistCResult {
        public int[] n;
        public int[] bin;
    };

    public static final HistCResult histc(final double[] vec, final int edges[])
    {
        HistCResult ret = new HistCResult();
        ret.n   = new int[edges.length];
        ret.bin = new int[vec.length  ];

        int       i  = 0;
        final int el = edges.length - 1;
        do {
            final double min   = edges[i    ];
            final double max   = edges[i + 1];
            int          count = 0;
            int          j     = 0;
            do {
                if(min <= vec[j] && vec[j] < max) {
                    ret.bin[j] = i + 1;
                    ++count;
                }
            } while(++j < vec.length);
            ret.n[i] = count;
        } while(++i < el);

        double last  = edges[el];
        int    count = 0;
        int    j     = 0;
        do {
            if(vec[j] == last) {
                ++count;
                ret.bin[j] = edges.length;
            }
        } while(++j < vec.length);
        ret.n[el] = count;

        return ret;
    }

    // Perform linear interpolation
    public static final double[] interp1q(final int[] x, final double[] y, final double[] xi)
    {
        final double[] ret = new double[xi.length];

        int            yidx = 0;
        final int      lmin = min(x);
        final int      lmax = max(x);
        final double[] lut  = new double[lmax + 1];
        int            i    = lmin;
        do {
            lut[i] = y[yidx++];
        } while(++i <= lmax);

        i = 0;
        do {
            final double ival = xi[i];
            final int    imin = (int) Math.floor(ival);
            final int    imax = (int) Math.ceil (ival);
            final double idel = ival - imin;
            final double vl   = lut[imin];
            final double vr   = lut[imax];
            ret[i] = (vr * idel) + (vl * (1.0 - idel));
        } while(++i < xi.length);
        
        return ret;
    }
    
    // Dump a vector
    public static final void _dump(final double[] vec)
    {
        for(int a = 0; a < vec.length; ++a) {
            System.out.printf("%g", vec[a]);
            if(a < vec.length - 1) System.out.printf(",");
        }
        System.out.printf("\n");
    }
    
    // Dump an integer vector
    public static final void _dump(final int[] vec)
    {
        for(int a = 0; a < vec.length; ++a) {
            System.out.printf("%d", vec[a]);
            if(a < vec.length - 1) System.out.printf(",");
        }
        System.out.printf("\n");
    }
}

