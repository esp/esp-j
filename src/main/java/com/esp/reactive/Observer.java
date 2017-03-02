package com.esp.reactive;

import com.esp.functions.*;

public class Observer<T> {

    private boolean _hasCompleted;
    private Action0 _onCompleted;
    private Action1<T> _onNext;

    public Observer(Action1<T> onNext) {
        this(onNext, null);
    }

    public Observer(Action1<T> onNext, Action0 onCompleted) {
        _onNext = onNext;
        _onCompleted = onCompleted;
    }

    public void onNext(T model) {
        _onNext.call(model);
    }

    public void onCompleted() {
        if(!_hasCompleted) {
            _hasCompleted = true;
            if (_onCompleted != null) _onCompleted.call();
        }
    }
}
