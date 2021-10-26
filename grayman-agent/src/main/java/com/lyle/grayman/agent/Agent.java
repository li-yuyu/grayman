package com.lyle.grayman.agent;

import com.lyle.grayman.agent.classloader.AgentClassLoader;
import com.lyle.grayman.agent.classloader.ClassLoaderUtils;
import com.lyle.grayman.agent.config.ConfigProperties;
import com.lyle.grayman.agent.config.InterceptConfig;
import com.lyle.grayman.agent.template.ConstructorInterceptorTemplate;
import com.lyle.grayman.agent.template.InstanceMethodInterceptorTemplate;
import com.lyle.grayman.agent.template.OverrideArgsInstanceMethodInterceptorTemplate;
import com.lyle.grayman.agent.template.StaticMethodInterceptorTemplate;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.interceptor.OverrideArgsInstanceMethodInterceptor;
import com.lyle.grayman.common.interceptor.StaticMethodInterceptor;
import com.lyle.grayman.common.threading.EnhancedInstance;
import com.lyle.grayman.common.threading.OverrideCallable;
import com.lyle.grayman.common.utils.AgentLogger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import static net.bytebuddy.jar.asm.Opcodes.ACC_PRIVATE;
import static net.bytebuddy.jar.asm.Opcodes.ACC_VOLATILE;

/**
 * javaagent核心类，通过ByteBuddy向Instrument添加了一系列Transformer，以此达到修改和替换类的定义的目的
 */
public class Agent {
    public static final String CONTEXT_ATTR_NAME = "_$EnhancedClassField_grayman";

    public static void premain(String agentArgs, Instrumentation inst) {
        loadConfig();
        try {
            appendToBootstrapClassLoader(inst);
            AgentBuilder agentBuilder = new AgentBuilder.Default();
            //拦截runnable, callable 相关类
            agentBuilder = enhanceThreadingClass("java.lang.Runnable", agentBuilder);
            agentBuilder = enhanceThreadingClass("java.util.concurrent.Callable", agentBuilder);

            for (Map.Entry<String, List<InterceptConfig>> configMap : InterceptConfig.getConfigMap().entrySet()) {
                String className = configMap.getKey();
                List<InterceptConfig> configs = configMap.getValue();

                agentBuilder = agentBuilder.type(ElementMatchers.named(className)).transform((builder, typeDescription, classLoader, module) -> {
                    for (InterceptConfig config : configs) {
                        try {
                            AgentClassLoader agentClassLoader = ClassLoaderUtils.getAgentClassLoader(Thread.currentThread().getContextClassLoader());
                            Class<?> aClass = Class.forName(config.getInterceptorClass(), false, agentClassLoader);
                            String interfaceName = aClass.getInterfaces()[0].getName();
                            if (InstanceMethodInterceptor.class.getName().equals(interfaceName)) {
                                builder = builder.method(ElementMatchers.named(config.getMethodName()))
                                        .intercept(MethodDelegation.to(new InstanceMethodInterceptorTemplate(config.getInterceptorClass())));
                            } else if (OverrideArgsInstanceMethodInterceptor.class.getName().equals(interfaceName)) {
                                builder = builder.method(ElementMatchers.named(config.getMethodName()))
                                        .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideCallable.class)).to(new OverrideArgsInstanceMethodInterceptorTemplate(config.getInterceptorClass())));
                            } else if (StaticMethodInterceptor.class.getName().equals(interfaceName)) {
                                builder = builder.method(ElementMatchers.named(config.getMethodName()))
                                        .intercept(MethodDelegation.to(new StaticMethodInterceptorTemplate(config.getInterceptorClass())));
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    return builder;
                });
            }
            agentBuilder.installOn(inst);
        } catch (Throwable throwable) {
            AgentLogger.getLogger().severe("install agent exception: " + AgentLogger.getStackTraceString(throwable));
        } finally {
            AgentLogger.getLogger().info("grayman-agent install on finish.");
        }
    }

    private static AgentBuilder enhanceThreadingClass(String className, AgentBuilder agentBuilder) {
        agentBuilder = agentBuilder.type(ElementMatchers.hasSuperType(ElementMatchers.named(className))).transform((builder, typeDescription, classLoader, module) -> {
            builder = builder.defineField(CONTEXT_ATTR_NAME, Object.class, ACC_PRIVATE | ACC_VOLATILE)
                    .implement(EnhancedInstance.class)
                    .intercept(FieldAccessor.ofField(CONTEXT_ATTR_NAME));
            builder = builder.constructor(ElementMatchers.any()).
                    intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructorInterceptorTemplate("com.lyle.grayman.plugin.threading.interceptor.ThreadingConstructorInterceptor"))));

            builder = builder.method(ElementMatchers.named("run"))
                    .intercept(MethodDelegation.to(new InstanceMethodInterceptorTemplate("com.lyle.grayman.plugin.threading.interceptor.ThreadingMethodInterceptor")));

            return builder;
        });
        return agentBuilder;
    }

    private static void loadConfig() {
        List<InterceptConfig> list = ConfigProperties.getInterceptConfig();
        if (list != null && list.size() > 0) {
            list.stream().forEach(item -> InterceptConfig.getConfigs().add(item));
        }
    }

    private static void appendToBootstrapClassLoader(Instrumentation instrumentation) {
        try {
            String libPath = Agent.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("grayman-agent.jar", "") + "/lib/";
            JarFile jarFile = new JarFile(libPath + "grayman-common.jar");
            instrumentation.appendToSystemClassLoaderSearch(jarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
