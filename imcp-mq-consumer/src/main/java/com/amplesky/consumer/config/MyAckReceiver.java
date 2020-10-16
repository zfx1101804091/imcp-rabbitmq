package com.amplesky.consumer.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *  消息接收处理类
 *
 *  对应的手动确认消息监听类，MyAckReceiver.java（手动确认模式需要实现 ChannelAwareMessageListener）：
 *     之前的相关监听器可以先注释掉，以免造成多个同类型监听器都监听同一个队列。
 *     这里的获取消息转换，只作参考，如果报数组越界可以自己根据格式去调整
 *
 *   basic.ack用于肯定确认
 *   basic.nack用于否定确认（注意：这是AMQP 0-9-1的RabbitMQ扩展）
 *   basic.reject用于否定确认，但与basic.nack相比有一个限制:一次只能拒绝单条消息
 */
@Component
public class MyAckReceiver implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message) {

    }

    /**
     *   除了直连交换机的队列TestDirectQueue需要变成手动确认以外，我们还需要将一个其他的队列
     *   或者多个队列也变成手动确认，而且不同队列实现不同的业务处理。
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag  = message.getMessageProperties().getDeliveryTag();
        try {
            //因为传递消息的时候用的map传递,所以将Map从Message内取出需要做些处理
            String msg = message.toString();
            String[] msgArray = msg.split("'");//可以点进Message里面看源码,单引号直接的数据就是我们的map消息数据
            JSONObject jsonObject = JSONUtil.parseObj(msgArray[1]);
            Map msgMap = JSONUtil.toBean(jsonObject, Map.class);
            String messageId=msgMap.get("messageId").toString();
            String messageData=msgMap.get("messageData").toString();
            String createTime=msgMap.get("createTime").toString();
//            System.out.println("  MyAckReceiver  messageId:"+messageId+"  messageData:"+messageData+"  createTime:"+createTime);
//            System.out.println("消费的主题消息来自："+message.getMessageProperties().getConsumerQueue());
            if("direct-queue".equals(message.getMessageProperties().getConsumerQueue())){
                System.out.println("消费的消息来自的队列名为："+message.getMessageProperties().getConsumerQueue());
                System.out.println("消息成功消费到  messageId:"+messageId+"  messageData:"+messageData+"  createTime:"+createTime);
                System.out.println("执行direct-queue中的消息的业务处理流程......");
            }

            if ("fanout.A".equals(message.getMessageProperties().getConsumerQueue())){
                System.out.println("消费的消息来自的队列名为："+message.getMessageProperties().getConsumerQueue());
                System.out.println("消息成功消费到  messageId:"+messageId+"  messageData:"+messageData+"  createTime:"+createTime);
                System.out.println("执行fanout.A中的消息的业务处理流程......");
            }

            if ("delay-queue".equals(message.getMessageProperties().getConsumerQueue())){
                System.out.println("消费的消息来自的队列名为："+message.getMessageProperties().getConsumerQueue());
                System.out.println("消息成功消费到  messageId:"+messageId+"  messageData:"+messageData+"  createTime:"+createTime);
                System.out.println("执行【delay-queue】中的消息的业务处理流程......");
            }
            channel.basicAck(deliveryTag, true); //第二个参数，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
//			channel.basicReject(deliveryTag, true);//第二个参数，true会重新放回队列，所以需要自己根据业务逻辑判断什么时候使用拒绝
        } catch (Exception e) {
            channel.basicReject(deliveryTag, false);
            e.printStackTrace();
        }
    }

    // 去除字符串前后，指定的字符
    public String trimStringWith(String str, char beTrim) {
        int st = 0;
        int len = str.length();
        char[] val = str.toCharArray();
        char sbeTrim = beTrim;
        while ((st < len) && (val[st] <= sbeTrim)) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= sbeTrim)) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }


}
