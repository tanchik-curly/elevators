package com.gachiMadElevator.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class CustomThread extends Thread {
    private final Runnable runnable;
    private final Object o = new Object();
    private volatile boolean suspended = false;

    public CustomThread(Runnable runnable) {
        super(runnable);
        this.runnable = runnable;
    }

    public void suspendThread(){
        suspended = true;
    }

    public void resumeThread(){
        suspended = false;
        synchronized (o) {
            o.notifyAll();
        }
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            if(!suspended){
                //Do work here
                runnable.run();
            } else {
                //Has been suspended
                try {
                    while(suspended) {
                        synchronized(o) {
                            o.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
