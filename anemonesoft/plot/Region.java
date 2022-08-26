/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.plot;

//
// A class to define a region
//
public class Region {
    // Region data
    public int x1, y1, x2, y2;

    // Construct a zero region 
    public Region()
    { x1 = 0; y1 = 0; x2 = 0; y2 = 0; }

    // Construct a region using the given parameters
    public Region(int x1_, int y1_, int x2_, int y2_)
    { x1 = (int) x1_; y1 = y1_; x2 = x2_; y2 = y2_; }

    // Return the width of the region
    public int w()
    { return x2 - x1 + 1; }

    // Return the height of the region
    public int h()
    { return y2 - y1 + 1; }
}

