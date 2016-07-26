package com.dianping.pigeon.governor.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GsonUtils {

    public static final Gson gson = new Gson();
    public static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();


    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    
    public static Object fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }


    public static String prettyPrint(String s){
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(s);
        String prettyJson = prettyGson.toJson(je);
        return prettyJson;
    }
    public static void Print(Object o){
        System.out.println(prettyPrint(toJson(o)));
    }
}
