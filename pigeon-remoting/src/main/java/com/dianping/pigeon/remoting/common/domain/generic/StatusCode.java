package com.dianping.pigeon.remoting.common.domain.generic;

/**
 * @author qi.yin
 *         2016/06/03  下午2:15.
 */
public enum StatusCode {

    Success((byte) 0),              // 成功
    ApplicationException((byte) 1), // 业务异常，业务接口方法定义抛出的异常
    RuntimeException((byte) 2),     // 运行时异常，一般由业务抛出
    RpcException((byte) 3),         // 框架异常，包含没有被下列异常覆盖到的框架异常
    TransportException((byte) 4),   // 传输异常
    ProtocolException((byte) 5),    // 协议异常
    DegradeException((byte) 6),     // 降级异常
    SecurityException((byte) 7),    // 安全异常
    ServiceException((byte) 8),     // 服务异常，如服务端找不到对应的服务或方法
    RemoteException((byte) 9);      // 远程异常

    private byte code;

    private StatusCode(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return this.code;
    }

    public static StatusCode getStatusCode(byte code) {
        switch (code) {
            case 0:
                return Success;
            case 1:
                return ApplicationException;
            case 2:
                return RuntimeException;
            case 3:
                return RpcException;
            case 4:
                return TransportException;
            case 5:
                return ProtocolException;
            case 6:
                return DegradeException;
            case 7:
                return SecurityException;
            case 8:
                return ServiceException;
            case 9:
                return RemoteException;

            default:
                throw new IllegalArgumentException("invalid StatusCode code: " + code);
        }
    }
}
