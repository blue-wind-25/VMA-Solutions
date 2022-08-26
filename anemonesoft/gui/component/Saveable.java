/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.gui.component;

import java.io.*;

//
// The parent class for all saveable class
//
public interface Saveable {
    // Return the interface version
    public int interfaceVersion();
    
    // Save the tab data to the given stream
    public void save(DataOutputStream ds) throws Exception;

    // Load the tab data from the given stream
    public boolean load(int interfaceVersion, DataInputStream ds) throws Exception;
}

