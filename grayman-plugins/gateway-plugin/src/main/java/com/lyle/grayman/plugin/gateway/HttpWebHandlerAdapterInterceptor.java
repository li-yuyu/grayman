package com.lyle.grayman.plugin.gateway;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

public class HttpWebHandlerAdapterInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        if (allArguments != null && allArguments.length == 2) {
            Object arg = allArguments[0];
            if (arg != null && arg instanceof ServerHttpRequest) {
                Context.clean();
                ServerHttpRequest request = (ServerHttpRequest) arg;
                HttpHeaders headers = request.getHeaders();
                if (headers != null && !headers.isEmpty()) {
                    List<String> envIdentitys = headers.get(Const.HTTP_HEADER_ENV_IDENTITY);
                    if (envIdentitys != null && !envIdentitys.isEmpty() && AgentStringUtils.isNotEmpty(envIdentitys.get(0))) {
                        Context.setEnvIdentity(envIdentitys.get(0));
                        return zuper.call();
                    }
                }
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if (cookies != null && !cookies.isEmpty()) {
                    HttpCookie graymanEnvCookie = cookies.getFirst(Const.HTTP_HEADER_ENV_IDENTITY);
                    if (graymanEnvCookie != null && AgentStringUtils.isNotEmpty(graymanEnvCookie.getValue())) {
                        Context.setEnvIdentity(graymanEnvCookie.getValue());
                    }
                }
            }
        }
        return zuper.call();
    }
}
