package com.esp.routerTestStubs;

import com.esp.PostEventProcessor;
import com.esp.PreEventProcessor;

import java.util.UUID;

public class TestModel5 implements PreEventProcessor, PostEventProcessor
{
    public TestModel5()
    {
        Id = java.util.UUID.randomUUID();
    }
    public UUID Id;

    public int PreProcessorInvocationCount;

    public int PostProcessorInvocationCount;

    @Override
    public void postProcess() {
        PostProcessorInvocationCount++;
    }

    @Override
    public void preProcess() {
        PreProcessorInvocationCount++;
    }
}
