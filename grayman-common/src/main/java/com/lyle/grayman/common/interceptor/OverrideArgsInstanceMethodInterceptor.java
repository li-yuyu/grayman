package com.lyle.grayman.common.interceptor;

import com.lyle.grayman.common.threading.OverrideCallable;

import java.lang.reflect.Method;

public interface OverrideArgsInstanceMethodInterceptor {
    Object intercept(Object obj,
                     Object[] allArguments,
                     OverrideCallable zuper,
                     Method method) throws Throwable;
}
