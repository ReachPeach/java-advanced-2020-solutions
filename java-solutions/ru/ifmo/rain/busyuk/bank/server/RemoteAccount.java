package ru.ifmo.rain.busyuk.bank.server;

import ru.ifmo.rain.busyuk.bank.common.Account;

import java.rmi.RemoteException;

public class RemoteAccount implements Account {
    private final String id;
    volatile private int amount;

    public RemoteAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    protected RemoteAccount(final Account another) throws RemoteException {
        this.id = another.getId();
        this.amount = another.getAmount();
    }

    public String getId() {
        return id;
    }

    public synchronized int getAmount() {
        return amount;
    }

    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}

