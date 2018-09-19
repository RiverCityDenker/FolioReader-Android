package com.sap_press.rheinwerk_reader.utils;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

public class Converters {

    public static <T> List<T> fromString(String value, Class<T[]> clazz) {
        return Arrays.asList(new Gson().fromJson(value, clazz));
    }

    public static <T> String fromArrayList(List<T> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}