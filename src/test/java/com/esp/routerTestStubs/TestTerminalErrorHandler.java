package com.esp.routerTestStubs;

import com.esp.TerminalErrorHandler;

import java.util.ArrayList;

public class TestTerminalErrorHandler implements TerminalErrorHandler {

    public TestTerminalErrorHandler() {
        Errors = new ArrayList<>();
    }

    public ArrayList<Exception> Errors;

    public void onError(Exception exception) {
        Errors.add(exception);
    }
}