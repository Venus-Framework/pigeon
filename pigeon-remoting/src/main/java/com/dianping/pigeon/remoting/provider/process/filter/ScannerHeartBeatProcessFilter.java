package com.dianping.pigeon.remoting.provider.process.filter;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.UnifiedResponse;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationFilter;
import com.dianping.pigeon.remoting.common.process.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.domain.ProviderContext;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;
import com.dianping.pigeon.log.Logger;

/**
 * Created by chenchongze on 16/7/25.
 */
public class ScannerHeartBeatProcessFilter implements ServiceInvocationFilter<ProviderContext> {

    private static final Logger logger = LoggerLoader.getLogger(ScannerHeartBeatProcessFilter.class);

    @Override
    public InvocationResponse invoke(ServiceInvocationHandler handler, ProviderContext invocationContext) throws Throwable {

        if (logger.isDebugEnabled()) {
            logger.debug("invoke the ScannerHeartbeatProcessFilter, invocationContext:" + invocationContext);
        }
        if (invocationContext.getRequest().getMessageType() == Constants.MESSAGE_TYPE_SCANNER_HEART) {
            UnifiedResponse response = (UnifiedResponse) ProviderUtils.createScannerHeartResponse(
                    invocationContext.getRequest());
            response.setPort(invocationContext.getChannel().getPort());

            return response;
        }
        return handler.handle(invocationContext);
    }
}
