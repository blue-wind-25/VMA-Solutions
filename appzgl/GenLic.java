/*
    Copyright (C) 2010-2011 Aloysius Indrayanto
                            AnemoneSoft.com
*/

package appzgl;

import java.io.*;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.security.*;
import java.security.spec.*;
import java.util.*;

//
// A class for generating and extracting license
//
public class GenLic {
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

    // Save the given two big integers (modulus and exponent) to file
    private static void _saveToFile(String fileName, BigInteger mod, BigInteger exp) throws Exception
    {
        String modStr = mod.toString(16);
        String expStr = exp.toString(16);

        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileName));
        os.write(modStr.getBytes("UTF-8"));
        os.write('\n');
        os.write(expStr.getBytes("UTF-8"));
        os.close();
    }

    // Load two big integers (modulus and exponent) from file
    private static BigInteger[] _loadFromFile(String fileName) throws Exception
    {
        InputStreamReader is = new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName)), "UTF-8");
        char[]         rb = new char[4096];
        StringBuilder  sb = new StringBuilder(4096);
        for(;;) {
            int len = is.read(rb, 0, rb.length);
            if(len <= 0) break;
            sb.append(rb, 0, len);
        }
        is.close();

        String[] tokens = sb.toString().split("\\n");
        return new BigInteger[] {
            new BigInteger(tokens[0], 16),
            new BigInteger(tokens[1], 16)
        };
    }

    // Read private key from file
    private static PrivateKey _readPrivateKeyFromFile(String fileName) throws Exception
    {
        BigInteger[] me = _loadFromFile(fileName);

        RSAPrivateKeySpec ks = new RSAPrivateKeySpec(me[0], me[1]);
        KeyFactory        kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(ks);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Generate a public-private key pair and save it to the given files
    public static void genKeyPair(String pubKeyFileName, String prvKeyFileName) throws Exception
    {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(4096);

        KeyPair kp = kpg.genKeyPair();

        KeyFactory        kf  = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec  pub = kf.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
        RSAPrivateKeySpec prv = kf.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

        _saveToFile(pubKeyFileName, pub.getModulus(), pub.getPublicExponent());
        _saveToFile(prvKeyFileName, prv.getModulus(), prv.getPrivateExponent());
    }

    // Generate license data for the given host key
    public static String genLicData(String prvKeyFileName, String hostKey) throws Exception
    {
        // Check the host key
        if(_extractHostID(hostKey) == null)
            throw new Exception("The given host key is not valid!");

        // Read the private key
        PrivateKey prvKey = _readPrivateKeyFromFile(prvKeyFileName);

        // Sign the given host key
        Signature s = Signature.getInstance("SHA1withRSA");
        s.initSign(prvKey);
        s.update(hostKey.getBytes());

        byte[] sig = s.sign();

        // Convert the signature into hex
        StringBuilder sb = new StringBuilder();
        for(byte b : sig) sb.append(String.format("%02X", b));

        // Append the masked host key to the signature to form the complete license data
        for(int i = 0, j = 0; i < hostKey.length(); i += 2, ++j) {
            String str = "";
            str += hostKey.charAt(i);
            str += hostKey.charAt(i + 1);
            sb.append( String.format( "%02X", (byte) (Integer.parseInt(str, 16) ^ sig[j]) ) );
        }

        // Separate the license data into multiple lines
        String licData = sb.toString();
        sb.setLength(0);
        for(int i = 0; i < licData.length(); i += HS_LINE_SIZE) {
            sb.append(licData.substring(i, i + HS_LINE_SIZE));
            sb.append("\n");
        }

        // Return the license data
        return sb.toString();
    }

    // Extract the host ID from the given host key or license data
    // (this function does not verify if the license data has a valid signature or not)
    public static String extractHostID(String hostKey_or_licData)
    {
        SHKPair shkp = _extractSigAndHostKey(hostKey_or_licData);
        return (shkp != null) ? _extractHostID(shkp.hostKey) : _extractHostID(hostKey_or_licData);
    }
}
