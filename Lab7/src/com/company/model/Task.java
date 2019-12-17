package com.company.model;

import sun.awt.Mutex;
import java.util.List;

public class Task implements Runnable {
    private List<Integer> content;
    private List<Mutex> mutexes;

    private int interval;
    private int index;

    public Task(List<Integer> content, List<Mutex> mutexes, int interval, int index) {
        this.content = content;
        this.mutexes = mutexes;
        this.interval = interval;
        this.index = index;
    }

    @Override
    public void run() {
        mutexes.get(index).lock();
        this.content.set(index, this.content.get(index) + this.content.get(index - interval));
        mutexes.get(index).unlock();
    }
}