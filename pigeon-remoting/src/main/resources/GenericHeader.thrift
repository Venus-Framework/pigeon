struct Header {                                                // 消息头 
    1: optional byte messageType = 0;         // 消息类型 
    2: optional byte compressType = 0;         // 压缩类型  
    3: optional RequestInfo requestInfo;                       // 请求信息 
    4: optional ResponseInfo responseInfo;                     // 响应信息 
    5: optional TraceInfo traceInfo;                           // 跟踪信息 
    6: optional Context globalContext;                         // 全链路消息上下文，总大小不超过 512 Bytes 
    7: optional Context localContext;                          // 单次消息上下文，总大小不超过 2K Bytes 
}

struct RequestInfo {                // 请求信息 
    1: required string serviceName; // 服务名 
    2: required i64 sequenceId;     // 消息序列号 
    3: required byte callType = 0;      // 调用类型  
    4: required i32 timeout;        // 请求超时时间 
}

struct ResponseInfo { // 响应信息 
    1: required i64 sequenceId; // 消息序列号 
    2: required byte status = 0; // 消息返回状态
    3: optional string message; //异常消息
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
