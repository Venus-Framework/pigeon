package com.dianping.pigeon.registry.mns.mock;

import com.dianping.pigeon.registry.util.Utils;

/**
 * Created by chenchongze on 16/6/15.
 */
public class FileClient {

    private final String basePath;
    private final String serverPath = "/server";
    private final String weightPath = "/weight";
    private final String protocolPath = "/protocol";
    private final String appPath = "/app";
    private final String versionPath = "/version";

    public FileClient(String basePath) throws Exception {
        this.basePath = basePath;
    }

    public String get(String path, boolean watch) {

        return "";
    }

    public void set(String path) {

    }

    public String getServicePath(String serviceName) {
        return basePath + serverPath + "/" + Utils.escapeServiceName(serviceName);
    }
}
