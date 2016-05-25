package com.dianping.pigeon.registry.mns;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Created by chenchongze on 16/5/25.
 */
public class MnsRegistry implements Registry {

    private Logger logger = LoggerLoader.getLogger(getClass());

    @Override
    public void init(Properties properties) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getValue(String key) {
        return null;
    }

    @Override
    public String getServiceAddress(String serviceName) throws RegistryException {
        return null;
    }

    @Override
    public String getServiceAddress(String serviceName, String group) throws RegistryException {
        return null;
    }

    @Override
    public String getServiceAddress(String serviceName, String group, boolean fallbackDefaultGroup) throws RegistryException {
        return null;
    }

    @Override
    public void registerService(String serviceName, String group, String serviceAddress, int weight) throws RegistryException {

    }

    @Override
    public void unregisterService(String serviceName, String serviceAddress) throws RegistryException {

    }

    @Override
    public void unregisterService(String serviceName, String group, String serviceAddress) throws RegistryException {

    }

    @Override
    public int getServerWeight(String serverAddress) throws RegistryException {
        return 0;
    }

    @Override
    public List<String> getChildren(String key) throws RegistryException {
        return null;
    }

    @Override
    public void setServerWeight(String serverAddress, int weight) throws RegistryException {

    }

    @Override
    public String getServerApp(String serverAddress) {
        return null;
    }

    @Override
    public void setServerApp(String serverAddress, String app) {

    }

    @Override
    public void unregisterServerApp(String serverAddress) {

    }

    @Override
    public void setServerVersion(String serverAddress, String version) {

    }

    @Override
    public String getServerVersion(String serverAddress) {
        return null;
    }

    @Override
    public void unregisterServerVersion(String serverAddress) {

    }

    @Override
    public String getStatistics() {
        return null;
    }

    @Override
    public void setServerService(String serviceName, String group, String hosts) throws RegistryException {

    }

    @Override
    public void delServerService(String serviceName, String group) throws RegistryException {

    }

    @Override
    public void registerAppHostList(String serviceAddress, String appName, Integer consolePort) {

    }

    @Override
    public void unregisterAppHostList(String serviceAddress, String appName) {

    }

    @Override
    public void updateHeartBeat(String serviceAddress, Long heartBeatTimeMillis) {

    }

    @Override
    public void deleteHeartBeat(String serviceAddress) {

    }
}
