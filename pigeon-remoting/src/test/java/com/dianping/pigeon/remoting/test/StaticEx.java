package com.dianping.pigeon.remoting.test;

/**
 * Created by chenchongze on 16/10/10.
 */
public class StaticEx {

    public static final int i = 1;

    static {
        init();
    }

    public static void pti(int i) {
        System.out.println(i);
    }

    public static void init() {
        throw new RuntimeException("11");
    }
}
