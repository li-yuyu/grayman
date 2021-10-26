package com.lyle.grayman.plugin.apache.http.client;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ApacheHttpClientInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        HttpRequest httpRequest = null;

        Object arg = null;
        if (allArguments != null && allArguments.length > 0) {
            arg = allArguments[0];
            if (arg != null && arg instanceof HttpRequest) {
                httpRequest = (HttpRequest) arg;
            } else if (allArguments.length >= 2) {
                arg = allArguments[1];
                if (arg != null && arg instanceof HttpRequest) {
                    httpRequest = (HttpRequest) arg;
                }
            }
        }

        if (httpRequest != null) {
            Header[] headers = httpRequest.getHeaders(Const.HTTP_HEADER_ENV_IDENTITY);
            if (headers == null || headers.length == 0) {
                String graymanIdentity = Context.getEnvIdentity();
                if (AgentStringUtils.isNotEmpty(graymanIdentity)) {
                    httpRequest.setHeader(Const.HTTP_HEADER_ENV_IDENTITY, graymanIdentity);
                }
            }
        }

        return zuper.call();
    }

}
