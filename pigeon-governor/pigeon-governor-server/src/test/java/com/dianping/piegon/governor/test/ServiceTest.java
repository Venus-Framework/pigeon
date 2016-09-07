package com.dianping.piegon.governor.test;

import com.dianping.ba.hris.md.api.dto.EmployeeDto;
import com.dianping.ba.hris.md.api.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;

/**
 * Created by chenchongze on 16/9/7.
 */
public class ServiceTest {

    private BeanFactory beanFactory;
    private Logger logger;
    private ObjectMapper objectMapper;

    @Before
    public void before() {
        PropertyConfigurator.configure(this.getClass().getClassLoader()
                .getResource("test_log4j.properties"));
        String[] springContexts = {"testContext.xml"};
        beanFactory = new ClassPathXmlApplicationContext(springContexts);
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void test() {
        try {
            EmployeeService employeeService = (EmployeeService) beanFactory.getBean("masterDataEmployeeService");
            List<EmployeeDto> employeeDtos = employeeService.queryEmployeeByKeyword("jin.li.sh");

            for (EmployeeDto employeeDto : employeeDtos) {
                logger.info(objectMapper.writeValueAsString(employeeDto));
            }
        } catch (Throwable t) {
            logger.error(t.toString());
        } finally {
        }
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
