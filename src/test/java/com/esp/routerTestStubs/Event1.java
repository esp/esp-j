package com.esp.routerTestStubs;

public class Event1 extends BaseEvent {

    private String _payload;

    public Event1() {
    }

    public Event1(String payload) {
        _payload = payload;
    }

    public String get_payload() {
        return _payload;
    }

}
