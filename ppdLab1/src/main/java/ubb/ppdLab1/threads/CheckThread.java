package ubb.ppdLab1.threads;

import ubb.ppdLab1.domain.Account;
import ubb.ppdLab1.domain.Transaction;
import java.util.List;

public class CheckThread extends Thread {

    private int initialAmount;
    private List<Account> initialAccounts;
    private List<Account> accountList;

    public CheckThread(int initialAmount, List<Account> initialAccounts, List<Account> accountList) {
        this.initialAmount = initialAmount;
        this.initialAccounts = initialAccounts;
        this.accountList = accountList;
    }

    @Override
    public void run() {

        //int sum = accountList.stream().map(Account::getBalance).reduce(Integer::sum).orElse(0);
        int sum = 0;
        for (Account account: accountList) {
            account.getMutex().lock();
            sum += account.getBalance();
            account.getMutex().unlock();
        }
        if (sum != initialAmount) {
            System.out.println("Check failed because the total amount is not constant");
            return;
        }

        for (int i = 0; i < initialAccounts.size(); i++) {
            initialAccounts.get(i).getMutex().lock();
            accountList.get(i).getMutex().lock();
            int initialBalance = initialAccounts.get(i).getBalance();
            int currentBalance = accountList.get(i).getBalance();
            int id = accountList.get(i).getId();
            int sentMoney = accountList.get(i).getLogs().stream()
                    .filter(transaction -> transaction.getSource().getId() == id)
                    .map(Transaction::getAmount).reduce(Integer::sum).orElse(0);

            int receivedMoney = accountList.get(i).getLogs().stream()
                    .filter(transaction -> transaction.getDestination().getId() == id)
                    .map(Transaction::getAmount).reduce(Integer::sum).orElse(0);

            if (initialBalance - sentMoney + receivedMoney != currentBalance) {
                System.out.println("Something went wrong for this account!");
                return;
            }
            initialAccounts.get(i).getMutex().unlock();
            accountList.get(i).getMutex().unlock();
            System.out.println("Everything is fine!");
        }
    }
}
