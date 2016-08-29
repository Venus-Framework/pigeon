package com.dianping.pigeon.governor.util;

/**
 * Created by shihuashen on 16/8/1.
 */
public class RandomUtils {
    private static String chars = "abcdefghijklmnopqrstuvwxyz";
    public static String getRandomString(int length){
        StringBuilder sb = new StringBuilder();
        for(int i =0;i<length;i++){
            sb.append(chars.charAt((int)(Math.random()*26)));
        }
        return sb.toString();
    }
}
