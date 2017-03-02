package com.esp.reactive;


import com.esp.disposables.Disposable;
import com.esp.functions.Func0;
import com.esp.functions.Func1;

import java.util.ArrayList;

public class Subject<T> extends Observable<T> {
    private boolean _hasCompleted;
    private Func0<ArrayList<Observer<T>>> _getObservers;

    public static <T> Subject<T> create() {
        ObservationSubscriptions<T> subscriptions = new ObservationSubscriptions<>();
        return new Subject<>(subscriptions::observe, subscriptions::getObservers);
    }

    protected Subject(
        Func1<Observer<T>, Disposable> subscribe,
        Func0<ArrayList<Observer<T>>> getObservers
    ) {
        super(subscribe);
        _getObservers = getObservers;
    }

    public void onNext(T model) {
        ArrayList<Observer<T>> observers = _getObservers.call();
        for (Observer<T> observer : observers) {
            if (_hasCompleted) break;
            observer.onNext(model);
        }
    }

    public void onCompleted() {
        if (!_hasCompleted) {
            _hasCompleted = true;
            ArrayList<Observer<T>> observers = _getObservers.call();
            for (Observer<T> observer : observers) {
                observer.onCompleted();
            }
        }
    }
}
