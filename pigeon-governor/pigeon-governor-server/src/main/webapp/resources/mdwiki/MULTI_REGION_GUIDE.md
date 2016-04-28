## 多region环境路由支持
_______

自15年10月新美大合并以来，新公司的业务整合迅速。为更好的支持北京侧和上海侧的业务互通需求，架构组件对异地机房，多网络的支持与改进也在持续开发与完善中。

pigeon的2.7.6-SNAPSHOT版本目前已经支持到可以在本地(local)与远程(remote)双逻辑机房的自动region切换支持。

### 快速入门

多region的开关控制由pigeon的配置项pigeon.regions.enable来控制，当前默认开关为false。

建议开启pigeon.regions.enable的同时，关闭pigeon.route.preferaddresses.enable的开关。

测试阶段，全局开关默认为关闭，可在应用级别开启自动切换region的开关。如`app.name=xxx`的应用，可以配置xxx.pigeon.regions.enable为true，以及xxx.pigeon.route.preferaddresses.enable为false。

region的判断由配置项pigeon.regions控制，例如beta环境的全局默认配置可能为：`region1:10.66;region2:192.168;region3:172.24`。即以ip网段来判断region的归属。

region切换逻辑：首次与local所有service建立tcp长连接，心跳线程扫描所有的可用client，当可用client少于业务调用方设定的比例(pigeon.regions.switchratio默认为0.5f即50%)时，切换当前region为remote，同时连接远程region的所有service；当前region为remote时，扫描local的client心跳变化，当可用client超过业务调用方设定的比例时，恢复region为local，重新与local所有service建立连接，关闭remote的service的连接。