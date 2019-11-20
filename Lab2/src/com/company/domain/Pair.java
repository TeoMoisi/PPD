package com.company.domain;

public class Pair<T, V>{

    private T firstValue;
    private V secondValue;

    public Pair(T firstValue, V secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public T getFirstValue() {
        return this.firstValue;
    }

    public V getSecondValue() {
        return this.secondValue;
    }
}
