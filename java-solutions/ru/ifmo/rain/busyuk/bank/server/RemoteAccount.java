package ru.ifmo.rain.busyuk.bank.server;

import ru.ifmo.rain.busyuk.bank.common.Account;

import java.rmi.RemoteException;

public class RemoteAccount implements Account {
    private final String id;
    private volatile int amount;

    public RemoteAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    protected RemoteAccount(final Account other) throws RemoteException {
        this.id = other.getId();
        this.amount = other.getAmount();
    }

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }
}

