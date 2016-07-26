namespace java com.dianping.pigeon.remoting.common.domain.generic.thrift

struct LoadInfo{
    1: optional double averageLoad;
    2: optional i32 oldGC;
    3: optional i32 threadNum;      //默认线程池
    4: optional i32 queueSize;       //主IO线程队列长度
    5: optional i32 qps;     //1分钟内的qps值
}

struct HeartbeatInfo {
    1: optional string appkey;      // 解决重复注册，修改错误appkey状态的问题
    2: optional i64 sendTime;       // 发送心跳时间，微秒，方便业务剔除历史心跳
    3: optional LoadInfo loadInfo;  // 负载信息
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
    9: optional bool clientIp;            //invoker ip
}
typedef map<string, string> Context // 消息上下文，用于传递自定义数据

struct Header {                                                // 消息头
    1: optional byte messageType = 0;         // 消息类型 
    2: optional RequestInfo requestInfo;                       // 请求信息
    3: optional ResponseInfo responseInfo;                     // 响应信息
    4: optional TraceInfo traceInfo;                           // 跟踪信息
    5: optional Context globalContext;                         // 全链路消息上下文，总大小不超过 512 Bytes
    6: optional Context localContext;                          // 单次消息上下文，总大小不超过 2K Bytes
    7: optional HeartbeatInfo heartbeatInfo;                   // 心跳信息
}