/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.stat;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

//
// A class that contains functions related to chromatogram analysis
//
public class Chromatogram {
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// Correlation Optimized Warping (COW)           ////////////////////////////////////////////
    ///// Adapted from http://www.models.kvl.dk/dtw_cow ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Interpolate coefficient (used by COW)
    private static class ICResult {
        double[][] coeff;
        int   [][] index;
    };

    private static ICResult _interpCoeff(final int n, final int[] nprime, final int[] offset)
    {
        int p = nprime.length;
        int q = n - 1;

        ICResult ret = new ICResult();
        ret.coeff = new double[p][n];
        ret.index = new int   [p][n];

        int  cumLoopCnt = 0;                 // Cumulative loop counter
        long startTime  = System.nanoTime(); // Start time

        int i = 0;
        do {
            final int   []          pp  = SUtil.rangevec(1, 1, nprime[i]);
            final double[]          r   = SUtil.rangevec(0, 1, q, (double) (nprime[i] - 1) / q, 1);
            final SUtil.HistCResult hcr = SUtil.histc(r, pp);
            final int   []          k   = hcr.bin;
            int                     j   = 0;

            do {
                final int npi = nprime[i];
                if(r[j] <  1  ) k[j] = 1;
                if(r[j] >= npi) k[j] = npi - 1;
            } while(++j < r.length);

            final double[] coeffi = ret.coeff[i];
            final int   [] indexi = ret.index[i];
            int            w      = 0;
            do {
                coeffi[w] = 1 + r[w] - pp[k[w]];
                indexi[w] = k[w] - offset[i];
            } while(++w < coeffi.length);

            // Check if the operation would takes too much time
            cumLoopCnt += (r.length + coeffi.length);
            if(cumLoopCnt > 10000000 && (System.nanoTime() - startTime) > 30000000000L) throw new RuntimeException("The operation would take too much time to complete!");
        } while(++i < p);

        return ret;
    }

    // Perform COW
    public static double[] performCOW(final double[] target, final double[] signal, final int segLen, final int slack, final double correlationPower)
    {
        // Check the correlation power
        if(correlationPower < 1.0 || correlationPower > 4.0)
            throw new RuntimeException("The correlation power must be in the range of 1.0 to 4.0!");

        // Get lengths
        final int pT = target.length; // Number of data points in the target (T)
        final int pX = signal.length; // Number of data points in the signal (X)

        // Initialize segments
        if(segLen > Math.min(pT, pX))
            throw new RuntimeException("The segment length cannot be larger than the length of the target and signal!");

        final int numOfSeg = (int) Math.floor((pT - 1) / segLen);
        
        final int[] segLenT = SUtil.valuevec(numOfSeg, (int) Math.floor((pT - 1) / numOfSeg));
        int         temp    = (pT - 1) % segLenT[0];
        if(temp > 0) segLenT[segLenT.length - 1] += temp;
        for(int i = 0; i < segLenT.length; ++i) {
            if(segLenT[i] <= slack + 2)
                throw new RuntimeException("The slack cannot be larger than the length of the segment!");
        }

        final int[] segLenX = SUtil.valuevec(numOfSeg, (int) Math.floor((pX - 1) / numOfSeg));
                    temp    = (pX - 1) % segLenT[0];
        if(temp > 0) segLenX[segLenX.length - 1] += temp;
        for(int i = 0; i < segLenX.length; ++i) {
            if(segLenX[i] <= slack + 2)
                throw new RuntimeException("The slack cannot be larger than the length of the segment!");
        }

        final int[] bT = SUtil.cumsum(SUtil.prepend(1, segLenT));
        final int[] bX = SUtil.cumsum(SUtil.prepend(1, segLenX));

        // Initialize slack vector
        final int[] slackVec = SUtil.rangevec(-slack, 1, slack);

        // Initialize slope constraints
        final int[] offset0 = SUtil.rangevec(0, 1, numOfSeg, -slack);
        final int[] offset1 = SUtil.rangevec(0, 1, numOfSeg,  slack);

        final int[] boundsA0 = SUtil.add(bX, offset0);
        final int[] boundsA1 = SUtil.add(bX, offset1);

        final int[] boundsB0 = SUtil.add(bX, SUtil.reverse(offset0));
        final int[] boundsB1 = SUtil.add(bX, SUtil.reverse(offset1));

        final int[] bounds0 = SUtil.ones(numOfSeg + 1);
        final int[] bounds1 = SUtil.ones(numOfSeg + 1);
        for(int i = 0; i < bounds0.length; ++i) {
            bounds0[i] = Math.max(boundsA0[i], boundsB0[i]);
            bounds1[i] = Math.min(boundsA1[i], boundsB1[i]);
        }

        // Calculate the first derivatives for interpolation
        final double[] xDiff = SUtil.diff(signal);

        // Calculate coefficients and indexes for interpolation
        ICResult icr = _interpCoeff(segLenT[0] + 1, SUtil.sadd(segLenX[0] + 1, slackVec), slackVec);

        final double[][][] intCoeff = new double[numOfSeg][][];
        intCoeff[0] = icr.coeff;
        for(int i = 1; i < intCoeff.length - 1; ++i) intCoeff[i] = SUtil.clone(icr.coeff);
        
        final int[][][] intIndex = new int[numOfSeg][][];
        intIndex[0] = icr.index;
        for(int i = 1; i < intIndex.length - 1; ++i) intIndex[i] = SUtil.clone(icr.index);

        icr = _interpCoeff(segLenT[numOfSeg - 1] + 1, SUtil.sadd(segLenX[numOfSeg - 1] + 1, slackVec), slackVec);
        intCoeff[intCoeff.length - 1] = icr.coeff;
        intIndex[intIndex.length - 1] = icr.index;

        // Prepare a table-index for dynamic programming
        // --> Indexes for the first node (boundary point) of each segment in the table below
        final int[] tableIndex = new int[bounds1.length + 1];
        for(int i = 1; i < tableIndex.length; ++i) {
            final int i1 = i - 1;
            tableIndex[i] = tableIndex[i1] + (bounds1[i1] - bounds0[i1] + 1);
        }

        // Prepare tables for dynamic programming
        // table0 ->  position of the boundary point in the signal
        // table1 ->  optimal value of the loss function up to node i
        // table2 ->  pointer to optimal preceding node in the table
        final int      tlen   = tableIndex[tableIndex.length - 1];
        final int   [] table0 = new int   [tlen];
        final double[] table1 = new double[tlen];
        final int   [] table2 = new int   [tlen];
        
        for(int i = 0; i <= numOfSeg; ++i) { // Initialize table 0
            final int[] v = SUtil.rangevec(bounds0[i], 1, bounds1[i]);
            int         k = 0;
            int         m = tableIndex[i + 1];
            for(int j = tableIndex[i]; j < m; ++j) {
                table0[j] = v[k++];
            }
        }
        
        Arrays.fill(table1, -Double.NEGATIVE_INFINITY); // All loss functions, except for node 0, are set to -Infinity
        table1[0] = 0;

        int  cumLoopCnt = 0;                 // Cumulative loop counter
        long startTime  = System.nanoTime(); // Start time

        // Forward phase - loop over the segments
        for(int i = 0; i < numOfSeg; ++i) {
            final int        i1           = i + 1;
            final int        i2           = i + 2;

            final int[]      na           = SUtil.saddn(segLenX[i], slackVec);                         // Auxiliary values that depend only on segment number and not node
            final int        b            = tableIndex[i] + 1 - bounds0[i];                            // ---
            final int        c            = segLenT[i] + 1;                                            // ---

            int              count        = 0;                                                         // Counter for local table for segment i
            final int        nodeZ        = tableIndex[i2];                                            // Last node for segment i
            final int        nodeA        = tableIndex[i1];                                            // First node for segment i

            final int   [][] intIndexSeg  = SUtil.transpose(intIndex[i], -segLenX[i] - 1);             // Indexes for interpolation of segment i
            final double[][] intCoeffSeg  = SUtil.transpose(intCoeff[i]);                              // Coefficients for interpolation of segment i

            final double[]   tSeg         = SUtil.slice(target, bT[i] - 1, bT[i1] - 1);                // Segment i of the target
            double           tSegAvg      = SUtil.sum(tSeg) / tSeg.length;                             // Average of tSeg
            final double[]   tSegCentered = SUtil.sadd(-tSegAvg, tSeg);                                // Centered tSeg (for calculating correlation coefficients)
            double           tSegCtrdNorm = SUtil.norm2(tSegCentered);                                 // (n - 1) * standard deviation of tSeg

            // Loop over nodes (possible boundary positions) for segment i
            for(int j = nodeA; j < nodeZ; ++j) {
                final int    [] precNodes        = SUtil.sadd(table0[j], na);               // Possible predecessors given the allowed segment lengths
                final boolean[] allowedArcs      = new boolean[precNodes.length];           // Arcs allowed by local and global constraints
                int             naa              = 0;                                       // Number of allowed arcs
                for(int k = 0; k < allowedArcs.length; ++k) {
                    final double pnk = precNodes[k];
                    if(pnk >= bounds0[i] && pnk <= bounds1[i]) {
                        allowedArcs[k] = true;
                        ++naa;
                    }
                }
                final int[] nodesTablePointer = SUtil.vecsel(precNodes, allowedArcs, b);    // Pointer to predecessors in Table

                // Continue only if we have got at least an arc
                if(naa > 0) {
                    final int   []   indexNode    = SUtil.mat2veccm(                                    // Interpolation signal indexes for all the allowed arcs for node j
                                                        SUtil.matselrm(
                                                            intIndexSeg, allowedArcs, table0[j]
                                                        )
                                                    );
                    final double[]   coeffB       = SUtil.mat2veccm(                                    // Interpolation coefficients for all the allowed arcs for node j
                                                        SUtil.matselrm(intCoeffSeg, allowedArcs)
                                                    ); 

                    final double[]   xiSeg        = new double[indexNode.length];
                    final double[]   xiDiff       = new double[indexNode.length];
                    int              k            = 0;
                    do {
                        final int ink1 = indexNode[k] - 1;
                        xiSeg [k] = signal[ink1];
                        xiDiff[k] = xDiff [ink1];
                    } while(++k < xiSeg.length);

                    final double[][] xiSeg2       = SUtil.vec2matcm(                                    // Interpolate for all allowed predecessors
                                                        SUtil.addmul(xiSeg, coeffB, xiDiff),
                                                        c,
                                                        naa
                                                    );
                    final double[]   xiSegMean    = SUtil.smul(1.0 / xiSeg2.length, SUtil.sum(xiSeg2)); // Means of the interpolated segments
                    final double[]   normXiSegCen = SUtil.sqrtsub(                                      // Fast method for calculating the covariance of T (target) and X (signal); no centering of X is needed
                                                        SUtil.sumsqrelem(xiSeg2),
                                                        SUtil.sqrelem(xiSegMean, xiSeg2.length)
                                                    );
                    final double[]   ccsNodeN     = new double[xiSeg2[0].length];
                    int              sc           = 0;
                    do {
                        int            dc    = 0;
                        final double   tscsc = tSegCentered[sc];
                        final double[] xs2sc = xiSeg2[sc];
                        do {
                            ccsNodeN[dc] += tscsc * xs2sc[dc];
                        } while(++dc < ccsNodeN.length); 
                    } while(++sc < tSegCentered.length);
                    
                                     k            = 0;
                    final double[]   ccsNode      = SUtil.divEx(ccsNodeN, normXiSegCen, tSegCtrdNorm);  // Correlation coefficients relative to all possible predecessors
                    
                                     k            = 0;
                    double[]         costFunc     = new double[nodesTablePointer.length];               // Loss functions from all predecessors
                    do {
                        costFunc[k] = table1[nodesTablePointer[k] - 1];
                    } while(++k < costFunc.length);
                    if(correlationPower == 1) costFunc = SUtil.add(costFunc, ccsNode);
                    else                      costFunc = SUtil.add(
                                                             costFunc,
                                                             SUtil.powelem(ccsNode, correlationPower)
                                                         );

                    double           costFuncMax  = Double.NEGATIVE_INFINITY;                           // Optimal value of loss functions from all predecessors
                    int              costFuncPos  = 0;                                                  // ---
                                     k            = 0;
                    do {
                        final double cfk = costFunc[k];
                        if(cfk > costFuncMax) {
                            costFuncMax = cfk;
                            costFuncPos = k;
                        }
                    } while(++k < costFunc.length);

                    final int tpos = nodeA + count;
                    table1[tpos] = costFuncMax;
                    table2[tpos] = nodesTablePointer[costFuncPos];                                      // Pointer to optimal predecessor
                    ++count;
                }
            }

            // Check if the operation would takes too much time
            cumLoopCnt += (nodeZ - nodeA);
            if(cumLoopCnt > 3000 && (System.nanoTime() - startTime) > 30000000000L) throw new RuntimeException("The operation would take too much time to complete!");
        }

        // Backward phase
        final int[] warping = new int[numOfSeg + 1]; // Initialize a vector of warping values
        int         pointer = table0.length;
        
        warping[numOfSeg] = pX;
        for(int i = numOfSeg - 1; i >= 0; --i) { // Backtrack optimal boundaries using the pointer in table
            pointer    = table2[pointer - 1];
            warping[i] = table0[pointer - 1];
        }

        // Reconstruct the aligned signals
        final double[] result = new double[pT];

        for(int i = 0; i < numOfSeg; ++i) {
            final int      i1    = i + 1;
            final int   [] indT  = SUtil.rangevec(bT[i], 1, bT[i1]);
            final int      lenT  = bT[i1] - bT[i];
            final int   [] indX  = SUtil.rangevec(warping[i] - 1, 1, warping[i1] - 1);
            final int      lenX  = warping[i1] - warping[i];

            final int   [] indXv = SUtil.sadd(2 - warping[i], indX);
            final double[] selXv = new double[indX.length];
            for(int j = 0; j < selXv.length; ++j) {
                selXv[j] = signal[indX[j]];
            }
            final double[] lenTv = SUtil.sadd(1, SUtil.rangevec(0, 1, lenT, (double) lenX / lenT));

            final double[] resXv = SUtil.interp1q(indXv, selXv, lenTv);
            for(int j = 0; j < indT.length; ++j) {
                result[indT[j] - 1] = resXv[j];
            }
        }

        // Return the warped signal
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// Peak Finder                                                            ///////////////////
    ///// Adapted from http://www.mathworks.com/matlabcentral/fileexchange/25500 ///////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Find peaks
    public static class FPResult {
        public int   [] loc;
        public double[] mag;
    };

    public static FPResult findPeaks(double[] signal, double selectivity, double threshold)
    {
        // Check if the signal (x0) has less than 3 points
        final double[] x0 = signal;
        if(x0.length < 3) throw new RuntimeException("The number of elements of the given vector is too few!");

        // Initialize the selectivity and threshold
        final double x0Max = SUtil.max(x0);
        final double x0Min = SUtil.min(x0);
        selectivity = (x0Max - x0Min) * selectivity * 0.01;
        threshold   = x0Max * threshold * 0.01;

        // Calculate the first derivatives
        final double[] dx0 = SUtil.diff(x0);

        // Set all zeroes to -Epsilon so we can find the first of repeated values
        for(int i = 0; i < dx0.length; ++i) {
            if(dx0[i] == 0) dx0[i] = -SUtil.Epsilon;
        }

        // Find where the derivative changes sign
        final double[] dx00 = SUtil.slice(dx0, 0, dx0.length - 2);
        final double[] dx01 = SUtil.slice(dx0, 1, dx0.length - 1);
        final double[] dx0m = SUtil.mul(dx00, dx01);

        int cntChg = 0;
        for(int i = 0; i < dx0m.length; ++i) {
            if(dx0m[i] < 0) ++cntChg;
        }

        final int[] ind0 = new int[cntChg];
        int         iidx = 0;
        for(int i = 0; i < dx0m.length; ++i) {
            if(dx0m[i] < 0) ind0[iidx++] = i + 1;
        }

        // Include endpoints in potential peaks and valleys
        double[] x = new double[ind0.length + 2];
        x[0           ] = x0[0            ];
        x[x.length - 1] = x0[x0.length - 1];
        for(int i = 1; i < x.length - 1; ++i) {
            x[i] = x0[ind0[i - 1]];
        }

        int[] ind = new int[ind0.length + 2];
        ind[0           ] = 1;
        ind[x.length - 1] = x0.length;
        for(int i = 1; i < ind.length - 1; ++i) {
            ind[i] = ind0[i - 1];
        }

        // Preparation (now x only has the peaks, valleys, and endpoints)
        int          len    = x.length;
        final double minMag = SUtil.min(x);

        // Preallocate maximum number of maxima
        final int      maxPeaks = (int) Math.ceil((double) len * 0.5);
        final int   [] peakLoc  = new int   [maxPeaks];
        final double[] peakMag  = new double[maxPeaks];
        int            cInd     = 0;

        // Set initial parameters for loop
        int     tempLoc   = 0;
        double  tempMag   = minMag;
        double  leftMin   = minMag;
        boolean foundPeak = false;
        int     ii        = -1;

        // Handle the first point
        final int[] signDx = SUtil.sign(SUtil.diff(SUtil.slice(x, 0, 2)));

        if(signDx[0] <= 0) { // The first point is larger than or equal to the second
            // We want alternating signs
            if(signDx[0] == signDx[1]) {
                x   = SUtil.erase(x, 1);
                ind = SUtil.erase(ind, 1);
                --len;
            }
        }
        else { // The first point is smaller than the second
            // Add the peak
            peakLoc[cInd] = 0;
            peakMag[cInd] = x[ind[0]];
            ++cInd;
            // We want alternating signs
            ii = 0;
            if(signDx[0] == signDx[1]) { 
                x   = SUtil.erase(x, 0);
                ind = SUtil.erase(ind, 0);
                --len;
            }
        }

        // Loop through the extremas which should be peaks and then valleys
        final int len1 = len - 1;
        while(ii < len1) {
            // This is a peak - reset peak finding if we had a peak
            ++ii;
            if(foundPeak) {
                tempMag   = minMag;
                foundPeak = false;
            }

            // Ensure we do not iterate past the end of the vector
            if(ii == len1) break;

            // Found a new peak that was larger than tempMag and selectivity larger than the minimum to its left
            if(x[ii] > tempMag && x[ii] > leftMin + selectivity) {
                tempLoc = ii;
                tempMag = x[ii];
            }

            // Move into the valley - come down at least as much as the selectivity from peak
            ++ii;
            if(!foundPeak && tempMag > selectivity + x[ii]) {
                foundPeak     = true;    // We have found a peak
                leftMin       = x[ii];
                peakLoc[cInd] = tempLoc; // Add the peak
                peakMag[cInd] = tempMag;
                ++cInd;
            }
            else if(x[ii] < leftMin) {
                leftMin = x[ii];         // New left minima
            }
        }

        // Check the last point
        final int xl1 = x.length - 1;
        if(x[xl1] > tempMag && x[xl1] > leftMin + selectivity) {
            peakLoc[cInd] = len;
            peakMag[cInd] = x[xl1];
            ++cInd;
        }
        else if(!foundPeak && tempMag > minMag) { // Check if the last point shall be added
            peakLoc[cInd] = tempLoc;
            peakMag[cInd] = tempMag;
            ++cInd;
        }

        // Create output
        final int   [] loc = new int   [cInd];
        final double[] mag = new double[cInd];

        for(int i = 0; i < loc.length; ++i) {
            loc[i] = ind[peakLoc[i]];
            mag[i] = peakMag[i];
        }

        // Apply threshold
        final boolean[] selector = new boolean[mag.length];
        for(int i = 0; i < selector.length; ++i) {
            selector[i] = (mag[i] > threshold);
        }

        // Generate and return the final result
        final FPResult ret = new FPResult();
        ret.loc = SUtil.vecsel(loc, selector);
        ret.mag = SUtil.vecsel(mag, selector);

        return ret;
    }

    public static double[][] detectSamePeaks(FPResult[] fprArray, int tolerance, int refIndex)
    {
        // Prepare the rack
        final Map<Integer, Double[]> rack = new HashMap<Integer, Double[]>();
        final int[]                  loc  = fprArray[refIndex].loc;
        for(int i = 0; i < loc.length; ++i) {
            // Create a new empty slot
            final Double[] slot = new Double[fprArray.length];
            Arrays.fill(slot, Double.NEGATIVE_INFINITY);
            // Store the slot into the rack
            rack.put(loc[i], slot);
        }

        // Walk through the chromatograms' peaks
        for(int i = 0; i < fprArray.length; ++i) {
            // Get the current chromatogram's peaks
            final FPResult fpr = fprArray[i];
            // Walk through the peaks
            for(int j = 0; j < fpr.loc.length; ++j) {
                // Get the current location and magnitude
                final int    curLoc = fpr.loc[j];
                final double curMag = fpr.mag[j];
                // Find in which slot the peak should be put
                final Integer[] keys = rack.keySet().toArray(new Integer[0]);
                      Integer   fkey = -1;
                for(int k = 0; k < keys.length; ++k) {
                    final int curKey = keys[k];
                    final int curDel = Math.abs(curKey - curLoc);
                    if(curDel <= tolerance) {
                        fkey = curKey;
                        break;
                    }
                }
                // Store the magnitude in the found slot
                if(fkey >= 0) {
                    rack.get(fkey)[i] = curMag;
                }
                // Store the magnitude in a new slot
                else {
                    // Create a new slot
                    final Double[] slot = new Double[fprArray.length];
                    Arrays.fill(slot, Double.NEGATIVE_INFINITY);
                    // Store the slot into the rack
                    rack.put(curLoc, slot);
                    // Store the magnitude
                    slot[i] = curMag;
                }
            }
        }

        // Extract the result into an array
        final double[][] ret = new double[fprArray.length][rack.size()]; // Initialize the array

        final Integer[] keys = rack.keySet().toArray(new Integer[0]);    // Get and sort the key (ascending)
        Arrays.sort(keys);
        
        for(int i = 0; i < keys.length; ++i) {                           // Walk through the keys
            final int      key = keys[i];
            final Double[] mag = rack.get(key);
            for(int j = 0; j < mag.length; ++j) {                        // Walk through the magnitudes
                ret[j][i] = mag[j];
            }
        }

        // Return the result
        return ret;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// Low-pass filter                                                              /////////////
    ///// Adapted from http://phrogz.net/js/framerate-independent-low-pass-filter.html /////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Apply LPF
    public static double[] lowPassFilter(double[] signal, int smoothingPower)
    {
        double[] ret = new double[signal.length];

        double value = signal[0];
        ret[0] = value;
        for(int i = 1; i < signal.length; ++i) {
            value += (signal[i] - value) / smoothingPower;
            ret[i] = value;
        }

        return ret;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// Baseline correction //////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Apply a constant baseline correction
    public static double[] constantBaselineCorrection(double[] signal)
    {
        double min = SUtil.min(signal);

        double[] ret = new double[signal.length];

        for(int i = 0; i < signal.length; ++i) {
            ret[i] = signal[i] - min;
        }

        return ret;
    }

    // Apply a linear baseline correction
    public static double[] linearBaselineCorrection(double[] signal) throws Exception
    {
        final double[] x = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) x[i] = i;

        final double[][] y = new double[1][signal.length];
        for(int i = 0; i < signal.length; ++i) y[0][i] = signal[i];

        FirstOrderRegression reg = new FirstOrderRegression(x, y, 99.5);

        double[] ret = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) {
            ret[i] = signal[i] - reg.calcYi(x[i]);
        }

        return constantBaselineCorrection(ret);
    }

    // Apply a polynomial-degree-2 baseline correction
    public static double[] polynomial2BaselineCorrection(double[] signal) throws Exception
    {
        final double[] x = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) x[i] = i;

        final double[][] y = new double[1][signal.length];
        for(int i = 0; i < signal.length; ++i) y[0][i] = signal[i];

        SecondOrderRegression reg = new SecondOrderRegression(x, y, 99.5);

        double[] ret = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) {
            ret[i] = signal[i] - reg.calcYi(x[i]);
        }

        return constantBaselineCorrection(ret);
    }    

    // Apply a polynomial-degree-3 baseline correction
    public static double[] polynomial3BaselineCorrection(double[] signal) throws Exception
    {
        final double[] x = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) x[i] = i;

        final double[][] y = new double[1][signal.length];
        for(int i = 0; i < signal.length; ++i) y[0][i] = signal[i];

        ThirdOrderRegression reg = new ThirdOrderRegression(x, y);

        double[] ret = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) {
            ret[i] = signal[i] - reg.calcYi(x[i]);
        }

        return constantBaselineCorrection(ret);
    }

    // Apply a polynomial-degree-4 baseline correction
    public static double[] polynomial4BaselineCorrection(double[] signal) throws Exception
    {
        final double[] x = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) x[i] = i;

        final double[][] y = new double[1][signal.length];
        for(int i = 0; i < signal.length; ++i) y[0][i] = signal[i];

        FourthOrderRegression reg = new FourthOrderRegression(x, y);

        double[] ret = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) {
            ret[i] = signal[i] - reg.calcYi(x[i]);
        }

        return constantBaselineCorrection(ret);
    }

    // Apply a polynomial-degree-5 baseline correction
    public static double[] polynomial5BaselineCorrection(double[] signal) throws Exception
    {
        final double[] x = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) x[i] = i;

        final double[][] y = new double[1][signal.length];
        for(int i = 0; i < signal.length; ++i) y[0][i] = signal[i];

        FifthOrderRegression reg = new FifthOrderRegression(x, y);

        double[] ret = new double[signal.length];
        for(int i = 0; i < signal.length; ++i) {
            ret[i] = signal[i] - reg.calcYi(x[i]);
        }

        return constantBaselineCorrection(ret);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///// Chromatogram-file loaders ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Load an MzML file
    public static double[] loadMzML(InputStream is)
    {
        try {
            class LoadXMLHandler extends DefaultHandler {
                public ArrayList<Double> al = new ArrayList<Double>();

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if(qName.equals("cvParam")) {
                        String name = attributes.getValue("name");
                        if(name.equals("total ion current")) al.add(Double.valueOf(attributes.getValue("value")));
                    }
                }
            };
            
            SAXParserFactory factory   = SAXParserFactory.newInstance();
            SAXParser        saxParser = factory.newSAXParser();
            LoadXMLHandler   handler   = new LoadXMLHandler();

            Reader      reader = new InputStreamReader(is, "UTF-8");
            InputSource isrc   = new InputSource(reader);
            isrc.setEncoding("UTF-8");
            saxParser.parse(isrc, handler);

            ArrayList<Double> al  = handler.al;
            double[]          ret = new double[al.size()];
            for(int i = 0; i < al.size(); ++i) ret[i] = (double) al.get(i);
            return ret;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Load an MzXML file
    public static double[] loadMzXML(InputStream is)
    {
        try {
            class LoadXMLHandler extends DefaultHandler {
                public ArrayList<Double> al = new ArrayList<Double>();

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if(qName.equals("scan")) al.add(Double.valueOf(attributes.getValue("totIonCurrent")));
                }
            };

            SAXParserFactory factory   = SAXParserFactory.newInstance();
            SAXParser        saxParser = factory.newSAXParser();
            LoadXMLHandler   handler   = new LoadXMLHandler();

            Reader      reader = new InputStreamReader(is, "UTF-8");
            InputSource isrc   = new InputSource(reader);
            isrc.setEncoding("UTF-8");
            saxParser.parse(isrc, handler);

            ArrayList<Double> al  = handler.al;
            double[]          ret = new double[al.size()];
            for(int i = 0; i < al.size(); ++i) ret[i] = (double) al.get(i);
            return ret;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Load an OpenChrom file
    public static double[] loadChrom(InputStream is)
    {
        try {
            class LoadXMLHandler extends DefaultHandler {
                public ArrayList<Double> al = new ArrayList<Double>();

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if(qName.equals("SupplierMassSpectrum")) al.add(Double.valueOf(attributes.getValue("totalSignal")));
                }
            };

            SAXParserFactory factory   = SAXParserFactory.newInstance();
            SAXParser        saxParser = factory.newSAXParser();
            LoadXMLHandler   handler   = new LoadXMLHandler();

            Reader      reader = new InputStreamReader(is, "UTF-8");
            InputSource isrc   = new InputSource(reader);
            isrc.setEncoding("UTF-8");
            saxParser.parse(isrc, handler);

            ArrayList<Double> al  = handler.al;
            double[]          ret = new double[al.size()];
            for(int i = 0; i < al.size(); ++i) ret[i] = (double) al.get(i);
            return ret;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Load an XY file
    public static double[] loadXY(InputStream is)
    {
        try {
            ArrayList<Double> al = new ArrayList<Double>();

            DataInputStream dis = new DataInputStream(is);
            BufferedReader  br  = new BufferedReader(new InputStreamReader(dis));

            String line;
            while((line = br.readLine()) != null) {
                line = line.trim();
                if(line.equals("")) break;
                String[] col = line.split(",");
                al.add(Double.valueOf(col[1]));
            }
  
            double[] ret = new double[al.size()];
            for(int i = 0; i < al.size(); ++i) ret[i] = (double) al.get(i);
            return ret;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
