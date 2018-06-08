#JoyTalk

joytalk是京东金融以mqtt协议为通讯协议的Android客户端长连接基础库。
### 支持功能
1. 服务端消息Push
2. 客户端信息上报
3. IM通讯聊天

###性能
1. **重连机制：**分别应对网络波动，无网络，服务器宕机不可用做了重连策略。
2. **质量等级机制：**高等级质量消息支持消息数据从硬盘恢复到内存中，避免因客户端程序自身异常而导致消息丢失。
3. **超时重传机制：**采用最基本的停止等待协议，在配置的超时时间内，若服务器没有应答，则再次发送未回应消息。
4. **智能心跳：**应用处于非活跃态时，自动探测该类型网络下最大的NAT超时时间，通过特定的确认策略将心跳间隔设置为略小于此时间的间隔，失败将回退已经确认的可行的最大心跳，在满足健壮的网络通信条件下，活跃态也可能会采用最大心跳间隔。最大限度降低流量消耗，电量，减轻服务器负载。
5. **GCM支持：**在支持google GCM的手机中将使用GCM推送辅助，根据支持的强弱等级来对使用不同的通信策略，尽可能节省资源。（待做...）
6. **轻量级读写：**采用读写队列处理消息，避免因消息累积而发生雪崩，一定程度上提高了消息的有序性。
7. **数据类型扩展：**消息体数据支持不同类型的数据解析格式，可以是json，protobuffer，xml等等，需要做扩展接口实现。


### API

#####客户端构建
* 在一个应用的生命周期里，客户端对象只能初始化一次，再次初始化将无效。
* 初始化过后可以使用 `MqttClient.getInstance()`来获取已经初始化过的对象。
* 目前长链接服务进程以私有进程方式存在，生命周期与主进程同步
* 初始化方式如下：

```
 MqttClient mqttClient = new MqttClient.Builder()
                .context(this)
                .converter(ProtobufferConverterFactory.create())//默认的数据解析方式为String
                .pushCallBack(this)//服务端主动push数据回调
                .qos(QOS_2) //质量等级，默认是qos_1
                .openLog() //是否打开调试日志，默认关闭,建议正式版本关闭
                .build();
```

#####连接配置创建
```
  MqttConnectOptions mqttConnectOptions = new MqttConnectOptions()
                .setAutomaticReconnect(true)//是否启用重连机制，默认是
                .setCleanSession(false)//是否清楚会话状态，默认否
                .setClientId(clientId)//客户端唯一标识
                .setKeepAliveInterval(8*60000)//服务端保持长连接最大时长
                // ，若超过则会主动断开连接，此值会影响心跳探测最大间隔，建议使用默认8分钟
                .setServerURIs(new String[]{serverUri})//服务器地址
                .setUserName("222")//账号
                .setPassword("sire")//密码
                .setProtocalName(MQTTVersion.VERSION_311);//协议名
```
#####发送连接
连接的回调在应用的整个生命周期内有效，连接的状态会通过此回调报告。

```
mqttClient.connect(mqttConnectOptions, new MessageCallBack() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed(MQTTException exception) {
            }
        });
```
#####订阅消息
```
MqttClient.getInstance().subscribe("我是被订阅的主题", new MessageCallBack() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed(MQTTException exception) {
            }
        });
```
#####取消订阅消息
```
 MqttClient.getInstance().unSubscribe("我是被订阅的消息", new MessageCallBack() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed(MQTTException exception) {
            }
        });
```
#####发布消息
```
MqttClient.getInstance().publish("我是被订阅的消息", "hello world", new MessageCallBack() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailed(MQTTException exception) {

            }
        });
```
#####断开连接
```
        MqttClient.getInstance().disconnect();
```
#####数据类型扩展
请参看 `StringConverterFactory`实现方式。   
    
    
    
---------------  
######若发现任何bug或者建议请联系我 <wangkai51@jd.com>
     
                
                