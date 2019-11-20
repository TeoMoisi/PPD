package ubb.ppdLab1.threads;

import ubb.ppdLab1.domain.Transaction;
import java.util.List;

public class TransactionThread extends Thread {

    private List<Transaction> transactionList;

    public TransactionThread(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @Override
    public void run() {

        for (Transaction transaction: this.transactionList) {
            try {
                transaction.transferMoney();

            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
