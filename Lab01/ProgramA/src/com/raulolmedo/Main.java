package com.raulolmedo;

import java.util.concurrent.TimeUnit;

public class Main implements Runnable {
    private static final int COUNTER_MAX = 1000000;
    private static Counter counter;

    @Override
    public void run() {
        for (int i = 0; i < COUNTER_MAX; i++) {
            counter.increaseCounter();
        }
    }

    public static void main(String[] args) {
        final long startTime, endTime;
        int numberOfThreads = Integer.valueOf(args[0]);
        Thread[] threads = new Thread[numberOfThreads];

        counter = new Counter();

        startTime = System.nanoTime();
        for (int i = 0; i < numberOfThreads; i++) {
            threads[i] = new Thread(new Main());
            threads[i].start();
        }

        for (int i = 0; i < numberOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        endTime = System.nanoTime();

        System.out.println("Final counter: " + counter.getCounter());
        System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toMillis((endTime - startTime)) + "ms");
    }
}
