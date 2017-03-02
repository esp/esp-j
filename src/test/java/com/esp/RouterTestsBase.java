package com.esp;

import com.esp.routerTestStubs.*;
import org.junit.Before;

import java.util.Objects;

public class RouterTestsBase {
    protected DefaultRouter<TestModel> _router;
    protected StubRouterDispatcher _routerDispatcher;
    protected StubTerminalErrorHandler _terminalErrorHandler;

    protected TestModel _model1;
    protected GenericModelEventProcessor<TestModel> _model1EventProcessor;
    protected GenericModelEventProcessor<TestModel> _model1EventProcessor2;
    protected TestModelObserver<TestModel> _model1Observer;

    protected final int EventProcessor1Id = 1;
    protected final int EventProcessor2Id = 2;

    @Before
    public void setUp() throws Exception {
        _routerDispatcher = new StubRouterDispatcher();
        _terminalErrorHandler = new StubTerminalErrorHandler();
        _router = new DefaultRouter<>(_routerDispatcher, _terminalErrorHandler);
        AddModel1();
    }

    private void AddModel1() {
        _model1 = new TestModel();
        _router.setModel(_model1);
        _model1EventProcessor = new GenericModelEventProcessor<>(_router, EventProcessor1Id);
        _model1EventProcessor2 = new GenericModelEventProcessor<>(_router, EventProcessor2Id);
        _model1Observer = new TestModelObserver<>(_router);
    }

    protected void publishEventWithMultipleSubsequentEvents(int numberOfSubsequentEvents) {
        _router
                .<Event1>getEventObservable(Event1.class)
                .where((e, c, m) -> !Objects.equals(e.get_payload(), "subsequent"))
                .observe((model, event) ->
                        {
                            for (int i = 0; i < numberOfSubsequentEvents; i++) {
                                try {
                                    _router.publishEvent(new Event1("subsequent"));
                                } catch (Exception e) {
                                    org.junit.Assert.fail(e.toString());
                                }
                            }
                        }
                );
        try {
            _router.publishEvent(new Event1());
        } catch (Exception e) {
            org.junit.Assert.fail(e.toString());
        }
    }
}
