package com.esp;

import org.junit.Assert;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class AssertExt {
    public static <T extends Throwable> T assertThrows(Runnable runnable, Class<T> exceptionType) {
        try {
            runnable.run();
        } catch (Throwable exception) {
            // Assert.assertTrue(exceptionType.isAssignableFrom(exceptionType));
            assertThat(exception, instanceOf(exceptionType));
            return (T)exception;
        }
        Assert.fail();
        return null;
    }
}
