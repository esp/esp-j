package com.esp.reactive;

import com.esp.disposables.Disposable;
import com.esp.disposables.EspDisposable;

import java.util.ArrayList;

public class EventObservationSubscriptions<TEvent, TEventContext, TModel> {
    private final Object _gate = new Object();

    private ArrayList<EventObserver<TEvent, TEventContext, TModel>> _observers = new ArrayList<>();

    public Disposable observe(EventObserver<TEvent, TEventContext, TModel> observer)     {
        synchronized (_gate) {
            _observers.add(observer);
        }
        return EspDisposable.create(() -> {
            synchronized (_gate) {
                _observers.remove(observer);
            }
        });
    }

    public ArrayList<EventObserver<TEvent, TEventContext, TModel>> getObservers() {
        return new ArrayList<>(_observers);
    }
}
