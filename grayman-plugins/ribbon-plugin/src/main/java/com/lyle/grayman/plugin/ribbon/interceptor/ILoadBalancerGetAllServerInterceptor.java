package com.lyle.grayman.plugin.ribbon.interceptor;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ILoadBalancerGetAllServerInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = Context.getEnvIdentity();

        List<Server> results = (List<Server>) zuper.call();
        List<Server> matchedServers = new ArrayList<>();
        List<Server> defaultServers = new ArrayList<>();
        for (Server server : results) {
            if (server instanceof DiscoveryEnabledServer) {
                Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();
                String serverEnvIdentity = metadata.get(Const.EUREKA_META_ENV_IDENTITY_KEY);
                if (AgentStringUtils.isEmpty(serverEnvIdentity)) {
                    defaultServers.add(server);
                } else if (serverEnvIdentity.equals(envIdentity)) {
                    matchedServers.add(server);
                }
            }else{
                defaultServers.add(server);
            }
        }
        if(matchedServers.size() >0){
            return Collections.unmodifiableList(matchedServers);
        }else{
            return Collections.unmodifiableList(defaultServers);
        }
    }
}
