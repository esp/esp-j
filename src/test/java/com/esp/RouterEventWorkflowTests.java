package com.esp;

import com.esp.reactive.EventObservable;
import com.esp.routerTestStubs.*;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class RouterEventWorkflowTests extends RouterTestsBase {
    @Test
    public void PreProcessorInvokedForFirstEvent() {
        publishEventWithMultipleSubsequentEvents(2);
        assertEquals(3, _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size());
        assertEquals(1, _model1.PreProcessInvocationCount);
    }

    @Test
    public void PreviewObservationStageObserversReceiveEvent() throws Exception {
        _router.publishEvent(new Event1());
        assertEquals(1, _model1EventProcessor.Event1Details.PreviewStage.ReceivedEvents.size());
        assertEquals(1, _model1EventProcessor2.Event1Details.PreviewStage.ReceivedEvents.size());
    }

    @Test
    public void NormalObservationStageObserversReceiveEvent() throws Exception {
        _router.publishEvent(new Event1());
        assertEquals(1, _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size());
        assertEquals(1, _model1EventProcessor2.Event1Details.NormalStage.ReceivedEvents.size());
    }

    @Test
    public void CommittedObservationStageObserversReceiveEvent() throws Exception {
        Event1 event = new Event1();
        event.ShouldCommit = true;
        event.CommitAtStage = ObservationStage.Normal;
        event.CommitAtEventProcesserId = EventProcessor1Id;
        _router.publishEvent(event);
        assertEquals(1, _model1EventProcessor.Event1Details.CommittedStage.ReceivedEvents.size());
        assertEquals(1, _model1EventProcessor2.Event1Details.CommittedStage.ReceivedEvents.size());
    }

    @Test
    public void PostProcessorInvokedAfterAllEventsPurged() {
        publishEventWithMultipleSubsequentEvents(2);
        assertEquals(3, _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size());
        assertEquals(1, _model1.PostProcessInvocationCount);
    }

    @Test
    public void EventSentToPreProcessorThenEventProcessorThenPostProcessors() throws Exception {
        List<Integer> order = new ArrayList<>();
        _model1.registerPreProcessAction(() -> order.add(1));
        _model1EventProcessor.Event1Details.PreviewStage.registerAction((m, e) -> order.add(2));
        _model1EventProcessor.Event1Details.NormalStage.registerAction((m, e) -> order.add(3));
        _model1EventProcessor.Event1Details.CommittedStage.registerAction((m, e) -> order.add(4));
        _model1.registerPostProcessAction(() -> order.add(5));
        Event1 event = new Event1();
        event.ShouldCommit = true;
        event.CommitAtStage = ObservationStage.Normal;
        event.CommitAtEventProcesserId = EventProcessor2Id;
        _router.publishEvent(event);
        // assertThat(order, is(Arrays.asList(1, 2, 3, 4, 5))); // using hamcrest
        assertTrue(order.equals(Arrays.asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void OnlyProcessEventsIfThereAreObservers() throws Exception {
        _router.publishEvent("AnEventWithNoObservers");
        assertEquals(0, _model1.PreProcessInvocationCount);
        assertEquals(0, _model1.PostProcessInvocationCount);
        assertEquals(0, _model1Observer.ReceivedModels.size());
    }

    @Test
    public void SubsequentEvents_EventsPublishedByPreProcessorGetProcessedFromBackingQueue() throws Exception {
        _model1.registerPreProcessAction(() -> _router.publishEvent(new Event1("B")));
        _router.publishEvent(new Event1("A"));
        AssertReceivedEventPayloadsAreInOrder("A", "B");
    }

    @Test
    public void SubsequentEvents_EventsPublishedByPreviewObservationStageObserversGetProcessedFromBackingQueue() throws Exception {
        _model1EventProcessor.Event1Details.PreviewStage.registerAction((m, e) ->
        {
            if (!Objects.equals(e.get_payload(), "B")) {
                try {
                    _router.publishEvent(new Event1("B"));
                } catch (Exception e1) {
                    fail(e1.toString());
                }
            }
        });
        _router.publishEvent(new Event1("A"));
        AssertReceivedEventPayloadsAreInOrder("A", "B");
    }

    @Test
    public void SubsequentEvents_EventsPublishedByNormalObservationStageObserversGetProcessedFromBackingQueue() throws Exception {
        _model1EventProcessor.Event1Details.PreviewStage.registerAction((m, e) ->
        {
            if (!Objects.equals(e.get_payload(), "B")) {
                try {
                    _router.publishEvent(new Event1("B"));
                } catch (Exception e1) {
                    fail(e1.toString());
                }
            }
        });
        _model1EventProcessor.Event1Details.CommittedStage.registerAction((m, e) ->
        {
            // at this point B should be published but not processed,
            // this hadler should first as we finish dispatching to handlers for the initial event 'A'
            AssertReceivedEventPayloadsAreInOrder("A");
        });
        Event1 event = new Event1("A");
        event.ShouldCommit = true;
        event.CommitAtStage = ObservationStage.Normal;
        event.CommitAtEventProcesserId = EventProcessor1Id;
        _router.publishEvent(event);
        AssertReceivedEventPayloadsAreInOrder("A", "B");
    }

    @Test
    public void SubsequentEvents_EventsPublishedByCommittedObservationStageObserversGetProcessedFromBackingQueue() throws Exception {
        class TempClass {
            boolean hasPublishedB;
            boolean passed;
        }
        TempClass t = new TempClass();
        _model1EventProcessor.Event1Details.CommittedStage.registerAction((m, e) -> {
            if (!t.hasPublishedB) {
                t.hasPublishedB = true;
                try {
                    _router.publishEvent(new Event1("B"));
                } catch (Exception e1) {
                    fail(e1.toString());
                }
            }
        });
        _model1EventProcessor2.Event1Details.CommittedStage.registerAction((m, e) -> {
            // We need to check that all the handlers for event A run before
            // event B is processed.
            t.passed =
                    t.hasPublishedB &&
                            _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size() == 1 &&
                            Objects.equals(_model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.get(0).get_payload(), "A") &&
                            _model1EventProcessor2.Event1Details.NormalStage.ReceivedEvents.size() == 1 &&
                            Objects.equals(_model1EventProcessor2.Event1Details.NormalStage.ReceivedEvents.get(0).get_payload(), "A");
        });
        Event1 event = new Event1("A");
        event.ShouldCommit = true;
        event.CommitAtStage = ObservationStage.Normal;
        event.CommitAtEventProcesserId = EventProcessor1Id;
        _router.publishEvent(event);
        assertTrue(t.passed);
    }

    @Test
    public void SubsequentEvents_EventsPublishedByPostProcessorGetProcessedInANewEventLoop() throws Exception {
        class TempClass {
            boolean hasPublishedB;
        }
        TempClass t = new TempClass();
        _model1.registerPostProcessAction(() -> {
            if (!t.hasPublishedB) {
                t.hasPublishedB = true;
                _router.publishEvent(new Event1("B"));
            }
        });
        _router.publishEvent(new Event1("A"));
        // the pre processor will run again as the event workflow for this model is restarted
        assertEquals(2, _model1.PreProcessInvocationCount);
        // however we won't dispatch 2 udpates as we'll process all events first
        assertEquals(1, _model1Observer.ReceivedModels.size());
    }

    private void AssertReceivedEventPayloadsAreInOrder(String... args) {
        List<String> payloads = _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents
                .stream()
                .map(Event1::get_payload)
                .collect(Collectors.toList());
        assertTrue(Arrays.asList(args).equals(payloads));
    }

    @Test
    public void BaseEventObservation_CanObserveEventsByBaseType() throws Exception {
        class TempClass {
            int receivedEventCount;
        }
        TempClass t = new TempClass();
        _router
                .getEventObservable(BaseEvent.class)
                .observe((e, c, m) -> t.receivedEventCount++);
        _router.publishEvent(new Event1());
        _router.publishEvent(new Event2());
        assertEquals(2, t.receivedEventCount);
    }

    @Test
    public void BaseEventObservation_CanMergeEventStreams() throws Exception {
        ArrayList<BaseEvent> receivedEvents = new ArrayList<>();
        EventObservable<BaseEvent, EventContext, TestModel> stream = EventObservable.merge(
                _router.getEventObservable(BaseEvent.class),//, // stream 1
                _router.getEventObservable(Event2.class, ObservationStage.Preview).<BaseEvent>cast(), // stream 2
                _router.getEventObservable(Event3.class).<BaseEvent>cast() // stream 3
        );
        stream.observe((baseEvent, context, model) ->
        {
            receivedEvents.add(baseEvent);
        });
        _router.publishEvent(new Event1());
        assertEquals(1, receivedEvents.size()); // stream 1 should procure
        _router.publishEvent(new Event2());
        assertEquals(3, receivedEvents.size()); // stream 1 and 2 should procure
        _router.publishEvent(new Event3());
        assertEquals(5, receivedEvents.size()); // stream 1 and 3 should procure
    }

    @Test
    public void BaseEventObservation_Issue45TestCase() throws Exception {
        class TempClass {
            boolean passed;
        }
        TempClass t = new TempClass();
        Router<TestModel> router = new DefaultRouter<>(new TestModel(), _routerDispatcher, _terminalErrorHandler);
        router.getEventObservable(BaseEvent.class).observe((ev, model) -> {
            t.passed = true;
        });
        router.publishEvent(new Event1());
        assertTrue(t.passed);
    }

    @Test
    public void ExecuteEvent_ThrowsIfExecutedOutsideOfEventLoop() {
        IllegalStateException exception = AssertExt.assertThrows(() -> _router.executeEvent(new Event1()), IllegalStateException.class);
        Assert.assertThat(exception.getMessage(), CoreMatchers.containsString("Can't execute event"));
    }

    @Test
    public void ExecuteEvent_ThrowsIfExecutedFromPreProcessor() {
        _model1.registerPreProcessAction(() -> _router.executeEvent(new Event3()));
        assertEventPublishThrows();
    }

    @Test
    public void ExecuteEvent_ThrowsIfExecutedFromAPostProcessor() {
        _model1.registerPostProcessAction(() -> _router.executeEvent(new Event3()));
        assertEventPublishThrows();
    }

    @Test
    public void ExecuteEvent_ThrowsIfExecutedDuringModelUpdate() {
        _model1Observer.registerAction(c -> _router.executeEvent(new Event3()));
        assertEventPublishThrows();
    }


    @Test
    public void ExecuteEvent_ThrowsIfExecutedHandlerRaisesAnotherEvent() {
        _model1EventProcessor.Event1Details.NormalStage.registerAction((m, e) ->
        {
            _router.executeEvent(new Event3());
        });
        _model1EventProcessor.Event3Details.NormalStage.registerAction((m, e) ->
        {
            _router.executeEvent(new Event1());
        });
        assertEventPublishThrows();
    }

    @Test
    public void ExecuteEvent_ImmediatelyPublishesTheExecutedEventObservers() {
        class TempClass {
            boolean passed;
        }
        TempClass t = new TempClass();
        _model1EventProcessor.Event1Details.NormalStage.registerAction((m, e) ->
        {
            Event3 event = new Event3();
            event.ShouldCommit = true;
            event.CommitAtStage = ObservationStage.Normal;
            event.CommitAtEventProcesserId = EventProcessor1Id;
            _router.executeEvent(event);
            t.passed = _model1EventProcessor.Event3Details.PreviewStage.ReceivedEvents.size() == 1;
            t.passed = t.passed && _model1EventProcessor.Event3Details.NormalStage.ReceivedEvents.size() == 1;
            t.passed = t.passed && _model1EventProcessor.Event3Details.CommittedStage.ReceivedEvents.size() == 1;
        });
        _router.publishEvent(new Event1());
        assertTrue(t.passed);
        assertEquals(1, _model1.PreProcessInvocationCount);
        assertEquals(1, _model1.PostProcessInvocationCount);
    }

    private void assertEventPublishThrows() {
        _router.publishEvent(new Event1());
        assertEquals(1, _terminalErrorHandler.getErrors().size());
//        assertThat(_terminalErrorHandler.getErrors().get(0).getCause(), instanceOf(IllegalStateException.class));
//        assertThat(_terminalErrorHandler.getErrors().get(0).getCause().getMessage(), CoreMatchers.containsString("Can't execute event"));
        assertThat(_terminalErrorHandler.getErrors().get(0), instanceOf(IllegalStateException.class));
        assertThat(_terminalErrorHandler.getErrors().get(0).getMessage(), CoreMatchers.containsString("Can't execute event"));
    }

//
//        public class Broadcast : RouterTests
//        {
//            @Test
//            public void DeliversEventToAllModels()
//            {
//                _router.BroadcastEvent(new Event1());
//                _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size().ShouldBe(1);
//                _model1Observer.ReceivedModels.size().ShouldBe(1);
//
//                _model2EventProcessor.Event1Details.NormalStage.ReceivedEvents.size().ShouldBe(1);
//                _model2Controller.ReceivedModels.size().ShouldBe(1);
//            }
//
//            @Test
//            public void CanBroadcastUsingObjectOverload()
//            {
//                _router.BroadcastEvent((object)new Event1());
//                _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size().ShouldBe(1);
//                _model1Observer.ReceivedModels.size().ShouldBe(1);
//
//                _model2EventProcessor.Event1Details.NormalStage.ReceivedEvents.size().ShouldBe(1);
//                _model2Controller.ReceivedModels.size().ShouldBe(1);
//            }
//        }
}
