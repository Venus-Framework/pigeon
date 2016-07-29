package com.dianping.pigeon.governor.util;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GsonUtils {
    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    public static final Gson gson = gsonBuilder.create();
    public static final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    public static final GsonBuilder gsonBuilderEscape = new GsonBuilder().disableHtmlEscaping();
    public static final Gson gsonEscape = gsonBuilderEscape.create();
    public static final Gson prettyGsonEscape = gsonBuilderEscape.setPrettyPrinting().create();
    public static String toJson(Object src,boolean isEscape){
        if(isEscape)
            return gson.toJson(src);
        else
            return gsonEscape.toJson(src);
    }
    public static String prettyPrint(String s,boolean isEscape){
        if(!isEscape){
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(s);
            String prettyJson = prettyGsonEscape.toJson(je);
            return prettyJson;
        }else
            return prettyPrint(s);
    }




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
