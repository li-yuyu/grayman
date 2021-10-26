package com.lyle.grayman.plugin.rocketmq;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.lyle.grayman.common.utils.SystemUtils;
import com.lyle.grayman.plugin.rocketmq.context.PushConsumerHolder;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class DefaultMQPushConsumerUnsubscribeInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
        if (AgentStringUtils.isEmpty(envIdentity)) {
            return zuper.call();
        }

        if (allArguments == null || allArguments.length == 0) {
            return zuper.call();
        }

        Object arg = allArguments[0];
        if (arg == null || !(arg instanceof String)) {
            return zuper.call();
        }

        String topicPrefix = envIdentity + "_";
        String topic = topicPrefix + arg;


        Field targetField = null;
        Field[] fields = zuper.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(DefaultMQPushConsumer.class)) {
                targetField = field;
                break;
            }
        }

        if (targetField == null) {
            return zuper.call();
        }

        targetField.setAccessible(true);
        Object target = targetField.get(zuper);
        targetField.setAccessible(false);

        DefaultMQPushConsumer consumer = (DefaultMQPushConsumer) target;
        String consumerGroup = consumer.getConsumerGroup();
        if (consumerGroup != null && consumerGroup.startsWith(topicPrefix)) {
            return zuper.call();
        }

        DefaultMQPushConsumer pushConsumer = PushConsumerHolder.getPushConsumer(consumer.hashCode());
        if (pushConsumer != null) {
            pushConsumer.unsubscribe(topic);
            AgentLogger.getLogger().info("取消订阅" + topic);
        }

        return zuper.call();
    }
}
