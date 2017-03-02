package com.esp.routerTestStubs;

import com.esp.EventContext;
import com.esp.ObservationStage;
import com.esp.Router;
import com.esp.functions.Action3;

public class GenericModelEventProcessor<TModel>
{
    private int _id;
    private Router<TModel> _router;

    public GenericModelEventProcessor(Router<TModel> router, int id)
    {
        _id = id;
        _router = router;

        Event1Details = this.ObserveEvent(Event1.class);
        Event2Details = this.ObserveEvent(Event2.class);
        Event3Details = this.ObserveEvent(Event3.class);
    }

    public EventObservationDetails<TModel, Event1> Event1Details;
    public EventObservationDetails<TModel, Event2> Event2Details;
    public EventObservationDetails<TModel, Event3> Event3Details;

    private <TEvent extends  BaseEvent> EventObservationDetails<TModel, TEvent> ObserveEvent(Class<TEvent> eventClass)
    {
        EventObservationDetails<TModel, TEvent> observationDetails = new EventObservationDetails<>();
        observationDetails.PreviewStage = this.WireUpObservationStage(eventClass, ObservationStage.Preview);
        observationDetails.NormalStage = this.WireUpObservationStage(eventClass, ObservationStage.Normal);
        observationDetails.CommittedStage = this.WireUpObservationStage(eventClass, ObservationStage.Committed);
        return observationDetails;
    }

    private <TEvent extends  BaseEvent> EventObservationStageDetails<TModel, TEvent> WireUpObservationStage(Class<TEvent> eventClass, ObservationStage stage)
    {
        EventObservationStageDetails<TModel, TEvent> details = new EventObservationStageDetails<>(stage);
        details.ObservationDisposable = _router
            .getEventObservable(eventClass, details.Stage)
            .observe((event, context, model) -> {
                details.ReceivedEvents.add(event);
                boolean shouldCancel = event.ShouldCancel && event.CancelAtStage != null && stage == event.CancelAtStage && event.CancelAtEventProcesserId == _id;
                if (shouldCancel) {
                    context.cancel();
                }
                boolean shouldCommit = event.ShouldCommit && event.CommitAtStage != null && stage == event.CommitAtStage && event.CommitAtEventProcesserId == _id;
                if (shouldCommit) {
                    context.commit();
                }
                for (Action3<TModel, TEvent, EventContext> action : details.Actions) {
                    action.call(model, event, context);
                }
            },
            () -> details.StreamCompletedCount ++);
        return details;
    }



}
