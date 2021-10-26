package com.lyle.grayman.plugin.zuul;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.netflix.zuul.context.RequestContext;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ZuulPreRouteInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        String envIdentity = Context.getEnvIdentity();
        if (AgentStringUtils.isNotEmpty(envIdentity)) {
            RequestContext currentContext = RequestContext.getCurrentContext();
            if (currentContext != null) {
                currentContext.addZuulRequestHeader(Const.HTTP_HEADER_ENV_IDENTITY, envIdentity);
            }
        }

        return zuper.call();
    }
}
