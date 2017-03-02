package com.esp.reactive;

import com.esp.disposables.Disposable;
import com.esp.functions.*;

public class Observable<T> {

    private Func1<Observer<T>, Disposable> _subscribe;

    public Observable(Func1<Observer<T>, Disposable> subscribe) {
        _subscribe = subscribe;
    }

    public Disposable observe(Action1<T> observer) {
        return observe(new Observer<>(observer));
    }

    public Disposable observe(Action1<T> observer, Action0 onCompleted) {
        return observe(new Observer<>(observer, onCompleted));
    }

    public Disposable observe(Observer<T> observer) {
        return _subscribe.call(observer);
    }

    public static <TModel> Observable<TModel> create(Func1<Observer<TModel>, Disposable> subscribe) {
        return new Observable<>(subscribe);
    }

    public Observable<T> where(Func1<T, Boolean> predicate) {
        return create(o -> {
            return observe(
                    (m) -> {
                        if (predicate.call(m)) {
                            o.onNext(m);
                        }
                    },
                    o::onCompleted
            );
        });
    }

    public <TSubModel> Observable<TSubModel> map(Func1<T, TSubModel> selector) {
        return Observable.<TSubModel>create(o -> {
            return observe(
                    (m) -> {
                        o.onNext(selector.call(m));
                    },
                    o::onCompleted
            );
        });
    }
}