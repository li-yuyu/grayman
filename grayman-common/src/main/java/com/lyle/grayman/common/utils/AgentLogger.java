package com.lyle.grayman.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AgentLogger {
    static Logger logger = Logger.getLogger("AgentLogger");

    static {
        FileHandler fh;
        try {
            String logFileName = getLogFile();
            fh = new FileHandler(logFileName);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLogFile() throws IOException {
        String userHome = System.getProperty("user.home");
        String logs = userHome + "/logs";
        File logHome = new File(logs);
        if (!logHome.exists()) {
            logHome.mkdir();
        }
        String logFileName = logs + "/agent_plugin.log";
        File logFile = new File(logFileName);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        return logFileName;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static String getStackTraceString(Throwable ex) {
        String result = "";
        try {
            StackTraceElement[] traceElements = ex.getStackTrace();
            StringBuilder traceBuilder = new StringBuilder();

            if (traceElements != null && traceElements.length > 0) {
                for (StackTraceElement traceElement : traceElements) {
                    traceBuilder.append(traceElement.toString());
                    traceBuilder.append("\n");
                }
            }


            String stackTrace = traceBuilder.toString();
            String exceptionType = ex.toString();
            String exceptionMessage = ex.getMessage();

            result = String.format("%s : %s \r\n %s", exceptionType, exceptionMessage, stackTrace);
        } catch (Exception stEx) {
            getLogger().severe("getStackTraceString error:" + stEx.getMessage());
            if (ex != null) {
                result = ex.getMessage();
            }
        }
        return result;
    }

}
