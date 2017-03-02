package com.esp.reactive;

import com.esp.disposables.*;
import com.esp.functions.*;

import java.util.Arrays;

public class EventObservable<TEvent, TEventContext, TModel> {

    private Func1<EventObserver<TEvent, TEventContext, TModel>, Disposable> _subscribe;

    public EventObservable(Func1<EventObserver<TEvent, TEventContext, TModel>, Disposable> subscribe) {
        _subscribe = subscribe;
    }

    public Disposable observe(Action1<TEvent> observer) {
        return observe(new EventObserver<>(observer));
    }

    public Disposable observe(Action1<TEvent> observer, Action0 onCompleted) {
        return observe(new EventObserver<>(observer, onCompleted));
    }

    public Disposable observe(Action2<TEvent, TEventContext> observer) {
        return observe(new EventObserver<>(observer));
    }

    public Disposable observe(Action2<TEvent, TEventContext> observer, Action0 onCompleted) {
        return observe(new EventObserver<>(observer, onCompleted));
    }

    public Disposable observe(Action3<TEvent, TEventContext, TModel> observer) {
        return observe(new EventObserver<>(observer));
    }

    public Disposable observe(Action3<TEvent, TEventContext, TModel> observer, Action0 onCompleted) {
        return observe(new EventObserver<>(observer, onCompleted));
    }

    public Disposable observe(EventObserver<TEvent, TEventContext, TModel> observer) {
        return _subscribe.call(observer);
    }

    public static <TEvent, TEventContext, TModel> EventObservable<TEvent, TEventContext, TModel> create(Func1<EventObserver<TEvent, TEventContext, TModel>, Disposable> subscribe) {
        return new EventObservable<>(subscribe);
    }

    @SafeVarargs
    public static <TEvent, TContext, TModel> EventObservable<TEvent, TContext, TModel> merge(EventObservable<TEvent, TContext, TModel>... sources) {
        return mergeInternal(Arrays.asList(sources));
    }

    public static <TEvent, TContext, TModel> EventObservable<TEvent, TContext, TModel> merge(Iterable<EventObservable<TEvent, TContext, TModel>> sources) {
        return mergeInternal(sources);
    }

    private static <TEvent, TContext, TModel> EventObservable<TEvent, TContext, TModel> mergeInternal(Iterable<EventObservable<TEvent, TContext, TModel>> sources) {
        return create(o -> {
            CollectionDisposable disposables = new CollectionDisposable();
            for (EventObservable<TEvent, TContext, TModel> source : sources) {
                Disposable disposable = source.observe(o);
                disposables.add(disposable);
            }
            return disposables;
        });
    }

    public EventObservable<TEvent, TEventContext, TModel> where(Func3<TEvent, TEventContext, TModel, Boolean> predicate) {
        return create(o -> {
            return observe(
                    (e, c, m) -> {
                        if (predicate.call(e, c, m)) {
                            o.onNext(e, c, m);
                        }
                    },
                    o::onCompleted
            );
        });
    }

    public <TMappedEvent> EventObservable<TMappedEvent, TEventContext, TModel> map(Func3<TEvent, TEventContext, TModel, TMappedEvent> mapper) {
        return create(o -> {
            return observe(
                    (e, c, m) -> {
                        TMappedEvent mapped = mapper.call(e, c, m);
                        o.onNext(mapped, c, m);
                    },
                    o::onCompleted
            );
        });
    }

    public EventObservable<TEvent, TEventContext, TModel> take(int count) {
        class TempClass {
            public int received = 0;
        }
        TempClass temp = new TempClass();
        return create(o -> {
            return observe(
                    (e, c, m) -> {
                        temp.received++;
                        if (temp.received <= count) {
                            o.onNext(e, c, m);
                        } else {
                            o.onCompleted();
                        }
                    },
                    o::onCompleted
            );
        });
    }

    public <TOther> EventObservable<TOther, TEventContext, TModel> cast() {
        return create(o -> {
            return observe(
                    (e, c, m) -> {
                        //noinspection unchecked
                        o.onNext((TOther) e, c, m);
                    },
                    o::onCompleted
            );
        });
    }
}