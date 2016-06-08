## 多region环境路由支持
_______

自15年10月新美大合并以来，新公司的业务整合迅速。

为更好的支持北京侧和上海侧的业务互通需求，架构组件对异地机房，多网络的支持与改进也在持续开发与完善中。

pigeon的2.7.6-SNAPSHOT版本目前可以提供多region路由支持的测试版本。

### 快速入门

多region的开关控制由pigeon的配置项pigeon.regions.enable来控制，当前默认开关为false。此配置项支持动态开关。

测试阶段，全局开关默认为关闭，可在应用级别开启自动切换region的开关。
如`app.name=xxx`的应用，可以配置xxx.pigeon.regions.enable为true。

region的判断由配置项pigeon.regions控制，即以ip网段来判断region的归属。

例如beta环境的全局默认配置可能为：

`region1:10.66,172.24;region2:192.168;region3:10.128`

region自动路由规则由Region Policy提供支持。

pigeon提供两种region路由策略auto switch policy，weight based policy。默认为auto switch policy。

配置粒度支持到service级别，配置方式如下，在invoker配置中加入regionPolicy属性:

     <bean id="echoService" class="com.dianping.pigeon.remoting.invoker.config.spring.ReferenceBean"
        init-method="init">
        <property name="url"
        value="http://service.dianping.com/com.dianping.pigeon.demo.EchoService" />
        <property name="interfaceName" value="com.dianping.pigeon.demo.EchoService" />
        <property name="callType" value="sync" />
        <property name="timeout" value="1000" />
        <property name="regionPolicy" value="weightBased" /> <!— 默认为autoSwitch —>
    </bean>

a) auto switch policy

`
pigeon.regions.prefer.region1=region1:3,region2:1,region3:0
pigeon.regions.prefer.region2=region2:10,region3:3,region1:1
pigeon.regions.prefer.region3=region3:3,region1:1,region2:0
`

其中特定region的优先级规则由`pigeon.regions.prefer.regionX`配置项定义(冒号后面为region权重，用于weight based policy)。

路由规则：按照优先级选择region中的可用client连接，当region可用率低于设置的切换阈值时，依次选择下一个优先级的region。

切换阈值从配置项pigeon.regions.switchratio读取，默认值为0.5f，即可用的client连接低于50%为region不可用。

b) weight based policy

`
pigeon.regions.prefer.region1=region1:3,region2:1,region3:0
pigeon.regions.prefer.region2=region2:10,region3:3,region1:1
pigeon.regions.prefer.region3=region3:3,region1:1,region2:0
`

其中region的权重由`pigeon.regions.prefer.regionX`配置项定义(冒号后面为region权重)。

路由规则：按照region权重，随机选择特定region中的可用client连接。