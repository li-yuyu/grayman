package com.lyle.grayman.agent.config;

import com.lyle.grayman.agent.classloader.AgentPackagePath;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 解析 agent.properties 文件，产生【被拦截类】，【被拦截方法】，【拦截方法】的list
 */
public class ConfigProperties {
    private static Properties configProperties = new Properties();
    private final static String filePath = "/agent.properties";
    private final static String key = "arch.agent.interceptors";

    static {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(AgentPackagePath.getPath() + filePath);
            configProperties.load(fileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<InterceptConfig> getInterceptConfig() {
        List<InterceptConfig> configs = new ArrayList<>();
        String props = configProperties.get(key).toString();
        String[] interceptors = props.split("/");
        for (int i = 0; i < interceptors.length; i++) {
            String interceptor = interceptors[i];
            String[] element = interceptor.split(",");
            configs.add(new InterceptConfig(element[0], element[1], element[2]));
        }
        return configs;
    }

    public static String getConfig(String configKey) {
        Object value = configProperties.get(configKey);
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    public static int cakeNumber(int n) {
        return getNumber(1, n + 1);
    }

    public static int getNumber(int number, int n) {
        if (n == 1) {
            return number;
        }
        number = (int) Math.ceil((number * 3.0 / 2.0));
        return getNumber(number, n - 1);
    }

    public static void main(String[] args) {
        int number = 824;

        int y64Count = number / 64;
        number = number - y64Count * 64;
        int y16Count = number / 16;
        number = number - y16Count * 16;
        int y4Count = number / 4;
        number = number - y4Count * 4;

        System.out.println(y64Count + y16Count + y4Count + number);
    }
}
