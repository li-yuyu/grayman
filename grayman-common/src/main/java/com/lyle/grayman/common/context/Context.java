package com.lyle.grayman.common.context;

public class Context {

    private static final ThreadLocal<RefCounted> envIdentity = new ThreadLocal<>();

    public static String getEnvIdentity() {
        RefCounted refCounted = envIdentity.get();
        if (refCounted == null) {
            return null;
        }
        return refCounted.val;
    }

    public static void setEnvIdentity(String env) {
        RefCounted refCounted = envIdentity.get();
        if (refCounted == null) {
            envIdentity.set(new RefCounted(env));
        } else {
            ++refCounted.cnt;
        }
    }

    public static void clean() {
        RefCounted refCounted = envIdentity.get();
        if (refCounted != null) {
            if (--refCounted.cnt <= 0) {
                envIdentity.remove();
            }
        }
    }

    private static class RefCounted {
        int cnt;
        String val;

        private RefCounted(String val) {
            this.cnt = 1;
            this.val = val;
        }

        @Override
        public String toString() {
            return "RefCounted{" +
                    "cnt=" + cnt +
                    ", val='" + val + '\'' +
                    '}';
        }
    }
}
