package com.lyle.grayman.agent.template;

import com.lyle.grayman.agent.classloader.AgentClassLoader;
import com.lyle.grayman.agent.classloader.ClassLoaderUtils;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 拦截器包装器，当指定方法调用时获取指定类加载器加载拦截器，并调用拦截方法
 */
public class InstanceMethodInterceptorTemplate {
    private String className;
    private InstanceMethodInterceptor interceptor = null;

    public InstanceMethodInterceptorTemplate(String className) {
        this.className = className;
    }

    @RuntimeType
    public Object intercept(@This Object obj,
                            @AllArguments Object[] allArguments,
                            @SuperCall Callable<?> zuper,
                            @Origin Method method) throws Throwable {
        initInterceptor();

        if (interceptor != null) {
            return interceptor.intercept(obj, allArguments, zuper, method);
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
                interceptor = (InstanceMethodInterceptor) agentClassLoader.loadClass(this.className).newInstance();
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("initInterceptor error " + AgentLogger.getStackTraceString(throwable));
            }
        }
    }
}
