package com.company.model;

import sun.awt.Mutex;
import java.util.List;

public class Task implements Runnable {
    private List<Integer> content;
    private List<Mutex> mutexes;

    private int halfDistance;
    private int index;

    public Task(List<Integer> content, List<Mutex> mutexes, int halfDistance, int index) {
        this.content = content;
        this.mutexes = mutexes;
        this.halfDistance = halfDistance;
        this.index = index;
    }

    @Override
    public void run() {
        mutexes.get(index).lock();
        this.content.set(index, this.content.get(index) + this.content.get(index - halfDistance));
        mutexes.get(index).unlock();
    }
}