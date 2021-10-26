package com.lyle.grayman.plugin.rocketmq;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.lyle.grayman.common.utils.SystemUtils;
import com.lyle.grayman.plugin.rocketmq.context.PushConsumerContext;
import com.lyle.grayman.plugin.rocketmq.context.PushConsumerContextHolder;
import com.lyle.grayman.plugin.rocketmq.context.PushConsumerHolder;
import com.lyle.grayman.plugin.rocketmq.listener.MessageListenerConcurrentlyWrapper;
import com.lyle.grayman.plugin.rocketmq.listener.MessageListenerOrderlyWrapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class DefaultMQPushConsumerStartInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
        if (AgentStringUtils.isEmpty(envIdentity)) {
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
        String consumerGroup = consumer.getConsumerGroup();
        if (consumerGroup != null && consumerGroup.startsWith(topicPrefix)) {
            return zuper.call();
        }

        PushConsumerContext context = PushConsumerContextHolder.getPushConsumerContext(consumer.hashCode());
        if (context == null || context.getRegisterArgs() == null || context.getSubscribeArgs() == null) {
            return zuper.call();
        }

        /*DefaultMQPushConsumerImpl defaultMQPushConsumerImpl = consumer.getDefaultMQPushConsumerImpl();
        if (defaultMQPushConsumerImpl == null) {
            return zuper.call();
        }
        PluginConsumeMessageHook consumeMessageHook = new PluginConsumeMessageHook();
        defaultMQPushConsumerImpl.registerConsumeMessageHook(consumeMessageHook);*/

        try {
            startStressTestingConsumer(consumer, context);
        } finally {
            PushConsumerContextHolder.removePushConsumerContext(consumer.hashCode());
        }

        return zuper.call();
    }

    private void startStressTestingConsumer(DefaultMQPushConsumer consumer, PushConsumerContext context) {
        String envIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
        if (AgentStringUtils.isEmpty(envIdentity)) {
            return;
        }

        String topicPrefix = envIdentity + "_";
        String group = consumer.getConsumerGroup();
        String stGroup = topicPrefix + group;

        DefaultMQPushConsumer stConsumer = new DefaultMQPushConsumer(stGroup);
        stConsumer.setNamesrvAddr(consumer.getNamesrvAddr());
        stConsumer.setConsumeFromWhere(consumer.getConsumeFromWhere());
        stConsumer.setAdjustThreadPoolNumsThreshold(consumer.getAdjustThreadPoolNumsThreshold());
        stConsumer.setVipChannelEnabled(consumer.isVipChannelEnabled());
        stConsumer.setUseTLS(consumer.isUseTLS());
        stConsumer.setUnitName(consumer.getUnitName());
        stConsumer.setPollNameServerInterval(consumer.getPollNameServerInterval());
        stConsumer.setPersistConsumerOffsetInterval(consumer.getPersistConsumerOffsetInterval());
        stConsumer.setLanguage(consumer.getLanguage());
        stConsumer.setInstanceName(consumer.getInstanceName());
        stConsumer.setHeartbeatBrokerInterval(consumer.getHeartbeatBrokerInterval());
        stConsumer.setClientIP(consumer.getClientIP());
        stConsumer.setClientCallbackExecutorThreads(consumer.getClientCallbackExecutorThreads());
        stConsumer.setUnitMode(consumer.isUnitMode());
        stConsumer.setSuspendCurrentQueueTimeMillis(consumer.getSuspendCurrentQueueTimeMillis());
        stConsumer.setSubscription(consumer.getSubscription());
        stConsumer.setPullThresholdSizeForTopic(consumer.getPullThresholdSizeForTopic());
        stConsumer.setPullThresholdSizeForQueue(consumer.getPullThresholdSizeForQueue());
        stConsumer.setPullThresholdForTopic(consumer.getPullThresholdForTopic());
        stConsumer.setPullThresholdForQueue(consumer.getPullThresholdForQueue());
        stConsumer.setPullInterval(consumer.getPullInterval());
        stConsumer.setPullBatchSize(consumer.getPullBatchSize());
        stConsumer.setPostSubscriptionWhenPull(consumer.isPostSubscriptionWhenPull());
        stConsumer.setOffsetStore(consumer.getOffsetStore());
        stConsumer.setMessageModel(consumer.getMessageModel());
        stConsumer.setMaxReconsumeTimes(consumer.getMaxReconsumeTimes());
        stConsumer.setConsumeTimestamp(consumer.getConsumeTimestamp());
        stConsumer.setConsumeTimeout(consumer.getConsumeTimeout());
        stConsumer.setConsumeThreadMin(consumer.getConsumeThreadMin());
        stConsumer.setConsumeThreadMax(consumer.getConsumeThreadMax());
        stConsumer.setConsumeMessageBatchMaxSize(consumer.getConsumeMessageBatchMaxSize());
        stConsumer.setConsumeConcurrentlyMaxSpan(consumer.getConsumeMessageBatchMaxSize());
        stConsumer.setAllocateMessageQueueStrategy(consumer.getAllocateMessageQueueStrategy());

        Object registerArg = context.getRegisterArgs()[0];
        if (registerArg instanceof MessageListenerConcurrently) {
            MessageListenerConcurrently target = (MessageListenerConcurrently) registerArg;
            MessageListenerConcurrentlyWrapper listener =
                    new MessageListenerConcurrentlyWrapper(target);
            stConsumer.registerMessageListener(listener);
        } else if (registerArg instanceof MessageListenerOrderly) {
            MessageListenerOrderly target = (MessageListenerOrderly) registerArg;
            MessageListenerOrderlyWrapper listener =
                    new MessageListenerOrderlyWrapper(target);
            stConsumer.registerMessageListener(listener);
        } else {
            stConsumer.registerMessageListener((MessageListener) registerArg);
        }

        try {
            Object[] subscribeArgs = context.getSubscribeArgs();
            if (subscribeArgs.length == 3) {
                stConsumer.subscribe((String) subscribeArgs[0], (String) subscribeArgs[1], (String) subscribeArgs[2]);
            } else if (subscribeArgs[1] instanceof String) {
                stConsumer.subscribe((String) subscribeArgs[0], (String) subscribeArgs[1]);
            } else {
                stConsumer.subscribe((String) subscribeArgs[0], (MessageSelector) subscribeArgs[1]);
            }
        } catch (Exception e) {
            AgentLogger.getLogger().warning(AgentLogger.getStackTraceString(e));
            return;
        }

        /*DefaultMQPushConsumerImpl defaultMQPushConsumerImpl = stConsumer.getDefaultMQPushConsumerImpl();
        if (defaultMQPushConsumerImpl != null) {
            defaultMQPushConsumerImpl.registerConsumeMessageHook(consumeMessageHook);
        }*/

        try {
            stConsumer.start();
            AgentLogger.getLogger().info("启动消费者消费topic:" + context.getSubscribeArgs()[0]);
        } catch (MQClientException e) {
            AgentLogger.getLogger().warning(AgentLogger.getStackTraceString(e));
            return;
        }

        PushConsumerHolder.setPushConsumer(consumer.hashCode(), stConsumer);
    }
}
