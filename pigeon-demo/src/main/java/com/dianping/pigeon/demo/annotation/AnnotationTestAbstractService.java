/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.annotation;

import com.dianping.pigeon.demo.UserService;
import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;

public abstract class AnnotationTestAbstractService extends AnnotationTestAbstractAService {

	@Reference(timeout = 500)
	private UserService userService;

	public String testUser(String input) {
		return userService.echo(input);
	}

}
