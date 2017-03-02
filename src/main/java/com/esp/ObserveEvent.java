package com.esp;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(value = ObserveEvents.class)
public @interface ObserveEvent {
    Class<?> eventClass();
    ObservationStage stage() default ObservationStage.Normal;
}