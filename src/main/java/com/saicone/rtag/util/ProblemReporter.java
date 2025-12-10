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
            if (MC.version().isNewerThanOrEquals(MC.V_1_20_3)) {
                try {
                    final Class<?> ProblemReporter = EasyLookup.addNMSClass("util.ProblemReporter");

                    String discarding = "a";
                    if (ServerInstance.Type.MOJANG_MAPPED) {
                        discarding = "DISCARDING";
                    }

                    if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                        return ProblemReporter.getDeclaredField(discarding).get(null);
                    } else {
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
            }
            return null;
        }
    }
}
