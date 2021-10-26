package com.lyle.grayman.plugin.rocketmq.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PushConsumerContextHolder {
    // 存在并发操作的可能性
    private static final Map<Integer, PushConsumerContext> PUSH_CONSUMER_CONTEXT_HOLDER;

    static {
        PUSH_CONSUMER_CONTEXT_HOLDER = new ConcurrentHashMap<>();
    }

    public static PushConsumerContext getPushConsumerContext(int hashcode) {
        return PUSH_CONSUMER_CONTEXT_HOLDER.get(hashcode);
    }

    public static void setPushConsumerContext(int hashcode, PushConsumerContext context) {
        PUSH_CONSUMER_CONTEXT_HOLDER.putIfAbsent(hashcode, context);
    }

    public static PushConsumerContext removePushConsumerContext(int hashcode) {
        return PUSH_CONSUMER_CONTEXT_HOLDER.remove(hashcode);
    }
}
