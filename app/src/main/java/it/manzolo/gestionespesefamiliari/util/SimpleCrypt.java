package it.manzolo.gestionespesefamiliari.util;

import android.util.Base64;

/**
 * Usage:
 * <pre>
 * String crypto = SimpleCrypto.encrypt(masterpassword, cleartext)
 * ...
 * String cleartext = SimpleCrypto.decrypt(masterpassword, crypto)
 * </pre>
 * @author ferenc.hechler
 */
public class SimpleCrypt {

    public static String encrypt(String seed) throws Exception {
        byte[] data = seed.getBytes("UTF-8");
        String base64 = Base64.encodeToString(data, Base64.URL_SAFE);
        return base64;
    }

    public static String decrypt(String seed) throws Exception {
        byte[] data = Base64.decode(seed, Base64.URL_SAFE);
        String text = new String(data, "UTF-8");
        return text;
    }

}
