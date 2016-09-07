package com.dianping.piegon.governor.test;

import com.dianping.ba.hris.md.api.dto.EmployeeDto;
import com.dianping.ba.hris.md.api.service.EmployeeService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by chenchongze on 16/9/7.
 */
public class PigeonServiceTest extends SpringBaseTestCase {

    @Autowired
    private EmployeeService employeeService;

    @Test
    public void test() {
        try {

            List<EmployeeDto> employeeDtos = employeeService.queryEmployeeByKeyword("jin.li.sh");

            for (EmployeeDto employeeDto : employeeDtos) {
                logger.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(employeeDto));
            }

        } catch (Throwable t) {
            logger.error(t.toString());
        } finally {
        }
    }

}
