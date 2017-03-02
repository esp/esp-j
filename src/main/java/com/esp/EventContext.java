package com.esp;

public class EventContext {
    private ObservationStage _currentStage;
    private boolean _isCanceled;
    private boolean _isCommitted;

    public ObservationStage get_currentStage() {
        return _currentStage;
    }

    public void set_currentStage(ObservationStage _currentStage) {
        this._currentStage = _currentStage;
    }

    public boolean get_isCanceled() {
        return _isCanceled;
    }

    public boolean get_isCommitted() {
        return _isCommitted;
    }

    public void cancel()
    {
        if (_isCanceled) throw new IllegalStateException("Already canceled");
        _isCanceled = true;
    }

    public void commit()
    {
        if (_isCommitted) throw new IllegalStateException("Already committed");
        _isCommitted = true;
    }
}
