package com.esp.routerTestStubs;

import com.esp.Router;
import com.esp.disposables.Disposable;
import com.esp.functions.Action1;

import java.util.ArrayList;

public class TestModelObserver<TModel> {
    private ArrayList<Action1<TModel>> _actions = new ArrayList<>();

    public TestModelObserver(Router<TModel> router) {
        ReceivedModels = new ArrayList<>();
        ModelObservationDisposable = router
            .getModelObservable()
            .observe(model ->
                    {
                        ReceivedModels.add(model);
                        for (Action1<TModel> action : _actions) {
                            action.call(model);
                        }
                    },
                    () ->
                    {
                        StreamCompletedCount++;
                    }
            );
    }

    public Disposable ModelObservationDisposable;

    public ArrayList<TModel> ReceivedModels;

    public int StreamCompletedCount;

    public void registerAction(Action1<TModel> action) {
        _actions.add(action);
    }
}
