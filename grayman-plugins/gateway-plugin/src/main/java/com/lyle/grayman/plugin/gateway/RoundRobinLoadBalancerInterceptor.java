package com.lyle.grayman.plugin.gateway;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import org.springframework.cloud.client.ServiceInstance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class RoundRobinLoadBalancerInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        if (allArguments == null || allArguments.length != 1) {
            return zuper.call();
        }

        Object arg = allArguments[0];
        if (arg == null || !(arg instanceof List)) {
            return zuper.call();
        }

        List<ServiceInstance> instances = (List<ServiceInstance>) arg;
        if (instances.size() == 0) {
            return zuper.call();
        }

        List<ServiceInstance> defaultInstances = new ArrayList<>();
        String envIdentity = Context.getEnvIdentity();
        Iterator<ServiceInstance> iter = instances.iterator();

        while (iter.hasNext()) {
            ServiceInstance instance = iter.next();
            Map<String, String> metadata = instance.getMetadata();
            String instanceEnvIdentity = metadata.get(Const.EUREKA_META_ENV_IDENTITY_KEY);

            if (AgentStringUtils.isEmpty(instanceEnvIdentity)) {
                defaultInstances.add(instance);
            }

            // 服务分组不配，移除，只保留匹配的服务实例
            if (!Objects.equals(envIdentity, instanceEnvIdentity)) {
                iter.remove();
            }
        }

        // 服务实例列表为空，没有匹配的服务实例，使用默认服务实例
        if (instances.size() == 0) {
            instances.addAll(defaultInstances);
        }

        return zuper.call();
    }
}
