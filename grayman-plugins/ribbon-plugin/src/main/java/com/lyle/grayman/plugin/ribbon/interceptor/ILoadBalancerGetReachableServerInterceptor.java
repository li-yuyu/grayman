package com.lyle.grayman.plugin.ribbon.interceptor;

import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ILoadBalancerGetReachableServerInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        return null;
    }
}
