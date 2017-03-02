package com.esp;

import com.esp.disposables.Disposable;
import com.esp.functions.Action0;
import com.esp.functions.Action1;
import com.esp.functions.Func1;
import com.esp.reactive.EventObservable;
import com.esp.reactive.Observable;

public class SubRouter<TModel, TSubModel> implements Router<TSubModel> {

    private Router<TModel> _parent;
    private Func1<TModel, TSubModel> _subModelSelector;

    public SubRouter(
            Router<TModel> parent,
            Func1<TModel, TSubModel> subModelSelector
    ) {
        _parent = parent;
        _subModelSelector = subModelSelector;
    }

    @Override
    public void publishEvent(Object event) {
        _parent.publishEvent(event);
    }

    @Override
    public void runAction(Action0 action) {
        _parent.runAction(action);
    }

    @Override
    public void runAction(Action1<TSubModel> action) {
        _parent.runAction(parent-> {
            action.call(_subModelSelector.call(parent));
        });
    }

    @Override
    public void executeEvent(Object event) {
        _parent.executeEvent(event);
    }

    @Override
    public Observable<TSubModel> getModelObservable() {
        return _parent.getModelObservable().map(tModel -> _subModelSelector.call(tModel));
    }

    @Override
    public <TEvent> EventObservable<TEvent, EventContext, TSubModel> getEventObservable(Class<TEvent> tEventClass) {
        return EventObservable.create(o -> {
            return _parent.getEventObservable(tEventClass).observe((e, c, m) -> {
                        o.onNext(e, c, _subModelSelector.call(m));
                    },
                    o::onCompleted);
        });
    }

    @Override
    public <TEvent> EventObservable<TEvent, EventContext, TSubModel> getEventObservable(Class<TEvent> tEventClass, ObservationStage observationStage) {
        return EventObservable.create(o -> {
            return _parent.getEventObservable(tEventClass, observationStage).observe((e, c, m) -> {
                        o.onNext(e, c, _subModelSelector.call(m));
                    },
                    o::onCompleted);
        });
    }

    @Override
    public FluentEventObserver<TSubModel> beginObserveEvents() {
        return new FluentEventObserver<>(this);
    }

    @Override
    public Disposable observeEventsOn(Object observer) {
        ReflectiveEventObservationWireup<TModel> wireup = new ReflectiveEventObservationWireup<>(observer, (Router<TModel>) this);
        wireup.observeEvents();
        return wireup;
    }

}
