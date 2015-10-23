package com.dianping.piegon.governor.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class CustomBatisGenerator {
	
	public static void main(String args[]) 
					throws IOException, 
							XMLParserException, 
							InvalidConfigurationException, 
							SQLException, 
							InterruptedException {
		
		Scanner scanner = new Scanner(System.in);
		boolean overwrite = true;
		System.out.println("print 'false' to decide overwrite property, otherwise true...");
		
		while (scanner.hasNext()){
			
			if("false".equals(scanner.next())){
				overwrite = false;
				break;
			}else{
				overwrite = true;
				break;
			}
			
		}
		
		List<String> warnings = new ArrayList<String>();
		URL url = Thread.currentThread().getContextClassLoader().getResource("generatorConfig.xml");
		System.out.println(url.toString());
		File configFile = new File(url.getFile());
		ConfigurationParser cp = new ConfigurationParser(warnings);
		Configuration config = cp.parseConfiguration(configFile);
		DefaultShellCallback callback = new DefaultShellCallback(overwrite);
		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
		myBatisGenerator.generate(null);
		
		System.out.println("CustomBatisGenerator finished...");
	}
}
