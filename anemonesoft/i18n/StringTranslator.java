/*
    Copyright (C) 2010-2022 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.i18n;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.UIManager;

import anemonesoft.gui.*;

//
// A string translator class for i18n
//
public class StringTranslator {
    // Instantiate the locale and resource bundle objects
    private static Locale         _locale    = null;
    private static MessageFormat  _mformat   = null;
    private static ResourceBundle _rbundle   = null;
    private static StringBuilder  _fsbuilder = new StringBuilder();
    private static Formatter      _formatter = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Initialize the translator
    public static void init(String country, String language)
    {
        if(_locale != null) return;

        _locale    = new Locale.Builder().setLanguage(language).setRegion(country).build();
        _mformat   = new MessageFormat("");
        _rbundle   = ResourceBundle.getBundle("anemonesoft/i18n/text/MessagesBundle", _locale);
        _formatter = new Formatter(_fsbuilder, _locale);

        _mformat.setLocale(_locale);

        // Set the file chooser texts
        UIManager.put("FileChooser.lookInLabelText",               getString("fch_look_in"             ));
        UIManager.put("FileChooser.fileNameLabelText",             getString("fch_file_name"           ));
        UIManager.put("FileChooser.filesOfTypeLabelText",          getString("fch_file_type"           ));
        UIManager.put("FileChooser.upFolderToolTipText",           getString("fch_up_one_level_tooltip"));
        UIManager.put("FileChooser.homeFolderToolTipText",         getString("fch_home_tooltip"        ));
        UIManager.put("FileChooser.newFolderToolTipText",          getString("fch_new_folder_tooltip"  ));
        UIManager.put("FileChooser.listViewButtonToolTipTextlist", getString("fch_list_tooltip"        ));
        UIManager.put("FileChooser.detailsViewButtonToolTipText",  getString("fch_details_tooltip"     ));
        UIManager.put("FileChooser.saveButtonText",                getString("fch_save"                ));
        UIManager.put("FileChooser.openButtonText",                getString("fch_open"                ));
        UIManager.put("FileChooser.cancelButtonText",              getString("fch_cancel"              ));
        UIManager.put("FileChooser.saveButtonToolTipText",         getString("fch_save_tooltip"        ));
        UIManager.put("FileChooser.openButtonToolTipText",         getString("fch_open_tooltip"        ));
        UIManager.put("FileChooser.cancelButtonToolTipText",       getString("fch_cancel_tooltip"      ));
        // FileChooser.updateButtonText=Update
        // FileChooser.helpButtonText=Help
        // FileChooser.updateButtonToolTipText=Update
        // FileChooser.helpButtonToolTipText=Help
    }

    // Return a locale indentifier string
    public static String getLocaleIDString()
    { return _locale.getLanguage() + "_" + _locale.getCountry(); }

    // Return an i18n string for "not enough data"
    public static String strNED()
    { return _rbundle.getString("res_not_enough_data"); }

    // Return an i18n string with the given key
    public static String getString(String key)
    { return _rbundle.getString(key); }

    // Return an i18n formatted string from the given key and arguments
    public static String formatString(String key, Object[] arguments)
    {
        _mformat.applyPattern( _rbundle.getString(key));
        return _mformat.format(arguments);
    }

    // Return an i18n formatted strings from the given double-values
    public static String[] formatDoubles(double[] v, int decPrec)
    {
        String[] strV = new String[v.length];
        boolean  useE = false;

        // Format using "%f"
        String formatString = "%." + decPrec + "f";
        for(int i = 0; i < v.length; ++i) {
            String  str = StringTranslator.format(formatString, v[i]);
            boolean any = false;
            for(int k = (str.length() - 1); k >= 0; --k) {
                char c = str.charAt(k);
                if(c == '.') break;
                if(c != '0') {
                    any = true;
                    break;
                }
            }
            if(!any) {
                useE = true;
                break;
            }
            strV[i] = str;
        }

        // Format using "%e" (if the previous one cannot be used)
        if(useE) {
            formatString = "%." + decPrec + "e";
            for(int i = 0; i < v.length; ++i) {
                strV[i] = StringTranslator.format(formatString, v[i]);
            }
        }

        // Return the formatted values
        return strV;
    }

    // Return an i18n formatted string from the given format string and data
    public static String format(String format, Object... data)
    {
        _fsbuilder.setLength(0);
        _formatter.format(format, data);
        return _formatter.toString();
    }

    // Generate and return a report using the template with the given name
    public static String generateReportFromTemplate(String templateName, String[] kvps, boolean html, boolean withNonEmptyDoubleLineBreak)
    {
        try {
            // Open the template resource
            InputStream is = GUIMain.instance.getClass().getResourceAsStream( "/anemonesoft/i18n/report_template/"
                                                                              + _locale.getLanguage() + "_" + _locale.getCountry() + "/"
                                                                              + templateName
                                                                              + (html ? ".html" : ".txt") );
            if(is == null) {
                // Try to load the en-US version if the former one is not available
                is = GUIMain.instance.getClass().getResourceAsStream(
                    "/anemonesoft/i18n/report_template/en_US/" + templateName + (html ? ".html" : ".txt")
                );
            }

            // Load the template data
            char[]            rb  = new char[1024];
            StringBuilder     sb  = new StringBuilder(1024);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            for(;;) {
                int len = isr.read(rb, 0, rb.length);
                if(len <= 0) break;
                sb.append(rb, 0, len);
            }

            // Prepare the hash-table
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            Hashtable<String, String> kvmap = new Hashtable<String, String>();
            kvmap.put("{app_name_ver}",  formatString("res_app_name_ver_T",  new String[]{ GUIMain.APP_VERSION }                         ));
            kvmap.put("{app_copyright}", formatString("res_app_copyright_T", new String[]{ GUIMain.APP_COPY_YEAR, GUIMain.APP_COPY_NAME }));
            kvmap.put("{date_time}",     dateFormat.format(Calendar.getInstance().getTime()));
            kvmap.put("{operator_name}", GUIMain.instance.getOperatorName());

            // Prepare the regular-expression string
            StringBuilder regex = new StringBuilder(128);
            regex.append("\\{app_name_ver\\}|\\{app_copyright\\}|\\{date_time\\}|\\{operator_name\\}");

            // Walk trough the key-value pairs to complete the hash-table and regular-expression string
            for(int i = 0; i < kvps.length; i += 2) {
                kvmap.put("{" + kvps[i] + "}", kvps[i + 1]);
                regex.append("|\\{");
                regex.append(kvps[i]);
                regex.append("\\}");
            }

            // The generated report
            StringBuffer sbProc = new StringBuffer();

            // Start the HTML document (if needed)
            if(html) {
                sbProc.append("<!DOCTYPE html>\n");
                sbProc.append("<html lang='en'>\n");
                sbProc.append("<head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/></head>\n");
                sbProc.append("<body><pre>\n");
            }

            // Perform string replacements
            Pattern pat = Pattern.compile(regex.toString(), Pattern.CANON_EQ | Pattern.MULTILINE);
            Matcher mat = pat.matcher(sb);
            while(mat.find()) {
                mat.appendReplacement(sbProc, kvmap.get(mat.group()));
            }
            mat.appendTail(sbProc);

            // End the HTML document (if needed)
            if(html) {
                sbProc.append("</pre></body>\n");
                sbProc.append("</html>\n");
            }

            // Return the final string
            if(withNonEmptyDoubleLineBreak)
                return sbProc.toString().replaceAll("\\n\\n", "\n \n");
            else
                return sbProc.toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
