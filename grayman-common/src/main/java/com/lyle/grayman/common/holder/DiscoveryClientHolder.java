package com.lyle.grayman.common.holder;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DiscoveryClientHolder {
    private static Object target;
    private static AtomicBoolean isSetted;

    static {
        isSetted = new AtomicBoolean(false);
    }

    private DiscoveryClientHolder() {
    }

    public static void setTarget(Object target) {
        if (isSetted.compareAndSet(false, true)) {
            DiscoveryClientHolder.target = target;
        }
    }

    public static Object getTarget() {
        return target;
    }
}
