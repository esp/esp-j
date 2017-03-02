package com.esp.routerTestStubs;

import com.esp.ObservationStage;

public class BaseEvent
{
    public String name;

    public boolean ShouldCancel;
    public ObservationStage CancelAtStage;
    public int CancelAtEventProcesserId;

    public boolean ShouldCommit;
    public ObservationStage CommitAtStage;
    public int CommitAtEventProcesserId;
}
