/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo;

import java.util.Date;



public interface EchoService {

	String echo(String input);
	
	String echo2(String input, int size);
	
	Date now();
}
