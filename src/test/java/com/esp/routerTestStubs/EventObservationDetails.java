package com.esp.routerTestStubs;

public class EventObservationDetails<TModel, TEvent>
{
    public EventObservationStageDetails<TModel, TEvent> PreviewStage;
    public EventObservationStageDetails<TModel, TEvent> NormalStage;
    public EventObservationStageDetails<TModel, TEvent> CommittedStage;
}
