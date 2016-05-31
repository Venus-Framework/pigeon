package com.dianping.pigeon.remoting.common.domain.generic;

//import com.dianping.cat.CatConstants;

import com.dianping.pigeon.remoting.common.codec.thrift.annotation.ThriftMethodProcessor;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.*;
import com.dianping.pigeon.remoting.common.exception.*;
import com.dianping.pigeon.remoting.common.exception.SecurityException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceDegradedException;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qi.yin
 *         2016/05/19  上午10:24.
 */
public class ThriftMapper {


    public static Header convertRequestToHeader(GenericRequest request) {
        Header header = new Header();

        //messageType
        if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
            header.setMessageType(MessageType.Heartbeat);
        } else {
            header.setMessageType(MessageType.Normal);
        }

        //compresstype
        switch (request.getCompressType()) {
            case Constants.COMPRESS_TYPE_NONE:
                header.setCompressType(CompressType.None);
                break;
            case Constants.COMPRESS_TYPE_SNAPPY:
                header.setCompressType(CompressType.Snappy);
                break;
            case Constants.COMPRESS_TYPE_GZIP:
                header.setCompressType(CompressType.Gzip);
                break;
        }

        //requestInfo
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setTimeout(request.getTimeout());
        //sequence
        requestInfo.setSequenceId(request.getSequence());
        //serviceName
        requestInfo.setServiceName(request.getServiceName());
        //calltype
        if (request.getCallType() == Constants.CALLTYPE_NOREPLY) {
            requestInfo.setCallType(CallType.NoReply);
        } else {
            requestInfo.setCallType(CallType.Reply);
        }

        header.setRequestInfo(requestInfo);

        //traceInfo
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setClientAppkey(request.getApp());
        Map<String, String> localContext = request.getLocalContext();

        if (localContext != null) {
//            traceInfo.setRootMessageId(localContext.get(CatConstants.PIGEON_ROOT_MESSAGE_ID));
//            traceInfo.setServerMessageId(localContext.get(CatConstants.PIGEON_SERVER_MESSAGE_ID));
//            traceInfo.setCurrentMessageId(localContext.get(CatConstants.PIGEON_CURRENT_MESSAGE_ID));
        }

        header.setTraceInfo(traceInfo);

        //globalContext
        header.setGlobalContext(request.getGlobalContext());

        //localContext
        header.setLocalContext(localContext);

        //needChecksum
        header.setNeedChecksum(false);
        return header;
    }

    public static Header convertResponseToHeader(GenericResponse response, boolean isUserException) {
        Header header = new Header();

        int messageType = response.getMessageType();

        //messageType
        if (messageType == Constants.MESSAGE_TYPE_HEART) {
            header.setMessageType(MessageType.Heartbeat);
        } else {
            header.setMessageType(MessageType.Normal);
        }

        //compresstype
        switch (response.getCompressType()) {
            case Constants.COMPRESS_TYPE_NONE:
                header.setCompressType(CompressType.None);
                break;
            case Constants.COMPRESS_TYPE_SNAPPY:
                header.setCompressType(CompressType.Snappy);
                break;
            case Constants.COMPRESS_TYPE_GZIP:
                header.setCompressType(CompressType.Gzip);
                break;
        }


        //requestInfo
        ResponseInfo responseInfo = new ResponseInfo();
        //sequence
        responseInfo.setSequenceId(response.getSequence());
        responseInfo.setStatus(StatusCode.Success);

        //exception
        if (messageType == Constants.MESSAGE_TYPE_EXCEPTION
                || messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {

            Throwable exception = (Throwable) response.getReturn();

            if (exception != null) {
                if (exception instanceof RpcException) {
                    if (exception instanceof NetworkException) {
                        responseInfo.setStatus(StatusCode.TransportException);
                    } else if (exception instanceof SerializationException) {
                        responseInfo.setStatus(StatusCode.ProtocolException);
                    } else if (exception instanceof ServiceDegradedException) {
                        responseInfo.setStatus(StatusCode.DegradeException);
                    } else if (exception instanceof SecurityException) {
                        responseInfo.setStatus(StatusCode.SecurityException);
                    } else if (exception instanceof RemoteInvocationException) {
                        responseInfo.setStatus(StatusCode.RemoteException);
                    } else if (exception instanceof InvocationFailureException
                            || exception instanceof RejectedException) {
                        responseInfo.setStatus(StatusCode.ServiceException);
                    } else {
                        responseInfo.setStatus(StatusCode.RpcException);
                    }

                } else {

                    if (isUserException) {
                        responseInfo.setStatus(StatusCode.ApplicationException);
                    } else {
                        responseInfo.setStatus(StatusCode.RuntimeException);
                    }
                }
                responseInfo.setMessage(exception.getMessage());
            }
        }

        header.setResponseInfo(responseInfo);

        //localContext
        header.setLocalContext(response.getLocalContext());
        //needChecksum
        header.setNeedChecksum(false);
        return header;
    }


    public static GenericRequest convertHeaderToRequest(Header header) {
        GenericRequest request = new GenericRequest();
        //messageType
        if (header.getMessageType() == MessageType.Heartbeat) {
            request.setMessageType(Constants.MESSAGE_TYPE_HEART);
        } else {
            request.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
        }

        //compresstype
        switch (header.getCompressType()) {
            case None:
                request.setCompressType(Constants.COMPRESS_TYPE_NONE);
                break;
            case Snappy:
                request.setCompressType(Constants.COMPRESS_TYPE_SNAPPY);
                break;
            case Gzip:
                request.setCompressType(Constants.COMPRESS_TYPE_GZIP);
                break;
        }


        //requestInfo
        if (header.getRequestInfo() != null) {
            RequestInfo requestInfo = header.getRequestInfo();
            request.setTimeout(requestInfo.getTimeout());
            if (requestInfo.getCallType() == CallType.NoReply) {
                request.setCallType(Constants.CALLTYPE_NOREPLY);
            } else {
                request.setCallType(Constants.CALLTYPE_REPLY);
            }
            //serviceName
            request.setServiceName(requestInfo.getServiceName());

            //sequenceId
            request.setSequence(requestInfo.getSequenceId());
        }

        //globalContext
        request.setGlobalContext(header.getGlobalContext());
        //localContext
        request.setLocalContext(header.getLocalContext());

        //traceInfo
        if (header.getTraceInfo() != null) {
            return request;
        }

        TraceInfo traceInfo = header.getTraceInfo();
        request.setApp(traceInfo.getClientAppkey());

        if (traceInfo.getRootMessageId() == null &&
                traceInfo.getCurrentMessageId() == null &&
                traceInfo.getServerMessageId() == null) {
            return request;
        }

        if (request.getLocalContext() == null) {
            Map<String, String> localContext = new HashMap<String, String>();
//            localContext.put(CatConstants.PIGEON_ROOT_MESSAGE_ID, traceInfo.getRootMessageId());
//            localContext.put(CatConstants.PIGEON_CURRENT_MESSAGE_ID, traceInfo.getCurrentMessageId());
//            localContext.put(CatConstants.PIGEON_SERVER_MESSAGE_ID, traceInfo.getServerMessageId());
        }
        return request;
    }

    public static GenericResponse convertHeaderToResponse(Header header) {
        GenericResponse response = new GenericResponse();

        StatusCode statusCode = header.getResponseInfo().getStatus();
        //messageType
        if (header.getMessageType() == MessageType.Heartbeat) {
            response.setMessageType(Constants.MESSAGE_TYPE_HEART);
        } else {
            if (statusCode == StatusCode.Success) {
                response.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
            } else if (statusCode == StatusCode.RpcException) {
                response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
            } else {
                response.setMessageType(Constants.MESSAGE_TYPE_SERVICE_EXCEPTION);
            }
        }

        //compresstype
        switch (header.getCompressType()) {
            case None:
                response.setCompressType(Constants.COMPRESS_TYPE_NONE);
                break;
            case Snappy:
                response.setCompressType(Constants.COMPRESS_TYPE_SNAPPY);
                break;
            case Gzip:
                response.setCompressType(Constants.COMPRESS_TYPE_GZIP);
                break;
        }

        //requestInfo
        if (header.getResponseInfo() != null) {
            ResponseInfo responseInfo = header.getResponseInfo();
            response.setSequence(responseInfo.getSequenceId());
        }

        //localContext
        response.setLocalContext(header.getLocalContext());
        return response;
    }

    public static void mapException(Header header, GenericResponse response, String expMessage) {
        if (header.getResponseInfo() != null) {
            ResponseInfo responseInfo = header.getResponseInfo();

            switch (responseInfo.getStatus()) {
                case Success:
                    break;
                case ApplicationException:
                    break;
                case RuntimeException:
                    response.setReturn(new RuntimeException(expMessage));
                    break;
                case RpcException:
                    response.setReturn(new RpcException(expMessage));
                    break;
                case TransportException:
                    response.setReturn(new NetworkException(expMessage));
                    break;
                case ProtocolException:
                    response.setReturn(new SerializationException(expMessage));
                    break;
                case DegradeException:
                    response.setReturn(new ServiceDegradedException(expMessage));
                    break;
                case SecurityException:
                    response.setReturn(new SecurityException(expMessage));
                    break;
                case ServiceException:
                    response.setReturn(new InvocationFailureException(expMessage));
                    break;
                case RemoteException:
                    response.setReturn(new RemoteInvocationException(expMessage));
                    break;
            }

        }

    }
}