package com.esp.functions;

@FunctionalInterface
public interface Action1<T> extends Action {
    void call(T t);
}
