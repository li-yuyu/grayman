package com.lyle.grayman.plugin.feign;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import feign.RequestTemplate;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

public class FeignInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        System.out.println("call feign,grayman identity is :" + Context.getEnvIdentity());
        if (allArguments != null && allArguments.length > 0) {
            Object arg = allArguments[0];
            if (arg instanceof RequestTemplate) {
                RequestTemplate requestTemplate = (RequestTemplate) arg;

                Map<String, Collection<String>> headers = requestTemplate.headers();
                if (!headers.containsKey(Const.HTTP_HEADER_ENV_IDENTITY)) {
                    String envIdentity = Context.getEnvIdentity();
                    if (AgentStringUtils.isNotEmpty(envIdentity)) {
                        requestTemplate.header(Const.HTTP_HEADER_ENV_IDENTITY, envIdentity);
                    }
                }
            }
        }

        return zuper.call();
    }
}
