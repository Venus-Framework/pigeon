## 服务质量路由支持
_________

Pigeon可在invoker端开启基于服务质量的路由支持，目前主要基于服务的超时率，支持力度为服务的方法级别。

### 快速入门

服务质量路由的开关控制由pigeon的配置项pigeon.invoker.request.quality.auto来控制，当前默认开关为false。此配置项支持动态开关。

测试阶段，全局开关默认为关闭，可在应用级别开启自动切换region的开关。
如`app.name=xxx`的应用，可以配置xxx.pigeon.invoker.request.quality.auto为true。

服务质量的分级主要分为good，normal，bad三种。默认情况下失败率低于1%为good级别，失败率介于1%到5%之间为normal，超过5%为bad。

路由时，首先选择good级别的节点，节点数低于默认阈值50%时，加入normal级别的节点参与服务路由。

服务质量路由可以与多region环境路由配合使用。