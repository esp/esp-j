package com.esp;

class RouterState {
    private TerminalErrorHandler _terminalErrorHandler;
    private Exception _haltingException;
    private RouterStatus _currentStatus;
    
    public RouterState(TerminalErrorHandler terminalErrorHandler) {
        _terminalErrorHandler = terminalErrorHandler;
        _currentStatus = RouterStatus.Idle;
    }

    public RouterStatus getCurrentStatus() {
        return _currentStatus;
    }

    public boolean isHalted() {
        return _currentStatus == RouterStatus.Halted;
    }

    public void moveToPreProcessing() {
        _currentStatus = RouterStatus.PreEventProcessing;
    }

    public void moveToEventDispatch() {
        _currentStatus = RouterStatus.EventProcessorDispatch;
    }

    public void moveToPostProcessing() {
        _currentStatus = RouterStatus.PostProcessing;
    }

    public void moveToDispatchModelUpdates() {
        _currentStatus = RouterStatus.DispatchModelUpdates;
    }

    public void moveToHalted(Exception exception) {
        _haltingException = exception;
        _currentStatus = RouterStatus.Halted;
        _terminalErrorHandler.onError(exception);
    }

    public void moveToIdle() {
        _currentStatus = RouterStatus.Idle;
    }

    public void moveToExecuting() {
        boolean canExecute = _currentStatus == RouterStatus.EventProcessorDispatch;
        if (canExecute) {
            _currentStatus = RouterStatus.Executing;
        } else {
            throw new IllegalStateException("Can't execute event. You can only execute an event 1) from within the observer passed to EventObservable.observe(EventObserver), 2) when the router is within an existing event loop");
        }
    }

    public void endExecuting() {
        if (_currentStatus != RouterStatus.Executing) {
            throw new IllegalStateException("Can't end executing state castTo event execution isn't underway.");
        }
        _currentStatus = RouterStatus.EventProcessorDispatch;
    }
//
//    public void notifyIfHalted() throws Exception {
//        if (_currentStatus == RouterStatus.Halted) {
//            Exception error = new Exception("Router halted due to previous error", _haltingException);
//            if (_terminalErrorHandler != null)
//                _terminalErrorHandler.onError(error);
//            else
//                throw error;
//        }
//    }
}
