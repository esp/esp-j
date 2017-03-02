package com.esp;

class Guard
{
    public static void ArgumentRequires(boolean test) {
        if(!test){
            throw new IllegalArgumentException();
        }
    }
}