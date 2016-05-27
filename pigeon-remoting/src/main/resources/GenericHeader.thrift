namespace java com.dianping.pigeon.remoting.common.domain.generic.thrift

enum MessageType { // 消息类型
    Normal = 0,    // 正常消息
    Heartbeat = 1, // 心跳消息
}
enum CompressType { // 压缩类型
    None = 0,       // 不压缩
    Snappy = 1,     // Snappy
    Gzip = 2        // Gzip
}
struct RequestInfo {                // 请求信息
    1: required i64 sequenceId;     // 消息序列号
    2: required string serviceName; // 服务名
    3: required CallType callType;  // 调用类型
    4: required i32 timeout;        // 请求超时时间
}

enum CallType {   // 调用类型
    Reply = 0,    // 需要响应
    NoReply = 1,  // 不需要响应
}

struct ResponseInfo { // 响应信息
    1: required i64 sequenceId;     // 消息序列号
    2: required StatusCode status; // 消息返回状态
    3: optional string message; // 错误消息
}

// DISCUSSION 状态码和异常的对应关系
enum StatusCode{
    Success = 0,              // 成功
    ApplicationException = 1, // 业务异常，业务接口方法定义抛出的异常
    RuntimeException = 2,     // 运行时异常，一般由业务抛出
    RpcException = 3,         // 框架异常，包含没有被下列异常覆盖到的框架异常
    TransportException = 4,   // 传输异常
    ProtocolException = 5,    // 协议异常
    DegradeException = 6,     // 降级异常
    SecurityException = 7,    // 安全异常
    ServiceException = 8,     // 服务异常，如服务端找不到对应的服务或方法
    RemoteException = 9,      // 远程异常
}
struct TraceInfo {                        // Mtrace 跟踪信息，原 MTthrift 中的 RequestHeader
    1: required string clientAppkey;      // 客户端应用名
    2: optional string traceId;           // Mtrace 的 traceId
    3: optional string spanId;            // Mtrace 的 spanId
    4: optional string rootMessageId;     // Cat 的 rootMessageId
    5: optional string currentMessageId;  // Cat 的 currentMessageId
    6: optional string serverMessageId;   // Cat 的 serverMessageId
    7: optional bool debug;               // 是否强制采样
    8: optional bool sample;              // 是否采样
}

typedef map<string, string> Context // 消息上下文，用于传递自定义数据

struct Header {                                                // 消息头
    1: optional MessageType messageType = MessageType.Normal;  // 消息类型
    2: optional CompressType compressType = CompressType.None; // 压缩类型
    3: optional RequestInfo requestInfo;                       // 请求信息
    4: optional ResponseInfo responseInfo;                     // 响应信息
    5: optional TraceInfo traceInfo;                           // 跟踪信息
    6: optional Context globalContext;                         // 全链路消息上下文，总大小不超过 512 Bytes
    7: optional Context localContext;                          // 单次消息上下文，总大小不超过 2K Bytes
    8: optional bool needChecksum = false;                     // 是否需要 checksum
}