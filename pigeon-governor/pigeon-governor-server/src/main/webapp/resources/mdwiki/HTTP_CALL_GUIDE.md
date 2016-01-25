目录

[pigeon http协议支持传送门](http://code.dianpingoa.com/arch/pigeon2/blob/master/pigeon-governor/pigeon-governor-server/src/main/webapp/resources/mdwiki/USER_GUIDE.md#toc_9)

[pigeon http协议支持 + slb负载均衡](#toc_0)

[运维规范](#toc_1)

[快速入门](#toc_2)

[1、POST method](#toc_3)

[2、GET method](#toc_4)

[更多方法示例](#toc_5)

[1、POST method](#toc_6)

[2、GET method](#toc_7)

[自定义请求数据(服务级别，推荐)](#toc_8)

[自定义请求数据(全局级别，不推荐)](#toc_9)

## pigeon http协议支持 + slb负载均衡
_______

pigeon提供的http协议支持 + slb软负载，可以实现http服务的横向扩展、负载均衡和心跳检测。

调用方只需要关心服务对外提供的url接口和调用方式即可。

### 运维规范

首先服务提供方要从workflow发起流程`开通给美团访问http域名`，填写pigeon service所在的`应用名`。

运维同学会根据流程，绑定应用下所有机器的ip的4080端口走slb软负载。域名规范如下：

beta环境：`http://pigeon.51ping.com/应用名`    ——》    `http://ip:4080`

线上环境：`http://pigeon.dper.com/应用名`    ——》    `http://ip:4080`

如仍有疑问可联系运维负责人许奎、沈玉龙、南海洋。

### 快速入门

以IpService为例。

    <bean id="echoService" class="com.dianping.pigeon.remoting.invoker.config.spring.ReferenceBean" init-method="init">
       <property name="url" value="com.dianping.iphub.service.IpService" /><!-- 服务全局唯一的标识url，默认是服务接口类名，必须设置 -->
       <property name="interfaceName" value="com.dianping.iphub.service.IpService" /><!-- 接口名称，必须设置 -->
       <property name="timeout" value="1000" /><!-- 超时时间，毫秒，默认5000，建议自己设置 -->
       <property name="callType" value="sync" /><!-- 调用方式，sync/future/callback/oneway，默认sync，可不设置 -->
    </bean>

这里作为公用测试示例，运维同学已经在slb上配置了IpService在beta和线上环境的转发规则：

`http://pigeon.51ping.com/iphub-service`  ——》 `http://ip:4080`     #beta环境

`http://pigeon.dper.com/iphub-service`  ——》 `http://ip:4080`     #线上环境

调用`IpService`的`getIpInfo`方法，参数为`String`类的ip地址，返回值为`IpInfo`类。

#### 1、POST method

首先拼写调用url：`http://pigeon.51ping.com/iphub-service/service?serialize=7`

其中serialize为序列化方式参数，7为json序列化，2为hessian序列化。

拼写调用数据，查询ip为`80.2.1.23`的相关信息：

    {
    	"url":"com.dianping.iphub.service.IpService",
    	"methodName":"getIpInfo",
    	"parameters":[
            ["java.lang.String","80.2.1.23"]
        ],
    	"timeout":1000,
    	"serialize":7,
    	"callType":1,
    	"messageType":2,
    	"seq":-985
    }

或可以将参数简写为：

    {
        "url":"com.dianping.iphub.service.IpService",
        "methodName":"getIpInfo",
        "parameters":["80.2.1.23"],
        "timeout":1000,
        "serialize":7,
        "callType":1,
        "messageType":2,
        "seq":-985
    }

调用成功将获得返回值：

    {
        "seq": -985,
        "messageType": 2,
        "context": null,
        "responseValues": null,
        "exception": null,
        "response": {
            "@class": "com.dianping.iphub.IpInfo",
            "ip": "80.2.1.23",
            "apn": "unknown",
            "countryName": "英国",
            "provinceId": 137,
            "provinceName": "英国",
            "cityId": 0,
            "cityName": "hertford",
            "carrierName": "as5089 - virgin media limited--virgin media - baguley",
            "sourceCityId": -329344574,
            "sourceProvinceId": 826059,
            "sourceCarrierId": 75653,
            "sourceCountryId": 826,
            "sourceProvinceName": "hertford",
            "sourceCityName": "hertford",
            "isAgent": null
        }
    }

#### 2、GET method

目前get方法仅支持使用json序列化。

首先拼写调用url：`http://pigeon.51ping.com/iphub-service/invoke.json`

拼写调用数据，查询ip为`80.2.1.23`的相关信息：

`url=com.dianping.iphub.service.IpService&method=getIpInfo&parameterTypes=java.lang.String&parameters=80.2.1.23`

用问号拼在一起得到完整url：

`http://pigeon.51ping.com/iphub-service/invoke.json?url=com.dianping.iphub.service.IpService&method=getIpInfo&parameterTypes=java.lang.String&parameters=80.2.1.23`

调用成功将获得返回值：

    {
        "ip": "80.2.1.23",
        "apn": "unknown",
        "countryName": "英国",
        "provinceId": 137,
        "provinceName": "英国",
        "cityId": 0,
        "cityName": "hertford",
        "carrierName": "as5089 - virgin media limited--virgin media - baguley",
        "sourceCityId": -329344574,
        "sourceProvinceId": 826059,
        "sourceCarrierId": 75653,
        "sourceCountryId": 826,
        "sourceProvinceName": "hertford",
        "sourceCityName": "hertford",
        "isAgent": null
    }

其他方法示例请看下节。

### 更多方法示例

调用`IpService`的`reportInfo`方法，参数为`String`和`HashMap`，返回值为`void`。

#### 1、POST method

请求地址：

`http://pigeon.51ping.com/iphub-service/service?serialize=7`

json请求：

    {
    	"url":"com.dianping.iphub.service.IpService",
    	"methodName":"reportInfo",
    	"parameters":[
          ["java.lang.String", "wux"],
          ["java.util.HashMap", {"today":"no","tomorrow":"yes"}]
        ],
    	"callType":1,
    	"messageType":2,
    	"serialize":7,
    	"timeout":1000,
    	"seq":-985
    }

返回：

    {
        "seq": -985,
        "messageType": 2,
        "context": null,
        "responseValues": null,
        "exception": null,
        "response": null
    }


#### 2、GET method

请求地址：

http://pigeon.51ping.com/iphub-service/invoke.json?url=com.dianping.iphub.service.IpService&method=reportInfo&parameterTypes=java.lang.String&parameters=wux&parameterTypes=java.util.HashMap&parameters={"today":"no","tomorrow":"yes"}

### 自定义请求数据(支持到服务级别，推荐)

Http接口的默认格式必须遵循前面几节介绍的规则，从Pigeon `2.7.4`及以上版本开始，开始支持用户自定义请求数据(服务级别)。

1、使用示例

以`http://192.168.225.173:4080/services`中的`http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0`服务为例。

要自定义请求数据，需要实现一个`com.dianping.pigeon.remoting.http.adapter.HttpAdapter`接口。

将自定义的`javax.servlet.http.HttpServletRequest`对象数据转化为`com.dianping.pigeon.remoting.http.adapter.HttpAdapterRequest`对象数据。

然后在http服务被调用之前注册到pigeon中，例如：

    HttpAdapterFactory.registerHttpAdapter("http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0", new CustomizeServiceHttpAdapter());


其中key值为服务名字，不可以填错，否则会找不到自定义的HttpAdapter。

http请求的url参数规则如下：

请求的url路由地址为`http://192.168.225.173:4080/service?customize=service&url=http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0`

除了以上的url必填参数外，还可以在url中继续追加参数，如：`&method=getList`

拼写完整的url请求地址为：

http://192.168.225.173:4080/service?customize=service&url=http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0&method=getList

将请求的json数据简化为方法参数，如下：

    {
        "usr": "ccz",
        "sys": "mac",
        "idle": "test"
    }


CustomizeServiceHttpAdapter.java文件代码示例：

    public class CustomizeServiceHttpAdapter implements HttpAdapter {

        private static ObjectMapper mapper = new ObjectMapper();

        @Override
        public HttpAdapterRequest convert(HttpServletRequest request) throws Exception {
            String method = request.getParameter("method");

            InputStream in = request.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            String info = sb.toString();

            if("getList".equals(method)) {

                //构造参数体
                List<Object> parameters = new ArrayList<Object>();
                HashMap cpuRatio = mapper.readValue(info, HashMap.class);
                parameters.add(cpuRatio);

                return new HttpAdapterRequest(
                        url,
                        method,
                        parameters.toArray(),
                        1000,
                        SerializerFactory.SERIALIZE_JSON,
                        -985L
                );

            } else if("getMap".equals(method)) {

                //构造参数体
                List<Object> parameters = new ArrayList<Object>();
                ArrayList usrList = mapper.readValue(info, ArrayList.class);
                parameters.add(usrList);

                return new HttpAdapterRequest(
                        url,
                        method,
                        parameters.toArray(),
                        1000,
                        SerializerFactory.SERIALIZE_JSON,
                        -985L,
                        1,
                        2
                );

            } else {
                throw new Exception("method not found: " + method);
            }

        }
    }

推荐使用以上方法，进行服务级别的http接口定制。

### 自定义请求数据(全局级别，不推荐)

Http接口的默认格式必须遵循前面几节介绍的规则，从Pigeon `2.7.2`及以上版本开始，开始支持用户自定义请求数据。

1、准备工作

要自定义请求数据，需要实现一个`com.dianping.pigeon.remoting.http.adapter.HttpAdapter`接口，采用jdk的ServiceLoader方式加载。

将自定义的`javax.servlet.http.HttpServletRequest`对象数据转化为`com.dianping.pigeon.remoting.http.adapter.HttpAdapterRequest`对象数据。

2、Demo演示

Demo以`http://192.168.225.173:4080/services`中的`http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0`服务为例。

提供的两个方法接口如下：

    public interface ZkMonitorService {

        public HashMap<String, String> getMap(ArrayList<String> ipList);

        public ArrayList<String> getList(HashMap<String, String> ipMap);

    }


以getList方法为例，接受一个HashMap参数，按照Pigeon的传统方法，

必须将数据Post到限定的url地址：http://192.168.225.173:4080/service?serialize=7，请求的数据格式也被限定为：

    {
        "url":"http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0",
        "methodName":"getList",
        "parameters":[
          ["java.util.HashMap", {"usr":"ccz","sys":"mac","idle":"test"}]
        ],
        "callType":1,
        "messageType":2,
        "serialize":7,
        "timeout":1000,
        "seq":-985
    }


现在我们尝试用HttpAdapter的方式来自定义请求数据：

请求的url地址为`http://192.168.225.173:4080/service?customize=true`的固定格式，

将服务名和方法名参数放在请求url中，如`&url=http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0&method=getList`

拼写完整的url请求地址为：

http://192.168.225.173:4080/service?customize=true&url=http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0&method=getList

将请求的json数据简化为方法参数，如下：

    {
        "usr": "ccz",
        "sys": "mac",
        "idle": "test"
    }


getMap方法也按这样的方式来设计，这里不再赘述。

根据设计思路，在项目的resources资源文件`META-INF/services/`下，新建`com.dianping.pigeon.remoting.http.adapter.HttpAdapter`文件。

注意是`src/main/resources/META-INF/services/`文件夹，而不是webapps下的那个META-INF。

在`com.dianping.pigeon.remoting.http.adapter.HttpAdapter`写入实现类，Demo中为：

    com.dianping.pigeon.remoting.http.adapter.CustomizeHttpAdapter


参见Demo文件链接：[com.dianping.pigeon.remoting.http.adapter.HttpAdapter](http://code.dianpingoa.com/chongze.chen/basicweb/blob/develop/zkmonitor/src/main/resources/META-INF/services/com.dianping.pigeon.remoting.http.adapter.HttpAdapter)

CustomizeHttpAdapter.java文件代码示例：

    public class CustomizeHttpAdapter implements HttpAdapter {

        private static ObjectMapper mapper = new ObjectMapper();

        @Override
        public HttpAdapterRequest convert(HttpServletRequest request) throws Exception {
            String url = request.getParameter("url");
            String method = request.getParameter("method");

            InputStream in = request.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            String info = sb.toString();

            if("http://service.dianping.com/arch/zkmonitor/service/GetMapOrListService_1.0.0".equals(url)) {
                if("getList".equals(method)) {

                    //构造参数体
                    List<Object> parameters = new ArrayList<Object>();
                    HashMap cpuRatio = mapper.readValue(info, HashMap.class);
                    parameters.add(cpuRatio);

                    return new HttpAdapterRequest(
                            url,
                            method,
                            parameters.toArray(),
                            1000,
                            SerializerFactory.SERIALIZE_JSON,
                            -985L
                    );

                } else if("getMap".equals(method)) {

                    //构造参数体
                    List<Object> parameters = new ArrayList<Object>();
                    ArrayList usrList = mapper.readValue(info, ArrayList.class);
                    parameters.add(usrList);

                    return new HttpAdapterRequest(
                            url,
                            method,
                            parameters.toArray(),
                            1000,
                            SerializerFactory.SERIALIZE_JSON,
                            -985L,
                            1,
                            2
                    );

                } else {
                    throw new Exception("method not found: " + method);
                }
            } else {
                throw new Exception("service not found: " + url);
            }

        }
    }


参见Demo文件链接：[CustomizeHttpAdapter.java](http://code.dianpingoa.com/chongze.chen/basicweb/blob/develop/zkmonitor/src/main/java/com/dianping/pigeon/remoting/http/adapter/CustomizeHttpAdapter.java)

也就是说通过将一些参数在`CustomizeHttpAdapter`中构造出`com.dianping.pigeon.remoting.http.adapter.HttpAdapterRequest`，实现自定义请求数据的需求。