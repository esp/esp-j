package com.esp.routerTestStubs;

import com.esp.EventContext;
import com.esp.ObservationStage;
import com.esp.disposables.Disposable;
import com.esp.functions.Action2;
import com.esp.functions.Action3;

import java.util.ArrayList;

public class EventObservationStageDetails<TModel, TEvent>
{
    public EventObservationStageDetails(ObservationStage stage)
    {
        Stage = stage;
        ReceivedEvents = new ArrayList<>();
        Actions = new ArrayList<>();
    }
    public ObservationStage Stage;
    public ArrayList<TEvent> ReceivedEvents;
    public Disposable ObservationDisposable;
    public int StreamCompletedCount;
    public ArrayList<Action3<TModel, TEvent, EventContext>> Actions;

    public void registerAction(Action2<TModel, TEvent> action)
    {
        Actions.add((m, e, c) -> action.call(m, e));
    }

    public void registerAction(Action3<TModel, TEvent, EventContext> action)
    {
        Actions.add(action);
    }
}
