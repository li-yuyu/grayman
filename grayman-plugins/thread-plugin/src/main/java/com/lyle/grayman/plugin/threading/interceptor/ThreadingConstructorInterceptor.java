package com.lyle.grayman.plugin.threading.interceptor;

import com.lyle.grayman.common.context.Context;
import com.lyle.grayman.common.interceptor.ConstructorInterceptor;
import com.lyle.grayman.common.threading.EnhancedInstance;
import com.lyle.grayman.common.utils.AgentStringUtils;

public class ThreadingConstructorInterceptor implements ConstructorInterceptor {
    public void intercept(Object obj, Object[] allArguments) {
        try {
            EnhancedInstance targetObject = (EnhancedInstance) obj;
            String graymanIdentity = Context.getEnvIdentity();
            if (AgentStringUtils.isNotEmpty(graymanIdentity)) {
                targetObject.setEnvIdentity(graymanIdentity);
            }
        } catch (Throwable t) {
        }

    }

}
