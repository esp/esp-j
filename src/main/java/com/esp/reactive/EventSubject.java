package com.esp.reactive;


import com.esp.disposables.*;
import com.esp.functions.*;

import java.util.ArrayList;

public class EventSubject<TEvent, TEventContext, TModel> extends EventObservable<TEvent, TEventContext, TModel> {
    private boolean _hasCompleted;
    private Func0<ArrayList<EventObserver<TEvent, TEventContext, TModel>>> _getObservers;

    public static <TEvent, TEventContext, TModel> EventSubject<TEvent, TEventContext, TModel> create() {
        EventObservationSubscriptions<TEvent, TEventContext, TModel> subscriptions = new EventObservationSubscriptions<>();
        return new EventSubject<>(subscriptions::observe, subscriptions::getObservers);
    }

    protected EventSubject(
            Func1<EventObserver<TEvent, TEventContext, TModel>, Disposable> subscribe,
            Func0<ArrayList<EventObserver<TEvent, TEventContext, TModel>>> getObservers
        ) {
        super(subscribe);
        _getObservers = getObservers;
    }

    public void onNext(TEvent event, TEventContext eventContext, TModel model) {
        ArrayList<EventObserver<TEvent, TEventContext, TModel>> observers = _getObservers.call();
        for(EventObserver<TEvent, TEventContext, TModel> observer: observers) {
            if (_hasCompleted) break;
            observer.onNext(event, eventContext, model);
        }
    }

    public void onCompleted() {
        if (!_hasCompleted) {
            _hasCompleted = true;
            ArrayList<EventObserver<TEvent, TEventContext, TModel>> observers = _getObservers.call();
            for(EventObserver<TEvent, TEventContext, TModel> observer: observers) {
                observer.onCompleted();
            }
        }
    }
}
