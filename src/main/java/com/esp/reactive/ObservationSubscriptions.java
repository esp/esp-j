package com.esp.reactive;

import com.esp.disposables.Disposable;
import com.esp.disposables.EspDisposable;

import java.util.ArrayList;

public class ObservationSubscriptions<T> {
    private final Object _gate = new Object();

    private ArrayList<Observer<T>> _observers = new ArrayList<>();

    public Disposable observe(Observer<T> observer)     {
        synchronized (_gate) {
            _observers.add(observer);
        }
        return EspDisposable.create(() -> {
            synchronized (_gate) {
                _observers.remove(observer);
            }
        });
    }

    public ArrayList<Observer<T>> getObservers() {
        return new ArrayList<>(_observers);
    }
}
