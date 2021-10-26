package com.lyle.grayman.plugin.eureka.interceptor;

import com.lyle.grayman.common.constant.Const;
import com.lyle.grayman.common.interceptor.InstanceMethodInterceptor;
import com.lyle.grayman.common.utils.AgentStringUtils;
import com.lyle.grayman.common.utils.ReflectionUtils;
import com.lyle.grayman.common.utils.SystemUtils;
import com.netflix.appinfo.InstanceInfo;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class EnvIdentityDiscoveryClientInterceptor implements InstanceMethodInterceptor {
    @Override
    public Object intercept(Object obj, Object[] allArguments, Callable<?> zuper, Method method) throws Throwable {
        try {
            InstanceInfo instanceInfo = ReflectionUtils.getFieldValue(obj, "instanceInfo", InstanceInfo.class);
            if (instanceInfo != null) {
                String envIdentity = SystemUtils.getPropetyOrEnv(Const.GRAYMAN_ENV_IDENTITY);
                if (AgentStringUtils.isNotEmpty(envIdentity)) {
                    instanceInfo.getMetadata().put(Const.EUREKA_META_ENV_IDENTITY_KEY, envIdentity);
                }
            }
        } catch (Throwable throwable) {

        }
        return zuper.call();
    }
}
