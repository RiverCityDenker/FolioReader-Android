package com.sap_press.rheinwerk_reader.crypto;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHandler {

    private static final String TAG = CryptoHandler.class.getSimpleName();
    private static CryptoHandler instance = null;

    public static CryptoHandler getInstance() {
        if (instance == null) {
            instance = new CryptoHandler();
        }
        return instance;
    }

    //    decrypted content_key + initial vector + AES128-CBC-PKCS#7 + file_string = decrypted file_string(e.g .html)
    public String decrypt(byte[] encryptedInput, byte[] byteContentKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {

        byte[] originalBytes = new byte[0];
        try {
            byte[] ivx = hexStringToByteArray(com.sap_press.rheinwerk_reader.crypto.Constant.IV);
            SecretKeySpec skeySpec = new SecretKeySpec(byteContentKey, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivx);
            Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            ecipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            originalBytes = ecipher.doFinal(encryptedInput);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(originalBytes, "UTF8");

    }

    public static byte[] xorTwoByteArrays(byte[] byte1, byte[] byte2) {
        byte[] array_3 = new byte[byte1.length];
        int i = 0;
        for (int b : byte1) {
            array_3[i] = (byte) (b ^ byte2[i++]);
        }
        return array_3;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}