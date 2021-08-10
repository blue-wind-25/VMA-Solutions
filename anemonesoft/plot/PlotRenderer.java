/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.plot;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import anemonesoft.gui.*;
import anemonesoft.i18n.*;

//
// A plot renderer class
//
public class PlotRenderer {
    // Line styles
    public static final int LINE_CONTINUOUS = 10;
    public static final int LINE_DASHED     = 20;
    public static final int LINE_DOTTED     = 30;

    // Symbol types
    public static final int SYM_CHAR_PLUS            = 10;
    public static final int SYM_CHAR_CROSS           = 11;
    public static final int SYM_CHAR_STAR            = 12;
    public static final int SYM_OPEN_CIRCLE          = 20;
    public static final int SYM_OPEN_SQUARE          = 21;
    public static final int SYM_OPEN_UP_TRIANGLE     = 22;
    public static final int SYM_OPEN_DOWN_TRIANGLE   = 23;
    public static final int SYM_OPEN_DIAMOND         = 24;
    public static final int SYM_CLOSED_CIRCLE        = 30;
    public static final int SYM_CLOSED_SQUARE        = 31;
    public static final int SYM_CLOSED_UP_TRIANGLE   = 32;
    public static final int SYM_CLOSED_DOWN_TRIANGLE = 33;
    public static final int SYM_CLOSED_DIAMOND       = 34;

    // Minimum step limit before the corresponding axis will be skipped from being rendered
    public static double MIN_STEP = 0.000000001;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Reference to the graphics states
    private Graphics2D _g2d = null;
    private int        _gw  = 0;
    private int        _gh  = 0;

    // Configuration of the plot's coordinate-system
    private double _xmin, _ymin, _xmax, _ymax; // Minimum and maximum
    private double _xstp, _ystp;               // Step
    private int    _xsdv, _ysdv;               // Sub-division

    // Regions
    private Region _regCSc  = null; // Caption and sub-caption
    private Region _regPlot = null; // Plot
    private Region _regLY   = null; // Left Y-axis
    private Region _regRY   = null; // Right Y-axis
    private Region _regX    = null; // X-axis

    // Strokes
    private BasicStroke _bsOrgnLine       = null; // Origin line
    private BasicStroke _bsOrgnLineNormal = null; // ---
    private BasicStroke _bsOrgnLineDashed = null; // ---
    private BasicStroke _bsOrgnLineDotted = null; // ---
    private BasicStroke _bsAxisLine       = null; // Axis line
    private BasicStroke _bsAxisMajorTick  = null; // Axis major tick
    private BasicStroke _bsAxisMinorTick  = null; // Axis minor tick
    private BasicStroke _bsAxisGrid       = null; // Axis grid
    private BasicStroke _bsAxisGridNormal = null; // ---
    private BasicStroke _bsAxisGridDashed = null; // ---
    private BasicStroke _bsAxisGridDotted = null; // ---
    private BasicStroke _bsSymbol         = null; // Symbol
    private BasicStroke _bsDLNormal       = null; // Data line
    private BasicStroke _bsDLDashed       = null; // ---
    private BasicStroke _bsDLDotted       = null; // ---

    // Fonts and font metrics
    private Font        _mcaptionFont  = null; // Caption
    private FontMetrics _mcaptionFM    = null; // ---
    private Font        _mcaptionFonti = null; // --
    private FontMetrics _mcaptionFMi   = null; // ---
    private Font        _scaptionFont  = null; // Sub-caption
    private FontMetrics _scaptionFM    = null; // ---
    private Font        _scaptionFonti = null; // ---
    private FontMetrics _scaptionFMi   = null; // ---
    private Font        _axisCaptFont  = null; // Axis caption
    private FontMetrics _axisCaptFM    = null; // ---
    private Font        _axisCaptFonti = null; // ---
    private FontMetrics _axisCaptFMi   = null; // ---
    private Font        _axisTickFont  = null; // Axis tick
    private FontMetrics _axisTickFM    = null; // ---
    private Font        _infoTextFont  = null; // Informational text
    private FontMetrics _infoTextFM    = null; // ---

    // Default colors
    private Color _clrBackground = new Color(255, 255, 255); // Plot background
    private Color _clrCaption    = new Color(  0,   0,   0); // Caption and sub-caption
    private Color _clrOrgnLine   = new Color(128, 128, 128); // Origin line
    private Color _clrAxisLine   = new Color(  0,   0,   0); // Axis line and tick
    private Color _clrAxisGrid   = new Color(128, 128, 128); // Axis grid
    private Color _clrAxisCapt   = new Color(  0,   0,   0); // Axis caption
    private Color _clrAxisText   = new Color(  0,   0,   0); // Axis tick text

    // Offset
    private int _ofsPlotX; // Plot region offset (X-axis)
    private int _ofsPlotY; // Plot region offset (Y-axis)
    private int _ofsTick;  // Axis tick offset
    private int _ofsSym1;  // Symbol offset
    private int _ofsSym2;  // ---
    private int _ofsSymQ;  // ---

    // Used for converting the plot's coordinate-system to the graphics-context's coordinate-system
    private double _scaleX, _scaleY;

    // Convert the coordinate-system of the given X coordinate for drawing
    private int _cnvX(double x)
    { return (int) Math.round( (x - _xmin) * _scaleX + _regPlot.x1); }

    // Convert the coordinate-system of the given Y coordinate for drawing
    private int _cnvY(double y)
    { return (int) Math.round( (y - _ymax) * _scaleY + _regPlot.y1); }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Internal caption-text-part class
    private static class CTPart {
        public static final int MODE_SUP = 1;
        public static final int MODE_SUB = 2;
        public static final int MODE_ITA = 4;

        public String text;
        public int    mode;
        public int    plen;

        public CTPart()
        { mode = 0; plen = 0; }

        public CTPart(String t, int m)
        { text = t; mode = m; plen = 0; }

        public CTPart(String t, int m, int p)
        { text = t; mode = m; plen = p; }
    }

    // Parse a caption texts into parts
    private static ArrayList<CTPart> _parseCaptionText(String str, FontMetrics fm, FontMetrics fmi)
    {
        // Temporary buffer
        ArrayList<CTPart> ctp = new ArrayList<CTPart>();

        // States
        int           ctpIndex   = 0;
        boolean       inTag      = false;
        boolean       inSup      = false;
        boolean       inSub      = false;
        boolean       inIta      = false;
        StringBuilder lastString = new StringBuilder(128);
        StringBuilder lastTag    = new StringBuilder(128);

        // Walk trough the characters
        for(int i = 0; i < str.length(); ++i) {
            // Get the current character
            char c = str.charAt(i);
            // Inside a tag
            if(inTag) {
                // Found another opening character for a tag
                if(c == '<') {
                    ctp.get(ctpIndex - 1).text += lastTag.toString();
                    lastTag.setLength(1);
                    lastTag.setCharAt(0, '<');
                }
                // Found a closing character for a tag
                if(c == '>') {
                    // Append the character to the tag and determine what is the tag
                    lastTag.append(c);
                    String lcase = lastTag.toString().toLowerCase();
                    if(lcase.equals("<sub>")) {
                        inSup = false;
                        inSub = true;
                    }
                    else if(lcase.equals("</sub>")) {
                        inSub = false;
                    }
                    else if(lcase.equals("<sup>")) {
                        inSup = true;
                        inSub = false;
                    }
                    else if(lcase.equals("</sup>")) {
                        inSup = false;
                    }
                    else if(lcase.equals("<i>")) {
                        inIta = true;
                    }
                    else if(lcase.equals("</i>")) {
                        inIta = false;
                    }
                    // Invalid tag, just assume as normal text
                    else {
                        int mode = 0;
                        if(inSup) mode |= CTPart.MODE_SUP;
                        if(inSub) mode |= CTPart.MODE_SUB;
                        if(inIta) mode |= CTPart.MODE_ITA;
                        if(ctpIndex > 0 && ctp.get(ctpIndex - 1).mode == mode) {
                            ctp.get(ctpIndex - 1).text += lastTag.toString();
                        }
                        else {
                            ctp.add(new CTPart(lastTag.toString(), mode));
                        }
                        lastTag.setLength(0);
                    }
                    inTag = false;
                }
                // Other characters, just append to the current tag
                else {
                    lastTag.append(c);
                }
            }
            // Normal string
            else {
                // Found an opening character for a tag
                if(c == '<') {
                    int mode = 0;
                    if(inSup) mode |= CTPart.MODE_SUP;
                    if(inSub) mode |= CTPart.MODE_SUB;
                    if(inIta) mode |= CTPart.MODE_ITA;
                    if(ctpIndex > 0 && ctp.get(ctpIndex - 1).mode == mode) {
                        ctp.get(ctpIndex - 1).text += lastString.toString();
                    }
                    else {
                        ctp.add(new CTPart(lastString.toString(), mode));
                    }
                    lastString.setLength(0);
                    lastTag.setLength(1);
                    lastTag.setCharAt(0, '<');
                    inTag = true;
                }
                // Found an '&' character
                else if(c == '&') {
                    // 01234567
                    // &#nnnn;
                    final char cn  = str.charAt(i + 1);
                    final char cd3 = str.charAt(i + 2);
                    final char cd2 = str.charAt(i + 3);
                    final char cd1 = str.charAt(i + 4);
                    final char cd0 = str.charAt(i + 5);
                    final char csc = str.charAt(i + 6);
                    // Check if it is in the correct form
                    if(cn == '#' && csc == ';') {
                        // Generate and append the unicode character
                        final int code = ( Character.getNumericValue(cd3) * 1000 +
                                           Character.getNumericValue(cd2) * 100  +
                                           Character.getNumericValue(cd1) * 10   +
                                           Character.getNumericValue(cd0)
                                         );
                        lastString.append(Character.toString((char) code));
                        // Incement the index
                        i = i + 6;
                    }
                    // Not in a valid form, just append to the current string
                    else {
                        lastString.append(c);
                    }
                }
                // Other characters, just append to the current string
                else {
                    lastString.append(c);
                }
            }
        }

        // Process any left-over string
        if(lastString.length() > 0) {
            int mode = 0;
            if(inSup) mode |= CTPart.MODE_SUP;
            if(inSub) mode |= CTPart.MODE_SUB;
            if(inIta) mode |= CTPart.MODE_ITA;
            if(ctpIndex > 0 && ctp.get(ctpIndex - 1).mode == mode) {
                ctp.get(ctpIndex - 1).text += lastString.toString();
            }
            else {
                ctp.add(new CTPart(lastString.toString(), mode));
            }
        }

        // Final buffer
        ArrayList<CTPart> fctp = new ArrayList<CTPart>();

        // Calculate the pixel length
        for(int i = 0; i < ctp.size(); ++i) {
            CTPart cur = ctp.get(i);
            if(cur.text.length() <= 0) continue;
            if((cur.mode & CTPart.MODE_ITA) != 0) fctp.add(new CTPart(cur.text, cur.mode, fmi.stringWidth(cur.text)));
            else                                  fctp.add(new CTPart(cur.text, cur.mode, fm .stringWidth(cur.text)));
        }

        // Return the final buffer
        return fctp;
    }

    // Draw a formatted text (mode: 0 = main-caption, 1 = sub-caption, 2 = x-axis-caption)
    private void _renderHorizontalText(String str, int xc, int yc, Color color, Font font, FontMetrics fm, Font fonti, FontMetrics fmi, int mode)
    {
        // Set color
        _g2d.setColor(color);

        // Render the text
        renderHorizontalText(_g2d, str, xc, yc, font, fm, fonti, fmi, mode);
    }

    // Draw a formatted text (mode: 0 = left-y-axis caption, 1 = right-y-axis caption)
    private void _renderVerticalText(String str, int xc, int yc, Color color, Font font, FontMetrics fm, Font fonti, FontMetrics fmi, int mode)
    {
        // Set color
        _g2d.setColor(color);

        // Render the text
        renderVerticalText(_g2d, str, xc, yc, font, fm, fonti, fmi, mode);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Draw a formatted text (mode: -1 = printing normal text, 0 = main-caption, 1 = sub-caption, 2 = x-axis-caption)
    public static void renderHorizontalText(Graphics2D g2d, String str, int xc, int yc, Font font, FontMetrics fm, Font fonti, FontMetrics fmi, int mode)
    {
        // Calculate the total length of the text
        ArrayList<CTPart> ctp = _parseCaptionText(str, fm, fmi);
        int               tlenCapt = 0;
        for(int i = 0; i < ctp.size(); ++i) {
            CTPart cur = ctp.get(i);
            tlenCapt += cur.plen;
        }

        // Prepare the rendering coordinate
        int oCapt = (fm.getAscent() + fm.getDescent()) / 4;
        int xCapt = (mode == -1) ? xc : ((xc - tlenCapt) / 2);
        int yCapt = 0;
             if(mode == 0) yCapt =   yc + oCapt + fm.getAscent ();
        else if(mode == 1) yCapt =   yc - oCapt - fm.getDescent();
        else if(mode == 2) yCapt = ( yc + oCapt + fm.getAscent () ) / 2;
        else               yCapt =   yc;

        // Render the text
        Graphics2D lyg = (Graphics2D) g2d.create();
        for(int i = 0; i < ctp.size(); ++i) {
            CTPart cur = ctp.get(i);
            AffineTransform lyt = new AffineTransform();
                if((cur.mode & CTPart.MODE_ITA) != 0) lyg.setFont(fonti);
                else                                  lyg.setFont(font );
                     if((cur.mode & CTPart.MODE_SUP) != 0) lyt.translate(xCapt, yCapt - oCapt);
                else if((cur.mode & CTPart.MODE_SUB) != 0) lyt.translate(xCapt, yCapt + oCapt);
                else                                       lyt.translate(xCapt, yCapt        );
            lyg.setTransform(lyt);
            lyg.drawString(cur.text, 0, 0);
            xCapt += cur.plen;
        }
        lyg.dispose();
    }

    // Draw a formatted text (mode: 0 = left-y-axis caption, 1 = right-y-axis caption)
    private void renderVerticalText(Graphics2D g2d, String str, int xc, int yc, Font font, FontMetrics fm, Font fonti, FontMetrics fmi, int mode)
    {
        // Calculate the total length of the text
        ArrayList<CTPart> ctp = _parseCaptionText(str, fm, fmi);
        int               tlenCapt = 0;
        for(int i = 0; i < ctp.size(); ++i) {
            CTPart cur = ctp.get(i);
            tlenCapt += cur.plen;
            if(mode == 0) cur.plen = -cur.plen;
        }

        // Prepare the rendering coordinate
        int    oCapt = (fm.getAscent() + fm.getDescent()) / 3;
        int    xCapt = 0;
        int    yCapt = 0;
        double rot   = 0;
        if(mode == 0) {
            xCapt = xc + (oCapt + fm.getAscent()) / 2;
            yCapt = yc + tlenCapt / 2;
            rot   = Math.PI * 1.5;
        }
        else {
            xCapt = xc + (oCapt - fm.getAscent()) / 2;
            yCapt = yc - tlenCapt / 2;
            rot   = Math.PI * 0.5;
            oCapt = -oCapt;
        }

        // Render the text
        Graphics2D lyg = (Graphics2D) g2d.create();
        for(int i = 0; i < ctp.size(); ++i) {
            CTPart cur = ctp.get(i);
            AffineTransform lyt = new AffineTransform();
                if((cur.mode & CTPart.MODE_ITA) != 0) lyg.setFont(fonti);
                else                                  lyg.setFont(font );
                     if((cur.mode & CTPart.MODE_SUP) != 0) lyt.translate(xCapt - oCapt, yCapt);
                else if((cur.mode & CTPart.MODE_SUB) != 0) lyt.translate(xCapt + oCapt, yCapt);
                else                                       lyt.translate(xCapt,         yCapt);
                lyt.rotate(rot);
            lyg.setTransform(lyt);
            lyg.drawString(cur.text, 0, 0);
            yCapt += cur.plen;
        }
        lyg.dispose();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Construct a plot renderer using the given parameters
    public PlotRenderer(Graphics2D g2d, int gw, int gh, double xmin, double xmax, double xstp, int xsdv, double ymin, double ymax, double ystp, int ysdv, boolean draft)
    { this(g2d, gw, gh, xmin, xmax, xstp, xsdv, ymin, ymax, ystp, ysdv, draft, false); }

    // Construct a plot renderer using the given parameters
    public PlotRenderer(Graphics2D g2d, int gw, int gh, double xmin, double xmax, double xstp, int xsdv, double ymin, double ymax, double ystp, int ysdv, boolean draft, boolean simple)
    {
        // Copy parameters
        _g2d  = g2d;  _gw   = gw;   _gh   = gh;
        _xmin = xmin; _xmax = xmax; _xstp = xstp; _xsdv = (xsdv > 1) ? xsdv : 1;
        _ymin = ymin; _ymax = ymax; _ystp = ystp; _ysdv = (ysdv > 1) ? ysdv : 1;

        // Get the larger value between the width and height
        final int maxSize = Math.max(gw, gh);

        // Select maximum quality (if needed)
        if(!draft) {
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON               ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING,     RenderingHints.VALUE_COLOR_RENDER_QUALITY       ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_DITHERING,           RenderingHints.VALUE_DITHER_ENABLE              ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_FRACTIONALMETRICS,   RenderingHints.VALUE_FRACTIONALMETRICS_ON       ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_INTERPOLATION,       RenderingHints.VALUE_INTERPOLATION_BICUBIC      ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING,           RenderingHints.VALUE_RENDER_QUALITY             ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_NORMALIZE           ));
            _g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON          ));
        }

        // Calculate the plot regions
        if(simple) { // Simple plot
            _regCSc  = new Region(0, 0, 0,      0     );
            _regPlot = new Region(0, 0, gw - 1, gh - 1);
            _regLY   = new Region(0, 0, 0,      0     );
            _regRY   = new Region(0, 0, 0,      0     );
            _regX    = new Region(0, 0, 0,      0     );
        }
        else { // Normal plot
            _regCSc  = new Region((int) Math.round(0.1 * gw),                  0,          (int) Math.round(0.9 * gw) - 1, (int) Math.round(0.15 * gh) - 1);
            _regPlot = new Region((int) Math.round(0.1 * gw), (int) Math.round(0.15 * gh), (int) Math.round(0.9 * gw) - 1, (int) Math.round(0.9  * gh) - 1);
            _regLY   = new Region(                 0,         (int) Math.round(0.15 * gh), (int) Math.round(0.1 * gw) - 1, (int) Math.round(0.9  * gh) - 1);
            _regRY   = new Region((int) Math.round(0.9 * gw), (int) Math.round(0.15 * gh),                        gw  - 1, (int) Math.round(0.9  * gh) - 1);
            _regX    = new Region((int) Math.round(0.1 * gw), (int) Math.round(0.9  * gh), (int) Math.round(0.9 * gw) - 1,                         gh  - 1);
        }

        // Create the strokes
        float[] dashed = new float[]{ maxSize / 100, maxSize / 200 };
        float[] dotted = new float[]{ maxSize / 800, maxSize / 200 };
        _bsOrgnLineNormal = new BasicStroke(maxSize / 200, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1);
        _bsOrgnLineDashed = new BasicStroke(maxSize / 200, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dashed, 0);
        _bsOrgnLineDotted = new BasicStroke(maxSize / 200, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dotted, 0);
        _bsAxisLine       = new BasicStroke(maxSize / 200, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        _bsAxisMajorTick  = new BasicStroke(maxSize / 200, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        _bsAxisMinorTick  = new BasicStroke(maxSize / 400, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        _bsAxisGridNormal = new BasicStroke(maxSize / 400, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1);
        _bsAxisGridDashed = new BasicStroke(maxSize / 400, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dashed, 0);
        _bsAxisGridDotted = new BasicStroke(maxSize / 400, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dotted, 0);
        _bsSymbol         = new BasicStroke(maxSize / 800, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        _bsDLNormal       = new BasicStroke(maxSize / 800, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
        _bsDLDashed       = new BasicStroke(maxSize / 800, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dashed, 0);
        _bsDLDotted       = new BasicStroke(maxSize / 800, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dotted, 0);

        _bsOrgnLine = _bsOrgnLineNormal;
        _bsAxisGrid = _bsAxisGridNormal;

        // Create fonts
        Font gfont = new Font(GUtil.getSysFontName("SansSerif"), Font.BOLD, 12);

        _mcaptionFont  = gfont.deriveFont(Font.BOLD,               maxSize / 30);
        _mcaptionFonti = gfont.deriveFont(Font.BOLD | Font.ITALIC, maxSize / 30);

        _scaptionFont  = gfont.deriveFont(Font.BOLD,               maxSize / 40);
        _scaptionFonti = gfont.deriveFont(Font.BOLD | Font.ITALIC, maxSize / 40);

        _axisCaptFont  = gfont.deriveFont(Font.BOLD,               maxSize / 40);
        _axisCaptFonti = gfont.deriveFont(Font.BOLD | Font.ITALIC, maxSize / 40);

        _axisTickFont  = gfont.deriveFont(Font.BOLD,               maxSize / 60);
        _infoTextFont  = gfont.deriveFont(Font.BOLD,               maxSize / 80);

        // Get the font metrics
        _g2d.setFont(_mcaptionFont ); _mcaptionFM  = _g2d.getFontMetrics();
        _g2d.setFont(_mcaptionFonti); _mcaptionFMi = _g2d.getFontMetrics();

        _g2d.setFont(_scaptionFont ); _scaptionFM  = _g2d.getFontMetrics();
        _g2d.setFont(_scaptionFonti); _scaptionFMi = _g2d.getFontMetrics();

        _g2d.setFont(_axisCaptFont ); _axisCaptFM  = _g2d.getFontMetrics();
        _g2d.setFont(_axisCaptFonti); _axisCaptFMi = _g2d.getFontMetrics();

        _g2d.setFont(_axisTickFont ); _axisTickFM  = _g2d.getFontMetrics();
        _g2d.setFont(_infoTextFont ); _infoTextFM  = _g2d.getFontMetrics();

        // Calculate the offsets
        _ofsPlotX = _axisTickFM.stringWidth("9") * 8;
        _ofsPlotY = (_axisTickFM.getAscent() + _axisTickFM.getDescent()) * 2;
        _ofsTick  = maxSize / 200;
        _ofsSym1  = (int) Math.round(maxSize / 300);
        _ofsSym2  = (int) Math.round(maxSize / 150);
        _ofsSymQ  = (int) Math.round(Math.sqrt((maxSize / 300) * (maxSize / 300) * 2) / 2);

        // Offset the plot region
        if(simple) { // Simple plot
            _regPlot.x1 += _ofsPlotX;     _regPlot.y1 += _ofsPlotY / 2;
            _regPlot.x2 -= _ofsPlotX / 4; _regPlot.y2 -= _ofsPlotY / 2;
        }
        else { // Normal plot
            _regPlot.x1 += _ofsPlotX; _regPlot.y1 += _ofsPlotY;
            _regPlot.x2 -= _ofsPlotX; _regPlot.y2 -= _ofsPlotY;
        }

        // Calculate the scalling factors for converting the plot coordinate-system
        _scaleX =  _regPlot.w() / (_xmax - _xmin);
        _scaleY = -_regPlot.h() / (_ymax - _ymin);
    }

    // Set the general style
    public void setGeneralStyle(Color background, Color caption, Color originLine, int originLineStyle, Color axisLine, Color axisGrid, int axisGridStyle, Color axisCapt, Color axisText)
    {
        _clrBackground = background;
        _clrCaption    = caption;
        _clrOrgnLine   = originLine;
        _clrAxisLine   = axisLine;
        _clrAxisGrid   = axisGrid;
        _clrAxisCapt   = axisCapt;
        _clrAxisText   = axisText;

        switch(originLineStyle) {
            case LINE_DOTTED : _bsOrgnLine = _bsOrgnLineDotted; break;
            case LINE_DASHED : _bsOrgnLine = _bsOrgnLineDashed; break;
            default          : _bsOrgnLine = _bsOrgnLineNormal; break;
        }

        switch(axisGridStyle) {
            case LINE_DOTTED : _bsAxisGrid = _bsAxisGridDotted; break;
            case LINE_DASHED : _bsAxisGrid = _bsAxisGridDashed; break;
            default          : _bsAxisGrid = _bsAxisGridNormal; break;
        }
    }

    // Clip all drawing operations to the the plot area
    public void clipToPlotArea(boolean clip, boolean strict)
    {
        if(clip) {
            if(strict)
                _g2d.setClip(_regPlot.x1, _regPlot.y1, _regPlot.w(), _regPlot.h());
            else
                _g2d.setClip(_regPlot.x1 - _ofsPlotX, _regPlot.y1 - _ofsPlotY, _regPlot.w() + _ofsPlotX * 2, _regPlot.h() + _ofsPlotY * 2);
        }
        else
            _g2d.setClip(0, 0, _gw, _gh);
    }

    // Draw the plot background
    public void drawBackground()
    {
        _g2d.setColor(_clrBackground);
        _g2d.fillRect(0, 0, _gw, _gh);
    }

    // Draw a line
    public void drawLine(double x1, double y1, double x2, double y2)
    { _g2d.drawLine(_cnvX(x1), _cnvY(y1), _cnvX(x2), _cnvY(y2)); }

    // Draw a horizontal line
    public void drawHLine(double x1, double x2, double y)
    {
        int cy = _cnvY(y);
        _g2d.drawLine(_cnvX(x1), cy, _cnvX(x2), cy);
    }

    // Draw a vertical line
    public void drawVLine(double x, double y1, double y2)
    {
        int cx = _cnvX(x);
        _g2d.drawLine(cx, _cnvY(y1), cx, _cnvY(y2));
    }

    // Draw an information horizontal tick
    public void drawInfoHTick(double x, double y, int size, Color col)
    {
        _g2d.setColor(col);

        int cx = _cnvX(x);
        int cy = _cnvY(y);
        _g2d.drawLine(cx, cy, cx + _ofsTick * size, cy);
    }

    // Draw an information vertical tick
    public void drawInfoVTick(double x, double y, int size, Color col)
    {
        _g2d.setColor(col);

        int cx = _cnvX(x);
        int cy = _cnvY(y);
        _g2d.drawLine(cx, cy, cx, cy - _ofsTick * size);
    }

    // Draw an informational text
    // Note: halign : -1 = left, 0 : center, 1 : right
    //       valign : -1 = top,  0 : middle, 1 : bottom
    public void drawInfoText(String str, double x, double y, int halign, int valign, Color col, int ofsx, int ofsy)
    {
        _g2d.setColor(col);
        _g2d.setFont(_infoTextFont);

        ofsx = ofsx * _ofsTick;
        ofsy = ofsy * _ofsTick;

        int xc = _cnvX(x);
             if(halign < 0) xc += ofsx;
        else if(halign > 0) xc -= (_infoTextFM.stringWidth(str) + ofsx);
        else                xc -= (_infoTextFM.stringWidth(str) / 2);

        int yc = _cnvY(y);
             if(valign < 0) yc += (_infoTextFM.getAscent () + ofsy);
        else if(valign > 0) yc -= (_infoTextFM.getDescent() + ofsy);
        else                yc += (_infoTextFM.getAscent() - _infoTextFM.getDescent()) / 2;

        _g2d.drawString(str, xc, yc);
    }

    // Draw the plot caption
    public void drawCaption(String main, String sub)
    {
        _renderHorizontalText(main, _regCSc.x1 + _regCSc.x2, _regCSc.y1, _clrCaption, _mcaptionFont, _mcaptionFM, _mcaptionFonti, _mcaptionFMi, 0);
        _renderHorizontalText(sub,  _regCSc.x1 + _regCSc.x2, _regCSc.y2, _clrCaption, _scaptionFont, _scaptionFM, _scaptionFonti, _scaptionFMi, 1);
    }

    // Draw the axis caption
    public void drawAxisCaption(String xcapt, String lycapt, String rycapt)
    {
        _renderHorizontalText(xcapt, _regX.x1 + _regX.x2, _regX.y1 + _regX.y2, _clrAxisCapt, _axisCaptFont, _axisCaptFM, _axisCaptFonti, _axisCaptFMi, 2);

        if(lycapt != null)
            _renderVerticalText(lycapt, (_regLY.x1 + _regLY.x2) / 2, (_regLY.y1 + _regLY.y2) / 2, _clrAxisCapt, _axisCaptFont, _axisCaptFM, _axisCaptFonti, _axisCaptFMi, 0);

        if(rycapt != null)
            _renderVerticalText(rycapt, (_regRY.x1 + _regRY.x2) / 2, (_regRY.y1 + _regRY.y2) / 2, _clrAxisCapt, _axisCaptFont, _axisCaptFM, _axisCaptFonti, _axisCaptFMi, 1);
    }

    // Draw the plot axis
    public void drawAxis(boolean drawGrid, boolean drawOriginAxis, boolean drawXAxis, boolean drawLeftYAxis, boolean drawRightYAxis, String[] customXAxisTick)
    {
        // Draw the plot's grid
        if(drawGrid) {
            _g2d.setColor(_clrAxisGrid);
            _g2d.setStroke(_bsAxisGrid);
            if(Math.abs(_xstp) >= MIN_STEP) {
                final int yg0 = _cnvY(_ymin);
                final int yg1 = _cnvY(_ymax);
                for(double x = _xmin; x <= _xmax; x += _xstp) {
                    final int xt = _cnvX(x);
                    _g2d.drawLine(xt, yg0, xt, yg1);
                }
            }
            if(Math.abs(_ystp) >= MIN_STEP) {
                final int xg0 = _cnvX(_xmin);
                final int xg1 = _cnvX(_xmax);
                for(double y = _ymin; y <= _ymax; y += _ystp) {
                    final int yt = _cnvY(y);
                    _g2d.drawLine(xg0, yt, xg1, yt);
                }
            }
        }

        // The origin axis
        if(drawOriginAxis) {
            _g2d.setColor(_clrOrgnLine);
            _g2d.setStroke(_bsOrgnLine);
            drawHLine(_xmin, _xmax, 0);
            drawVLine(0, _ymin, _ymax);
        }

        // Set the font to draw the axis ticks
        _g2d.setFont(_axisTickFont);

        // Draw the plot's X-axis
        if(drawXAxis) {
            // Draw the axis
            _g2d.setColor(_clrAxisLine);
            _g2d.setStroke(_bsAxisLine);
            if(_xmin != _xmax) drawHLine(_xmin, _xmax, _ymin);
            // Skip drawing of the step is too small
            if(Math.abs(_xstp) >= MIN_STEP) {
                // Determine the format string
                double max    = _xmax + _xstp * 0.1;
                String format = "%.3f";
                if(customXAxisTick == null) {
                    boolean e000 = true;
                    boolean e00  = true;
                    boolean e0   = true;
                    for(double x = _xmin; x < max; x += _xstp) {
                        String str = StringTranslator.format(format, x);
                        if(!str.endsWith("000")) e000 = false;
                        if(!str.endsWith("00" )) e00  = false;
                        if(!str.endsWith("0"  )) e0   = false;
                        if(!e000 && !e00 && !e0) break;
                    }
                         if(e000) format = "%.0f";
                    else if(e00 ) format = "%.1f";
                    else if(e0  ) format = "%.2f";
                }
                // Draw the major ticks
                _g2d.setStroke(_bsAxisMajorTick);
                int yt0 = _cnvY(_ymin) - _ofsTick;
                int yt1 = _cnvY(_ymin) + _ofsTick;
                int ytt = (int) Math.round(yt1 + _axisTickFM.getAscent() + _ofsTick);
                int idx = 0;
                for(double x = _xmin; x < max; x += _xstp) {
                    int    xt  = _cnvX(x);
                    String str = (customXAxisTick != null) ? customXAxisTick[idx++] : StringTranslator.format(format, x);
                    _g2d.setColor(_clrAxisLine);
                    _g2d.drawLine(xt, yt0, xt, yt1);
                    if(str == null) continue;
                    _g2d.setColor(_clrAxisText);
                    _g2d.drawString(str, xt - _axisTickFM.stringWidth(str) / 2, ytt);
                }
                // Draw the minor ticks
                if(_xsdv > 1) {
                    double xinc = (_xstp / _xsdv);
                    _g2d.setColor (_clrAxisLine);
                    _g2d.setStroke(_bsAxisMinorTick);
                    for(double x = _xmin; x <= _xmax; x += xinc) {
                        int xt = _cnvX(x);
                        _g2d.drawLine(xt, yt0, xt, yt1);
                    }
                }
            }
        }

        // Draw the plot's left Y-axis
        if(drawLeftYAxis) {
            // Draw the axis
            _g2d.setColor(_clrAxisLine);
            _g2d.setStroke(_bsAxisLine);
            if(_ymin != _ymax) drawVLine(_xmin, _ymin, _ymax);
            // Skip drawing of the step is too small
            if(Math.abs(_ystp) >= MIN_STEP) {
                // Determine the format string
                double max    = _ymax + _ystp * 0.1;
                String format = "%.3f";
                if(true) {
                    boolean e000 = true;
                    boolean e00  = true;
                    boolean e0   = true;
                    for(double y = _ymin; y < max; y += _ystp) {
                        String str = StringTranslator.format(format, y);
                        if(!str.endsWith("000")) e000 = false;
                        if(!str.endsWith("00" )) e00  = false;
                        if(!str.endsWith("0"  )) e0   = false;
                        if(!e000 && !e00 && !e0) break;
                    }
                         if(e000) format = "%.0f";
                    else if(e00 ) format = "%.1f";
                    else if(e0  ) format = "%.2f";
                }
                // Draw the major ticks
                _g2d.setStroke(_bsAxisMajorTick);
                int xt0 = _cnvX(_xmin) - _ofsTick;
                int xt1 = _cnvX(_xmin) + _ofsTick;
                int xtt = (int) Math.round(xt0 - _ofsTick);
                int yto = (int) Math.round((_axisTickFM.getAscent() - _axisTickFM.getDescent()) / 2);
                for(double y = _ymin; y < max; y += _ystp) {
                    int    yt  = _cnvY(y);
                    String str = StringTranslator.format(format, y);
                    _g2d.setColor(_clrAxisLine);
                    _g2d.drawLine(xt0, yt, xt1, yt);
                    _g2d.setColor(_clrAxisText);
                    _g2d.drawString(str, xtt - _axisTickFM.stringWidth(str), yt + yto);
                }
                // Draw the minor ticks
                if(_ysdv > 1) {
                    double yinc = (_ystp / _ysdv);
                    _g2d.setColor (_clrAxisLine);
                    _g2d.setStroke(_bsAxisMinorTick);
                    for(double y = _ymin; y <= _ymax; y += yinc) {
                        int yt  = _cnvY(y);
                        _g2d.drawLine(xt0, yt, xt1, yt);
                    }
                }
            }
        }

        // Draw the plot's left Y-axis
        if(drawRightYAxis) {
            // Draw the axis
            _g2d.setColor(_clrAxisLine);
            _g2d.setStroke(_bsAxisLine);
            if(_ymin != _ymax) drawVLine(_xmax, _ymin, _ymax);
            // Skip drawing of the step is too small
            if(Math.abs(_ystp) >= MIN_STEP) {
                // Determine the format string
                double  max    = _ymax + _ystp * 0.1;
                boolean neg    = false;
                boolean pos    = false;
                String  format = "%.3f";
                if(true) {
                    boolean e000 = true;
                    boolean e00  = true;
                    boolean e0   = true;
                    for(double y = _ymin; y < max; y += _ystp) {
                        String str = StringTranslator.format(format, y);
                        if(!str.endsWith("000")) e000 = false;
                        if(!str.endsWith("00" )) e00  = false;
                        if(!str.endsWith("0"  )) e0   = false;
                        if(y < 0) neg = true;
                        else      pos = true;
                    }
                         if(e000) format = "%.0f";
                    else if(e00 ) format = "%.1f";
                    else if(e0  ) format = "%.2f";
                }
                // Draw the major ticks
                _g2d.setStroke(_bsAxisMajorTick);
                int xt0 = _cnvX(_xmax) - _ofsTick;
                int xt1 = _cnvX(_xmax) + _ofsTick;
                int xtt = (int) Math.round(xt1 + _ofsTick);
                int xtp = xtt + ( (neg && pos) ? _axisTickFM.stringWidth("-") : 0 );
                int yto = (int) Math.round((_axisTickFM.getAscent() - _axisTickFM.getDescent()) / 2);
                for(double y = _ymin; y < max; y += _ystp) {
                    int    yt  = _cnvY(y);
                    String str = StringTranslator.format(format, y);
                    _g2d.setColor(_clrAxisLine);
                    _g2d.drawLine(xt0, yt, xt1, yt);
                    _g2d.setColor(_clrAxisText);
                    _g2d.drawString(str, (y >= 0) ? xtp : xtt, yt + yto);
                }
                // Draw the minor ticks
                if(_ysdv > 1) {
                    double yinc = (_ystp / _ysdv);
                    _g2d.setColor (_clrAxisLine);
                    _g2d.setStroke(_bsAxisMinorTick);
                    for(double y = _ymin; y <= _ymax; y += yinc) {
                        int yt  = _cnvY(y);
                        _g2d.drawLine(xt0, yt, xt1, yt);
                    }
                }
            }
        }
    }

    // Draw the plot axis
    public void drawAxis(boolean drawGrid, boolean drawOriginAxis, boolean drawXAxis, boolean drawLeftYAxis, boolean drawRightYAxis)
    { drawAxis(drawGrid, drawOriginAxis, drawXAxis, drawLeftYAxis, drawRightYAxis, null); }

    // Draw symbol point
    public void drawSymbolPoint(double x, double y, int sym)
    {
        int xc = _cnvX(x);
        int yc = _cnvY(y);
        int f1 = _ofsSym1;
        int f2 = _ofsSym2;
        int fq = _ofsSymQ;

        switch(sym) {
            // Characte shapes
            case SYM_CHAR_PLUS :
                _g2d.drawLine(xc, yc - f1, xc, yc + f1);
                _g2d.drawLine(xc - f1, yc, xc + f1, yc);
                break;
            case SYM_CHAR_CROSS :
                _g2d.drawLine(xc - f1, yc - f1, xc + f1, yc + f1);
                _g2d.drawLine(xc - f1, yc + f1, xc + f1, yc - f1);
                break;
            case SYM_CHAR_STAR :
                _g2d.drawLine(xc,      yc - f1, xc,      yc + f1);
                _g2d.drawLine(xc - f1, yc,      xc + f1, yc     );
                _g2d.drawLine(xc - fq, yc - fq, xc + fq, yc + fq);
                _g2d.drawLine(xc - fq, yc + fq, xc + fq, yc - fq);
                break;

            // Open shapes
            case SYM_OPEN_CIRCLE          : _g2d.drawOval(xc - f1, yc - f1, f2, f2);                                               break;
            case SYM_OPEN_SQUARE          : _g2d.drawRect(xc - f1, yc - f1, f2, f2);                                               break;
            case SYM_OPEN_UP_TRIANGLE     : _g2d.drawPolygon(new int[]{xc-f1, xc, xc+f1}, new int[]{yc+f1, yc-f1, yc+f1}, 3);      break;
            case SYM_OPEN_DOWN_TRIANGLE   : _g2d.drawPolygon(new int[]{xc-f1, xc, xc+f1}, new int[]{yc-f1, yc+f1, yc-f1}, 3);      break;
            case SYM_OPEN_DIAMOND         : _g2d.drawPolygon(new int[]{xc-f1, xc, xc+f1, xc}, new int[]{yc, yc-f1, yc, yc+f1}, 4); break;

            // Closed shapes
            case SYM_CLOSED_CIRCLE        : _g2d.fillOval(xc - f1, yc - f1, f2, f2);                                               break;
            case SYM_CLOSED_SQUARE        : _g2d.fillRect(xc - f1, yc - f1, f2, f2);                                               break;
            case SYM_CLOSED_UP_TRIANGLE   : _g2d.fillPolygon(new int[]{xc-f1, xc, xc+f1}, new int[]{yc+f1, yc-f1, yc+f1}, 3);      break;
            case SYM_CLOSED_DOWN_TRIANGLE : _g2d.fillPolygon(new int[]{xc-f1, xc, xc+f1}, new int[]{yc-f1, yc+f1, yc-f1}, 3);      break;
            case SYM_CLOSED_DIAMOND       : _g2d.fillPolygon(new int[]{xc-f1, xc, xc+f1, xc}, new int[]{yc, yc-f1, yc, yc+f1}, 4); break;
        }
    }

    // Draw symbol points
    public void drawSymbolPoints(double[] xy, Color col, int sym)
    {
        // Set color and stroke
        _g2d.setColor(col);
        _g2d.setStroke(_bsSymbol);

        // Walk trough the points
        for(int i = 0; i < xy.length; i += 2) {
            drawSymbolPoint(xy[i], xy[i + 1], sym);
        }
    }

    // Draw symbol points with deviation bar
    public void drawSymbolPoints(double[] xyd, Color pcol, int psym, Color sdcol, int sdlin)
    {
        // Get the stroke for the deviation line
        Stroke ls = null;
        switch(sdlin) {
            case LINE_DOTTED : ls = _bsDLDotted; break;
            case LINE_DASHED : ls = _bsDLDashed; break;
            default          : ls = _bsDLNormal; break;
        }

        // Walk trough the points
        for(int i = 0; i < xyd.length; i += 3) {
            // Get the data
            double x = xyd[i    ];
            double y = xyd[i + 1];
            double d = xyd[i + 2];
            // Draw the deviation bar
            int xc  = _cnvX(x);
            int yc0 = _cnvY(y - d);
            int yc1 = _cnvY(y + d);
            _g2d.setColor(sdcol);
            _g2d.setStroke(ls);
            _g2d.drawLine(xc - _ofsSym1, yc0, xc + _ofsSym1, yc0);
            _g2d.drawLine(xc,            yc0, xc,            yc1);
            _g2d.drawLine(xc - _ofsSym1, yc1, xc + _ofsSym1, yc1);
            // Draw the symbol
            _g2d.setColor(pcol);
            _g2d.setStroke(_bsSymbol);
            drawSymbolPoint(x, y, psym);
        }
    }

    // Draw boxes
    public void drawBoxes(double[] xyxy, Color col, int style)
    {
        // Set color and stroke
        _g2d.setColor(col);
        switch(style) {
            case LINE_DOTTED : _g2d.setStroke(_bsDLDotted); break;
            case LINE_DASHED : _g2d.setStroke(_bsDLDashed); break;
            default          : _g2d.setStroke(_bsDLNormal); break;
        }

        // Draw the lines
        // TODO: OPTIMIZE!!!
        for(int i = 0; i < xyxy.length; i += 4) {
            int x1 = _cnvX(xyxy[i    ]);
            int y1 = _cnvY(xyxy[i + 1]);
            int x2 = _cnvX(xyxy[i + 2]);
            int y2 = _cnvY(xyxy[i + 3]);
            if(x1 > x2) {
                int t = x2;
                x2 = x1;
                x1 = t;
            }
            if(y1 > y2) {
                int t = y2;
                y2 = y1;
                y1 = t;
            }
            _g2d.drawRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
        }
    }

    // Draw lines
    public void drawLines(double[] xyxy, Color col, int style)
    {
        // Set color and stroke
        _g2d.setColor(col);
        switch(style) {
            case LINE_DOTTED : _g2d.setStroke(_bsDLDotted); break;
            case LINE_DASHED : _g2d.setStroke(_bsDLDashed); break;
            default          : _g2d.setStroke(_bsDLNormal); break;
        }

        // Draw the lines
        for(int i = 0; i < xyxy.length; i += 4) {
            _g2d.drawLine(_cnvX(xyxy[i]), _cnvY(xyxy[i + 1]), _cnvX(xyxy[i + 2]), _cnvY(xyxy[i + 3]));
        }
    }

    // Draw polyline
    public void drawPolyline(double[] xy, Color col, int style)
    {
        // Set color and stroke
        _g2d.setColor(col);
        switch(style) {
            case LINE_DOTTED : _g2d.setStroke(_bsDLDotted); break;
            case LINE_DASHED : _g2d.setStroke(_bsDLDashed); break;
            default          : _g2d.setStroke(_bsDLNormal); break;
        }

        // Sort the data from the smallest X to the largest X
        for(int i = 0; i < xy.length; i += 2) {
            for(int j = i + 2; j < xy.length; j += 2) {
                if(xy[i] > xy[j]) {
                    double cx = xy[i    ];
                    double cy = xy[i + 1];
                    xy[i    ] = xy[j    ];
                    xy[i + 1] = xy[j + 1];
                    xy[j    ] = cx;
                    xy[j + 1] = cy;
                }
            }
        }

        // Transform the data
        int[] x = new int[xy.length / 2];
        int[] y = new int[xy.length / 2];
        int   c = 0;
        for(int i = 0; i < xy.length; i += 2) {
            x[c] = _cnvX(xy[i]);
            y[c] = _cnvY(xy[i + 1]);
            ++c;
        }

        // Draw the polyline
        _g2d.drawPolyline(x, y, c);
    }
}
