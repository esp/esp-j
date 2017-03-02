package com.esp.reactive;

import com.esp.functions.*;

public class EventObserver<TEvent, TContext, TModel> {

    private boolean _hasCompleted;
    private Action0 _onCompleted;
    private Action3<TEvent, TContext, TModel> _onNext;

    public EventObserver(Action1<TEvent> onNext) {
        this(onNext, null);
    }

    public EventObserver(Action1<TEvent> consumer, Action0 onCompleted) {
        this((e, c, m) -> consumer.call(e), onCompleted);
    }

    public EventObserver(Action2<TEvent, TContext> action) {
        this(action, null);
    }

    public EventObserver(Action2<TEvent, TContext> action, Action0 onCompleted) {
        this((e, c, m) -> action.call(e, c), onCompleted);
    }

    public EventObserver(Action3<TEvent, TContext, TModel> onNext) {
        this(onNext, null);
    }

    public EventObserver(Action3<TEvent, TContext, TModel> onNext, Action0 onCompleted) {
        _onNext = onNext;
        _onCompleted = onCompleted;
    }

    public void onNext(TEvent event, TContext context, TModel model) {
        _onNext.call(event, context, model);
    }

    public void onCompleted() {
        if(!_hasCompleted) {
            _hasCompleted = true;
            if (_onCompleted != null) _onCompleted.call();
        }
    }
}
