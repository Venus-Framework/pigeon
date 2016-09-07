package com.dianping.piegon.governor.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Created by chenchongze on 16/9/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations = {
        "classpath:testContext.xml"
})
public class SpringBaseTestCase {

    protected Logger logger;
    protected ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void before() {
        PropertyConfigurator.configure(this.getClass().getClassLoader()
                .getResource("test_log4j.properties"));
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @After
    public void after() {
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
