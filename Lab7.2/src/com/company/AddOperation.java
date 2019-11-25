package com.company;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class AddOperation implements Runnable {
    private ArrayBlockingQueue<BigInteger> input;
    private ArrayBlockingQueue<BigInteger> output;
    private BigInteger number;
    private BigInteger position = BigInteger.ONE;
    private BigInteger remainder = BigInteger.ZERO;
    private BigInteger sum = BigInteger.ZERO;

    public AddOperation(BigInteger firstNumber, BigInteger secondNumber, ArrayBlockingQueue<BigInteger> output) {
        this.input = new ArrayBlockingQueue<>(secondNumber.toString().length() + 1);
        this.output = output;
        this.number = firstNumber;
        this.createInputQueue(secondNumber);

    }

    public AddOperation(BigInteger number, ArrayBlockingQueue<BigInteger> input, ArrayBlockingQueue<BigInteger> output) {
        this.input = input;
        this.output = output;
        this.number = number;
    }

    @Override
    public void run() {
        try {
            addition();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addition() throws InterruptedException {
        //retrieves and removes the head of the input queue, waiting 5 seconds for the element to become available(if its the case)
        BigInteger head = this.input.poll(5, TimeUnit.SECONDS);

        //while the head of the input queue is not -1 || while we are not at the end
        while (head.compareTo(new BigInteger("-1")) != 0) {
            //we move forward
            this.position = this.position.multiply(BigInteger.TEN);

            //we compute the sum by adding the number and the remainder
            sum = head
                    .add(this.number
                            .mod(this.position)
                            .divide(this.position.divide(BigInteger.TEN)))
                    .add(this.remainder);

            this.setRemainderAndSum();
            //we add the sum at the tail of the output queue
            output.offer(sum);

            //we get again the head of the queue
            head = this.input.poll(5, TimeUnit.SECONDS);
        }

        //while position is smaller than number or remainder is not 0
        while (this.position.compareTo(this.number) <= 0 || this.remainder.compareTo(BigInteger.ZERO) != 0){
            if (this.position.compareTo(this.number) <= 0) {
                //we move forward
                this.position = this.position.multiply(BigInteger.TEN);

                sum = head
                        .add(this.number
                                .mod(this.position)
                                .divide(this.position.divide(BigInteger.TEN)))
                        .add(this.remainder);

                this.setRemainderAndSum();
                //we add the sum at the tail of the output queue
                output.offer(sum);

            } else {
                //we add the remainder at the tail of the output queue
                output.offer(this.remainder);
                //the remainder becomes 0 again
                this.remainder = BigInteger.ZERO;
            }

        }
        //we add -1 at the tail of the output queue
        output.offer(new BigInteger("-1"));

    }

    private void setRemainderAndSum(){
        //if the computed sum is greater than 10
        if (sum.compareTo(BigInteger.TEN) >= 0) {
            //we take the ramainder
            remainder = sum.divide(BigInteger.TEN);
            //sum gets the value of the quotient
            sum = sum.mod(BigInteger.TEN);
        } else {
            //otherwise, the remainder is 0
            remainder = BigInteger.ZERO;
        }
    }

    private void createInputQueue(BigInteger number) {
        this.input.clear();

        //while number is not 0
        while (number.compareTo(BigInteger.ZERO) != 0) {
            //we add each digit of the number into the queue
            this.input.add(number.mod(BigInteger.TEN));
            number = number.divide(BigInteger.TEN);
        }

        //we add -1 at the tail of the queue
        this.input.add(new BigInteger("-1"));
    }
}
