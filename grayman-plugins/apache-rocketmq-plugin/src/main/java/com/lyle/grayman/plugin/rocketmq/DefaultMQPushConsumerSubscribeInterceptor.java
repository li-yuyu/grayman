package com.lyle.grayman.plugin.rocketmq;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.lyle.grayman.common.utils.SystemUtils;
import com.lyle.grayman.plugin.rocketmq.context.PushConsumerContextHolder;
import com.lyle.grayman.plugin.rocketmq.context.PushConsumerContext;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class DefaultMQPushConsumerSubscribeInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
        if (AgentStringUtils.isEmpty(envIdentity)) {
            AgentLogger.getLogger().info("test tag为空，不满足拦截条件");
            return zuper.call();
        }

        if (allArguments == null || allArguments.length == 0) {
            return zuper.call();
        }

        Object arg = allArguments[0];

        if (arg == null || !(arg instanceof String)) {
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
        Object target = targetField.get(zuper);
        targetField.setAccessible(false);

        DefaultMQPushConsumer consumer = (DefaultMQPushConsumer) target;
        String topicPrefix = envIdentity + "_";
        String topicOrigin = (String) arg;
        if (topicOrigin != null && topicOrigin.startsWith(topicPrefix)) {
            return zuper.call();
        }

        Object[] newArgs = Arrays.copyOf(allArguments, allArguments.length);
        String newTopic = topicPrefix + topicOrigin;
        newArgs[0] = newTopic;

        PushConsumerContext context = PushConsumerContextHolder.getPushConsumerContext(consumer.hashCode());
        if (context == null) {
            context = new PushConsumerContext();
            PushConsumerContextHolder.setPushConsumerContext(consumer.hashCode(), context);
        }

        context.setSubscribeArgs(newArgs);

        return zuper.call();
    }
}
