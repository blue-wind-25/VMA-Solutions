/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package anemonesoft.lic;

import java.io.*;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.*;
import java.security.spec.*;
import java.util.*;

//
// A class for licensing-related operations
//
public class Licensing {
    // Application ID
    private static String APP_ID = "VMA@AnemoneSoft.com";

    // Length of siganture and host key hex string
    private static int HKEY_HEX_LEN =  512;
    private static int SIGN_HEX_LEN = 1024;

    // Maximum length of host ID
    private static int MAX_HSID_LEN = 256;

    // Line size for hex string
    private static int HS_LINE_SIZE = 64;

    // Host ID and key
    private static String _hostID  = null;
    private static String _hostKey = null;

    // Signature and host-key pair class
    private static class SHKPair {
        public byte[] sig;
        public String hostKey;

        SHKPair(byte[] sig_, String hostKey_)
        { sig = sig_; hostKey = hostKey_; }
    }
    
    // Read public key from string
    private static PublicKey _readPublicKeyFromString(String pubKeyRawData) throws Exception
    {
        String[] tokens = pubKeyRawData.split("\\n");

        RSAPublicKeySpec ks = new RSAPublicKeySpec(new BigInteger(tokens[0], 16), new BigInteger(tokens[1], 16));
        KeyFactory       kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(ks);
    }

    // Extract the signature and unmasked host key from the given license data
    private static SHKPair _extractSigAndHostKey(String licData)
    {
        // Check the length
        if(licData.length() != SIGN_HEX_LEN + HKEY_HEX_LEN) return null;

        // Extract the signature and host key
        String hexSig = licData.substring(0, SIGN_HEX_LEN);
        String hexKey = licData.substring(SIGN_HEX_LEN, SIGN_HEX_LEN + HKEY_HEX_LEN);

        // Convert the signature to bytes
        byte[] sig = new byte[hexSig.length() / 2];
        for(int i = 0, j = 0; i < hexSig.length(); i += 2, ++j) {
            String str = "";
            str += hexSig.charAt(i);
            str += hexSig.charAt(i + 1);
            sig[j] = (byte) Integer.parseInt(str, 16);
        }

        // Unmask the host key
        StringBuilder sb = new StringBuilder();
        for(int i = 0, j = 0; i < hexKey.length(); i += 2, ++j) {
            String str = "";
            str += hexKey.charAt(i);
            str += hexKey.charAt(i + 1);
            sb.append( String.format( "%02X", (byte) (Integer.parseInt(str, 16) ^ sig[j]) ) );
        }

        // Return the signature and host key
        return new SHKPair(sig, sb.toString());
    }

    // Extract the host ID from the given host key
    private static String _extractHostID(String hostKey)
    {
        // Check the length of the host key
        if(hostKey.length() != HKEY_HEX_LEN) return null;

        // Get the mask
        int    len   = hostKey.length();
        String sch1a = "" + hostKey.charAt(len - 8) + hostKey.charAt(len - 7);
        String sch1b = "" + hostKey.charAt(len - 6) + hostKey.charAt(len - 5);
        String sch2a = "" + hostKey.charAt(len - 4) + hostKey.charAt(len - 3);
        String sch2b = "" + hostKey.charAt(len - 2) + hostKey.charAt(len - 1);
        char   ch1a  = (char) Integer.parseInt(sch1a, 16);
        char   ch1b  = (char) Integer.parseInt(sch1b, 16);
        char   ch2a  = (char) Integer.parseInt(sch2a, 16);
        char   ch2b  = (char) Integer.parseInt(sch2b, 16);
        char[] mask  = { (char) (ch1a ^ ch1b), (char) (ch2a ^ ch2b) };

        // Unmask the host key bytes to get the host ID
        StringBuilder sb  = new StringBuilder();
        int           cnt = 0;
        for(int i = 0; i < len - 8; i += 2) {
            // Get the hex character
            String str = "";
            str += hostKey.charAt(i);
            str += hostKey.charAt(i + 1);
            // Convert to character and append
            char c = (char) (Integer.parseInt(str, 16) ^ mask[(i / 2) % 2]);
            sb.append(c);
            // Check if we have reached the end of the key
            if(c == '$') ++ cnt;
            if(cnt == 2) break;
        }

        // Return the host ID
        return (cnt == 2) ? sb.toString() : null;
    }

    // Check if the given reference host key (extracted from the license data) match the the host key of this host
    private static boolean _checkHostKey(String hostKey)
    {
        // Generate the host ID and host key of this host if they are not available yet
        if(_hostID == null) getHostKey();

        // Split the reference and current host IDs using the ":" character
        String[] tknRef = _extractHostID(hostKey).split(":");
        String[] tknCmp = _hostID.split(":");

        // Check if the number of the tokens are too few
        if(tknRef.length < 7 && tknCmp.length < 7) return false;

        // The first seven tokens and the last token must match exactly
        for(int i = 0; i < 7; ++i) {
            if(!tknRef[i].equals(tknCmp[i])) return false;
        }
        if(!tknRef[tknRef.length - 1].equals(tknCmp[tknCmp.length - 1])) return false;

        // No need to check the MAC addresses if the current host ID does not have any network card
        if(tknCmp.length == 7) return true;

        // Walk through the rest of the tokens
        int numTotal = 0;
        int numMatch = 0;
        for(int i = 7; i < tknCmp.length; ++i) {
            // Get the token and check if it is a character "$"
            String cmp = tknCmp[i];
            if(cmp.equals("$")) break;
            // Check for a match in the reference tokens
            boolean match = false;
            for(int j = 7; j < tknRef.length; ++j) {
                // Get the token and check if it is a character "$"
                String ref = tknRef[j];
                if(ref.equals("$")) break;
                // Check if the reference match the test string
                if(ref.equals(cmp)) {
                    ++numMatch;
                    break;
                }
            }
            // Increment the total count
            ++numTotal;
        }

        // At least 75% of the MAC addresses must match
        if(numTotal > 0 && (numMatch < 1 || numMatch < numTotal * 3 / 4)) return false;

        // Assume the license as OK
        return true;
    }

    // Check the given license data
    private static boolean _checkLicenseData(String licData, String publicKeyRawData)
    {
        try {
            // Extract the signature and host key
            SHKPair shkp = _extractSigAndHostKey(licData);
            if(shkp == null) return false;

            // Read the public key
            PublicKey pubKey = _readPublicKeyFromString(publicKeyRawData);

            // Check the signature
            Signature s = Signature.getInstance("SHA1withRSA");
            s.initVerify(pubKey);
            s.update(shkp.hostKey.getBytes());
            if(!s.verify(shkp.sig)) return false;

            // Check the host key
            return _checkHostKey(shkp.hostKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Assume invalid if there is any error
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Get the host key of this host
    public static String getHostKey()
    {
        // Get the host key from cache (if any)
        if(_hostKey != null) return _hostKey;

        // Used for buidling the host ID
        StringBuilder sb = new StringBuilder();

        // Generate and store the host ID
        if(true) {
            // Put the opening marker
            sb.append('$'); sb.append(':');
            // Put the application ID
            sb.append(APP_ID); sb.append(':');
            // Put the number of CPU
            sb.append("" + Runtime.getRuntime().availableProcessors()); sb.append(':');
            // Put the operating system informations
            sb.append(System.getProperty("os.arch"   )); sb.append(':');
            sb.append(System.getProperty("os.name"   )); sb.append(':');
            sb.append(System.getProperty("os.version")); sb.append(':');
            // Put the size of the main partitions
            File f = new File("/");
            sb.append("" + f.getTotalSpace());
            sb.append(':');
            // Put the MAC addresses of all installed network cards
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while(interfaces.hasMoreElements()) {
                    // Get the MAC address
                    byte[] mac = interfaces.nextElement().getHardwareAddress();
                    if(mac == null) continue;
                    // Convert  the MAC address to hex string
                    StringBuilder sbMAC = new StringBuilder();
                    for(byte b : mac) sbMAC.append(String.format("%02X", b));
                    // Append the MAC address
                    if(sbMAC.length() > 0) {
                        sbMAC.append(':');
                        if(sb.length() + sbMAC.length() > MAX_HSID_LEN - 4) break;
                        sb.append(sbMAC.toString());
                    }
                }
            }
            catch(Exception e) {}
            // Put the ending marker
            sb.append('$');
            // Store the host ID
            _hostID = sb.toString();
        }
        sb.setLength(0);

        // Initialize random number generator
        Random r = new Random();

        // Mask the host ID and convert it into hex string to generate the host key
        char   ch1a = (char) r.nextInt(256);
        char   ch1b = (char) r.nextInt(256);
        char   ch2a = (char) r.nextInt(256);
        char   ch2b = (char) r.nextInt(256);
        char[] mask = { (char) (ch1a ^ ch1b), (char) (ch2a ^ ch2b) };
        for(int i = 0; i < _hostID.length(); ++i) {
            sb.append( String.format( "%02X", (byte) ( _hostID.charAt(i) ^ mask[i % 2] ) ) );
        }

        // Pad the host key with some random data
        int addCnt = MAX_HSID_LEN - 4 - _hostID.length();
        for(int i = 0; i < addCnt; ++i) {
            sb.append(String.format("%02X", r.nextInt(256)));
        }

        // Append the mask at the end of the host key
        sb.append(String.format("%02X", (int) ch1a));
        sb.append(String.format("%02X", (int) ch1b));
        sb.append(String.format("%02X", (int) ch2a));
        sb.append(String.format("%02X", (int) ch2b));

        // Separate the host key into multiple lines
        String hostKey = sb.toString();
        sb.setLength(0);
        for(int i = 0; i < hostKey.length(); i += HS_LINE_SIZE) {
            sb.append(hostKey.substring(i, i + HS_LINE_SIZE));
            sb.append("\n");
        }

        // Save the host key to cache
        _hostKey = sb.toString();

        // Return the host key
        return _hostKey;
    }

    // Check the given license file
    // Return values: -1 -> cannot read the license file
    //                 0 -> invalid license
    //                 1 -> valid license
    public static int checkLicenseFile(String fileName, String publicKeyRawData)
    {
        // Read the license data
        String licData = null;
        try {
            // Read the file
            InputStreamReader is = new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName)), "UTF-8");
            char[]         rb = new char[4096];
            StringBuilder  sb = new StringBuilder(4096);
            for(;;) {
                int len = is.read(rb, 0, rb.length);
                if(len <= 0) break;
                sb.append(rb, 0, len);
            }
            is.close();
            // Remove new line and space
            licData = sb.toString().replaceAll("\\n", "").replaceAll("\\r", "").replaceAll("\\t", "").replaceAll(" ", "");
        }
        catch(Exception e) {
            e.printStackTrace();
            return -1;
        }

        // Ensure that the license data has the correct length
        if(licData.length() != SIGN_HEX_LEN + HKEY_HEX_LEN) return 0;

        // Check the license
        return _checkLicenseData(licData, publicKeyRawData) ? 1 : 0;
    }

    // Write the given license data to the given file
    public static boolean writeLicenseFile(String fileName, String licData, String publicKeyRawData) throws Exception
    {
        if(!_checkLicenseData(licData, publicKeyRawData)) return false;

        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileName));
        os.write(licData.getBytes("UTF-8"));
        os.close();

        return true;
    }
}
