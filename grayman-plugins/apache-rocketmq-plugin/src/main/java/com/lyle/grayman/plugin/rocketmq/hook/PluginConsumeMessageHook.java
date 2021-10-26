package com.lyle.grayman.plugin.rocketmq.hook;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.lyle.grayman.common.utils.SystemUtils;
import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Map;

@Deprecated
public class PluginConsumeMessageHook implements ConsumeMessageHook {

    @Override
    public String hookName() {
        return PluginConsumeMessageHook.class.getSimpleName();
    }

    /**
     * 当消息的数量只有一条的时候，设置当前线程的TraceContext
     * 这个操作的意义不大
     * 本质上是想针对单条msg，设置TraceContext。由于是批量消费，无法在字节码增强阶段，确认究竟是哪条msg在消费
     * 解决方案：
     * 1、业务代码侵入，最不友好
     * 2、sdk支持，类似于maihaoche的jar，可以知道单条msg消费的起点和终点。在起点设置TraceContext，在终点移除TraceContext
     *
     * @param context
     */
    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        Context.clean();

        String envIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
        if (AgentStringUtils.isEmpty(envIdentity)) {
            return;
        }

        if (context == null || context.getMsgList() == null || context.getMsgList().size() != 1) {
            return;
        }

        MessageExt messageExt = context.getMsgList().get(0);
        String topic = messageExt.getTopic();
        String topicPrefix = envIdentity + "_";

        if (!topic.startsWith(topicPrefix) &&
                !topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX + topicPrefix) &&
                !topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX + topicPrefix)) {
            return;
        }

        Map<String, String> properties = messageExt.getProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equals(Const.HTTP_HEADER_ENV_IDENTITY)) {
                Context.setEnvIdentity(entry.getValue());
            }
        }
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        Context.clean();
    }
}
