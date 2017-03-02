package com.esp;

import com.esp.disposables.Disposable;
import com.esp.functions.Action0;
import com.esp.functions.Action1;
import com.esp.reactive.EventObservable;
import com.esp.reactive.Observable;

public interface Router<TModel> {
    void publishEvent(Object event);

    /**
     *
     * @deprecated can't serialise an action, thus re-start debugging will be hard
     */
    @Deprecated
    void runAction(Action0 action);

    /**
     *
     * @deprecated can't serialise an action, thus re-start debugging will be hard
     */
    @Deprecated
    void runAction(Action1<TModel> action);

    void executeEvent(Object event);

    Observable<TModel> getModelObservable();

    <TEvent> EventObservable<TEvent, EventContext, TModel> getEventObservable(Class<TEvent> eventClass);

    <TEvent> EventObservable<TEvent, EventContext, TModel> getEventObservable(Class<TEvent> eventClass, ObservationStage observationStage);

    FluentEventObserver<TModel> beginObserveEvents();

    Disposable observeEventsOn(Object observer);
}
