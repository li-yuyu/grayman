package com.lyle.grayman.agent.template;

import com.lyle.grayman.agent.classloader.AgentClassLoader;
import com.lyle.grayman.agent.classloader.ClassLoaderUtils;
import com.lyle.grayman.common.interceptor.StaticMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class StaticMethodInterceptorTemplate {
    private String className;
    private StaticMethodInterceptor interceptor = null;

    public StaticMethodInterceptorTemplate(String className) {
        this.className = className;
    }

    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @AllArguments Object[] allArguments, @Origin Method method,
                            @SuperCall Callable<?> zuper) throws Throwable {
        initInterceptor();

        if (interceptor != null) {
            return interceptor.intercept(clazz, allArguments, zuper, method);
        } else {
            return zuper.call();
        }
    }

    private void initInterceptor() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (interceptor == null) {
            try {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader == null) {
                    contextClassLoader = ClassLoader.getSystemClassLoader();
                }
                AgentClassLoader agentClassLoader = ClassLoaderUtils.getAgentClassLoader(contextClassLoader);
                interceptor = (StaticMethodInterceptor) agentClassLoader.loadClass(this.className).newInstance();
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("initInterceptor error " + AgentLogger.getStackTraceString(throwable));
            }
        }
    }
}
