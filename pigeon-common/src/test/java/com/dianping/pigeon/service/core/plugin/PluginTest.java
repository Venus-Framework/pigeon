package com.dianping.pigeon.service.core.plugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.extension.plugin.Plugin;

public class PluginTest {
	@Test
	public void test() {
		XMap xmap = new XMap();
		xmap.register(Plugin.class);
		try {
			Plugin plugin = (Plugin) xmap.load(this.getClass().getClassLoader()
					.getResourceAsStream("META-INF/pigeon/plugin.xml"));
			System.out.println(plugin.getDescriptor());
			Class<?> descriptorClass = Class.forName(plugin.getDescriptor());
			xmap.register(descriptorClass);
			InputStream content = new ByteArrayInputStream(plugin.getContent().getBytes());
			Object testPlugin = xmap.load(content);
			System.out.println("PLUGIN " + testPlugin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
