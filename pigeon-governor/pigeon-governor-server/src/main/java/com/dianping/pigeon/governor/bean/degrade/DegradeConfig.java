package com.dianping.pigeon.governor.bean.degrade;

/**
 * Created by shihuashen on 16/8/17.
 */
public class DegradeConfig {
    private String projectName;
    private String serviceName;
    private String methodName;
    private int returnPattern;
    private String returnValue;
    private String classType;
    private String userDefinedValue;
    private String mockImpl;
    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getUserDefinedValue() {
        return userDefinedValue;
    }

    public void setUserDefinedValue(String userDefinedValue) {
        this.userDefinedValue = userDefinedValue;
    }

    public String getMockImpl() {
        return mockImpl;
    }

    public void setMockImpl(String mockImpl) {
        this.mockImpl = mockImpl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public int getReturnPattern() {
        return returnPattern;
    }

    public void setReturnPattern(int returnPattern) {
        this.returnPattern = returnPattern;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }
}
