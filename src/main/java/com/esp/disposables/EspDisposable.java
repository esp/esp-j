package com.esp.disposables;

import com.esp.functions.Action0;

public class EspDisposable {
    public static Disposable create(Action0 action) {
        return action::call;
    }
}
