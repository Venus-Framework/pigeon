package com.dianping.pigeon.remoting.common.config;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.generic.CompressType;
import org.apache.logging.log4j.Logger;

/**
 * @author qi.yin
 *         2016/06/06  下午8:25.
 */
public class CodecConfig {

    private static final Logger logger = LoggerLoader.getLogger(CodecConfig.class);

    private static final String COMPRESS_ENABLE_KEY = "pigeon.codec.compress.enable";

    private static final String COMPRESS_THRESHOLD_KEY = "pigeon.codec.compress.threshold";

    private static final String COMPRESS_TYPE_KEY = "pigeon.codec.compress.type";

    private static final String CHECKSUM_ENABLE_KEY = "pigeon.codec.checksum.enable";

    private static ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    public static boolean isCompress(int frameSize) {

        boolean isCompact = configManager.getBooleanValue(COMPRESS_ENABLE_KEY, false);

        if (isCompact) {

            int threshold = configManager.getIntValue(COMPRESS_THRESHOLD_KEY, 5000);

            if (frameSize > threshold) {
                return true;
            }
        }
        return false;
    }

    public static final CompressType getCompressType() {

        byte code = (byte) configManager.getIntValue(COMPRESS_TYPE_KEY, 1);
        CompressType compressType = CompressType.None;

        try {
            compressType = CompressType.getCompressType(code);
        } catch (Exception e) {
            logger.error("Invalid compressType. code:" + code, e);
        }

        return compressType;

    }

    public static boolean isChecksum() {
        return configManager.getBooleanValue(CHECKSUM_ENABLE_KEY, false);
    }

}
