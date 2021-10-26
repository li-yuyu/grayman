package com.lyle.grayman.plugin.threading.interceptor;

import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.threading.EnhancedInstance;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ThreadingMethodInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            EnhancedInstance targetObject = (EnhancedInstance) obj;
            if (targetObject.getEnvIdentity() != null) {
                Context.setEnvIdentity(targetObject.getEnvIdentity().toString());
            }
            return zuper.call();
        } finally {
            Context.clean();
        }

    }
}
