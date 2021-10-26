package com.lyle.grayman.plugin.eureka.interceptor;

import com.lyle.grayman.common.holder.DiscoveryClientHolder;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class DiscoveryClientInterceptor implements InstanceMethodInterceptor {
    EnvIdentityDiscoveryClientInterceptor envIdentityDiscoveryClientInterceptor = new EnvIdentityDiscoveryClientInterceptor();

    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        DiscoveryClientHolder.setTarget(obj);
        return envIdentityDiscoveryClientInterceptor.intercept(obj, allArguments, zuper, method);
    }
}
