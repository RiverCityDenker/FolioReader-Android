package com.sap_press.rheinwerk_reader.download.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by DUY on 4/29/2018.
 */

public class StringUtil {
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public static String convertEditionNumberToString(int editionNumber) {
        String result = "edition";
        switch (editionNumber) {
            case 1:
                result = "1st " + result;
                break;
            case 2:
                result = "2nd " + result;
                break;
            case 3:
                result = "3rd " + result;
                break;
            default:
                result = editionNumber + "th " + result;
                break;
        }

        return result;
    }
}
