package com.dianping.pigeon.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S").format(new Date(1415007600867l)));
	}

}
