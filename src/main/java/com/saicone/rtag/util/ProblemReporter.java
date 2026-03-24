package com.saicone.rtag.util;

import com.saicone.rtag.util.reflect.Lookup;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Modifier;
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
                    final Lookup.AClass<?> ProblemReporter = Lookup.SERVER.importClass("net.minecraft.util.ProblemReporter");
                    if (MC.version().isNewerThanOrEquals(MC.V_1_21_6)) {
                        return ProblemReporter.field(Modifier.STATIC, ProblemReporter, "DISCARDING").getValue();
                    } else {
                        final Class<?> clazz = ProblemReporter.get();
                        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, (proxy, method, args) -> {
                            if (method.getReturnType().equals(void.class)) {
                                return null;
                            } else if (method.getReturnType().isAssignableFrom(clazz)) {
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
