package com.lyle.grayman.plugin.ribbon.interceptor;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ILoadBalancerGetServerInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = Context.getEnvIdentity();
        if (AgentStringUtils.isEmpty(envIdentity)) {
            envIdentity = "";
        }
        List<Server> results = (List<Server>) zuper.call();

        Iterator<Server> iterator = results.iterator();
        while (iterator.hasNext()) {
            Server server = iterator.next();

            if (server instanceof DiscoveryEnabledServer) {
                Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();
                String serverEnvIdentity = metadata.get(Const.EUREKA_META_ENV_IDENTITY_KEY);
                if (AgentStringUtils.isEmpty(serverEnvIdentity)) {
                    serverEnvIdentity = "";
                }

                if (!envIdentity.equals(serverEnvIdentity)) {
                    iterator.remove();
                }
            }
        }

        return results;
    }
}
