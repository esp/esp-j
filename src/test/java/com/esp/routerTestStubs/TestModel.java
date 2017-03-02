package com.esp.routerTestStubs;

import com.esp.*;
import com.esp.functions.*;

import java.util.ArrayList;

public class TestModel implements PreEventProcessor, PostEventProcessor
{
    private ArrayList<Action0> _preProcessingActions = new ArrayList<>();
    private ArrayList<Action0> _postProcessingActions = new ArrayList<>();

    public TestModel()
    {
        SubTestModel = new SubTestModel();
    }

    public SubTestModel SubTestModel;
    public int PreProcessInvocationCount;
    public int PostProcessInvocationCount;

    public void registerPreProcessAction(Action0 action)
    {
        _preProcessingActions.add(action);
    }

    public void registerPostProcessAction(Action0 action)
    {
        _postProcessingActions.add(action);
    }

    @Override
    public void preProcess() {
        PreProcessInvocationCount++;
        for (Action0 action : _preProcessingActions)
        {
            action.call();
        }
    }

    @Override
    public void postProcess() {
        PostProcessInvocationCount++;
        for (Action0 action : _postProcessingActions)
        {
            action.call();
        }
    }
}
