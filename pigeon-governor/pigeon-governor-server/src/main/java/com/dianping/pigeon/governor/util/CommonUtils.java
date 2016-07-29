package com.dianping.pigeon.governor.util;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenchongze on 16/1/22.
 */
public class CommonUtils {

    public static String normalizeHosts(String hosts) {
        return StringUtils.join(new HashSet<String>(Arrays.asList(hosts.split(","))), ",");
    }


    public static String escapeSpace(String input){
        StringBuilder sb = new StringBuilder();
        int size = input.length();
        for(int i = 0;i<size;i++){
            char c = input.charAt(i);
            switch(c){
                case ' ':
                    sb.append("%20");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(normalizeHosts("1.1.1.1,2.2.2.2,1.1.1.1,"));
    }
}
