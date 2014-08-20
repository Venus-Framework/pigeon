package com.dianping.pigeon.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.test.jetty.JettyServer;

import com.dianping.pigeon.container.SpringContainer;

@RunWith(JUnit4.class)
public class TestServer extends JettyServer {
	public static void main(String[] args) throws Exception {
		TestServer server = new TestServer();

		server.startServer();
		server.startWebapp();
		server.stopServer();
	}

	@Before
	public void before() throws Exception {
		System.setProperty("devMode", "true");
		super.startServer();
	}

	@Override
	protected String getContextPath() {
		return "/";
	}

	@Override
	protected int getServerPort() {
		return 3473;
	}

	@Test
	public void startWebapp() throws Exception {
		// open the page in the default browser
		display("/inspect/status");

		new SpringContainer("classpath*:META-INF/spring/typical/invoker.xml").start();

		waitForAnyKey();
	}
}
