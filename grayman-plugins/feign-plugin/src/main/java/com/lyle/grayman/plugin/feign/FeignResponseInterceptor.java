package com.lyle.grayman.plugin.feign;

import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class FeignResponseInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        Object call = zuper.call();
        return call;
    }

}
