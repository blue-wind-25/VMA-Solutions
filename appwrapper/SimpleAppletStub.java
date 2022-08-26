/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package appwrapper;

import java.applet.*;
import java.util.*;
import javax.swing.JApplet;

//
// A simple applet-stub class
//
class SimpleAppletStub implements AppletStub {
    private JApplet                   _applet;
    private Hashtable<String, String> _properties;
  
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public SimpleAppletStub(String argv[], JApplet a)
    {
        _applet     = a;
        _properties = new Hashtable<String, String>();

        for(int i = 0; i < argv.length; ++i) {
            try {
                StringTokenizer parser = new StringTokenizer (argv[i], "=");
                String          name   = parser.nextToken().toString();
                try {
                    String value  = parser.nextToken("\"").toString();
                    _properties.put(name, value.substring(1));
                }
                catch(Exception e) {
                    _properties.put(name, "");
                }
            }
            catch(Exception e) {
            }
        }
    }

    public void appletResize(int w, int h)
    { _applet.resize(w, h); }

    public AppletContext getAppletContext()
    { return null; }

    public java.net.URL getCodeBase()
    { return null; }

    public java.net.URL getDocumentBase()
    { return getCodeBase(); }

    public String getParameter(String p)
    { return _properties.get(p); }

    public boolean isActive ()
    { return true; }
}
