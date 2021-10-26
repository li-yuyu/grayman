package com.lyle.grayman.plugin.spring.async;

import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.OverrideArgsInstanceMethodInterceptor;
import com.lyle.grayman.common.threading.OverrideCallable;
import com.lyle.grayman.common.threading.WrapCallable;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class AsyncExecutionInterceptor implements OverrideArgsInstanceMethodInterceptor {

    @Override
    public Object intercept(Object obj, Object[] allArguments, OverrideCallable zuper, Method method) throws Throwable {
        if (allArguments != null && allArguments.length > 0) {
            Object arg = allArguments[0];
            if (arg != null && arg instanceof Callable) {
                String envIdentity = Context.getEnvIdentity();
                allArguments[0] = new WrapCallable((Callable) arg, envIdentity);
            }
        }
        return zuper.call(allArguments);
    }
}
