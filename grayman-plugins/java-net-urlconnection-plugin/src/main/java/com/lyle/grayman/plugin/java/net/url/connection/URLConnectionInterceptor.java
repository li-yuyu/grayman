package com.lyle.grayman.plugin.java.net.url.connection;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;

import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.concurrent.Callable;

public class URLConnectionInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        // TODO: JDK类拦截不到
        if (obj != null && obj instanceof URLConnection) {
            URLConnection urlConnection = (URLConnection) obj;
            String headerValue = urlConnection.getHeaderField(Const.HTTP_HEADER_ENV_IDENTITY);

            if (headerValue == null) {
                String envIdentity = Context.getEnvIdentity();
                if (AgentStringUtils.isNotEmpty(envIdentity)) {
                    urlConnection.setRequestProperty(Const.HTTP_HEADER_ENV_IDENTITY, envIdentity);
                }
            }
        }

        return zuper.call();
    }
}
