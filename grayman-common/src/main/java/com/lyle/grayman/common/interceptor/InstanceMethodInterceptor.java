package com.lyle.grayman.common.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public interface InstanceMethodInterceptor {
    Object intercept(Object obj,
                     Object[] allArguments,
                     Callable<?> zuper,
                     Method method) throws Throwable;
}
