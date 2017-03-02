package com.esp;

import com.esp.disposables.Disposable;
import com.esp.functions.Action0;

public interface RouterDispatcher extends Disposable {
    boolean checkAccess();
    void ensureAccess();
    void dispatch(Action0 action);
}
