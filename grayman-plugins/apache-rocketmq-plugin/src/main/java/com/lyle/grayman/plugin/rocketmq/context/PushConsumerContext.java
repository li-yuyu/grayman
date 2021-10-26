package com.lyle.grayman.plugin.rocketmq.context;

public class PushConsumerContext {
    private Object[] subscribeArgs;
    private Object[] registerArgs;

    public Object[] getSubscribeArgs() {
        return subscribeArgs;
    }

    public void setSubscribeArgs(Object[] subscribeArgs) {
        this.subscribeArgs = subscribeArgs;
    }

    public Object[] getRegisterArgs() {
        return registerArgs;
    }

    public void setRegisterArgs(Object[] registerArgs) {
        this.registerArgs = registerArgs;
    }
}
