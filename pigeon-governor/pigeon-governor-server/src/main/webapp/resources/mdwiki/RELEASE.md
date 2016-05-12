## pigeon版本发布说明
------

### 2.7.6
增加安全认证，支持token认证及IP过滤
增加服务降级支持，接口任意方法可动态降级，返回默认值，默认值可通过json配置

### 2.7.5
http返回格式标准化，异常时返回400/500状态码
增加压缩请求组对象CompactRequest，减少请求对象大小

### 2.7.4
增加http适配器，可以适配来自业务方自定义格式的http请求
修复http callback问题
增加注册中心心跳，用于自动下线无效的服务地址

### 2.7.1
添加了对pigeon-octo扩展包的支持

### 2.7.0
增加了timeline信息
修复了服务测试支持Map对象的问题

### 2.6.9
增加PigeonCallback与PigeonServiceCallback监控信息

### 2.6.8
解决服务配置变更通知的多线程问题

### 2.6.7
修复curator通知会block线程的问题

### 2.6.6
修改callback模式下CAT Transaction打点信息

### 2.6.5
修复netty连接过长的问题

### 2.6.4
修复关闭自动注册后，客户端调用ServiceFactory中online()、offline()和
setServerWeight()方法时，客户端不会调用governor-server的api问题

### 2.6.3
服务调用者初始化服务连接时会并行与多个远程服务地址建立连接，加快启动速度
服务提供者自动实现服务隔离，慢请求会转到慢请求线程池，不影响其他正常请求
修复future和callback模式下的耗时统计不准确的问题

### 2.6.2
修改了jackson序列化的默认class信息配置

### 2.6.1
修改pigeonCall.QPS和pigeonService.QPS监控信息，cat event改成秒显示

### 2.6.0
增加pigeonCall.QPS和pigeonService.QPS监控信息

### 2.5.9
修复服务注册时会引起客户端少量超时的问题

### 2.5.8
修复cat里messageid传递混乱的问题

### 2.5.6
修改log4j2配置加载方式，避免与应用log4j2配置冲突的问题

### 2.5.5
修复客户端不能重连已关闭连接的问题

### 2.5.4
去除kryo序列化方式

### 2.5.3
修复与cat的getStatus接口不兼容的问题

### 2.5.2
修复http测试验证码的问题
console测试默认记录监控信息，并记录http来源ip
增加setResponseContext接口

### 2.5.1
增加InvokerHelper.setCallback接口，调服务之前可以动态设置callback
启动时提前加载pigeon所有类，解决启动后第一次请求容易超时的问题
去除console模块里phoenix-environment的status检测逻辑

### 2.5.0
每个请求带上最源头发送app和ip信息，可追踪请求的源头
增加pigeonCall.responseSize，pigeonService.requestSize监控信息
重构ContextUtils接口，计划去除老的avatar-tracker，增加了setRequestContext、setGlobalContext接口
增加ProviderHelper.writeSuccessResponse等接口，支持用户自行发送response
future调用的cat监控不再显示为PigeonFuture，而是复用PigeonCall

### 2.4.7
log4j改为log4j2，提高日志性能
服务端Error异常不再在客户端直接抛出，而是抛出RemoteInvocationException
修复curator在zookeeper连接丢失时不能自动恢复的问题，pigeon内部会重建zookeeper连接
增加pigeon-context模块，修改中断超时线程的机制，不中断cache请求线程
修复心跳线程计算总机器数不准的问题

### 2.4.6 
修复了zookeeper不可用时可能导致客户端调用线程卡住的问题
更新curator版本到2.7.1
应用可以设置在服务端收到请求后不重设客户端超时时间，默认情况服务端会计算请求的剩余超时时间
修改log4j配置
服务注册、反注册、权重调整事件都记录到cat

### 2.4.5 
修复了服务端线程池隔离存在的问题
修复了DefaultRequest对象与老版本不兼容的问题
对于error的log4j日志改成了异步缓冲模式，防止错误日志太多影响性能

### 2.4.4 
修复了stats接口的统计问题
启用了actor模型和threadpool模型两种模式

### 2.4.3 
增加了protostuff等序列化方式支持

### 2.4.2
在cat上增加了PigeonFuture打点，可以统计future的get操作耗时情况
增加了invoker/provider的秒、分钟等维度下，app调用统计指标
服务端能对不同app进行QPS流控限制

### 2.4.1 
修复schema配置方式时默认roundRobin负载均衡策略的问题，默认改为weightedAutoaware方式

### 2.3.13 
增加cluster类型forking，可并发调用某个服务所有可用服务器，并返回最快的那个返回结果
可配置客户端超时异常打印cat的频率
改为自行读取app.properties，不依赖phoenix-environment里的接口

### 2.3.8 
修复使用@Reference引用服务方式的roundrobin负载均衡会导致CPU负载高的问题，默认改为weightedAutoaware负载均衡方式；

### 2.3.7 
修复cat上客户端和服务端方法名不一致的问题； 
服务端业务异常可配置关闭日志；
服务端可配置记录每个服务请求的日志；

### 2.3.6 
修复心跳偶尔不能摘除不可用服务端的bug； 
修复了服务端完全不可用时会导致客户端卡住的bug，找不到服务地址后不再同步去zookeeper获取新地址，改为异步线程检查服务地址是否可用； 
支持appname前缀的pigeon配置； 
zookeeper改为curator接口； 
加入了单机status健康检查页面； 
cat监控信息加入了appname；
服务端流控支持动态配置并发数，实时调整并发数，并在服务端流控里加入了针对客户端appname进行动态控制的参数；

### 2.2.3 
修复了以jar运行方式时不能启动pigeon的bug； 

### 2.2.2 
修复了关闭自动注册后自行注册时有时候注册不上去的bug； 

### 2.2.1 
修复了pigeon-console不能显示annotation service的bug； 

### 2.2.0 
修复了roundrobin负载均衡策略的bug，解决预热功能部分服务路由不均衡的问题； 
增加了weightedAutoaware负载均衡策略，该策略也支持服务预热； 
applicationContext.properties里的pigeon服务地址配置可在dev和alpha环境同时生效； 

### 2.1.23 
解决hessian不支持BigInteger的bug，扩展hesssian接口支持BigInteger对象的传递； 
支持客户端为服务接口的某个方法设置单独的超时时间； 
支持服务端为某个方法设置最大并发数（可以实现方法级别的独立线程池，实现服务方法的隔离）； 
修复了部分其他bug； 

### 2.1.21 
增加了服务预热功能，解决服务端重启时因为没有预热大量超时的问题（预热原理是客户端发送到服务端的请求会逐步增加，所以需要相关客户端应用升级）； 
增加了服务调用生命周期每个区间的耗时打点信息，方便定位超时原因； 
解决了zookeeper连接断了后未自动重连的bug； 
解决http服务未打cat日志等bug； 
支持开启多个服务端口； 
新的异常设计； 

### 2.1.18 
优化了内存使用，占用内存更少； 
超时的消息日志打到cat时，对消息进行了大小限制，防止出现old gc； 

### 2.1.16 
改进了自动注册机制：之前的版本在pigeon服务启动完成后会马上注册到zookeeper（weight设置为1）这样会导致客户端请求马上发过来，但这时可能其他相关业务组件未初始化完成会导致客户端请求超时。改进后，pigeon服务在生产环境启动后默认会首先将weight设置为0，而线上tomcat启动脚本会等程序完全启动后等待一段时间再调pigeon的接口设置weight为1，同时pigeon内部也有后台线程会等待60秒（可配）后再将weight设置为1。 
去除客户端多余的超时日志。 

### 2.1.13 
去除了与lionapi的强依赖关系 

### 2.1.12 
pigeon-console控制台增加了更多的监控信息，方便监控连接的客户端、心跳、重连线程所对应的服务端地址信息 

### 2.1.11 
修复了reconnect线程的一个bug，该bug可能会导致服务端重启后，客户端感知不到服务端的正常恢复 
修复了服务端泳道配置变化不能正常被客户端zookeeper正常监听的bug 

### 2.1.9 
增加了http支持，可供其他语言方便调用 
增加了对protobuf和json序列化的支持 
增加了annotation编程的支持 
提供了服务测试工具，新增模块pigeon-console（pigeon1.x里的pigeon-engine项目改良集成到pigeon2），pigeon-console提供pigeon单机服务控制台的功能，目前可以对单机服务进行页面测试 

### 2.0.0  
自动注册功能 
在pigeon1.x中服务的注册需要人工通过lion配置。 
pigeon2实现了服务的自动注册功能，服务提供者启动服务之后将自动注册到注册中心（zookeeper），服务调用者将自动感知到服务提供者的变化。 
提供了原生api接口供开发人员使用 
在pigeon1.x中需要通过spring集成pigeon，而pigeon2中提供了原生java api方式调用服务和注册服务。 
代码彻底重构优化，与其他系统如lion，cat完全解耦，并提供了pigeon扩展接口 

### 1.9.1 
提供了泳道功能的支持 
 
### 1.8.0 
修复序列化异常透传的bug 
  1、发生在序列化阶段的异常由于和主线程处于不同的线程，异常不能抛出来，新版本增加了异步异常透传通道，是发生在客户端和服务端的序列化异常及时传递出来； 
  2、超时时间点和时间段统一 
     在连环调用的系统中，超时计算的起始时间由原来的各个系统在收到消息的时刻作为起始时间，统一改为最上端调用系统消息发出的时间； 
     由于连环调用中大家都定义了自己的超时时间间隔，新版本在向后传递的构成中使用时间间隔最短的作为自己的超时时间间隔； 
 
### 1.7.3 
修复Cat埋点NullPointException 
当Servcie调用中某个方法参数 为null时，服务端Cat埋点会抛出NullPointException； 
 
### 1.7.2 
修复心跳消息bug 
此Bug对每天请求很少的业务，如后台人工操作调用的业务有影响； 
 
### 1.7.1 
修复Cat统计问题 
此Bug不影响业务，但在Cat异常和调用统计上会有偏差； 
 
### 1.7.0 
增加pigeon-Engine模块 
可以查看Service的Metadata信息，并在页面上调用Service； 
可以通过Http接口调用Service； 
可以通过Http接口调用Service的pigeonProxy，pigeonProxy在通过pigeon调用Service，以便真实的反应网络通信过程； 
注意：此功能目前仅可在开发测试环境使用，生产环境暂时屏蔽此功能； 
Cat埋点中增加调用端和被调用端的IP； 
 
### 1.6.3 
修复心跳序列化错误的bug 
pigeon1.6.0在DefaultRequest中增加一个字段用于测试，合并时忘记删除，导致Java序列化出问题，由于目前只有心跳用Java序列化，所以基本不影响业务，此版本修复这个bug，去掉DefaultRequest中的多余字段； 
 
### 1.6.2 
增加反序列化异常时日志输出对端IP. 
 
### 1.6.1 
解决1.6.0Cat埋点时的一个NullPointException； 
 
### 1.6.0 
兼容.Net pigeon Client的通信； 
引入Cat监控埋点； 
 
### 1.5.1 
由于pigeon1.5.0在负载很低的情况下负载并不是很均衡，1.5.1解决了低负载下不均衡的问题。 
 
### 1.5.0 
新增基于服务端机器处理能力进行负载均衡； 
可以解决后端某台机器过慢，在原先随机负载模式下请求仍会被平等的发给他，导致某些请求会产生无法响应的错误； 
在服务集群中机器处理请求能力不同的情况下，优先将请求路由到处理能力更强的机器，增加系统吞吐量； 
可以解决由于某台机器响应过慢，Web线程在等待那台过慢机器上的请求超时时，线程被大量block住，导致Web服务器无法响应的问题； 
一定程度上解决服务端在刚启动的一刹那间，由于本地Cache未被预热，所有请求都冲向数据库，连接数超过应用程序限制的最大值，导致超时的问题； 
更加完善的心跳管理机制； 
心跳请求试错取代业务请求试错（网络断开错误），解决后台管理系统在极少量业务请求操作的情况下，业务请求试错不可接受的情况； 
服务端机器假死自动切除和恢复； 
重启时优雅的关闭服务； 
在某台服务被关闭之前，Jboss的stop脚本将调用Lion，将这台Service从所有调用者中将请求切除，等待前面所有Rquest的Response都返回后再关闭这台服务，来避免大量超时的发生； 
由于事先切除了请求流，避免了由于之前版本中，服务端被关闭后，客户无法立马知道Socket已经断开，有部分请求继续发往被关闭的服务端，造成业务报网络Exception； 
 
### 1.4.6 
对pigeon直接使用API，添加不依赖Lion的标识和相应的改造； 

### 1.4.5 
解决极端参数导致狂打日志问题； 
解决业务抛出的Error不能转换问题； 
解决DPSF Exception不能识别问题； 
解决同步调用业务异常不能透传问题； 
用心跳试错代替业务试错，间隔3秒；