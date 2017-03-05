package com.esp;


import com.esp.disposables.Disposable;
import com.esp.routerTestStubs.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class RouterObserveEventsOnTests {

    @Test
    public void CanObserveUsingWithNoParams() throws Exception {
        new CanObserveUsingWithNoParams().run();
    }

    @Test
    public void CanObserveUsingWithEvent() throws Exception {
        new CanObserveUsingWithEvent().run();
    }

    @Test
    public void CanObserveUsingWithContext() throws Exception {
        new CanObserveUsingWithContext().run();
    }

    @Test
    public void CanObserveUsingWithEventAndContext() throws Exception {
        new CanObserveUsingWithEventAndContext().run();
    }

    @Test
    @Ignore 
    public void CanObserveUsingPrivateObserveMethod() throws Exception {
        new CanObserveUsingPrivateObserveMethod().run();
    }

    @Test
    @Ignore 
    public void CanObserveMultipleEventsByBaseEventType() throws Exception {
        new CanObserveMultipleEventsByBaseEventType().run();
    }

    @Test
    @Ignore 
    public void CanObserveMultipleEventsByBaseEventTypeAtCorrectStage() throws Exception {
        new CanObserveMultipleEventsByBaseEventTypeAtCorrectStage().run();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void ThrowsIfObserveEventOnCalledTwiceWithSameObserver() {
        DefaultRouter<SimpleModel> router = new DefaultRouter<>(new StubRouterDispatcher(), new StubTerminalErrorHandler());
        SimpleModel m = new SimpleModel();
        router.setModel(m);
        router.observeEventsOn(m);
        exception.expect(RuntimeException.class);
        router.observeEventsOn(m);
    }

    @Test
    public void CanReObserveAfterDisposingPreviousSubscription() {
        DefaultRouter<SimpleModel> router = new DefaultRouter<>(new StubRouterDispatcher(), new StubTerminalErrorHandler());
        SimpleModel m = new SimpleModel();
        router.setModel(m);
        Disposable d = router.observeEventsOn(m);
        d.dispose();
        router.observeEventsOn(m);
    }

    public class SimpleModel  {
        @ObserveEvent(eventClass = FooEvent.class)
        public void observeFooEvent() {

        }
    }

    public class CanObserveUsingWithNoParams extends ObserveEventsOnBase {
        private boolean _received;

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            Router.publishEvent(new FooEvent());
            assertTrue(_received);
        }

        @ObserveEvent(eventClass = FooEvent.class)
        public void observeFooEvent() {
            _received = true;
        }
    }

    public class CanObserveUsingWithEvent extends ObserveEventsOnBase {
        private FooEvent _receivedEvent;

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            FooEvent fooEvent = new FooEvent();
            Router.publishEvent(fooEvent);
            assertEquals(_receivedEvent, fooEvent);
        }

        @ObserveEvent(eventClass = FooEvent.class)
        public void observeFooEvent(FooEvent event) {
            _receivedEvent = event;
        }
    }

    public class CanObserveUsingWithContext extends ObserveEventsOnBase {
        private boolean _contextWasReceived;

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            FooEvent fooEvent = new FooEvent();
            Router.publishEvent(fooEvent);
            assertTrue(_contextWasReceived);
        }

        @ObserveEvent(eventClass = FooEvent.class)
        public void observeFooEvent(EventContext context) {
            _contextWasReceived = context != null;
        }
    }

    public class CanObserveUsingWithEventAndContext extends ObserveEventsOnBase {
        private FooEvent _receivedEvent;
        private boolean _contextWasReceived;

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            FooEvent fooEvent = new FooEvent();
            Router.publishEvent(fooEvent);
            assertTrue(_contextWasReceived);
            assertEquals(_receivedEvent, fooEvent);
        }

        @ObserveEvent(eventClass = FooEvent.class)
        public void observeFooEvent(FooEvent e, EventContext context) {
            _receivedEvent = e;
            _contextWasReceived = context != null;
        }
    }

    public class CanObserveUsingPrivateObserveMethod extends ObserveEventsOnBase {
        private boolean _received;

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            Router.publishEvent(new FooEvent());
            assertTrue(_received);
        }

        @ObserveEvent(eventClass = FooEvent.class)
        private void observeFooEvent() {
            _received = true;
        }
    }

    public class CanObserveMultipleEventsByBaseEventType extends ObserveEventsOnBase {
        private ArrayList<BaseEvent> _receivedEvents = new ArrayList<>();

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            FooEvent fooEvent = new FooEvent();
            BarEvent barEvent = new BarEvent();
            Router.publishEvent(fooEvent);
            Router.publishEvent(barEvent);
            assertEquals(3, _receivedEvents.size());
            assertEquals(_receivedEvents.get(0), fooEvent);
            assertEquals(_receivedEvents.get(1), barEvent);
        }

        @ObserveEvent(eventClass = FooEvent.class)
        @ObserveEvent(eventClass = BarEvent.class)
        public void observeByBaseEvent(BaseEvent e) {
            _receivedEvents.add(e);
        }
    }

    public class CanObserveMultipleEventsByBaseEventTypeAtCorrectStage extends ObserveEventsOnBase
    {
        private ArrayList<Tuple> _receivedEvents = new ArrayList<>();

        @Override
        public void runTest() throws Exception {
            observeEventsOnThis();
            FooEvent fooEvent = new FooEvent();
            BarEvent barEvent = new BarEvent();
            BazEvent bazEvent = new BazEvent();
            BuzzEvent buzzEvent = new BuzzEvent();

            Router.publishEvent(fooEvent);
            AssertLastReceivedEvent(1, ObservationStage.Preview, fooEvent);

            Router.publishEvent(barEvent);
            AssertLastReceivedEvent(2, ObservationStage.Normal, barEvent);

            Router.publishEvent(bazEvent);
            AssertLastReceivedEvent(3, ObservationStage.Normal, bazEvent);

            Router.publishEvent(buzzEvent);
            AssertLastReceivedEvent(4, ObservationStage.Committed, buzzEvent);
        }

        @ObserveEvent(eventClass = BuzzEvent.class)
        public void CommitBuzz(BuzzEvent e, EventContext context, TestModel model)
        {
            context.commit();
        }

        @ObserveEvent(eventClass =FooEvent.class, stage= ObservationStage.Preview)
        @ObserveEvent(eventClass =BarEvent.class)
        @ObserveEvent(eventClass =BazEvent.class, stage= ObservationStage.Normal)
        @ObserveEvent(eventClass =BuzzEvent.class,stage = ObservationStage.Committed)
        public void observeByBaseEvent(BaseEvent e, EventContext context, TestModel model)
        {
            Tuple tuple = new Tuple();
            tuple.Event = e;
            tuple.Stage = context.get_currentStage();
            _receivedEvents.add(tuple);
        }

        private void AssertLastReceivedEvent(int expectedEventReceivedCount, ObservationStage expectedObservationStage, BaseEvent sent)
        {
            assertEquals(expectedEventReceivedCount, _receivedEvents.size());
            assertEquals(expectedObservationStage, _receivedEvents.get(expectedEventReceivedCount -1).Stage);
            assertEquals(sent, _receivedEvents.get(expectedEventReceivedCount -1).Event);
        }

        private class Tuple
        {
            public BaseEvent Event;
            public ObservationStage Stage;
        }
    }
}
