package com.amplesky.common.constant;

public interface RabbitConstant {

    String DIRECT_QUEUE = "direct-queue";
    String DIRECT_EXCHANGE_NAME = "direct-exchange";

    String FANOUT_EXCHANGE_NAME = "fanout-exchange";    //rabbitmq交换机
    String FANOUT_QUEUE_A = "fanout.A";                 //队列
    String FANOUT_QUEUE_B = "fanout.B";
    String FANOUT_QUEUE_C = "fanout.C";


    String TOPIC_QUEUE_D = "topic.d";
    String TOPIC_QUEUE_E = "topic.e";
    String TOPIC_EXCHANGE_NAME = "topic-exchange";

}
