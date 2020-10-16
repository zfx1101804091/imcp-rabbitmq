## RabbitMQ 基本使用
### 概述
~~~~
1、RabbitMQ是一个开源的消息代理和队列服务器，用来通过普通协议在完全不同的应用之间共享数据，或者简单地将作业队列以便让分布式服务器进行处理。
2、它现实了AMQP协议，并且遵循Mozilla Public License开源协议，它支持多种语言，可以方便的和spring集成。
3、消息队列使用消息将应用程序连接起来，这些消息通过像RabbitMQ这样的消息代理服务器在应用程序之间路由
~~~~
### 基本概念
#### Broker
~~~~
用来处理数据的消息队列服务器实体
~~~~
#### vhost
~~~~
由RabbitMQ服务器创建的虚拟消息主机，拥有自己的权限机制，一个broker里可以开设多个vhost，用于不同用户的权限隔离，vhost之间是也完全隔离的。
~~~~
#### productor
~~~~
产生用于消息通信的数据
~~~~
#### channel
~~~~
消息通道，在AMQP中可以建立多个channel，每个channel代表一个会话任务。
~~~~
#### exchange

##### direct
~~~~
转发消息到routing-key指定的队列
~~~~
### 消息回调
#### 生产者的回调
```
先从总体的情况分析，推送消息存在四种情况：
     * ①消息推送到server，但是在server里找不到交换机 ==>> ConfirmCallback
     * ②消息推送到server，找到交换机了，但是没找到队列 ==>> ConfirmCallback和RetrunCallback
     * ③消息推送到sever，交换机和队列啥都没找到 ==>> ConfirmCallback
     * ④消息推送成功 ==>> ConfirmCallback
```
**application.yml**
```
spring:
  application:
    name: imcp-mq-provider
  rabbitmq:
    host: 10.200.195.6
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    ## 消息确认配置项
    publisher-confirms: true # 确认消息已经发送到交换机(Exchange)
    publisher-returns: true  # 确认消息已发送到队列(Queue)


```

**RabbitmqConfig**
```
@Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        //设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函
        template.setMandatory(true);
        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("ConfirmCallback:     "+"相关数据："+correlationData);
                System.out.println("ConfirmCallback:     "+"确认情况："+ack);
                System.out.println("ConfirmCallback:     "+"原因："+cause);
            }
        });

        template.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("ReturnCallback:     "+"消息："+message);
                System.out.println("ReturnCallback:     "+"回应码："+replyCode);
                System.out.println("ReturnCallback:     "+"回应信息："+replyText);
                System.out.println("ReturnCallback:     "+"交换机："+exchange);
                System.out.println("ReturnCallback:     "+"路由键："+routingKey);
            }
        });
        return template;
    }
```
#### 生产者回调详解
![Image text](https://img-blog.csdnimg.cn/20190420091742485.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl8zODQxOTEzMw==,size_16,color_FFFFFF,t_70)