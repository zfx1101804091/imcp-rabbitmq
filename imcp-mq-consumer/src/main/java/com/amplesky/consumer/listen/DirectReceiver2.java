package com.amplesky.consumer.listen;

import com.amplesky.common.constant.RabbitConstant;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息接收监听类
 * 直连交换机既然是一对一，那如果咱们配置多台监听绑定到同一个直连交互的同一个队列，会怎么样？
 *
 * DirectReceiver消费者收到消息【1】  : {createTime=2020-10-15 16:11:03, messageId=f52fb8b9-8c4f-4e47-b751-85b20298d147, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【2】  : {createTime=2020-10-15 16:11:04, messageId=b3b7a7c0-4c46-4ce4-8f9d-0668c97ae02a, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【1】  : {createTime=2020-10-15 16:11:10, messageId=3524bd89-0a69-43dd-97e7-e1ef5bedaca3, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【2】  : {createTime=2020-10-15 16:11:10, messageId=4941bbd3-4672-4d07-95d7-c57505d5b27b, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【1】  : {createTime=2020-10-15 16:11:15, messageId=0131d203-8845-4399-a099-530940a4aa08, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【2】  : {createTime=2020-10-15 16:11:16, messageId=d3d82b4e-9392-464e-9bec-2e4c1e8c80c3, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【1】  : {createTime=2020-10-15 16:11:18, messageId=ec037a98-b6bd-4680-afcd-7a19387eb1c0, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【2】  : {createTime=2020-10-15 16:11:19, messageId=f9465346-b2b9-44ce-998e-ec9701e609f5, messageData=test message, hello!}
 * DirectReceiver消费者收到消息【1】  : {createTime=2020-10-15 16:11:19, messageId=23d18e97-abe9-487d-a858-dee771b19f6c, messageData=test message, hello!}
 *
 * 可以看到是实现了轮询的方式对消息进行消费，而且不存在重复消费。
 */

@Component
@RabbitListener(queues = RabbitConstant.DIRECT_QUEUE)//监听的队列名称 TestDirectQueue
public class DirectReceiver2 {

    @RabbitHandler
    public void process(Map testMessage) {
        System.out.println("DirectReceiver消费者收到消息【2】  : " + testMessage.toString());
    }

}
