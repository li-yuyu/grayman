package com.lyle.grayman.agent.template;

import com.lyle.grayman.agent.classloader.AgentClassLoader;
import com.lyle.grayman.agent.classloader.ClassLoaderUtils;
import com.lyle.grayman.common.interceptor.OverrideArgsInstanceMethodInterceptor;
import com.lyle.grayman.common.threading.OverrideCallable;
import com.lyle.grayman.common.utils.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

public class OverrideArgsInstanceMethodInterceptorTemplate {
    private String className;
    private OverrideArgsInstanceMethodInterceptor interceptor = null;

    public OverrideArgsInstanceMethodInterceptorTemplate(String className) {
        this.className = className;
    }

    @RuntimeType
    public Object intercept(@This Object obj,
                            @AllArguments Object[] allArguments,
                            @Morph OverrideCallable zuper,
                            @Origin Method method) throws Throwable {
        initInterceptor();

        if (interceptor != null) {
            return interceptor.intercept(obj, allArguments, zuper, method);
        } else {
            return zuper.call(allArguments);
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
                interceptor = (OverrideArgsInstanceMethodInterceptor) agentClassLoader.loadClass(this.className).newInstance();
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("initInterceptor error " + AgentLogger.getStackTraceString(throwable));
            }
        }
    }
}
