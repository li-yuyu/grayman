package com.lyle.grayman.common.threading;

import com.lyle.grayman.common.context.Context;

public class WrapRunnable implements EnhancedInstance, Runnable {
    private Object context;
    private Runnable runnable;

    public WrapRunnable(Runnable runnable, Object context) {
        this.runnable = runnable;
        this.context = context;
    }

    @Override
    public Object getEnvIdentity() {
        return context;
    }

    @Override
    public void setEnvIdentity(Object value) {
        this.context = value;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            Context.clean();
        }
    }
}
