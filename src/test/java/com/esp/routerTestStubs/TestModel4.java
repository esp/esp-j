package com.esp.routerTestStubs;

import java.util.UUID;

public class TestModel4 implements Cloneable {
    public TestModel4() {
        Id = java.util.UUID.randomUUID();
    }

    public UUID Id;

    public Boolean IsClone;

    public TestModel4 Clone() {
        TestModel4 clone = new TestModel4();
        clone.Id = Id;
        clone.IsClone = true;
        return clone;
    }
}
