package com.lyle.grayman.plugin.servlet.interceptor;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentLogger;
import com.lyle.grayman.common.utils.AgentStringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class EnvIdentityServletInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        if (method.getModifiers() != 4) {
            return zuper.call();
        }
        Object ret;
        try {
            resolveGraymanIdentityFromRequest(allArguments[0]);
            ret = zuper.call();
        } catch (Throwable t) {
            throw t;
        } finally {
            try {
                Context.clean();
            } catch (Throwable t) {
                AgentLogger.getLogger().severe("HttpServletPreInterceptor.intercept after: " + AgentLogger.getStackTraceString(t));
            }
        }
        return ret;
    }

    private void resolveGraymanIdentityFromRequest(Object allArgument) {
        //获取http request中的测试环境相关信息
        HttpServletRequest servletRequest = (HttpServletRequest) allArgument;
        try {
            String envIdentity = servletRequest.getHeader(Const.HTTP_HEADER_ENV_IDENTITY);
            if (envIdentity == null || envIdentity.length() == 0) {
                envIdentity = getGraymanIdentityFromCookie(servletRequest, envIdentity);
            }
            if (AgentStringUtils.isNotEmpty(envIdentity)) {
                Context.setEnvIdentity(envIdentity);
            }
        } catch (Throwable t) {
            AgentLogger.getLogger().severe("EnvIdentityServletInterceptor set test tag : " + AgentLogger.getStackTraceString(t));
        }
    }

    private String getGraymanIdentityFromCookie(HttpServletRequest servletRequest, String envIdentity) {
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie != null && Const.HTTP_HEADER_ENV_IDENTITY.equals(cookie.getName())) {
                    envIdentity = cookie.getValue();
                    break;
                }
            }
        }
        return envIdentity;
    }
}
