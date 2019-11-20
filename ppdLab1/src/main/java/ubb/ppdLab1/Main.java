package ubb.ppdLab1;

import ubb.ppdLab1.domain.Account;
import ubb.ppdLab1.domain.Transaction;
import ubb.ppdLab1.threads.CheckThread;
import ubb.ppdLab1.threads.TransactionThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    private static final int ACCOUNTS_NUMBER = 50;
    private static final int THREADS_NUMBER = 10;
    private static final int MAX_BALANCE_AMOUNT = 100;
    private static final int TRANSACTIONS_ON_THREAD = 5;
    private static List<Account> accountList = new ArrayList<>();
    private static List<Account> originalAccountList = new ArrayList<>();
    private static int initialAmount = 0;

    private static void generateRandomAccounts() {
        Random random = new Random();
        for (int i = 0; i < ACCOUNTS_NUMBER; i++) {
            Account account = new Account(i + 1, random.nextInt(MAX_BALANCE_AMOUNT) + 1);
            accountList.add(account);
            originalAccountList.add(new Account(account.getId(), account.getBalance()));
            initialAmount += account.getBalance();
        }
    }

    private static List<Transaction> generateRandomTransactions() {
        List<Transaction> transactionList = new ArrayList<Transaction>();
        Random random = new Random();
        for (int i = 0; i < TRANSACTIONS_ON_THREAD; i++) {
            int source = random.nextInt(accountList.size());
            int destination = random.nextInt(accountList.size());

            while (source == destination) {
                destination = random.nextInt(accountList.size());
            }

            Account sourceAccount = accountList.get(source);
            Account destinationAccount = accountList.get(destination);
            int amount = random.nextInt(MAX_BALANCE_AMOUNT) + 1;

            Transaction transaction = new Transaction(sourceAccount, destinationAccount, amount);
            transactionList.add(transaction);
        }

        return transactionList;
    }

    public static void main(String[] args) throws InterruptedException {
        List<TransactionThread> transactionThreads = new ArrayList<>();
        List<CheckThread> checkThreads = new ArrayList<>();

        float start =  System.nanoTime() / 1000000;
        generateRandomAccounts();

        for (int i = 0; i < THREADS_NUMBER; i++) {
            TransactionThread transactionThread = new TransactionThread(generateRandomTransactions());
            transactionThread.start();
            transactionThreads.add(transactionThread);

            CheckThread checkThread = new CheckThread(initialAmount, originalAccountList, accountList);
            checkThread.start();
            checkThreads.add(checkThread);
        }

        for (int i = 0; i < THREADS_NUMBER; i++) {
            transactionThreads.get(i).join();
            checkThreads.get(i).join();
        }

        float end = System.nanoTime() / 1000000;
        float total = (end - start) / 1000;

        System.out.println(THREADS_NUMBER + " threads,"
                +  ACCOUNTS_NUMBER + " accounts and "
                + TRANSACTIONS_ON_THREAD + " transactions/thread " +
                "exectuted in: " + total);
    }
}
