package com.dianping.pigeon.remoting.common.codec.thrift;

import com.dianping.pigeon.remoting.common.codec.AbstractSerializer;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.domain.generic.GenericRequest;
import com.dianping.pigeon.remoting.common.domain.generic.GenericResponse;
import com.dianping.pigeon.remoting.common.domain.generic.ThriftMapper;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/06/02  下午3:39.
 */
public abstract class AbstractThriftSerializer {

    protected static final int HEADER_FIELD_LENGTH = 2;

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

    protected int getSequenceId() {
        return 1;
    }
}
