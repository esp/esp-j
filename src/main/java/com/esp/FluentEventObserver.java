package com.esp;

import com.esp.disposables.CollectionDisposable;
import com.esp.disposables.Disposable;
import com.esp.functions.*;

import java.util.ArrayList;

public class FluentEventObserver<TModel> {

    ArrayList<Func0> _items = new ArrayList<>();
    private Router<TModel> _router;

    public FluentEventObserver(Router<TModel> router) {
        _router = router;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action0 observer) {
        _items.add(() -> _router.getEventObservable(eventClass).observe((e, c) -> {
            observer.call();
        }));
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action1<TEvent> observer) {
        _items.add(() -> _router.<TEvent>getEventObservable(eventClass).observe((e, c) -> {
            observer.call(e);
        }));
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action1<TEvent> observer, ObservationStage observationStage) {
        _items.add(() -> _router.<TEvent>getEventObservable(eventClass, observationStage).observe((e, c) -> {
            observer.call(e);
        }));
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action1<TEvent> observer, Func1<TEvent, Boolean> predicate) {
        _items.add(() -> _router
                .<TEvent>getEventObservable(eventClass)
                .where((e, c, m) -> predicate.call(e))
                .observe(observer::call)
        );
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action2<TEvent, EventContext> observer) {
        _items.add(() -> _router.<TEvent>getEventObservable(eventClass).observe(observer::call));
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action2<TEvent, EventContext> observer, Func2<TEvent, EventContext, Boolean> predicate) {
        _items.add(() -> _router
                .<TEvent>getEventObservable(eventClass)
                .where((e, c, m) -> predicate.call(e, c))
                .observe(observer::call)
        );
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action3<TEvent, EventContext, TModel> observer) {
        _items.add(() -> _router.<TEvent>getEventObservable(eventClass).observe(observer::call));
        return this;
    }

    public <TEvent> FluentEventObserver<TModel> addObserver(Class<TEvent> eventClass, Action3<TEvent, EventContext, TModel> observer, Func3<TEvent, EventContext, TModel, Boolean> predicate) {
        _items.add(() -> _router
                .<TEvent>getEventObservable(eventClass)
                .where(predicate::call)
                .observe(observer::call)
        );
        return this;
    }

    public Disposable observe() {
        Disposable[] disposables = _items
                .stream()
                .map(Func0::call)
                .toArray(Disposable[]::new);
        return new CollectionDisposable(disposables);
    }
}
