package com.lyle.grayman.common.threading;

import com.lyle.grayman.common.context.Context;

import java.util.concurrent.Callable;

public class WrapCallable<V> implements EnhancedInstance, Callable<V> {
    private Callable<V> callable;
    private Object context;

    public WrapCallable(Callable<V> callable, Object context) {
        this.callable = callable;
        this.context = context;
    }

    public Object getEnvIdentity() {
        return context;
    }

    @Override
    public void setEnvIdentity(Object value) {
        this.context = value;
    }

    @Override
    public V call() throws Exception {
        if (context != null) {
            Context.setEnvIdentity((String) context);
        }
        try {
            return callable.call();
        } finally {
            Context.clean();
        }
    }
}
