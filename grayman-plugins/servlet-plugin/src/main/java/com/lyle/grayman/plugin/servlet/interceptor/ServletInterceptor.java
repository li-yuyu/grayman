package com.lyle.grayman.plugin.servlet.interceptor;

import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;


public class ServletInterceptor implements InstanceMethodInterceptor {
    private EnvIdentityServletInterceptor testTagServletInterceptor = new EnvIdentityServletInterceptor();

    public Object intercept(Object obj,
                            Object[] allArguments,
                            Callable<?> zuper,
                            Method method) throws Throwable {
        return testTagServletInterceptor.intercept(obj, allArguments, zuper, method);
    }
}
