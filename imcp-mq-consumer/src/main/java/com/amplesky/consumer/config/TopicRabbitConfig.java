package com.amplesky.consumer.config;

import com.amplesky.common.constant.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * topic 主题交换机
 */
@Configuration
public class TopicRabbitConfig {

    @Bean("topicD")
    public Queue topicD(){
        return new Queue(RabbitConstant.TOPIC_QUEUE_D);
    }

    @Bean("topicE")
    public Queue topicE(){
        return new Queue(RabbitConstant.TOPIC_QUEUE_E);
    }

    @Bean("topicExchange")
    public TopicExchange topicExchange(){
        return new TopicExchange(RabbitConstant.TOPIC_EXCHANGE_NAME);
    }

    //将topicD和topicExchange绑定,而且绑定的键值为topic.d
    //这样只要是消息携带的路由键是topic.d,才会分发到该队列
    @Bean
    public Binding bindingExchangeMessage(@Qualifier("topicD") Queue topicD, @Qualifier("topicExchange") TopicExchange topicExchange){
        return BindingBuilder.bind(topicD).to(topicExchange).with(RabbitConstant.TOPIC_QUEUE_D);
    }

    /**
     *  将topicE和topicExchange绑定,而且绑定的键值为用上通配路由键规则topic.#
     *  这样只要是消息携带的路由键是以topic.开头,都会分发到该队列
     */
    @Bean
    public Binding bindingExchangeMessage2(@Qualifier("topicE")Queue topicE,TopicExchange topicExchange){
        return BindingBuilder.bind(topicE).to(topicExchange).with("topic.#");
    }


}
