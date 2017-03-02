package com.esp;

import com.esp.routerTestStubs.Event1;
import org.junit.Test;

import static org.junit.Assert.*;

public class RouterModelObservationTests extends RouterTestsBase {

    @Test
    public void ObserversReceiveModelOnEventWorkflowCompleted() throws Exception {
        _router.publishEvent(new Event1());
        assertEquals(1, _model1Observer.ReceivedModels.size());
    }

    @Test
    public void DisposedObserversReceiveDontModelOnEventWorkflowCompleted() throws Exception {
        _model1Observer.ModelObservationDisposable.dispose();
        _router.publishEvent(new Event1());
        assertEquals(0, _model1Observer.ReceivedModels.size());
    }

    @Test
    public void MultipleSubsequentEventsOnlyYield1ModelUpdate()
    {
        publishEventWithMultipleSubsequentEvents(5);
        assertEquals(6, _model1EventProcessor.Event1Details.NormalStage.ReceivedEvents.size());
        assertEquals(1, _model1Observer.ReceivedModels.size());
    }

    @Test
    public void EventsPublishedDuringModelDispatchGetProcessed() throws Exception {
        class TempClass {
            boolean publishedEventFromController;
        }
        TempClass t = new TempClass();
        _model1Observer.registerAction(m ->
                {
            if (!t.publishedEventFromController)
            {
                t.publishedEventFromController = true;
                try {
                    _router.publishEvent(new Event1());
                } catch (Exception e) {
                    fail(e.toString());
                }
            }
        });
        _router.publishEvent(new Event1());
        assertTrue(t.publishedEventFromController);
        assertEquals(2, _model1Observer.ReceivedModels.size());
    }
}
