package com.lyle.grayman.plugin.rocketmq.hook;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.common.utils.AgentStringUtils;
import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.common.message.Message;

public class PluginSendMessageHook implements SendMessageHook {
    @Override
    public String hookName() {
        return PluginSendMessageHook.class.getSimpleName();
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        if (context == null) {
            return;
        }

        Message msg = context.getMessage();
        if (msg == null) {
            return;
        }

        String envTag = Context.getEnvIdentity();
        if (AgentStringUtils.isNotEmpty(envTag)) {
            AgentLogger.getLogger().info("向message中记录UserProperty:[" + Const.HTTP_HEADER_ENV_IDENTITY + "=" + envTag + "]");
            msg.putUserProperty(Const.HTTP_HEADER_ENV_IDENTITY, envTag);
        }
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {

    }
}
