package com.esp;

import com.esp.disposables.Disposable;
import com.esp.disposables.DisposableBase;
import com.esp.functions.Action0;
import com.esp.functions.Action1;
import com.esp.functions.Action2;
import com.esp.functions.Action3;
import com.esp.reactive.EventObservable;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ReflectiveEventObservationWireup<TModel> extends DisposableBase {
    private Object _observer;
    private Router<TModel> _router;

    public ReflectiveEventObservationWireup(Object observer, Router<TModel> router) {
        _observer = observer;
        _router = router;
    }

    public void observeEvents() {
        try {
            for (Method method : _observer.getClass().getMethods()) {
                ObserveEvent[] observeEvents = method.getAnnotationsByType(ObserveEvent.class);
                if (observeEvents.length == 1) {
                    ObserveEvent observeEvent = observeEvents[0];
                    EventObservable observable = _router.getEventObservable(observeEvent.eventClass(), observeEvent.stage());
                    Action3<Object, EventContext, TModel> eventObserver;
                    eventObserver = GetEventObservationDelegate(observeEvent.eventClass(), method);
                    Disposable observe = observable.observe(eventObserver);
                    addDisposable(observe);
                } else if (observeEvents.length > 1) {
                    throw new RuntimeException("not supported"); // TODO
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e); // TODO
        }
    }

    private Action3<Object, EventContext, TModel> GetEventObservationDelegate(Class<?> eventClass, Method targetMethod) throws Throwable {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle targetMethodHandle = lookup.unreflect(targetMethod);
        Parameter[] parameters = targetMethod.getParameters();
        if (parameters.length == 0) {
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "call",
                    MethodType.methodType(Action0.class, _observer.getClass()),
                    MethodType.methodType(void.class),
                    targetMethodHandle,
                    MethodType.methodType(void.class)
            );
            MethodHandle target = callSite.getTarget();
            target = target.bindTo(_observer);
            Action0 action = (Action0) target.invoke();
            return (e, c, m) -> {
                action.call();
            };
        } else if (parameters.length == 1) {
            Class<?> paramClass = parameters[0].getType();
            if(paramClass == eventClass || paramClass == EventContext.class) {
                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "call",
                        MethodType.methodType(Action1.class, _observer.getClass()),
                        MethodType.methodType(void.class, Object.class),
                        targetMethodHandle,
                        MethodType.methodType(void.class, paramClass)
                );
                MethodHandle target = callSite.getTarget();
                target = target.bindTo(_observer);
                Action1 action = (Action1) target.invoke();
                if (EventContext.class.isAssignableFrom(paramClass)) {
                    return (e, c, m) -> {
                        action.call(c);
                    };
                } else if (eventClass.isAssignableFrom(paramClass)) {
                    return (e, c, m) -> {
                        action.call(e);
                    };
                }
            }
        } else if (parameters.length == 2) {
            Class<?> param1Class = parameters[0].getType();
            Class<?> param2Class = parameters[1].getType();
            if(param1Class == eventClass && param2Class == EventContext.class) {
                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "call",
                        MethodType.methodType(Action2.class, _observer.getClass()),
                        MethodType.methodType(void.class, Object.class, Object.class),
                        targetMethodHandle,
                        MethodType.methodType(void.class, eventClass, EventContext.class)
                );
                MethodHandle target = callSite.getTarget();
                target = target.bindTo(_observer);
                Action2 action = (Action2) target.invoke();
                return (e, c, m) -> {
                    action.call(e, c);
                };
            }
        }
        throw new UnsupportedOperationException("Unsupported event observation signature on method " + eventClass.getName() + "." + targetMethod.getName());
    }
}

