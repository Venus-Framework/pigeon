package com.dianping.pigeon.util;

public class VersionUtils {

	public static final String VERSION = "2.8.0-SNAPSHOT";
	
    public static final String DP_THRIFT_VERSION = "2.8.0-SNAPSHOT";

    public static final String MT_THRIFT_VERSION = "mtthrift-v1.7.0-NightlyBuild-SNAPSHOT";

    public static final String MT_THRIFT_VERSION_BASE = "mtthrift";

    public static final String PROTO_FST_VERSION = "2.4.3";

    public static final String COMPACT_VERSION = "2.7.5";

    public static boolean isThriftSupported(String version) {
        if (version.startsWith(MT_THRIFT_VERSION_BASE)) {
            return compareVersion(version, MT_THRIFT_VERSION) >= 0;
        } else {
            return compareVersion(version, DP_THRIFT_VERSION) >= 0;
        }
    }

    public static boolean isProtoFstSupported(String version) {
        return compareVersion(version, PROTO_FST_VERSION) >= 0;
    }

    public static boolean isCompactSupported(String version) {
        return compareVersion(version, COMPACT_VERSION) >= 0;
    }

    public static int compareVersion(String version1, String version2) {
        String[] s1 = version1.split("\\.|-");
        String[] s2 = version2.split("\\.|-");

        int len1 = s1.length;
        int len2 = s2.length;
        int compareCount = len1;
        if (len1 <= len2) {
            compareCount = len1;
        } else if (len1 > len2) {
            compareCount = len2;
        }
        for (int i = 0; i < compareCount; i++) {
            int v1 = 0;
            try {
                v1 = Integer.parseInt(s1[i]);
            } catch (RuntimeException e) {
                return s1[i].compareToIgnoreCase(s2[i]);
            }
            int v2 = 0;
            try {
                v2 = Integer.parseInt(s2[i]);
            } catch (RuntimeException e) {
                return s1[i].compareToIgnoreCase(s2[i]);
            }
            int r = v1 - v2;
            if (r > 0) {
                return 1;
            }
            if (r < 0) {
                return -1;
            }
        }
        return len2 - len1;
    }

}
