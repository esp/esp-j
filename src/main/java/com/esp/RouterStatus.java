package com.esp;

public enum RouterStatus {
    Idle,
    PreEventProcessing,
    EventProcessorDispatch,
    PostProcessing,
    DispatchModelUpdates,
    Halted,
    Executing
}
