package com.raulolmedo;

class Counter {

    private int counter = 0;

    int getCounter() {
        return counter;
    }

    synchronized void increaseCounter() {
        counter++;
    }

}
