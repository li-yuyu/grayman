package com.lyle.grayman.plugin.rocketmq.context;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PushConsumerHolder {
    private static final Map<Integer, DefaultMQPushConsumer> PUSH_CONSUMER_MAP;

    static {
        PUSH_CONSUMER_MAP = new ConcurrentHashMap<>();
    }

    public static DefaultMQPushConsumer getPushConsumer(int hashcode) {
        return PUSH_CONSUMER_MAP.get(hashcode);
    }

    public static void setPushConsumer(int hashcode, DefaultMQPushConsumer consumer) {
        PUSH_CONSUMER_MAP.putIfAbsent(hashcode, consumer);
    }

    public static DefaultMQPushConsumer removePushConsumer(int hashcode) {
        return PUSH_CONSUMER_MAP.remove(hashcode);
    }
}