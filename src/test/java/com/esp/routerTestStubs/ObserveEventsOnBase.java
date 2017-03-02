package com.esp.routerTestStubs;

import com.esp.DefaultRouter;

public abstract class ObserveEventsOnBase {
    protected DefaultRouter<TestModel> Router;
    protected TestModel Model;

    public void setUp() {
        Router = new DefaultRouter<>(new StubRouterDispatcher(), new StubTerminalErrorHandler());
        Model = new TestModel();
        Router.setModel(Model);
    }

    public void run() throws Exception {
        setUp();
        runTest();
    }

    protected abstract void runTest() throws Exception;

    protected void observeEventsOnThis() {
       Router.observeEventsOn(this);
    }
}
