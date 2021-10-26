package com.lyle.grayman.plugin.rocketmq.listener;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.holder.DiscoveryClientHolder;
import com.lyle.grayman.common.utils.ReflectionUtils;
import com.lyle.grayman.common.utils.SystemUtils;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.Application;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class MessageListenerWrapper implements MessageListener {
    protected String groupPrefix;

    public MessageListenerWrapper() {
        groupPrefix = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY) + "_";
    }

    protected boolean hasTag(MessageExt messageExt) {
        return messageExt.getUserProperty(Const.HTTP_HEADER_ENV_IDENTITY) != null &&
                messageExt.getUserProperty(Const.HTTP_HEADER_ENV_IDENTITY).length() > 0;
    }

    protected boolean tagEquals(MessageExt messageExt) {
        String graymanIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
        String userProperty = messageExt.getUserProperty(Const.HTTP_HEADER_ENV_IDENTITY);
        boolean equals = Objects.equals(graymanIdentity, userProperty);

        // 如果已经匹配了，直接返回
        if (equals) {
            return equals;
        }

        // 如果不是default节点，直接返回
        if (null != graymanIdentity && !"".equals(graymanIdentity)) {
            return equals;
        }

        // 1、在default节点（已满足）
        // 2、没有其他节点匹配env_identity（下面的代码需要去实现的）
        // 满足以上两点，返回true，否则换回false
        try {
            Class<?> clazz = Class.forName("com.netflix.discovery.DiscoveryClient");
        } catch (ClassNotFoundException e) {
            return equals;
        }

        boolean hasInstance = false;
        Object target = DiscoveryClientHolder.getTarget();
        if (target == null) {
            return equals;
        }

        try {
            // 理论上不会发生
            if (!(target instanceof DiscoveryClient)) {
                return equals;
            }

            DiscoveryClient discoveryClient = (DiscoveryClient) target;
            InstanceInfo selfInstanceInfo = ReflectionUtils.getFieldValue(discoveryClient, "instanceInfo",
                    InstanceInfo.class);
            if (selfInstanceInfo == null) {
                return equals;
            }

            Application application = discoveryClient.getApplication(selfInstanceInfo.getAppName());
            if (application == null) {
                return equals;
            }

            List<InstanceInfo> instanceInfos = application.getInstances();
            if (instanceInfos != null && instanceInfos.size() > 0) {
                for (InstanceInfo instanceInfo : instanceInfos) {
                    Map<String, String> metadata = instanceInfo.getMetadata();
                    if (metadata == null || metadata.size() == 0) {
                        continue;
                    }

                    if (Objects.equals(metadata.get(Const.EUREKA_META_ENV_IDENTITY_KEY), userProperty)) {
                        hasInstance = true;
                        break;
                    }

                }
            }
        } catch (Exception e) {
            return equals;
        }

        return !hasInstance;
    }

    protected void consumeMessageBefore(MessageExt messageExt) {
        Context.clean();

        if (hasTag(messageExt)) {
            Context.setEnvIdentity(messageExt.getUserProperty(Const.HTTP_HEADER_ENV_IDENTITY));
        }

    }

    protected void consumeMessageAfter() {
        Context.clean();
    }
}
