package com.esp.routerTestStubs;

import com.esp.TerminalErrorHandler;

import java.util.ArrayList;

public class StubTerminalErrorHandler implements TerminalErrorHandler {
    private final ArrayList<Exception> _errors;

    public StubTerminalErrorHandler() {
        _errors = new ArrayList<Exception>();
    }

    public ArrayList<Exception> getErrors() {
        return _errors;
    }

    public void onError(Exception exception) {
        System.out.printf("Terminal error: %s", exception);
        _errors.add(exception);
    }
}
