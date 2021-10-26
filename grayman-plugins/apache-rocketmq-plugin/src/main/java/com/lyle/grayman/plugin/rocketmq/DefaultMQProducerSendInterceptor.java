package com.lyle.grayman.plugin.rocketmq;

import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.common.utils.AgentStringUtils;
import org.apache.rocketmq.common.message.Message;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class DefaultMQProducerSendInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = Context.getEnvIdentity();
        if (AgentStringUtils.isEmpty(envIdentity)) {
            return zuper.call();
        }

        if (allArguments == null || allArguments.length == 0) {
            return zuper.call();
        }

        Object arg = allArguments[0];

        if (arg == null) {
            return zuper.call();
        }

        if (arg instanceof Message) {
            Message msg = (Message) arg;
            changeTopic(msg);
            return zuper.call();
        }

        if (arg instanceof Collection) {
            Collection colle = (Collection) arg;
            Iterator iterator = colle.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof Message) {
                    Message msg = (Message) next;
                    changeTopic(msg);
                }
            }
        }

        return zuper.call();
    }

    private void changeTopic(Message msg) {
        String topicOrigin = msg.getTopic();
        String graymanIdentity = Context.getEnvIdentity();
        if (AgentStringUtils.isEmpty(graymanIdentity)) {
            return;
        }

        String topicPrefix = graymanIdentity + "_";
        if (topicOrigin == null || topicOrigin.startsWith(topicPrefix)) {
            return;
        }

        String newTopic = topicPrefix + topicOrigin;
        AgentLogger.getLogger().info("改变topic名称：[" + topicOrigin + "]->[" + newTopic + "]");
        msg.setTopic(newTopic);
    }
}
