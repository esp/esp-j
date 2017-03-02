package com.esp;

import org.junit.Test;

import static org.junit.Assert.*;

public class RouterRunActionTests extends RouterTestsBase {
    @Test
    public void ErrorRunActionFlows_RunningAnActionPushesAModelUpdate() throws Exception {
        class TempClass {
            boolean wasRun;
        }
        TempClass t = new TempClass();
        _router.runAction(() -> {
            t.wasRun = true;
        });
        assertEquals(1, _model1Observer.ReceivedModels.size());
        assertTrue(t.wasRun);
    }

    @Test
    public void ErrorRunActionFlows_RunningAnActionReceivesModel() throws Exception {
        class TempClass {
            int runCount;
        }
        TempClass t = new TempClass();
        _router.runAction(model ->
        {
            t.runCount++;
        });
        assertEquals(1, _model1Observer.ReceivedModels.size());
        assertEquals(1, t.runCount);
    }

//        @Test
//        public void ErrorRunActionFlows_RunningAnActionRunsAction()
//        {
//            int action1RunCount = 0, action2RunCount = 0;
//            bool modelUpdated = false;
//            var testScheduler = new  TestScheduler();
//            var router = new Router<TestModel>(new TestModel());
//            router.GetEventObservable<int>().Observe((m, e) =>
//            {
//                var observable = Observable.Timer(TimeSpan.FromSeconds(1), testScheduler);
//                observable.Subscribe(i =>
//                        {
//                                router.RunAction(() =>
//                                        {
//                                                action1RunCount++;
//                });
//                router.RunAction((model) =>
//                        {
//                                action2RunCount++;
//                model.Count++;
//                });
//                });
//            });
//            router.GetModelObservable().Observe(m =>
//                    {
//                            Console.WriteLine(m.Count);
//            modelUpdated = m.Count == 1 && m.Version == 3;
//            });
//            router.PublishEvent(1);
//            testScheduler.AdvanceBy(TimeSpan.FromSeconds(1).Ticks);
//            action1RunCount.ShouldBe(1);
//            action2RunCount.ShouldBe(1);
//            modelUpdated.ShouldBe(true);
//        }
//
//        public class TestModel : IPreEventProcessor
//        {
//            public int Version { get; set; }
//
//            public int Count { get; set; }
//
//        public void Process()
//        {
//            Version++;
//        }
//        }
}
