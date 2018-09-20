package com.sap_press.rheinwerk_reader.utils;

import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.InputStream;

import okhttp3.ResponseBody;

/**
 * Created by hale on 6/28/2018.
 */
public class NetworkErrorUtil {

    public static String ConvertErrorMessageToString(Throwable throwable) {
        String message = "";
        try {
            ResponseBody body = ((HttpException) throwable).response().errorBody();

            InputStream in = body.byteStream();
            String json = IOUtils.toString(in, "UTF-8");
            in.close();
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("detail")) {
                message = jsonObject.getString("detail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    public static int ConvertErrorMessageToStatusCode(Throwable throwable) {
        return throwable instanceof HttpException ? ((HttpException) throwable).response().code() : 0;
    }
}
