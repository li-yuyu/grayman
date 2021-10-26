package com.lyle.grayman.agent.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterceptConfig {

    private static List<InterceptConfig> configs = new ArrayList<>();

    public static void loadConfig(String file) {

    }

    public static List<InterceptConfig> getConfigs() {
        return configs;
    }

    public static Map<String, List<InterceptConfig>> getConfigMap() {
        Map<String, List<InterceptConfig>> map = new HashMap<>();
        for (InterceptConfig config : configs) {
            String className = config.getClassName();
            List<InterceptConfig> interceptConfigs = map.get(className);
            if (interceptConfigs == null) {
                interceptConfigs = new ArrayList<>();
                map.put(className, interceptConfigs);
            }

            interceptConfigs.add(config);
        }

        return map;
    }

    private String className;
    private String methodName;
    private String interceptorClass;

    public InterceptConfig() {

    }

    public InterceptConfig(String className, String methodName, String interceptorClass) {
        this.className = className;
        this.methodName = methodName;
        this.interceptorClass = interceptorClass;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(String interceptorClass) {
        this.interceptorClass = interceptorClass;
    }
}
