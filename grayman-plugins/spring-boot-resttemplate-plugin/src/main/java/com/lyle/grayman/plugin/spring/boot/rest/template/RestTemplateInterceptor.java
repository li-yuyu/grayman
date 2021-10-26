package com.lyle.grayman.plugin.spring.boot.rest.template;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import org.springframework.http.HttpHeaders;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class RestTemplateInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        if (allArguments != null && allArguments.length > 0) {
            Object arg = allArguments[0];
            if (arg != null && arg instanceof HttpHeaders) {
                HttpHeaders httpHeaders = (HttpHeaders) arg;
                if (!httpHeaders.containsKey(Const.HTTP_HEADER_ENV_IDENTITY)) {
                    String envIdentity = Context.getEnvIdentity();
                    if (AgentStringUtils.isNotEmpty(envIdentity)) {
                        httpHeaders.add(Const.HTTP_HEADER_ENV_IDENTITY, envIdentity);
                    }
                }
            }
        }
        Object call = zuper.call();
        return call;
    }

}
