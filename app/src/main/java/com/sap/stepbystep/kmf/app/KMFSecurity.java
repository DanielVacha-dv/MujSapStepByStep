package com.sap.stepbystep.kmf.app;

import android.util.Base64;
import android.util.Log;

import com.sap.stepbystep.kmf.KMFSPConstants;
import com.sap.stepbystep.kmf.android.KMFApplication;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class KMFSecurity {
    protected static String TAG = KMFSecurity.class.getName();

    protected static String UTF8 = "UTF-8";
    protected static String ALGORITHM_AES = "AES";
    protected static String ALGORITHM_AESECBPKCS5Padding = "AES/ECB/PKCS5Padding";
    protected static String ALGORITHM_PBKDF2WithHmacSHA1 = "PBKDF2WithHmacSHA1";
    protected static int ITERATION_COUNT = 100;
    protected static int KEY_LENGTH = 256;

    /**
     * Encrypt string.
     *
     * @param source source
     * @return {@link java.lang.String} encrypted source
     */
    public static String encrypt(String source) {
        if (source == null)
            return null;

        String destination = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_AESECBPKCS5Padding);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(
                    KMFApplication.getDefaultSharedPreferencesString(
                            KMFSPConstants.SPC_ENCRYPTION_KEY_OFFLINE
                            , null)
                    )
            );
            destination = Base64.encodeToString(cipher.doFinal(source.getBytes(UTF8)), Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "encrypt()", e);
        }

        destination = destination.replaceAll("\n", "");
        return destination;
    }

    /**
     * Decrypt string.
     *
     * @param source source
     * @return {@link java.lang.String} decrypted source
     */
    public static String decrypt(String source) {
        if (source == null)
            return null;

        String destination = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_AESECBPKCS5Padding);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(
                    KMFApplication.getDefaultSharedPreferencesString(
                            KMFSPConstants.SPC_ENCRYPTION_KEY_OFFLINE
                            , null)
                    )
            );
            destination = new String(cipher.doFinal(Base64.decode(source, Base64.DEFAULT)));
        } catch (Exception e) {
            Log.e(TAG, "decrypt() ", e);
        }
        return destination;
    }

    /**
     * Return SecretKeySpec for encrypt/decrypt.
     *
     * @param seed seed
     * @return {@link javax.crypto.spec.SecretKeySpec}
     * @throws Exception
     */
    private static SecretKeySpec getSecretKeySpec(String seed) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM_PBKDF2WithHmacSHA1);
        SecretKey secretKey = factory.generateSecret(new PBEKeySpec(seed.toCharArray(), seed.getBytes(UTF8), ITERATION_COUNT, KEY_LENGTH));
        return new SecretKeySpec(secretKey.getEncoded(), ALGORITHM_AES);
    }
}
