package com.sap_press.rheinwerk_reader.crypto;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.internal.io.FileSystem;
import okio.BufferedSource;
import okio.Okio;

public class CryptoManager {
    public static String decryptContentKey(String encryptedContentKey, String encryptedUserKey, String filePath) {
        byte[] userKeyByteArray = CryptoHandler.hexStringToByteArray(encryptedUserKey);
        byte[] appKeyByteArray = new byte[0];
        try {
            appKeyByteArray = Constant.APP_KEY.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] contentKeyByteArray = CryptoHandler.hexStringToByteArray(encryptedContentKey);
        byte[] xorUserKey = CryptoHandler.xorTwoByteArrays(userKeyByteArray, appKeyByteArray);
        byte[] xorContentKey = CryptoHandler.xorTwoByteArrays(contentKeyByteArray, xorUserKey);
        byte[] xorContentKey2 = CryptoHandler.xorTwoByteArrays(xorContentKey, appKeyByteArray);

        return decryptSingleFile(xorContentKey2, filePath);
    }

    private static String decryptSingleFile(byte[] contentKey, String filePath) {
        byte[] fileInput = null;
        try {
            fileInput = getByteArrayFromFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String decryptedUserKey_ContentKey = "";
        try {
            decryptedUserKey_ContentKey = CryptoHandler.getInstance().decrypt(fileInput, contentKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException
                | BadPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decryptedUserKey_ContentKey;
    }

    private static byte[] getByteArrayFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        BufferedSource source = Okio.buffer(FileSystem.SYSTEM.source(file));
        byte[] result = source.readByteArray();
        source.close();
        return result;
    }
}
