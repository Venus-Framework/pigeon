package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import org.apache.thrift.protocol.TProtocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/06/02  下午3:39.
 */
public abstract class AbstractThriftSerializer {

    protected static final int HEADER_FIELD_LENGTH = 2;

    private static final AtomicInteger SEQID = new AtomicInteger();

    protected ServiceInvocationRepository repository = ServiceInvocationRepository.getInstance();


    protected abstract void doDeserializeRequest(GenericRequest request, TProtocol protocol)
            throws Exception;

    protected abstract void doSerializeRequest(GenericRequest request, TProtocol protocol)
            throws Exception;


    protected abstract void doDeserializeResponse(GenericResponse response, GenericRequest request,
                                                  TProtocol protocol, Header header)
            throws Exception;

    protected abstract void doSerializeResponse(GenericResponse response, TProtocol protocol,
                                                Header header, DynamicByteArrayOutputStream bos)
            throws Exception;

    protected static int getSequenceId() {
        return SEQID.getAndIncrement();
    }
}
