package com.lyle.grayman.plugin.rocketmq;

import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.plugin.rocketmq.hook.PluginSendMessageHook;
import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class DefaultMQProducerStartInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        Field targetField = null;
        Field[] fields = zuper.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(DefaultMQProducer.class)) {
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

        if (target instanceof DefaultMQProducer) {
            DefaultMQProducer producer = (DefaultMQProducer) target;
            DefaultMQProducerImpl defaultMQProducerImpl = producer.getDefaultMQProducerImpl();
            if (defaultMQProducerImpl != null) {
                defaultMQProducerImpl.registerSendMessageHook(new PluginSendMessageHook());
                AgentLogger.getLogger().info("向生产者注册PluginSendMessageHook");
            }
        }

        return zuper.call();
    }
}
