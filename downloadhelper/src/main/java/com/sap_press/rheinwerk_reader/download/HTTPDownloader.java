package com.sap_press.rheinwerk_reader.download;

import android.util.Log;

import com.sap_press.rheinwerk_reader.utils.FileUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.sap_press.rheinwerk_reader.utils.Constant.X_CONTENT_KEY;

/**
 * Created by hale on 31.10.2018.
 */
public class HTTPDownloader {
    private static final String TAG = HTTPDownloader.class.getSimpleName();

    public static String downloadFile(String fileUrl,
                                      String token,
                                      String folderPath,
                                      String href,
                                      String appVersion) throws Exception {

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(60 * 1000);
        connection.setConnectTimeout(60 * 1000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", token);
        connection.setRequestProperty("x-project", "sap-press");
        connection.setRequestProperty("app_version", appVersion);
        connection.setRequestProperty("file_path", href);
        final int BUFFER_SIZE = 23 * 1024;
        InputStream is = connection.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is, BUFFER_SIZE);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int current;

        while ((current = bis.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, current);
        }

        File file = FileUtil.getFile(folderPath, href);
        Log.d(TAG, "downloadFile: >>>" + file.getCanonicalPath());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(buffer.toByteArray());
        fos.flush();
        fos.close();
        buffer.close();
        bis.close();
        is.close();

        return connection.getHeaderField(X_CONTENT_KEY);
    }
}
