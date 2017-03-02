package com.esp.reactive;

import com.esp.routerTestStubs.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class EventObservableTests {
    @Test
    public void whereFilters() throws Exception {
        ArrayList<Integer> observedItems = new ArrayList<>();
        EventSubject<Integer, Object, TestModel> subject = EventSubject.<Integer, Object, TestModel>create();
        subject
                .where((e, c, m) -> e > 2)
                .observe((e, c, m) -> {
                    observedItems.add(e);
                });
        subject.onNext(1, new Object(), new TestModel());
        subject.onNext(2, new Object(), new TestModel());
        subject.onNext(3, new Object(), new TestModel());
        subject.onNext(4, new Object(), new TestModel());
        Assert.assertArrayEquals(new Integer[]{3, 4}, observedItems.toArray(new Integer[observedItems.size()]));
    }

    @Test
    public void takeTakesRightNumber() {
        ArrayList<Integer> observedItems = new ArrayList<>();
        EventSubject<Integer, Object, TestModel> subject = EventSubject.<Integer, Object, TestModel>create();
        subject
                .take(2)
                .observe((e, c, m) -> {
                    observedItems.add(e);
                }, () -> {
                    observedItems.add(-1);
                });
        subject.onNext(1, new Object(), new TestModel());
        subject.onNext(2, new Object(), new TestModel());
        subject.onNext(3, new Object(), new TestModel());
        subject.onNext(4, new Object(), new TestModel());
        Assert.assertArrayEquals(new Integer[]{1, 2, -1}, observedItems.toArray(new Integer[observedItems.size()]));
    }

    @Test
    public void takeNothingIfNumberIs0() {
        ArrayList<Integer> observedItems = new ArrayList<>();
        EventSubject<Integer, Object, TestModel> subject = EventSubject.<Integer, Object, TestModel>create();
        subject
                .take(0)
                .observe((e, c, m) -> {
                    observedItems.add(e);
                }, () -> {
                    observedItems.add(-1);
                });
        subject.onNext(1, new Object(), new TestModel());
        Assert.assertArrayEquals(new Integer[]{-1}, observedItems.toArray(new Integer[observedItems.size()]));
    }
}

