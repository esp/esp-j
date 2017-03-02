package com.esp.reactive;

public class ObservedEventItems<T1, T2, T3> {
    public void set(T1 t1, T2 t2, T3 t3) {
        T1 = t1;
        T2 = t2;
        T3 = t3;
        setInvokedCount++;
    }
    public T1 T1;
    public T2 T2;
    public T3 T3;
    public int setInvokedCount;
}