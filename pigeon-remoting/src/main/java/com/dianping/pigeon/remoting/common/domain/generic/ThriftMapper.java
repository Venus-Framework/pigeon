package com.dianping.pigeon.remoting.common.domain.generic;

import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.monitor.MonitorConstants;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.Header;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.HeartbeatInfo;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.LoadInfo;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.RequestInfo;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.ResponseInfo;
import com.dianping.pigeon.remoting.common.domain.generic.thrift.TraceInfo;
import com.dianping.pigeon.remoting.common.exception.*;
import com.dianping.pigeon.remoting.common.exception.SecurityException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.exception.RemoteInvocationException;
import com.dianping.pigeon.remoting.invoker.exception.ServiceDegradedException;
import com.dianping.pigeon.remoting.provider.exception.InvocationFailureException;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderSystemInfoCollector;

/**
 * @author qi.yin
 *         2016/05/19  上午10:24.
 */
public class ThriftMapper {


    public static Header convertRequestToHeader(GenericRequest request) {
        Header header = new Header();

        //messageType
        if (request.getMessageType() == Constants.MESSAGE_TYPE_SERVICE) {
            header.setMessageType(MessageType.Normal.getCode());
        } else if (request.getMessageType() == Constants.MESSAGE_TYPE_HEART) {
            header.setMessageType(MessageType.Heartbeat.getCode());
        } else {
            throw new SerializationException("Serialize unknown messageType.");
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
            requestInfo.setCallType(CallType.NoReply.getCode());
        } else {
            requestInfo.setCallType(CallType.Reply.getCode());
        }

        header.setRequestInfo(requestInfo);

        //traceInfo
        TraceInfo traceInfo = new TraceInfo();
        traceInfo.setClientAppkey(request.getApp());
        Map<String, String> localContext = request.getLocalContext();

        if (localContext != null) {
            traceInfo.setRootMessageId(localContext.get(MonitorConstants.ROOT_MSG_ID));
            traceInfo.setServerMessageId(localContext.get(MonitorConstants.SERVER_MSG_ID));
            traceInfo.setCurrentMessageId(localContext.get(MonitorConstants.CURRENT_MSG_ID));
        }

        header.setTraceInfo(traceInfo);

        //globalContext
        header.setGlobalContext(request.getGlobalContext());

        //localContext
        header.setLocalContext(localContext);

        return header;
    }

    public static Header convertResponseToHeader(GenericResponse response) {
        Header header = new Header();

        int messageType = response.getMessageType();

        //messageType
        if (messageType == Constants.MESSAGE_TYPE_SERVICE ||
                messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION ||
                messageType == Constants.MESSAGE_TYPE_EXCEPTION) {
            header.setMessageType(MessageType.Normal.getCode());

        } else if (messageType == Constants.MESSAGE_TYPE_HEART) {
            header.setMessageType(MessageType.Heartbeat.getCode());

        } else if (messageType == Constants.MESSAGE_TYPE_SCANNER_HEART) {
            header.setMessageType(MessageType.ScannerHeartbeat.getCode());
            // 响应心跳信息
            HeartbeatInfo heartbeatInfo = new HeartbeatInfo();
            heartbeatInfo.setAppkey(ConfigManagerLoader.getConfigManager().getAppName());
            heartbeatInfo.setSendTime(response.getCreateMillisTime());
            ProviderSystemInfoCollector providerSystemInfoCollector = ProviderSystemInfoCollector.INSTANCE;
            heartbeatInfo.setStatus(providerSystemInfoCollector.getStatus(response.getPort()));

            LoadInfo loadInfo = new LoadInfo();
            loadInfo.setAverageLoad(providerSystemInfoCollector.getSystemLoadAverage());
            loadInfo.setOldGC(providerSystemInfoCollector.getOldGC());
            loadInfo.setThreadNum(providerSystemInfoCollector.getThreadNum());
            loadInfo.setQueueSize(providerSystemInfoCollector.getQueueSize());
            loadInfo.setMethodQpsMap(providerSystemInfoCollector.getQpsMap());

            heartbeatInfo.setLoadInfo(loadInfo);
            header.setHeartbeatInfo(heartbeatInfo);

        } else {
            throw new SerializationException("Deserialize unknown messageType.");
        }

        //requestInfo
        ResponseInfo responseInfo = new ResponseInfo();
        //sequence
        responseInfo.setSequenceId(response.getSequence());
        responseInfo.setStatus(StatusCode.Success.getCode());

        //exception
        if (messageType == Constants.MESSAGE_TYPE_EXCEPTION
                || messageType == Constants.MESSAGE_TYPE_SERVICE_EXCEPTION) {

            Throwable exception = (Throwable) response.getReturn();

            if (exception != null) {
                if (exception instanceof RpcException) {
                    if (exception instanceof NetworkException) {
                        responseInfo.setStatus(StatusCode.TransportException.getCode());
                    } else if (exception instanceof SerializationException) {
                        responseInfo.setStatus(StatusCode.ProtocolException.getCode());
                    } else if (exception instanceof ServiceDegradedException) {
                        responseInfo.setStatus(StatusCode.DegradeException.getCode());
                    } else if (exception instanceof SecurityException) {
                        responseInfo.setStatus(StatusCode.SecurityException.getCode());
                    } else if (exception instanceof RemoteInvocationException) {
                        responseInfo.setStatus(StatusCode.RemoteException.getCode());
                    } else if (exception instanceof InvocationFailureException
                            || exception instanceof RejectedException) {
                        responseInfo.setStatus(StatusCode.ServiceException.getCode());
                    } else {
                        responseInfo.setStatus(StatusCode.RpcException.getCode());
                    }

                } else {
                    responseInfo.setStatus(StatusCode.RuntimeException.getCode());
                }
                responseInfo.setMessage(exception.getMessage());
            }
        }

        header.setResponseInfo(responseInfo);

        //localContext
        header.setLocalContext(response.getLocalContext());
        return header;
    }


    public static GenericRequest convertHeaderToRequest(Header header) {
        GenericRequest request = new GenericRequest();
        //messageType
        if (header.getMessageType() == MessageType.Normal.getCode()) {
            request.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
        } else if (header.getMessageType() == MessageType.Heartbeat.getCode()) {
            request.setMessageType(Constants.MESSAGE_TYPE_HEART);
        } else if (header.getMessageType() == MessageType.ScannerHeartbeat.getCode()) {
            request.setMessageType(Constants.MESSAGE_TYPE_SCANNER_HEART);
        } else {
            throw new SerializationException("Deserialize unknown messageType.");
        }

        //requestInfo
        if (header.getRequestInfo() != null) {
            RequestInfo requestInfo = header.getRequestInfo();
            request.setTimeout(requestInfo.getTimeout());
            if (requestInfo.getCallType() == CallType.NoReply.getCode()) {
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
        if (header.getTraceInfo() == null) {
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
            localContext.put(MonitorConstants.ROOT_MSG_ID, traceInfo.getRootMessageId());
            localContext.put(MonitorConstants.CURRENT_MSG_ID, traceInfo.getCurrentMessageId());
            localContext.put(MonitorConstants.SERVER_MSG_ID, traceInfo.getServerMessageId());
        }
        return request;
    }

    public static GenericResponse convertHeaderToResponse(Header header) {
        GenericResponse response = new GenericResponse();

        StatusCode statusCode = StatusCode.getStatusCode(header.getResponseInfo().getStatus());
        //messageType
        if (header.getMessageType() == MessageType.Heartbeat.getCode()) {
            response.setMessageType(Constants.MESSAGE_TYPE_HEART);
        } else if (header.getMessageType() == MessageType.Normal.getCode()) {
            switch (statusCode) {
                case Success:
                    response.setMessageType(Constants.MESSAGE_TYPE_SERVICE);
                    break;
                case ApplicationException:
                case RuntimeException:
                    response.setMessageType(Constants.MESSAGE_TYPE_SERVICE_EXCEPTION);
                    break;
                case RpcException:
                case TransportException:
                case ProtocolException:
                case DegradeException:
                case SecurityException:
                case ServiceException:
                case RemoteException:
                    response.setMessageType(Constants.MESSAGE_TYPE_EXCEPTION);
                    break;
            }
        } else {
            throw new SerializationException("Deserialize unknown messageType.");
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

            switch (StatusCode.getStatusCode(responseInfo.getStatus())) {
                case Success:
                    break;
                case ApplicationException:
                    break;
                case RuntimeException:
                    response.setReturn(new ApplicationException(expMessage));
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
                    response.setReturn(new BadRequestException(expMessage));
                    break;
                case RemoteException:
                    response.setReturn(new RemoteInvocationException(expMessage));
                    break;
            }

        }

    }
}