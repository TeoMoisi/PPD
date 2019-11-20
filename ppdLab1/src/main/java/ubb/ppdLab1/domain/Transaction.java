package ubb.ppdLab1.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Transaction {

    private static int serialNumber = 0;
    private Account source;
    private Account destination;
    private int amount;

    public Transaction(Account source, Account destination, int amount) {
        serialNumber++;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
    }

    //critical section
    public void transferMoney() {
        if (this.source.getId() < this.destination.getId()) {
            this.source.getMutex().lock();
            this.destination.getMutex().lock();
        } else {
            this.destination.getMutex().lock();
            this.source.getMutex().lock();
        }

        if (this.source.getBalance() < amount) {
            this.destination.getMutex().unlock();
            this.source.getMutex().unlock();
            throw new RuntimeException(this.toString()+ ": Not enough money!");
        }

        System.out.println(this.toString());
        this.source.setBalance(this.source.getBalance() - amount);
        this.destination.setBalance(this.destination.getBalance() + amount);

        this.source.getLogs().add(this);
        this.destination.getLogs().add(this);

        this.destination.getMutex().unlock();
        this.source.getMutex().unlock();
    }
}
