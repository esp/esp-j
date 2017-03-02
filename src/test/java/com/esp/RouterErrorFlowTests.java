package com.esp;

import com.esp.routerTestStubs.Event1;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class RouterErrorFlowTests extends RouterTestsBase {

    @Test
    public void CancelingAtNormalObservationStageThrows() throws Exception {
        Event1 event = new Event1("A");
        event.ShouldCancel = true;
        event.CancelAtStage = ObservationStage.Normal;
        event.CancelAtEventProcesserId = EventProcessor1Id;
        _router.publishEvent(event);
        assertEquals(1, _terminalErrorHandler.getErrors().size());
        assertThat(_terminalErrorHandler.getErrors().get(0), instanceOf(IllegalStateException.class));
    }

    @Test
    public void CancelingAtCommittedObservationStageThrows() throws Exception {
        Event1 event = new Event1();
        event.ShouldCommit = true;
        event.CommitAtStage = ObservationStage.Normal;
        event.CommitAtEventProcesserId = EventProcessor1Id;
        event.ShouldCancel = true;
        event.CancelAtStage = ObservationStage.Committed;
        event.CancelAtEventProcesserId = EventProcessor1Id;
        _router.publishEvent(event);
        assertEquals(1, _terminalErrorHandler.getErrors().size());
        assertThat(_terminalErrorHandler.getErrors().get(0), instanceOf(IllegalStateException.class));
    }

    @Test
    public void CommittingAtPreviewObservationStageThrows() throws Exception {
        Event1 event = new Event1();
        event.ShouldCommit = true;
        event.CommitAtStage = ObservationStage.Preview;
        event.CommitAtEventProcesserId = EventProcessor1Id;
        _router.publishEvent(event);
        assertEquals(1, _terminalErrorHandler.getErrors().size());
        assertThat(_terminalErrorHandler.getErrors().get(0), instanceOf(IllegalStateException.class));
    }
}
