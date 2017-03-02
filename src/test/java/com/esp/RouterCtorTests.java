package com.esp;

import com.esp.routerTestStubs.*;
import org.junit.Test;

public class RouterCtorTests {
    @Test(expected=IllegalArgumentException.class)
    public void CtorThrowsWhenTerminalErrorNull()
    {
        new DefaultRouter<>(new TestModel(), new StubRouterDispatcher(), null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void ThrowsIfIRouterDispatcherNull()
    {
        new DefaultRouter<>(new TestModel(), null, new StubTerminalErrorHandler());
    }
}
