package com.amplesky.provider.config;

import com.amplesky.common.constant.RabbitConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * direct exchange(直连型交换机)
 * 类似于点对点
 */
@Configuration
public class DirectRabbitConfig {

    /**
     *  durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
     *  exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
     *  autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
     *  return new Queue("TestDirectQueue",true,true,false);
     *
     *  一般设置一下队列的持久化就好,其余两个就是默认false
     */
    @Bean("TestDirectQueue")
    public Queue TestDirectQueue(){
        return new Queue(RabbitConstant.DIRECT_QUEUE,true);
    }

    //Direct交换机 起名：TestDirectExchange
    @Bean("testDirectExchange")
    public DirectExchange testDirectExchange() {
        DirectExchange directExchange = new DirectExchange(RabbitConstant.DIRECT_EXCHANGE_NAME, true, false);
        //延时队列
//        directExchange.setDelayed(true);
        return directExchange;
    }


    //绑定  将队列和交换机绑定, 并设置用于匹配键：TestDirectRouting
    @Bean
    public Binding bindingDirect(@Qualifier("TestDirectQueue") Queue testDirectQueue, @Qualifier("testDirectExchange") DirectExchange testDirectExchange) {
        return BindingBuilder.bind(testDirectQueue).to(testDirectExchange).with("TestDirectRouting");
    }


    /**
     * 消息推送到server，找到交换机了，但是没找到队列
     * 这种情况就是需要新增一个交换机，但是不给这个交换机绑定队列，
     *   我来简单地在DirectRabitConfig里面新增一个直连交换机，名叫‘lonelyDirectExchange’，
     *   但没给它做任何绑定配置操作：
     */
    @Bean("lonelyDirectExchange")
    public DirectExchange lonelyDirectExchange() {
        return new DirectExchange("lonelyDirectExchange");
    }

    // 修改RabbitMq默认序列化策略
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
