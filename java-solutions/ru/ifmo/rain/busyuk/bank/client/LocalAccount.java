package ru.ifmo.rain.busyuk.bank.client;

import ru.ifmo.rain.busyuk.bank.common.Account;

public class LocalAccount implements Account {
    private String id;
    private int amount;

    public LocalAccount(String id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

}
