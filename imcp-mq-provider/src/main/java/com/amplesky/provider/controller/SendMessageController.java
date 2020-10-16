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

    @GetMapping("/sendFanoutMessage")
    public String sendFanoutMessage() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "message: testFanoutMessage ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);
        rabbitTemplate.convertAndSend("fanoutExchange", null, map);
        return "ok";
    }

    /**
     * 到这里，生产者推送消息的消息确认调用回调函数已经完毕。
     * 可以看到上面写了两个回调函数，一个叫 ConfirmCallback ，一个叫 RetrunCallback；
     * 那么以上这两种回调函数都是在什么情况会触发呢？
     *
     * 先从总体的情况分析，推送消息存在四种情况：
     *
     * ①消息推送到server，但是在server里找不到交换机 ==>> ConfirmCallback
     * ②消息推送到server，找到交换机了，但是没找到队列 ==>> ConfirmCallback和RetrunCallback
     * ③消息推送到sever，交换机和队列啥都没找到 ==>> ConfirmCallback
     * ④消息推送成功 ==>> ConfirmCallback
     */
    /**
     * 1、消息推送到server，但是在server里找不到交换机《触发 ConfirmCallback 回调》
     * 写个测试接口，把消息推送到名为‘non-existent-exchange’的交换机上（这个交换机是没有创建没有配置的）
     */
    @GetMapping("/TestMessageAck")
    public String TestMessageAck() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "message: non-existent-exchange test message ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);
        rabbitTemplate.convertAndSend("non-existent-exchange", "TestDirectRouting", map);
        return "ok";
    }

    /**
     * 2、消息推送到server，找到交换机了，但是没找到队列 《ConfirmCallback和RetrunCallback两个回调函数》
     *
     * 这种情况就是需要新增一个交换机，但是不给这个交换机绑定队列，
     * 我来简单地在DirectRabitConfig里面新增一个直连交换机，名叫‘lonelyDirectExchange’，
     * 但没给它做任何绑定配置操作：
     *
     * 结论：
     *   可以看到这种情况，两个函数都被调用了；
     *   这种情况下，消息是推送成功到服务器了的，所以ConfirmCallback对消息确认情况是true；
     *   而在RetrunCallback回调函数的打印参数里面可以看到，消息是推送到了交换机成功了，但是在路由分发给队列的时候，找不到队列，所以报了错误 NO_ROUTE 。
     *   结论：②这种情况触发的是 ConfirmCallback和RetrunCallback两个回调函数。
     */
    @GetMapping("/TestMessageAck2")
    public String TestMessageAck2() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "message: lonelyDirectExchange test message ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);
        // String exchange, String routingKey, Object object
        rabbitTemplate.convertAndSend("lonelyDirectExchange", "TestDirectRouting", map);
        return "ok";
    }

    /**
     * ③消息推送到sever，交换机和队列啥都没找到
     * 这种情况其实一看就觉得跟①很像，没错 ，③和①情况回调是一致的，所以不做结果说明了。
     *   结论： ③这种情况触发的是 ConfirmCallback 回调函数。
     */
    @GetMapping("/TestMessageAck3")
    public String TestMessageAck3() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "message: lonelyDirectExchange test message ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);
        // String exchange, String routingKey, Object object
        rabbitTemplate.convertAndSend("lonelyDirectExchange1", "TestDirectRouting", map);
        return "ok";
    }

    /**
     * 延迟队列
     * @return
     */
    @GetMapping("/sendDelayMessage")
    public String sendDelayMessage() {
        String messageId = String.valueOf(UUID.randomUUID());
        String messageData = "message: sendDelayMessage test message ";
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Map<String, Object> map = new HashMap<>();
        map.put("messageId", messageId);
        map.put("messageData", messageData);
        map.put("createTime", createTime);
        // String exchange, String routingKey, Object object
        rabbitTemplate.convertAndSend("delay.exchange", "delay-queue", map,message -> {
            //延迟队列
            message.getMessageProperties().setHeader("x-delay",10000);
            return message;
        });
        return "ok";
    }

}
