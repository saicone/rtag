package com.saicone.rtag.util;

/**
 * Represents a function that accepts one argument and produces a result.<br>
 * Function will throw a Throwable.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface ThrowableFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return  the function result
     * @throws Throwable on execution
     */
    R apply(T t) throws Throwable;
}
