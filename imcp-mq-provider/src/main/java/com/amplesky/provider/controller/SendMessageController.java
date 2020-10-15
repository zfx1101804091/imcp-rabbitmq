package com.amplesky.provider.controller;

import com.amplesky.common.constant.RabbitConstant;
import com.amplesky.common.utils.CommonUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class SendMessageController {

    @Autowired
    AmqpTemplate rabbitTemplate;  //使用RabbitTemplate,这提供了接收/发送等等方法

    @GetMapping("/sendDirectMessage")
    public String sendDirectMessage() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "test message, hello!";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String,Object> map=new HashMap<>();
        map.put("messageId",messageId);
        map.put("messageData",messageData);
        map.put("createTime",createTime);
        //将消息携带绑定键值：TestDirectRouting 发送到交换机TestDirectExchange
        rabbitTemplate.convertAndSend(RabbitConstant.DIRECT_EXCHANGE_NAME, "TestDirectRouting", map);
        return "ok";
    }

    /**
     * TopicOneReceiver监听队列1，绑定键为：topic.d
     * TopicTotalReceiver监听队列2，绑定键为：topic.#
     * 而当前推送的消息，携带的路由键为：topic.d
     *
     * 所以可以看到两个监听消费者receiver都成功消费到了消息，
     * 因为这两个recevier监听的队列的绑定键都能与这条消息携带的路由键匹配上。
     * @return
     */
    @GetMapping("/sendTopicMessage1")
    public String sendTopicMessage1() {
        String messageId = String.valueOf(CommonUtil.getDistributedId());
        String messageData = "message: topic.d ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> manMap = new HashMap<>();
        manMap.put("messageId", messageId);
        manMap.put("messageData", messageData);
        manMap.put("createTime", createTime);
        rabbitTemplate.convertAndSend("topic-exchange", "topic.d", manMap);
        return "ok";
    }

    /**
     * convertAndSend(String exchange, String routingKey, Object message)
     * 参数1：交换机名称；参数2：路由键, 没有使用到路由键，可以为空；参数3：发送的消息内容
     *
     * 然后看消费者rabbitmq-consumer的控制台输出情况：
     * TopicOneReceiver监听队列1，绑定键为：topic.d
     * TopicTotalReceiver监听队列2，绑定键为：topic.#
     * 而当前推送的消息，携带的路由键为：topic.e
     *
     * 所以可以看到两个监听消费者只有TopicTotalReceiver成功消费到了消息。
     */
    @GetMapping("/sendTopicMessage2")
    public String sendTopicMessage2() {
        String messageId = String.valueOf(CommonUtil.getDistributedId());
        String messageData = "message: topic.# is all ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> womanMap = new HashMap<>();
        womanMap.put("messageId", messageId);
        womanMap.put("messageData", messageData);
        womanMap.put("createTime", createTime);
        rabbitTemplate.convertAndSend("topic-exchange", "topic.e", womanMap);
        return "ok";
    }
}
