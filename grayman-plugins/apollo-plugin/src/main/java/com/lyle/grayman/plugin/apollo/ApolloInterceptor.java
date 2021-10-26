package com.lyle.grayman.plugin.apollo;

import com.ctrip.framework.apollo.spring.config.PropertySourcesConstants;
import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.SystemUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ApolloInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String apolloNamespaces = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_APOLLO_NAMESPACE);
        if (apolloNamespaces != null && apolloNamespaces.length() > 0) {
            if (allArguments != null && allArguments.length > 0) {
                Object arg = allArguments[0];

                if (arg instanceof ConfigurableApplicationContext) {
                    ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) arg;
                    ConfigurableEnvironment env = ctx.getEnvironment();
                    String ns = env.getProperty(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES);
                    
                    String nsEnvPrefix = apolloNamespaces + ",";
                    if (ns == null || ns.indexOf(nsEnvPrefix) < 0) {
                        String nsEnv = nsEnvPrefix + ns;
                        Map<String, Object> source = new HashMap<>();
                        source.put(PropertySourcesConstants.APOLLO_BOOTSTRAP_NAMESPACES, nsEnv);
                        PropertySource<?> ps = new MapPropertySource("grayman-agent", source);

                        MutablePropertySources propertySources = env.getPropertySources();
                        propertySources.addFirst(ps);
                    }
                }
            }
        }


        return zuper.call();

    }
}
