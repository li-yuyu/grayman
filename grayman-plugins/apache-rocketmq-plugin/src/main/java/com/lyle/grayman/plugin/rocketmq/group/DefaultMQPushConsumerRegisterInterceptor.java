package com.lyle.grayman.plugin.rocketmq.group;

import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.plugin.rocketmq.listener.MessageListenerConcurrentlyWrapper;
import com.lyle.grayman.plugin.rocketmq.listener.MessageListenerOrderlyWrapper;
import com.lyle.grayman.plugin.rocketmq.listener.MessageListenerWrapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class DefaultMQPushConsumerRegisterInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        /*
        *
        String envIdentity = SystemUtils.getPropetyOrEnv(Const.ENV_IDENTITY);
        if (AgentStringUtils.isEmpty(envIdentity)) {
            AgentLogger.getLogger().info("test tag为空，不满足拦截条件");
            return zuper.call();
        }*/

        if (allArguments == null || allArguments.length == 0) {
            return zuper.call();
        }

        // 在改变原始consumer的MessageListener时，返回
        Object arg = allArguments[0];
        if (arg instanceof MessageListenerWrapper) {
            return zuper.call();
        }

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
        Object targetConsumer = targetField.get(zuper);
        targetField.setAccessible(false);

        DefaultMQPushConsumer consumer = (DefaultMQPushConsumer) targetConsumer;

        // 将原始MessageListener改掉
        if (arg instanceof MessageListenerConcurrently) {
            MessageListenerConcurrently target = (MessageListenerConcurrently) arg;
            MessageListenerConcurrentlyWrapper listener =
                    new MessageListenerConcurrentlyWrapper(target);
            consumer.registerMessageListener(listener);
            AgentLogger.getLogger().info("MessageListener被修改为MessageListenerConcurrentlyWrapper");
            return null;
        } else if (arg instanceof MessageListenerOrderly) {
            MessageListenerOrderly target = (MessageListenerOrderly) arg;
            MessageListenerOrderlyWrapper listener =
                    new MessageListenerOrderlyWrapper(target);
            consumer.registerMessageListener(listener);
            AgentLogger.getLogger().info("MessageListener被修改为MessageListenerOrderlyWrapper");
            return null;
        }

        return zuper.call();
    }
}
