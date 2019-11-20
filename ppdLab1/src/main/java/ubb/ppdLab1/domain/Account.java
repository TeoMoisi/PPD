package ubb.ppdLab1.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Account {

    private int Id;
    private int balance;
    @ToString.Exclude
    private List<Transaction> logs;
    @ToString.Exclude
    private ReentrantLock mutex;

    public Account(int Id, int balance) {
        this.Id = Id;
        this.balance = balance;
        this.logs = new ArrayList<>();
        this.mutex = new ReentrantLock();
    }
}