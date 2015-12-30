目录

[pigeon http协议支持 + slb负载均衡](#toc_0)

[快速入门](#toc_1)

[1、POST method](#toc_2)

[2、GET method](#toc_3)

[详细使用说明](#toc_4)

## pigeon http协议支持 + slb负载均衡
_______

pigeon提供的http协议支持 + slb软负载，可以实现http服务的横向扩展、负载均衡和心跳检测。

调用方只需要关心服务对外提供的url接口和调用方式即可。

### 快速入门

以IpService为例。

    <bean id="echoService" class="com.dianping.pigeon.remoting.invoker.config.spring.ReferenceBean" init-method="init">
       <property name="url" value="com.dianping.iphub.service.IpService" /><!-- 服务全局唯一的标识url，默认是服务接口类名，必须设置 -->
       <property name="interfaceName" value="com.dianping.iphub.service.IpService" /><!-- 接口名称，必须设置 -->
       <property name="timeout" value="1000" /><!-- 超时时间，毫秒，默认5000，建议自己设置 -->
       <property name="callType" value="sync" /><!-- 调用方式，sync/future/callback/oneway，默认sync，可不设置 -->
    </bean>

IpService服务提供方要联系运维，接入slb。运维负责人许奎、沈玉龙、南海洋。

这里作为示例，运维同学已经在slb上配置了IpService在beta环境下的转发规则：

`http://service.51ping.com/`  ——》 `http://ip:4080/`     #其中ip即为提供pigeon service的机器ip。

调用`IpService`的`getIpInfo`方法，参数为`String`类的ip地址，返回值为`IpInfo`类。

#### 1、POST method

首先拼写调用url：`http://service.51ping.com/service?serialize=7`

其中serialize为序列化方式参数，7为json序列化，2为hessian序列化。

拼写调用数据，查询ip为`80.2.1.23`的相关信息：

    {
    	"url":"com.dianping.iphub.service.IpService",
    	"methodName":"getIpInfo",
    	"parameters":["80.2.1.23"],
    	"callType":1,
    	"messageType":2,
    	"serialize":7,
    	"timeout":1000,
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

首先拼写调用url：`http://service.51ping.com/invoke.json`

拼写调用数据，查询ip为`80.2.1.23`的相关信息：：`url=com.dianping.iphub.service.IpService&method=getIpInfo&parameterTypes=java.lang.String&parameters=80.2.1.23`

用问号拼在一起得到完整url：

`http://service.51ping.com/invoke.json?url=com.dianping.iphub.service.IpService&method=getIpInfo&parameterTypes=java.lang.String&parameters=80.2.1.23`

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

### 运维规范
