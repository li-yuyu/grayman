package com.lyle.grayman.common.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public interface StaticMethodInterceptor {
    Object intercept(Class<?> clazz,
                     Object[] allArguments,
                     Callable<?> zuper,
                     Method method) throws Throwable;
}
