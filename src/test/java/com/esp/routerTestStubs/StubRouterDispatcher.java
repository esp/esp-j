package com.esp.routerTestStubs;

import com.esp.RouterDispatcher;
import com.esp.functions.Action0;

import java.util.ArrayList;

public class StubRouterDispatcher implements RouterDispatcher {

    private boolean _hasAccess;
    private ArrayList<Action0> _actions = new ArrayList<>();
    private boolean _isDisposed;

    public StubRouterDispatcher()
    {
        _hasAccess = true;
    }

    @Override
    public boolean checkAccess()
    {
        throwIfDisposed();
        return _hasAccess;
    }

    @Override
    public void ensureAccess()
    {
        throwIfDisposed();
        if (!_hasAccess) throw new IllegalStateException("Invalid access");
    }

    @Override
    public void dispatch(Action0 action)
    {
        throwIfDisposed();
        _actions.add(action);
    }

    @Override
    public void dispose()
    {
        _isDisposed = true;
    }

    public int get_queuedActionCount()
    {
        return _actions.size();
    }

    public boolean get_hasAccess() {
        return _hasAccess;
    }

    public void set_hasAccess(Boolean hasAccess) {
        _hasAccess = hasAccess;
    }

    private void throwIfDisposed()
    {
        if (_isDisposed) throw new IllegalStateException();
    }

    public void invokeDispatchedActions(int numberToInvoke)
    {
        Boolean oldHasAccess = _hasAccess;
        _hasAccess = true;
        for (int i = 0; i < numberToInvoke || i < _actions.size() - 1; i++)
        {
            _actions.get(i).call();
        }
        _hasAccess = oldHasAccess;
    }
}
