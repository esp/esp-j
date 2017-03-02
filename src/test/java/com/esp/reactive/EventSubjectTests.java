package com.esp.reactive;

import com.esp.routerTestStubs.*;
import org.junit.Assert;
import org.junit.Test;

public class EventSubjectTests {

    @Test
    public void onNextPushesToObserver() throws Exception {

        Event1 event = new Event1();
        Object context = new Object();
        TestModel model = new TestModel();
        ObservedEventItems<Event1, Object, TestModel> observedItems = new ObservedEventItems<>();

        EventSubject<Event1, Object, TestModel> subject = EventSubject.<Event1, Object, TestModel>create();
        subject.observe((e, c, m) ->{
            observedItems.set(e, c, m);
        });
        subject.onNext(event, context, model);

        boolean passed = observedItems.T1 == event && observedItems.T2 == context && observedItems.T3 == model;
        Assert.assertTrue(passed);
    }
}

