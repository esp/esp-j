package com.esp;

import com.esp.disposables.Disposable;
import com.esp.functions.Action0;
import com.esp.functions.Action1;
import com.esp.reactive.EventObservable;
import com.esp.reactive.EventSubject;
import com.esp.reactive.Observable;
import com.esp.reactive.Subject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRouter<TModel> implements Router<TModel> {
    private ConcurrentHashMap<Object, EventSubjects> _eventSubjects = new ConcurrentHashMap<>();
    private Subject<TModel> _modelSubject = Subject.create();
    private TModel _model;
    private RouterDispatcher _dispatcher;
    private Queue<Action0> _eventDispatchQueue = new LinkedList<>();
    private RouterState _state;
    private PreEventProcessor _prePreEventProcessor;
    private PostEventProcessor _postEventProcessor;

    public DefaultRouter(RouterDispatcher dispatcher, TerminalErrorHandler errorHandler) {
        this(null, dispatcher, errorHandler);
    }

    public DefaultRouter(TModel model, RouterDispatcher dispatcher, TerminalErrorHandler errorHandler) {
        Guard.ArgumentRequires(dispatcher != null);
        Guard.ArgumentRequires(errorHandler != null);
        _dispatcher = dispatcher;
        _state = new RouterState(errorHandler);
        if(model != null) {
            setModel(model);
        }
    }

    public void setModel(TModel model) {
        if (model == null) throw new IllegalArgumentException("model can not be null");
        if (_model != null) throw new IllegalStateException("Model already set");
        _model = model;
        if (_model instanceof PreEventProcessor) {
            _prePreEventProcessor = ((PreEventProcessor) _model);
        } else {
            _prePreEventProcessor = () -> { /* noop */ };
        }
        if (_model instanceof PostEventProcessor) {
            _postEventProcessor = ((PostEventProcessor) _model);
        } else {
            _postEventProcessor = () -> { /* noop */ };
        }
    }

    @Override
    public void publishEvent(Object event)  {
        ensureModelSet();
        if (!_dispatcher.checkAccess()) {
            _dispatcher.dispatch(() -> publishEvent(event));
            return;
        }
        if(_state.isHalted()) return;
        Class eventClass = event.getClass();
        boolean foundObserver = _eventSubjects.containsKey(eventClass.getTypeName());
        if (!foundObserver) {
            eventClass = eventClass.getSuperclass();
            while (eventClass != null) {
                foundObserver = _eventSubjects.containsKey(eventClass.getTypeName());
                if (foundObserver) break;
                eventClass = eventClass.getSuperclass();
            }
            if (!foundObserver) return;
        }
        _eventDispatchQueue.add(createEventDispatchAction(event));
        purgeEventQueues();
    }

    @Override
    public void runAction(Action0 action) {
        ensureModelSet();
        if (!_dispatcher.checkAccess()) {
            _dispatcher.dispatch(() -> runAction(action));
            return;
        }
        if(_state.isHalted()) return;
        _eventDispatchQueue.add(action);
        purgeEventQueues();
    }

    @Override
    public void runAction(Action1<TModel> action) {
        ensureModelSet();
        if (!_dispatcher.checkAccess()) {
            _dispatcher.dispatch(() -> runAction(action));
            return;
        }
        if(_state.isHalted()) return;
        _eventDispatchQueue.add(() -> action.call(_model));
        purgeEventQueues();
    }

    @Override
    public void executeEvent(Object event) {
        ensureModelSet();
        if (!_dispatcher.checkAccess()) {
            _dispatcher.dispatch(() -> executeEvent(event));
            return;
        }
        if(_state.isHalted()) return;
        _state.moveToExecuting();
        Action0 dispatchAction = createEventDispatchAction(event);
        dispatchAction.call();
        _state.endExecuting();
    }

    @Override
    public Observable<TModel> getModelObservable() {
        return Observable.create(o -> _modelSubject.observe(o));
    }

    @Override
    public <TEvent> EventObservable<TEvent, EventContext, TModel> getEventObservable(Class<TEvent> eventClass) {
        return getEventObservable(eventClass, ObservationStage.Normal);
    }

    @Override
    public <TEvent> EventObservable<TEvent, EventContext, TModel> getEventObservable(Class<TEvent> eventClass, ObservationStage observationStage) {
        return EventObservable.create(o -> {
            EventSubjects eventSubjects;
            eventSubjects = _eventSubjects.computeIfAbsent(
                    eventClass.getTypeName(),
                    key -> new EventSubjects()
            );
            Object subject;
            switch (observationStage) {
                case Preview:
                    subject = eventSubjects.PreviewSubject;
                    break;
                case Normal:
                    subject = eventSubjects.NormalSubject;
                    break;
                case Committed:
                    subject = eventSubjects.CommittedSubject;
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("The observationStage %s not supported", observationStage));
            }
            //noinspection unchecked
            return ((EventSubject<TEvent, EventContext, TModel>) subject).observe(o);
        });
    }

    @Override
    public FluentEventObserver<TModel> beginObserveEvents() {
        return new FluentEventObserver<>(this);
    }

    @Override
    public Disposable observeEventsOn(Object observer) {
        ReflectiveEventObservationWireup<TModel> wireup = new ReflectiveEventObservationWireup<>(observer, this);
        wireup.observeEvents();
        return wireup;
    }

    private Action0 createEventDispatchAction(Object event) {
        return () -> {
            ArrayList<EventSubjects> eventsSubjects = new ArrayList<>();
            Class eventClass = event.getClass();
            while (eventClass != null) {
                String typeName = eventClass.getTypeName();
                EventSubjects subjects = _eventSubjects.get(typeName);
                if (subjects != null) {
                    eventsSubjects.add(subjects);
                }
                eventClass = eventClass.getSuperclass();
            }
            if (eventsSubjects.size() > 0) {
                EventContext eventContext = new EventContext();
                eventContext.set_currentStage(ObservationStage.Preview);
                for (EventSubjects subjects : eventsSubjects) {
                    subjects.PreviewSubject.onNext(event, eventContext, _model);
                }
                if (eventContext.get_isCommitted()) {
                    throw new IllegalStateException(String.format("Committing event [%s] at the ObservationStage.Preview is invalid", event.getClass().getTypeName()));
                }
                if (!eventContext.get_isCanceled()) {
                    eventContext.set_currentStage(ObservationStage.Normal);
                    for (EventSubjects subjects : eventsSubjects) {
                        subjects.NormalSubject.onNext(event, eventContext, _model);
                    }
                    if (eventContext.get_isCanceled()) {
                        throw new IllegalStateException(String.format("Cancelling event [%s] at the ObservationStage.Normal is invalid", event.getClass().getTypeName()));
                    }
                    if (eventContext.get_isCommitted()) {
                        eventContext.set_currentStage(ObservationStage.Committed);
                        for (EventSubjects subjects : eventsSubjects) {
                            subjects.CommittedSubject.onNext(event, eventContext, _model);
                        }
                        if (eventContext.get_isCanceled()) {
                            throw new IllegalStateException(String.format("Cancelling event [%s] at the ObservationStage.Committed is invalid", event.getClass().getTypeName()));
                        }
                    }
                }
            }
        };
    }

    private void purgeEventQueues() {
        if (_state.getCurrentStatus() == RouterStatus.Idle) {
            try {
                boolean hasEvents = _eventDispatchQueue.size() > 0;
                while (hasEvents) {
                    while (hasEvents) {
                        _state.moveToPreProcessing();
                        _prePreEventProcessor.preProcess();
                        _state.moveToEventDispatch();
                        while (hasEvents) {
                            Action0 dispatchAction = _eventDispatchQueue.remove();
                            dispatchAction.call();
                            hasEvents = _eventDispatchQueue.size() > 0;
                        }
                        _state.moveToPostProcessing();
                        _postEventProcessor.postProcess();
                        hasEvents = _eventDispatchQueue.size() > 0;
                    }
                    _state.moveToDispatchModelUpdates();
                    dispatchModel();
                    hasEvents = _eventDispatchQueue.size() > 0;
                }
                _state.moveToIdle();
            } catch (Exception ex) {
                _state.moveToHalted(ex);
            }
        }
    }

    public void dispatchModel() {
        _modelSubject.onNext(_model);
    }

    private class EventSubjects {
        public EventSubjects() {
            PreviewSubject = EventSubject.<Object, EventContext, TModel>create();
            NormalSubject = EventSubject.<Object, EventContext, TModel>create();
            CommittedSubject = EventSubject.<Object, EventContext, TModel>create();
        }

        public EventSubject<Object, EventContext, TModel> PreviewSubject;
        public EventSubject<Object, EventContext, TModel> NormalSubject;
        public EventSubject<Object, EventContext, TModel> CommittedSubject;
    }


    private void ensureModelSet() {
        if(_model == null) {
            throw new IllegalStateException("Model not set");
        }
    }
}