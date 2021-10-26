package com.lyle.grayman.agent.template;

import com.lyle.grayman.agent.classloader.AgentClassLoader;
import com.lyle.grayman.agent.classloader.ClassLoaderUtils;
import com.lyle.grayman.common.interceptor.ConstructorInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

public class ConstructorInterceptorTemplate {
    private String className;
    ConstructorInterceptor constructorInterceptor;

    public ConstructorInterceptorTemplate(String className) {
        this.className = className;
    }

    @RuntimeType
    public void intercept(@This Object obj, @AllArguments Object[] allArguments) {
        initInterceptor();
        if (constructorInterceptor != null) {
            try {
                constructorInterceptor.intercept(obj, allArguments);
            } catch (Exception ex) {
            }
        }
    }

    private void initInterceptor() {
        if (constructorInterceptor == null) {
            try {
                AgentClassLoader agentClassLoader = ClassLoaderUtils.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                constructorInterceptor = (ConstructorInterceptor) agentClassLoader.loadClass(this.className).newInstance();
            } catch (Throwable throwable) {
                AgentLogger.getLogger().severe("initInterceptor error " + AgentLogger.getStackTraceString(throwable));
            }
        }
    }
}
