package com.saicone.rtag.util;

import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Proxy;

/**
 * Class to invoke ProblemReporter related methods.
 *
 * @author Rubenicos
 */
@ApiStatus.Experimental
public interface ProblemReporter {

    /**
     * A ProblemReporter that ignores any error and doesn't generate anything.
     */
    Object DISCARDING = Static.discarding();

    class Static {

        private static Object discarding() {
            try {
                final Class<?> ProblemReporter = EasyLookup.addNMSClass("util.ProblemReporter");

                String discarding = "a";
                if (ServerInstance.Type.MOJANG_MAPPED) {
                    discarding = "DISCARDING";
                }

                if (ServerInstance.VERSION >= 21.05f) { // 1.21.6
                    return ProblemReporter.getDeclaredField(discarding).get(null);
                } else if (ServerInstance.VERSION >= 20.03f) { // 1.20.3
                    return Proxy.newProxyInstance(ProblemReporter.getClassLoader(), new Class[] { ProblemReporter }, (proxy, method, args) -> {
                        if (method.getReturnType().equals(void.class)) {
                            return null;
                        } else if (method.getReturnType().isAssignableFrom(ProblemReporter)) {
                            return proxy;
                        }

                        return null;
                    });
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return null;
        }
    }
}
