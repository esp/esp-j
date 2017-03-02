package com.esp;


import com.esp.disposables.Disposable;
import com.esp.routerTestStubs.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RouterBeginObserveEventsTests {

    private DefaultRouter<TestModel> _router;
    private TestModel _model1;
    private ObservationMonitor _monitor;

    class ObservationMonitor {
        int noParamsReceivedCount;
        int eventReceivedCount;
        int eventAndContextReceivedCount;
        int eventAndContextAndModelReceivedCount;
    }

    @Before
    public void setUp() throws Exception {
        _router = new DefaultRouter<>(new StubRouterDispatcher(), new StubTerminalErrorHandler());
        _model1 = new TestModel();
        _router.setModel(_model1);
        _monitor = new ObservationMonitor();
    }

    @Test
    public void CanObserveUsingWithNoParams() throws Exception {
        _router
                .beginObserveEvents()
                .addObserver(FooEvent.class, this::noParamsObserver)
                .observe();
        publishAndAssertReceived(1, 0, 0, 0);
    }

    @Test
    public void CanObserveUsingEvent() throws Exception {
        _router
                .beginObserveEvents()
                .addObserver(FooEvent.class, this::eventOnlyObserver)
                .observe();
        publishAndAssertReceived(0, 1, 0, 0);
    }

    @Test
    public void CanObserveUsingEventAndContext() throws Exception {
        _router
                .beginObserveEvents()
                .addObserver(FooEvent.class, this::eventAndContextObserver)
                .observe();
        publishAndAssertReceived(0, 0, 1, 0);
    }

    @Test
    public void CanObserveUsingEventContextAndModel() throws Exception {
        _router
                .beginObserveEvents()
                .addObserver(FooEvent.class, this::eventAndContextAndModelObserver)
                .observe();
        publishAndAssertReceived(0, 0, 0, 1);
    }

    @Test
    public void CanObserveMultipleEvent() throws Exception {
        _router
                .beginObserveEvents()
                .addObserver(FooEvent.class, this::noParamsObserver)
                .addObserver(FooEvent.class, this::eventOnlyObserver)
                .addObserver(FooEvent.class, this::eventAndContextObserver)
                .addObserver(FooEvent.class, this::eventAndContextAndModelObserver)
                .observe();
        publishAndAssertReceived(1, 1, 1, 1);
        publishAndAssertReceived(2, 2, 2, 2);
    }

    @Test
    public void disposingStreamsStopsPublication() throws Exception {
        Disposable disposable = _router
                .beginObserveEvents()
                .addObserver(FooEvent.class, this::noParamsObserver)
                .addObserver(FooEvent.class, this::eventOnlyObserver)
                .addObserver(FooEvent.class, this::eventAndContextObserver)
                .addObserver(FooEvent.class, this::eventAndContextAndModelObserver)
                .observe();
        publishAndAssertReceived(1, 1, 1, 1);
        disposable.dispose();
        publishAndAssertReceived(1, 1, 1, 1);
    }

    private void noParamsObserver() {
        _monitor.noParamsReceivedCount++;
    }

    private void eventOnlyObserver(FooEvent e) {
        _monitor.eventReceivedCount++;
    }

    private void eventAndContextObserver(FooEvent e, EventContext c) {
        _monitor.eventAndContextReceivedCount++;
    }

    private void eventAndContextAndModelObserver(FooEvent e, EventContext c, TestModel m) {
        _monitor.eventAndContextAndModelReceivedCount++;
    }

    private void publishAndAssertReceived(int noParamsReceivedCount, int eventReceivedCount, int eventAndContextReceivedCount, int eventAndContextAndModelReceivedCount) throws Exception {
        _router.publishEvent(new FooEvent());
        assertEquals(noParamsReceivedCount, _monitor.noParamsReceivedCount);
        assertEquals(eventReceivedCount, _monitor.eventReceivedCount);
        assertEquals(eventAndContextReceivedCount, _monitor.eventAndContextReceivedCount);
        assertEquals(eventAndContextAndModelReceivedCount, _monitor.eventAndContextAndModelReceivedCount);
    }
}