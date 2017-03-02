package com.esp;

import com.esp.functions.Action0;

public class CurrentThreadRouterDispatcher implements RouterDispatcher{

    private final long _threadId;
    private Boolean _isDisposed;

    public CurrentThreadRouterDispatcher() {
        _threadId = Thread.currentThread().getId();
    }

    @Override
    public boolean checkAccess() {
        long threadId = Thread.currentThread().getId();
        return _threadId == threadId;
    }

    @Override
    public void ensureAccess() {
        long threadId = Thread.currentThread().getId();
        if(_threadId != threadId) {
            throw new  IllegalStateException("Invalid thread access");
        }
    }

    @Override
    public void dispatch(Action0 action) {
        throw new  IllegalStateException("The current scheduler doesn't suport async operations");
    }

    @Override
    public void dispose() {
        throw new  IllegalStateException("Not supported");
    }
}
